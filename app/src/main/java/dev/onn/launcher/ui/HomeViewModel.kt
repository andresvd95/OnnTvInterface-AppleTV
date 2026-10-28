package dev.onn.launcher.ui

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.onn.launcher.R
import dev.onn.launcher.data.AppRepository
import dev.onn.launcher.data.PreferencesStore
import dev.onn.launcher.data.RecentsRepository
import dev.onn.launcher.domain.AppEntry
import dev.onn.launcher.domain.LastUsed
import dev.onn.launcher.domain.MediaItem
import dev.onn.launcher.domain.UsageRecord
import dev.onn.launcher.domain.freshMedia
import dev.onn.launcher.domain.lastUsed
import dev.onn.launcher.domain.moveFavorite
import dev.onn.launcher.domain.orderApps
import dev.onn.launcher.domain.selectRecents
import dev.onn.launcher.domain.toggleFavorite
import dev.onn.launcher.system.MediaHub
import dev.onn.launcher.system.SleepTimer
import dev.onn.launcher.system.SystemShortcuts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.roundToInt

data class CardItem(val app: AppEntry, val lastUsed: LastUsed)

/** Imagen ya convertida (banner, icono o portada de contenido) y su color dominante para la luz ambiente. */
data class AppArt(val bitmap: ImageBitmap, val isBanner: Boolean, val ambient: Color)

/** App que se está moviendo: en la fila de Apps (HU-018) o en "Mis apps" del panel (HU-006). */
data class Moving(val packageName: String, val inPanel: Boolean)

data class HomeUiState(
    val apps: List<CardItem> = emptyList(),
    val recents: List<CardItem> = emptyList(),
    val pinned: List<AppEntry> = emptyList(),
    val favorites: List<String> = emptyList(),
    val media: Map<String, MediaItem> = emptyMap(),
    val moving: Moving? = null,
    val message: String? = null,
    val canSearch: Boolean = false,
    val online: Boolean = true,
    val sleepEndAt: Long? = null,
    val loaded: Boolean = false,
)

enum class Shortcut { SEARCH, NOTIFICATIONS, PLAY, SETTINGS, PROFILE, WIFI, BLUETOOTH, AUDIO }

private data class Extras(val moving: Moving?, val message: String?, val online: Boolean, val sleepEndAt: Long?)
private data class Prefs(val order: List<String>, val favorites: List<String>, val hidden: Set<String>)

class HomeViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AppRepository(app)
    private val prefs = PreferencesStore(app)
    private val recentsRepo = RecentsRepository(app)
    private val shortcuts = SystemShortcuts(app)
    private val selfPackage = app.packageName
    private val artCache = ConcurrentHashMap<String, AppArt>()
    private val editLock = Mutex()

    private val installed = repo.changes().map { repo.launchableApps() }.flowOn(Dispatchers.IO)
    // Se llena en refreshRecents() (onResume), fuera del hilo principal.
    private val usage = MutableStateFlow<List<UsageRecord>>(emptyList())
    private val moving = MutableStateFlow<Moving?>(null)
    private val message = MutableStateFlow<String?>(null)
    private val online = MutableStateFlow(true)
    private val extras = combine(moving, message, online, SleepTimer.endAt, ::Extras)
    private val savedPrefs = combine(prefs.order, prefs.favorites, prefs.hidden, ::Prefs)

    init { MediaHub.load(app) }

    val state: StateFlow<HomeUiState> = combine(
        installed, savedPrefs, usage, MediaHub.items, extras,
    ) { all, p, usageList, mediaItems, ex ->
        val hidden = p.hidden + SYSTEM_SETTINGS
        // Antes de HU-018 el orden eran las favoritas: se usan como punto de partida.
        val apps = orderApps(all, p.order.ifEmpty { p.favorites }, hidden, selfPackage)
        val visible = apps.associateBy { it.packageName }
        val recents = selectRecents(usageList, visible.keys, hidden + STOCK_HOME + selfPackage).mapNotNull { visible[it] }
        val lastByPkg = usageList.groupBy { it.packageName }.mapValues { (_, v) -> v.maxOf { it.lastUsedMs } }
        val now = System.currentTimeMillis()
        fun card(a: AppEntry) = CardItem(a, lastUsed(lastByPkg[a.packageName], now))
        HomeUiState(
            apps = apps.map(::card),
            recents = recents.map(::card),
            pinned = p.favorites.mapNotNull { visible[it] },
            favorites = p.favorites,
            media = freshMedia(mediaItems.values, visible.keys, now),
            moving = ex.moving,
            message = ex.message,
            online = ex.online,
            sleepEndAt = ex.sleepEndAt,
            canSearch = shortcuts.canSearch(),
            loaded = true,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    /** Al volver al launcher: Recientes y estado de red (HU-019, HU-017). */
    fun refreshRecents() {
        viewModelScope.launch { usage.value = withContext(Dispatchers.IO) { recentsRepo.usage() } }
        online.value = isOnline()
    }

    private fun isOnline(): Boolean {
        val cm = getApplication<Application>().getSystemService(ConnectivityManager::class.java)
        val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /** Temporizador (HU-017): null desactiva. */
    fun setSleepTimer(minutes: Int?) {
        val app = getApplication<Application>()
        message.value = when {
            minutes == null -> { SleepTimer.cancel(); app.getString(R.string.timer_cancelled) }
            SleepTimer.start(minutes) -> app.getString(R.string.timer_set, minutes)
            else -> app.getString(R.string.timer_no_service)
        }
    }

    /** Arte de la app, convertido una sola vez y guardado en memoria (rendimiento). */
    suspend fun art(packageName: String): AppArt? = artCache[packageName] ?: withContext(Dispatchers.IO) {
        repo.visual(packageName)?.let { v ->
            val bmp = if (v.isBanner) {
                val iw = v.drawable.intrinsicWidth
                val w = iw.takeIf { it > 0 }?.coerceIn(BANNER_MIN_W, BANNER_MAX_W) ?: BANNER_MIN_W
                val h = v.drawable.intrinsicHeight.takeIf { it > 0 && iw > 0 }?.let { (it * w / iw.toFloat()).roundToInt() } ?: (w * 9 / 16)
                v.drawable.toBitmap(w, h)
            } else {
                v.drawable.toBitmap(ICON_PX, ICON_PX)
            }
            AppArt(bmp.asImageBitmap(), v.isBanner, ambientOf(bmp)).also { artCache[packageName] = it }
        }
    }

    /** Portada del contenido "Continuar" (HU-020), leída del archivo guardado en la TV. */
    suspend fun mediaArt(item: MediaItem): AppArt? {
        val path = item.artPath ?: return null
        val key = "$path@${item.updatedMs}"
        return artCache[key] ?: withContext(Dispatchers.IO) {
            runCatching { BitmapFactory.decodeFile(path) }.getOrNull()?.let { bmp ->
                AppArt(bmp.asImageBitmap(), isBanner = true, ambient = ambientOf(bmp)).also { artCache[key] = it }
            }
        }
    }

    private fun ambientOf(bmp: Bitmap): Color =
        runCatching { Color(Bitmap.createScaledBitmap(bmp, 1, 1, true).getPixel(0, 0)).copy(alpha = 1f) }
            .getOrDefault(Tokens.Colors.wallpaperA)

    fun launch(app: AppEntry) {
        val intent = repo.launchIntent(app.packageName)
        if (intent == null || !shortcuts.start(intent)) showError(app.label)
    }

    fun open(shortcut: Shortcut, label: String) {
        val ok = when (shortcut) {
            Shortcut.SEARCH -> shortcuts.openSearch()
            Shortcut.NOTIFICATIONS -> shortcuts.openNotifications()
            Shortcut.PLAY -> shortcuts.openPlay()
            Shortcut.SETTINGS -> shortcuts.openSettings()
            Shortcut.PROFILE -> shortcuts.openProfiles()
            Shortcut.WIFI -> shortcuts.openWifi()
            Shortcut.BLUETOOTH -> shortcuts.openBluetooth()
            Shortcut.AUDIO -> shortcuts.openAudioOutput()
        }
        if (!ok) showError(label)
    }

    fun togglePin(packageName: String) = editFavorites { toggleFavorite(it, packageName) }

    fun hide(packageName: String) {
        viewModelScope.launch {
            editLock.withLock {
                prefs.hide(packageName)
                prefs.setFavorites(prefs.favorites.first() - packageName)
            }
        }
    }

    /**
     * Mover: en la fila de Apps (HU-018, ← →) o en "Mis apps" (HU-006, ↑ ↓).
     * En el panel, si la app no está fijada, se fija primero.
     */
    fun startMove(packageName: String, inPanel: Boolean) {
        if (inPanel) editFavorites { if (packageName in it) it else it + packageName }
        moving.value = Moving(packageName, inPanel)
    }

    fun moveBy(delta: Int) {
        val m = moving.value ?: return
        if (m.inPanel) editFavorites { moveFavorite(it, m.packageName, delta) } else editOrder { moveFavorite(it, m.packageName, delta) }
    }

    fun endMove() { moving.value = null }

    fun clearMessage() { message.value = null }

    private fun showError(label: String) {
        message.value = getApplication<Application>().getString(R.string.error_open, label)
    }

    private fun editFavorites(change: (List<String>) -> List<String>) {
        viewModelScope.launch { editLock.withLock { prefs.setFavorites(change(prefs.favorites.first())) } }
    }

    /** Edita el orden completo de las apps visibles (las nuevas entran al final, como se ven). */
    private fun editOrder(change: (List<String>) -> List<String>) {
        viewModelScope.launch {
            editLock.withLock {
                val shown = state.value.apps.map { it.app.packageName }
                val stored = prefs.order.first().ifEmpty { prefs.favorites.first() }
                val base = (stored.filter { it in shown } + shown).distinct()
                prefs.setOrder(change(base))
            }
        }
    }

    private companion object {
        const val SYSTEM_SETTINGS = "com.android.tv.settings"
        const val STOCK_HOME = "com.google.android.apps.tv.launcherx"
        const val BANNER_MIN_W = 640
        const val BANNER_MAX_W = 1280
        const val ICON_PX = 192
    }
}
