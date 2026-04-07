package com.gridclash.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.gridclash.app.GridClashApplication
import com.gridclash.app.core.model.Difficulty
import com.gridclash.app.core.model.GameMode
import com.gridclash.app.core.model.GridSize
import com.gridclash.app.ui.game.GameScreen
import com.gridclash.app.ui.home.HomeScreen
import com.gridclash.app.ui.multiplayer.MultiplayerScreen
import com.gridclash.app.ui.rules.RulesScreen
import com.gridclash.app.ui.settings.SettingsScreen
import com.gridclash.app.ui.solo.SoloSetupScreen

// ─── Routes ──────────────────────────────────────────────────────────────────

object Routes {
    const val HOME        = "home"
    const val SOLO_SETUP  = "solo_setup"
    const val MULTIPLAYER = "multiplayer"
    const val GAME        = "game/{mode}/{difficulty}/{gridSize}/{localName}/{opponentName}"
    const val RULES       = "rules"
    const val SETTINGS    = "settings"

    fun game(
        mode: GameMode,
        difficulty: Difficulty = Difficulty.MEDIUM,
        gridSize: GridSize = GridSize.SMALL,
        localName: String = "Toi",
        opponentName: String = "Bot"
    ) = "game/${mode.name}/${difficulty.name}/${gridSize.name}/${localName.encode()}/${opponentName.encode()}"

    private fun String.encode() = java.net.URLEncoder.encode(this, "UTF-8")
}

// ─── Graphe de navigation ────────────────────────────────────────────────────

@Composable
fun AppNavGraph(
    navController: NavHostController,
    application: GridClashApplication
) {
    NavHost(navController = navController, startDestination = Routes.HOME) {

        composable(Routes.HOME) {
            HomeScreen(
                onSoloClick        = { navController.navigate(Routes.SOLO_SETUP) },
                onMultiplayerClick = { navController.navigate(Routes.MULTIPLAYER) },
                onRulesClick       = { navController.navigate(Routes.RULES) },
                onSettingsClick    = { navController.navigate(Routes.SETTINGS) }
            )
        }

        composable(Routes.SOLO_SETUP) {
            SoloSetupScreen(
                application = application,
                onBack  = { navController.popBackStack() },
                onStart = { difficulty, gridSize, playerName ->
                    navController.navigate(
                        Routes.game(GameMode.SOLO, difficulty, gridSize, playerName, "Bot")
                    )
                }
            )
        }

        composable(Routes.MULTIPLAYER) {
            MultiplayerScreen(
                application       = application,
                onBack            = { navController.popBackStack() },
                onGameStartHost   = { gridSize, localName, opponentName ->
                    navController.navigate(
                        Routes.game(GameMode.MULTI_HOST, gridSize = gridSize,
                            localName = localName, opponentName = opponentName)
                    ) { popUpTo(Routes.MULTIPLAYER) { inclusive = true } }
                },
                onGameStartClient = { gridSize, localName, opponentName ->
                    navController.navigate(
                        Routes.game(GameMode.MULTI_CLIENT, gridSize = gridSize,
                            localName = localName, opponentName = opponentName)
                    ) { popUpTo(Routes.MULTIPLAYER) { inclusive = true } }
                }
            )
        }

        composable(
            route     = Routes.GAME,
            arguments = listOf(
                navArgument("mode")         { type = NavType.StringType },
                navArgument("difficulty")   { type = NavType.StringType },
                navArgument("gridSize")     { type = NavType.StringType },
                navArgument("localName")    { type = NavType.StringType },
                navArgument("opponentName") { type = NavType.StringType }
            )
        ) { entry ->
            val mode         = GameMode.valueOf(entry.arguments?.getString("mode") ?: "SOLO")
            val difficulty   = Difficulty.valueOf(entry.arguments?.getString("difficulty") ?: "MEDIUM")
            val gridSize     = GridSize.valueOf(entry.arguments?.getString("gridSize") ?: "SMALL")
            val localName    = java.net.URLDecoder.decode(entry.arguments?.getString("localName") ?: "Toi", "UTF-8")
            val opponentName = java.net.URLDecoder.decode(entry.arguments?.getString("opponentName") ?: "Bot", "UTF-8")

            GameScreen(
                mode         = mode,
                difficulty   = difficulty,
                gridSize     = gridSize,
                localName    = localName,
                opponentName = opponentName,
                application  = application,
                onBackToMenu = {
                    application.container.networkRepository.clear()
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.RULES) {
            RulesScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(application = application, onBack = { navController.popBackStack() })
        }
    }
}
