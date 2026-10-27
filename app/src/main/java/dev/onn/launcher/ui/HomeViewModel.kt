package dev.onn.launcher.ui

import android.app.Application
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
import dev.onn.launcher.domain.UsageRecord
import dev.onn.launcher.domain.cardOrder
import dev.onn.launcher.domain.heroCandidates
import dev.onn.launcher.domain.lastUsed
import dev.onn.launcher.domain.moveFavorite
import dev.onn.launcher.domain.orderApps
import dev.onn.launcher.domain.selectRecents
import dev.onn.launcher.domain.toggleFavorite
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
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

data class CardItem(val app: AppEntry, val lastUsed: LastUsed)

/** Imagen de una app ya convertida: banner de TV o icono. */
data class AppArt(val bitmap: ImageBitmap, val isBanner: Boolean)

data class HomeUiState(
    val cards: List<CardItem> = emptyList(),
    val pinned: List<AppEntry> = emptyList(),
    val hero: List<AppEntry> = emptyList(),
    val favorites: List<String> = emptyList(),
    val moving: String? = null,
    val message: String? = null,
    val canSearch: Boolean = false,
    val loaded: Boolean = false,
)

enum class Shortcut { SEARCH, NOTIFICATIONS, PLAY, SETTINGS, PROFILE }

class HomeViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AppRepository(app)
    private val prefs = PreferencesStore(app)
    private val recentsRepo = RecentsRepository(app)
    private val shortcuts = SystemShortcuts(app)
    private val selfPackage = app.packageName
    private val artCache = ConcurrentHashMap<String, AppArt>()

    private val installed = repo.changes().map { repo.launchableApps() }.flowOn(Dispatchers.IO)
    // Se llena en refreshRecents() (onResume), fuera del hilo principal.
    private val usage = MutableStateFlow<List<UsageRecord>>(emptyList())
    private val moving = MutableStateFlow<String?>(null)
    private val message = MutableStateFlow<String?>(null)

    val state: StateFlow<HomeUiState> = combine(
        installed, prefs.favorites, prefs.hidden, usage, combine(moving, message, ::Pair),
    ) { all, favorites, hidden, usageList, (mov, msg) ->
        val visibleHidden = hidden + SYSTEM_SETTINGS
        val apps = orderApps(all, favorites, visibleHidden, selfPackage)
        val byPackage = all.associateBy { it.packageName }
        val excluded = visibleHidden + STOCK_HOME + selfPackage
        val recents = selectRecents(usageList, byPackage.keys, excluded).mapNotNull { byPackage[it] }
        val lastByPkg = usageList.groupBy { it.packageName }.mapValues { (_, v) -> v.maxOf { it.lastUsedMs } }
        val now = System.currentTimeMillis()
        HomeUiState(
            cards = cardOrder(apps, recents).map { CardItem(it, lastUsed(lastByPkg[it.packageName], now)) },
            pinned = apps.filter { it.packageName in favorites },
            hero = heroCandidates(recents),
            favorites = favorites,
            moving = mov,
            message = msg,
            canSearch = shortcuts.canSearch(),
            loaded = true,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun refreshRecents() {
        viewModelScope.launch { usage.value = withContext(Dispatchers.IO) { recentsRepo.usage() } }
    }

    /** Arte de la app, convertido una sola vez y guardado en memoria (rendimiento). */
    suspend fun art(packageName: String): AppArt? = artCache[packageName] ?: withContext(Dispatchers.IO) {
        repo.visual(packageName)?.let { v ->
            val bmp = if (v.isBanner) v.drawable.toBitmap(BANNER_PX_W, BANNER_PX_H) else v.drawable.toBitmap(ICON_PX, ICON_PX)
            AppArt(bmp.asImageBitmap(), v.isBanner).also { artCache[packageName] = it }
        }
    }

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
        }
        if (!ok) showError(label)
    }

    fun togglePin(packageName: String) = editFavorites { toggleFavorite(it, packageName) }

    fun hide(packageName: String) {
        viewModelScope.launch {
            prefs.hide(packageName)
            prefs.setFavorites(prefs.favorites.first() - packageName)
        }
    }

    /** Mover (HU-006) en "Mis apps": si la app no esta fijada, se fija primero. */
    fun startMove(packageName: String) {
        editFavorites { if (packageName in it) it else it + packageName }
        moving.value = packageName
    }

    fun moveBy(delta: Int) {
        val pkg = moving.value ?: return
        editFavorites { moveFavorite(it, pkg, delta) }
    }

    fun endMove() { moving.value = null }

    fun clearMessage() { message.value = null }

    private fun showError(label: String) {
        message.value = getApplication<Application>().getString(R.string.error_open, label)
    }

    private fun editFavorites(change: (List<String>) -> List<String>) {
        viewModelScope.launch { prefs.setFavorites(change(prefs.favorites.first())) }
    }

    private companion object {
        const val SYSTEM_SETTINGS = "com.android.tv.settings"
        const val STOCK_HOME = "com.google.android.apps.tv.launcherx"
        const val BANNER_PX_W = 320
        const val BANNER_PX_H = 180
        const val ICON_PX = 96
    }
}