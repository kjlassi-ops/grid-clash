package com.gridclash.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.gridclash.app.GridClashApplication
import com.gridclash.app.core.model.*
import com.gridclash.app.ui.game.GameScreen
import com.gridclash.app.ui.home.HomeScreen
import com.gridclash.app.ui.multiplayer.MultiplayerScreen
import com.gridclash.app.ui.rules.RulesScreen
import com.gridclash.app.ui.settings.SettingsScreen
import com.gridclash.app.ui.solo.SoloSetupScreen

object Routes {
    const val HOME = "home"
    const val SOLO_SETUP = "solo_setup"
    const val MULTIPLAYER = "multiplayer"
    const val GAME = "game/{mode}/{difficulty}/{grid}/{win}/{name}"
    const val RULES = "rules"
    const val SETTINGS = "settings"

    fun game(config: GameConfig): String {
        val safeName = java.net.URLEncoder.encode(config.localPlayerName, "UTF-8")
        return "game/${config.mode.name}/${config.difficulty.name}/${config.gridSize.name}/${config.winLength}/$safeName"
    }
}

@Composable
fun AppNavGraph(navController: NavHostController, application: GridClashApplication) {
    val settings by application.container.preferencesRepository.settings.collectAsState(initial = AppSettings())

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onSoloClick = { navController.navigate(Routes.SOLO_SETUP) },
                onMultiplayerClick = { navController.navigate(Routes.MULTIPLAYER) },
                onRulesClick = { navController.navigate(Routes.RULES) },
                onSettingsClick = { navController.navigate(Routes.SETTINGS) }
            )
        }

        composable(Routes.SOLO_SETUP) {
            SoloSetupScreen(
                initialPseudo = settings.lastLocalPseudo,
                initialGridSize = settings.lastGridSize,
                initialDifficulty = settings.lastDifficulty,
                onBack = { navController.popBackStack() },
                onStart = { config ->
                    navController.navigate(Routes.game(config))
                }
            )
        }

        composable(Routes.MULTIPLAYER) {
            MultiplayerScreen(
                application = application,
                onBack = { navController.popBackStack() },
                onGameStartHost = {
                    navController.navigate(Routes.game(GameConfig(mode = GameMode.MULTI_HOST, localPlayerName = settings.lastLocalPseudo))) {
                        popUpTo(Routes.MULTIPLAYER) { inclusive = true }
                    }
                },
                onGameStartClient = {
                    navController.navigate(Routes.game(GameConfig(mode = GameMode.MULTI_CLIENT, localPlayerName = settings.lastLocalPseudo))) {
                        popUpTo(Routes.MULTIPLAYER) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Routes.GAME,
            arguments = listOf(
                navArgument("mode") { type = NavType.StringType },
                navArgument("difficulty") { type = NavType.StringType },
                navArgument("grid") { type = NavType.StringType },
                navArgument("win") { type = NavType.IntType },
                navArgument("name") { type = NavType.StringType }
            )
        ) { entry ->
            val mode = GameMode.valueOf(entry.arguments?.getString("mode") ?: GameMode.SOLO.name)
            val config = if (mode == GameMode.SOLO) {
                GameConfig(
                    mode = mode,
                    difficulty = Difficulty.valueOf(entry.arguments?.getString("difficulty") ?: Difficulty.MEDIUM.name),
                    gridSize = GridSize.valueOf(entry.arguments?.getString("grid") ?: GridSize.THREE.name),
                    winLength = entry.arguments?.getInt("win") ?: 3,
                    localPlayerName = java.net.URLDecoder.decode(entry.arguments?.getString("name") ?: "Joueur", "UTF-8"),
                    remotePlayerName = "Bot"
                )
            } else {
                val lobby = application.container.networkRepository.lobbyConfig
                GameConfig(
                    mode = mode,
                    difficulty = lobby.difficulty,
                    gridSize = lobby.gridSize,
                    winLength = lobby.winLength,
                    localPlayerName = if (mode == GameMode.MULTI_HOST) lobby.hostName else lobby.clientName,
                    remotePlayerName = if (mode == GameMode.MULTI_HOST) lobby.clientName else lobby.hostName
                )
            }

            GameScreen(
                config = config,
                application = application,
                onBackToMenu = {
                    application.container.networkRepository.clear()
                    navController.navigate(Routes.HOME) { popUpTo(Routes.HOME) { inclusive = true } }
                }
            )
        }

        composable(Routes.RULES) { RulesScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.SETTINGS) { SettingsScreen(application.container.preferencesRepository, onBack = { navController.popBackStack() }) }
    }
}
