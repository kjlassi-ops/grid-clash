package com.gridclash.app.data

import com.gridclash.app.core.model.Difficulty
import com.gridclash.app.core.model.GridSize

data class UserPreferences(
    val playerName: String   = "Joueur",
    val gridSize: GridSize   = GridSize.SMALL,
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val soundEnabled: Boolean  = true,
    val musicEnabled: Boolean  = true,
    val vibrationEnabled: Boolean = true
)
