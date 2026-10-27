package dev.onn.launcher.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.onn.launcher.data.AppRepository
import dev.onn.launcher.data.PreferencesStore
import dev.onn.launcher.data.RecentsRepository
import dev.onn.launcher.domain.AppEntry
import dev.onn.launcher.domain.UsageRecord
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

data class HomeUiState(
    val apps: List<AppEntry> = emptyList(),
    val recents: List<AppEntry> = emptyList(),
    val favorites: List<String> = emptyList(),
    val moving: String? = null,
    val message: String? = null,
    val loaded: Boolean = false,
)

enum class Shortcut { NOTIFICATIONS, PLAY, SETTINGS, PROFILE }

class HomeViewModel(app: Application) : AndroidViewModel(app) {
    val repo = AppRepository(app)
    private val prefs = PreferencesStore(app)
    private val recentsRepo = RecentsRepository(app)
    private val shortcuts = SystemShortcuts(app)
    private val selfPackage = app.packageName

    private val installed = repo.changes().map { repo.launchableApps() }.flowOn(Dispatchers.IO)
    // Se llena en refreshRecents() (onResume), fuera del hilo principal.
    private val usage = MutableStateFlow<List<UsageRecord>>(emptyList())
    private val moving = MutableStateFlow<String?>(null)
    private val message = MutableStateFlow<String?>(null)

    val state: StateFlow<HomeUiState> = combine(
        installed, prefs.favorites, prefs.hidden, usage, combine(moving, message, ::Pair),
    ) { all, favorites, hidden, usageList, (mov, msg) ->
        val apps = orderApps(all, favorites, hidden + SYSTEM_SETTINGS, selfPackage)
        val byPackage = all.associateBy { it.packageName }
        val excluded = hidden + EXCLUDED_FROM_RECENTS + selfPackage
        val recents = selectRecents(usageList, byPackage.keys, excluded).mapNotNull { byPackage[it] }
        HomeUiState(apps, recents, favorites, mov, msg, loaded = true)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun refreshRecents() {
        viewModelScope.launch { usage.value = withContext(Dispatchers.IO) { recentsRepo.usage() } }
    }

    fun launch(app: AppEntry) {
        val intent = repo.launchIntent(app.packageName)
        if (intent == null || !shortcuts.start(intent)) showError(app.label)
    }

    fun open(shortcut: Shortcut, label: String) {
        val ok = when (shortcut) {
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

    /** Mover (HU-006): si la app no esta fijada, se fija primero para poder ordenarla. */
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
        message.value = getApplication<Application>().getString(dev.onn.launcher.R.string.error_open, label)
    }

    private fun editFavorites(change: (List<String>) -> List<String>) {
        viewModelScope.launch { prefs.setFavorites(change(prefs.favorites.first())) }
    }

    private companion object {
        const val SYSTEM_SETTINGS = "com.android.tv.settings"
        val EXCLUDED_FROM_RECENTS = setOf(SYSTEM_SETTINGS, "com.google.android.apps.tv.launcherx")
    }
}