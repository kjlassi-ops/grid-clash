package com.gridclash.app

import android.app.Application
import com.gridclash.app.audio.AudioManager
import com.gridclash.app.data.PreferencesRepository
import com.gridclash.app.network.NetworkRepository

/**
 * Application class — point d'entrée du graphe de dépendances.
 */
class GridClashApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }

    override fun onTerminate() {
        super.onTerminate()
        container.audioManager.release()
    }
}

/**
 * Conteneur de dépendances partagées entre tous les écrans.
 */
class AppContainer(val application: Application) {
    val networkRepository: NetworkRepository    = NetworkRepository()
    val preferencesRepository: PreferencesRepository = PreferencesRepository(application)
    val audioManager: AudioManager              = AudioManager(application)
}
