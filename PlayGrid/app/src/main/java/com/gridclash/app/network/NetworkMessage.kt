package com.gridclash.app.network

import kotlinx.serialization.Serializable

enum class MessageType {
    PLAYER_JOIN,
    GAME_START,
    PLAY_MOVE,
    GAME_OVER,
    REMATCH,
    DISCONNECT,
    PING
}

@Serializable
data class NetworkMessage(
    val type: MessageType,
    val playerName: String? = null,
    val hostName: String? = null,
    val clientName: String? = null,
    val hostSymbol: String? = null,
    val clientSymbol: String? = null,
    val gridSize: Int? = null,
    val winLength: Int? = null,
    val difficulty: String? = null,
    val cellIndex: Int? = null,
    val result: String? = null,
    val winner: String? = null,
    val error: String? = null
)
