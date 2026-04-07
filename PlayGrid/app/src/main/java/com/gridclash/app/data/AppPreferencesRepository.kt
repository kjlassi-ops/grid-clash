package com.gridclash.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.gridclash.app.core.model.AppSettings
import com.gridclash.app.core.model.Difficulty
import com.gridclash.app.core.model.GridSize
import com.gridclash.app.core.model.ThemePreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("grid_clash_settings")

class AppPreferencesRepository(private val context: Context) {

    val settings: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            theme = runCatching { ThemePreference.valueOf(prefs[KEY_THEME] ?: ThemePreference.SYSTEM.name) }
                .getOrDefault(ThemePreference.SYSTEM),
            soundEnabled = prefs[KEY_SOUND] ?: true,
            musicEnabled = prefs[KEY_MUSIC] ?: true,
            lastLocalPseudo = prefs[KEY_PSEUDO] ?: "Joueur",
            lastHostIp = prefs[KEY_HOST_IP] ?: "",
            lastGridSize = runCatching { GridSize.valueOf(prefs[KEY_GRID_SIZE] ?: GridSize.THREE.name) }
                .getOrDefault(GridSize.THREE),
            lastDifficulty = runCatching { Difficulty.valueOf(prefs[KEY_DIFFICULTY] ?: Difficulty.MEDIUM.name) }
                .getOrDefault(Difficulty.MEDIUM)
        )
    }

    suspend fun setTheme(value: ThemePreference) = set(KEY_THEME, value.name)
    suspend fun setSoundEnabled(value: Boolean) = set(KEY_SOUND, value)
    suspend fun setMusicEnabled(value: Boolean) = set(KEY_MUSIC, value)
    suspend fun setLastPseudo(value: String) = set(KEY_PSEUDO, value)
    suspend fun setLastHostIp(value: String) = set(KEY_HOST_IP, value)
    suspend fun setLastGridSize(value: GridSize) = set(KEY_GRID_SIZE, value.name)
    suspend fun setLastDifficulty(value: Difficulty) = set(KEY_DIFFICULTY, value.name)

    private suspend fun set(key: androidx.datastore.preferences.core.Preferences.Key<String>, value: String) {
        context.dataStore.edit { it[key] = value }
    }

    private suspend fun set(key: androidx.datastore.preferences.core.Preferences.Key<Boolean>, value: Boolean) {
        context.dataStore.edit { it[key] = value }
    }

    private companion object {
        val KEY_THEME = stringPreferencesKey("theme")
        val KEY_SOUND = booleanPreferencesKey("sound")
        val KEY_MUSIC = booleanPreferencesKey("music")
        val KEY_PSEUDO = stringPreferencesKey("last_pseudo")
        val KEY_HOST_IP = stringPreferencesKey("last_host_ip")
        val KEY_GRID_SIZE = stringPreferencesKey("last_grid_size")
        val KEY_DIFFICULTY = stringPreferencesKey("last_difficulty")
    }
}
