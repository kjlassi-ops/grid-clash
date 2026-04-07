package com.gridclash.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.gridclash.app.core.model.Difficulty
import com.gridclash.app.core.model.GridSize
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "gridclash_prefs")

class PreferencesRepository(private val context: Context) {

    private object Keys {
        val PLAYER_NAME       = stringKey("player_name")
        val GRID_SIZE         = stringKey("grid_size")
        val DIFFICULTY        = stringKey("difficulty")
        val SOUND_ENABLED     = booleanKey("sound_enabled")
        val MUSIC_ENABLED     = booleanKey("music_enabled")
        val VIBRATION_ENABLED = booleanKey("vibration_enabled")
    }

    val preferences: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        UserPreferences(
            playerName        = prefs[Keys.PLAYER_NAME] ?: "Joueur",
            gridSize          = GridSize.valueOf(prefs[Keys.GRID_SIZE] ?: GridSize.SMALL.name),
            difficulty        = Difficulty.valueOf(prefs[Keys.DIFFICULTY] ?: Difficulty.MEDIUM.name),
            soundEnabled      = prefs[Keys.SOUND_ENABLED] ?: true,
            musicEnabled      = prefs[Keys.MUSIC_ENABLED] ?: true,
            vibrationEnabled  = prefs[Keys.VIBRATION_ENABLED] ?: true
        )
    }

    suspend fun savePlayerName(name: String) {
        context.dataStore.edit { it[Keys.PLAYER_NAME] = name.take(20).ifBlank { "Joueur" } }
    }

    suspend fun saveGridSize(gridSize: GridSize) {
        context.dataStore.edit { it[Keys.GRID_SIZE] = gridSize.name }
    }

    suspend fun saveDifficulty(difficulty: Difficulty) {
        context.dataStore.edit { it[Keys.DIFFICULTY] = difficulty.name }
    }

    suspend fun saveSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SOUND_ENABLED] = enabled }
    }

    suspend fun saveMusicEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.MUSIC_ENABLED] = enabled }
    }

    suspend fun saveVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.VIBRATION_ENABLED] = enabled }
    }
}
