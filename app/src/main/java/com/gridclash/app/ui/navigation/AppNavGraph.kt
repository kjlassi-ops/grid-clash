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
    const val GAME        = "game/{mode}/{difficulty}"
    const val RULES       = "rules"
    const val SETTINGS    = "settings"

    fun game(mode: GameMode, difficulty: Difficulty = Difficulty.MEDIUM) =
        "game/${mode.name}/${difficulty.name}"
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
                onBack  = { navController.popBackStack() },
                onStart = { difficulty ->
                    navController.navigate(Routes.game(GameMode.SOLO, difficulty))
                }
            )
        }

        composable(Routes.MULTIPLAYER) {
            MultiplayerScreen(
                application     = application,
                onBack          = { navController.popBackStack() },
                onGameStartHost = {
                    navController.navigate(Routes.game(GameMode.MULTI_HOST)) {
                        popUpTo(Routes.MULTIPLAYER) { inclusive = true }
                    }
                },
                onGameStartClient = {
                    navController.navigate(Routes.game(GameMode.MULTI_CLIENT)) {
                        popUpTo(Routes.MULTIPLAYER) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route     = Routes.GAME,
            arguments = listOf(
                navArgument("mode")       { type = NavType.StringType },
                navArgument("difficulty") { type = NavType.StringType }
            )
        ) { entry ->
            val mode       = GameMode.valueOf(entry.arguments?.getString("mode") ?: "SOLO")
            val difficulty = Difficulty.valueOf(entry.arguments?.getString("difficulty") ?: "MEDIUM")

            GameScreen(
                mode            = mode,
                difficulty      = difficulty,
                application     = application,
                onBackToMenu    = {
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
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
