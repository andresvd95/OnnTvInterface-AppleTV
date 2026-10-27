package dev.onn.launcher.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "launcher_prefs")

/** Favoritas y su orden, y apps ocultas (TDD 5). Unico estado que se guarda. */
class PreferencesStore(private val context: Context) {
    private val favoritesKey = stringPreferencesKey("favorites")
    private val hiddenKey = stringSetPreferencesKey("hidden")

    val favorites: Flow<List<String>> = context.dataStore.data.map { prefs ->
        prefs[favoritesKey]?.split(SEPARATOR)?.filter { it.isNotBlank() } ?: emptyList()
    }

    val hidden: Flow<Set<String>> = context.dataStore.data.map { it[hiddenKey] ?: emptySet() }

    suspend fun setFavorites(list: List<String>) {
        context.dataStore.edit { it[favoritesKey] = list.joinToString(SEPARATOR) }
    }

    suspend fun hide(packageName: String) {
        context.dataStore.edit { it[hiddenKey] = (it[hiddenKey] ?: emptySet()) + packageName }
    }

    private companion object { const val SEPARATOR = "|" }
}