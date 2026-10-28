package dev.onn.launcher.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "launcher_prefs")

/** Orden de las apps (HU-018), favoritas de "Mis apps" y apps ocultas (TDD 5). */
class PreferencesStore(private val context: Context) {
    private val favoritesKey = stringPreferencesKey("favorites")
    private val hiddenKey = stringSetPreferencesKey("hidden")
    private val orderKey = stringPreferencesKey("order")

    val order: Flow<List<String>> = context.dataStore.data.map { prefs ->
        prefs[orderKey]?.split(SEPARATOR)?.filter { it.isNotBlank() } ?: emptyList()
    }

    suspend fun setOrder(list: List<String>) {
        context.dataStore.edit { it[orderKey] = list.joinToString(SEPARATOR) }
    }

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