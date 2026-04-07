package com.gridclash.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gridclash.app.audio.AudioManager
import com.gridclash.app.core.model.Difficulty
import com.gridclash.app.core.model.GridSize
import com.gridclash.app.data.PreferencesRepository
import com.gridclash.app.data.UserPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferencesRepository: PreferencesRepository,
    private val audioManager: AudioManager
) : ViewModel() {

    val preferences: StateFlow<UserPreferences> = preferencesRepository.preferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserPreferences())

    fun savePlayerName(name: String) {
        viewModelScope.launch { preferencesRepository.savePlayerName(name) }
    }

    fun saveGridSize(gs: GridSize) {
        viewModelScope.launch { preferencesRepository.saveGridSize(gs) }
    }

    fun saveDifficulty(d: Difficulty) {
        viewModelScope.launch { preferencesRepository.saveDifficulty(d) }
    }

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.saveSoundEnabled(enabled)
            audioManager.applySettings(
                sound     = enabled,
                music     = preferences.value.musicEnabled,
                vibration = preferences.value.vibrationEnabled
            )
        }
    }

    fun setMusicEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.saveMusicEnabled(enabled)
            audioManager.applySettings(
                sound     = preferences.value.soundEnabled,
                music     = enabled,
                vibration = preferences.value.vibrationEnabled
            )
        }
    }

    fun setVibrationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.saveVibrationEnabled(enabled)
            audioManager.vibrationEnabled = enabled
        }
    }
}
