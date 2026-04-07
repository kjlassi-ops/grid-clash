package com.gridclash.app

import android.app.Application
import com.gridclash.app.audio.GameAudioManager
import com.gridclash.app.data.AppPreferencesRepository
import com.gridclash.app.network.NetworkRepository

class GridClashApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

class AppContainer(val application: Application) {
    val networkRepository: NetworkRepository = NetworkRepository()
    val preferencesRepository: AppPreferencesRepository = AppPreferencesRepository(application)
    val audioManager: GameAudioManager = GameAudioManager(application)
}
