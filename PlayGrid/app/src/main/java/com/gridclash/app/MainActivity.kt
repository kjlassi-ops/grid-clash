package com.gridclash.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.gridclash.app.ui.navigation.AppNavGraph
import com.gridclash.app.ui.theme.GridClashTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as GridClashApplication

        lifecycle.addObserver(
            androidx.lifecycle.LifecycleEventObserver { _, event ->
                when (event) {
                    androidx.lifecycle.Lifecycle.Event.ON_RESUME -> app.container.audioManager.onAppResume()
                    androidx.lifecycle.Lifecycle.Event.ON_PAUSE -> app.container.audioManager.onAppPause()
                    else -> Unit
                }
            }
        )

        setContent {
            val settings by app.container.preferencesRepository.settings.collectAsState(initial = com.gridclash.app.core.model.AppSettings())
            app.container.audioManager.soundEnabled = settings.soundEnabled
            app.container.audioManager.musicEnabled = settings.musicEnabled
            GridClashTheme(themePreference = settings.theme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    AppNavGraph(navController = navController, application = app)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleScope.launch {
            (application as GridClashApplication).container.audioManager.release()
        }
    }
}
