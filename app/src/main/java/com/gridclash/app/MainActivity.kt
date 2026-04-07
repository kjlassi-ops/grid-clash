package com.gridclash.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.gridclash.app.ui.navigation.AppNavGraph
import com.gridclash.app.ui.theme.Background
import com.gridclash.app.ui.theme.GridClashTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as GridClashApplication

        setContent {
            GridClashTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color    = Background
                ) {
                    val navController = rememberNavController()
                    AppNavGraph(
                        navController = navController,
                        application   = app
                    )
                }
            }
        }
    }
}
