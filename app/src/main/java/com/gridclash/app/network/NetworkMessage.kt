package com.gridclash.app.network

import kotlinx.serialization.Serializable

// ─── Types de messages ────────────────────────────────────────────────────────

enum class MessageType {
    PLAYER_JOIN,    // client → host : annonce sa présence + pseudo
    GAME_START,     // host → client : démarre la partie avec les symboles + taille de grille
    PLAY_MOVE,      // bidirectionnel : indique un coup joué
    GAME_OVER,      // bidirectionnel : fin de partie
    REMATCH,        // bidirectionnel : demande / accepte une revanche
    DISCONNECT,     // bidirectionnel : déconnexion propre
    PING            // keepalive
}

// ─── Structure du message (JSON flat) ────────────────────────────────────────

@Serializable
data class NetworkMessage(
    val type: MessageType,

    // PLAYER_JOIN
    val playerName: String? = null,

    // GAME_START
    val hostSymbol: String?   = null,   // "X" ou "O"
    val clientSymbol: String? = null,
    val gridSize: String?     = null,   // "SMALL", "MEDIUM", "LARGE"
    val hostName: String?     = null,

    // PLAY_MOVE
    val cellIndex: Int? = null,

    // GAME_OVER
    val result: String? = null,         // "X_WINS", "O_WINS", "DRAW"
    val winner: String? = null          // "X" ou "O" ou null
)
