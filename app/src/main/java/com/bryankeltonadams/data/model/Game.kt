package com.bryankeltonadams.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp


data class DomainGame(
    val dateLastModified: Timestamp? = null,
    val joinCode: String = "",
    var players: List<Player> = emptyList(),
    val round: Round? = null,
    val endRoundNum: Int = 0,
    val orderedPlayerNames: List<String> = emptyList(),
    val host: String = "",
    val currentPlayer: String = "",
    val settings: List<Setting> = emptyList(),
    val finished : Boolean = false,
)

data class FirebaseGame(
    val dateLastModified: Timestamp? = null,
    val joinCode: String = "",
    val round: Round? = null,
    val endRoundNum: Int = 0,
    val orderedPlayerNames: List<String> = emptyList(),
    val host: String = "",
    val currentPlayer: String = "",
    val settings: List<Setting> = emptyList(),
    val finished : Boolean = false,
)

data class Setting(
    val name: String = "",
    val value: String = "",
)

@Entity(
    tableName = "player",
)
data class Player(
    @PrimaryKey
    val name: String = "",
    val points: Int = 0,
    val hostCreated: Boolean = false,
)

data class Round(
    val roundNum: Int = 0,
    val currentPoints: Int = 0,
    val dieOne: Int = 0,
    val dieTwo: Int = 0,
    val currentRoll: Int = 0,
    val activeOrderedPlayerNames: List<String> = emptyList(),
)