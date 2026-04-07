package com.gridclash.app

import android.app.Application
import com.gridclash.app.network.NetworkRepository

/**
 * Application class — point d'entrée du graphe de dépendances.
 * Injection manuelle simple : pas de Hilt pour garder le MVP minimal.
 */
class GridClashApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

/**
 * Conteneur de dépendances partagées entre tous les écrans.
 */
class AppContainer(val application: Application) {
    val networkRepository: NetworkRepository = NetworkRepository()
}
