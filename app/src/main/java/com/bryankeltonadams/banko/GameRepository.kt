package com.bryankeltonadams.banko

import android.content.ContentValues.TAG
import android.util.Log
import com.bryankeltonadams.data.model.DomainGame
import com.bryankeltonadams.data.model.FirebaseGame
import com.bryankeltonadams.data.model.Player
import com.bryankeltonadams.data.model.Round
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Singleton


@Singleton
class GameRepository(
) {
    private val db = FirebaseFirestore.getInstance()

    suspend fun deleteGame(gameCode: String) {
        val gameDocRef = db.collection("games").document(gameCode)

        val players = gameDocRef.collection("players").get().await()
        players.forEach { player ->
            gameDocRef.collection("players").document(player.id).delete().await()
        }

        gameDocRef.delete().await()
    }

    suspend fun bankPoints(
        gameCode: String,
        currentPlayer: String,
        remainingPlayers: List<String>,
        currentScore: Int,
        fullOrderedPlayerList: List<String>,
    ) {
        val gameDocRef = db.collection("games").document(gameCode)
        val currentPlayerDocRef = gameDocRef.collection("players").document(currentPlayer)

        currentPlayerDocRef.update("points", FieldValue.increment(currentScore.toDouble())).await()

        if (remainingPlayers.isEmpty()) {
            gameDocRef.update(
                "round.roundNum", FieldValue.increment(1),
                "round.currentScore", 0,
                "round.0", 0,
                "round.0", 0,
                "round.currentRoll", 1,
                "round.activeOrderedPlayerNames", fullOrderedPlayerList
            ).await()

            val myIndexInOrderedList = fullOrderedPlayerList.indexOf(currentPlayer)
            val nextPlayerInOrderedList =
                fullOrderedPlayerList.getOrNull(myIndexInOrderedList + 1)
                    ?: fullOrderedPlayerList[0]
            gameDocRef.update("currentPlayer", nextPlayerInOrderedList)
        } else {
            val myIndexInOrderedList = remainingPlayers.indexOf(currentPlayer)
            val nextPlayerInOrderedList =
                remainingPlayers.getOrNull(myIndexInOrderedList + 1)
                    ?: remainingPlayers[0]
            gameDocRef.update(
                "round.activeOrderedPlayerNames", remainingPlayers,
                "currentPlayer", nextPlayerInOrderedList
            ).await()
        }
    }

    suspend fun rollDice(
        gameCode: String,
        currentRoll: Int,
        currentScore: Int,
        nextPlayer: String,
        dice: Pair<Int, Int>,
        roundNum: Int,
        activeOrderedPlayerNames: List<String>,
        fullOrderedPlayerNames: List<String>,
        manuallyEntered: Int? = null
    ) {
        val gameDocRef = db.collection("games").document(gameCode)

        gameDocRef.update(
            "round.dieOne", 0,
            "round.dieTwo", 0
        ).await()


        val isBeforeFirstThreeRolls = currentRoll <= 3


        var cumulativeDiceValue = dice.first + dice.second

        if (manuallyEntered != null) {
            cumulativeDiceValue = manuallyEntered
        }

        var pointValueToAdd = if (isBeforeFirstThreeRolls) {
            if (cumulativeDiceValue == 7) {
                70
            } else {
                cumulativeDiceValue
            }
        } else {
            if (cumulativeDiceValue == 7) {
                0
            } else if (dice.first == dice.second || manuallyEntered == 13) {
                currentScore
            } else {
                cumulativeDiceValue
            }
        }


        var finalRoundNum = roundNum
        var finalRoll = currentRoll
        var finalActiveOrderedPlayerNames = activeOrderedPlayerNames
        if (pointValueToAdd == 0) {
            finalRoundNum += 1
            finalRoll = 1
            finalActiveOrderedPlayerNames = fullOrderedPlayerNames

        } else {
            pointValueToAdd += currentScore
            finalRoll += 1
        }

        val currentRound =
            Round(
                finalRoundNum,
                pointValueToAdd,
                if (manuallyEntered != null) 0 else dice.first,
                if (manuallyEntered != null) 0 else dice.second,
                finalRoll,
                activeOrderedPlayerNames = finalActiveOrderedPlayerNames
            )
        gameDocRef.update("round", currentRound).await()
        gameDocRef.update("currentPlayer", nextPlayer).await()
    }


    fun startGame(gameCode: String, activeOrderedPlayerNames: List<String>) {
        val gameDocRef = db.collection("games").document(gameCode)

        gameDocRef.update("round", Round(1, 0, 0, 0, 1, activeOrderedPlayerNames))
    }

    suspend fun createGame(name: String): String {
        val gameCode = generateValidGameCode()
        val player = Player(
            name = name,
            points = 0,
        )


        val game = FirebaseGame(
            dateLastModified = Timestamp.now(),
            joinCode = gameCode,
            endRoundNum = 10,
            orderedPlayerNames = listOf(name),
            host = name,
            currentPlayer = name
        )


        db.collection("games").document(gameCode)
            .set(game)

        db.collection("games").document(gameCode).collection("players").document(name)
            .set(player)

        return gameCode
    }

    suspend fun getGame(gameCode: String): Flow<DomainGame> {
        return callbackFlow {

            val gameDocRef = db.collection("games").document(gameCode)

            var fireBaseGame: FirebaseGame? = null
            var players: List<Player>? = null

            val tryEmitDomainGame = {
                if (fireBaseGame != null && players != null) {
                    val domainGame = mapToDomainGame(fireBaseGame!!, players!!)
                    trySend(domainGame)
                }
            }

            val listener = gameDocRef.addSnapshotListener { snapshot, e ->
                if (snapshot != null && snapshot.exists()) {
                    Log.d(TAG, "Current data: ${snapshot.data}")
                    fireBaseGame = snapshot.toObject<FirebaseGame>()
                    tryEmitDomainGame()
                }
            }

            val playerListener = db.collection("games").document(gameCode).collection("players")
                .addSnapshotListener { snapshot, e ->
                    if (snapshot != null && !snapshot.isEmpty) {
                        Log.d(TAG, "Current data: ${snapshot.documents}")
                        players = snapshot.documents.map { it.toObject<Player>()!! }
                        tryEmitDomainGame()
                    }
                }

            awaitClose {
                listener.remove()
                playerListener.remove()
            }
        }
    }


    fun mapToDomainGame(firebaseGame: FirebaseGame, players: List<Player>): DomainGame {
        return DomainGame(
            dateLastModified = firebaseGame.dateLastModified,
            joinCode = firebaseGame.joinCode,
            players = players,
            round = firebaseGame.round,
            endRoundNum = firebaseGame.endRoundNum,
            orderedPlayerNames = firebaseGame.orderedPlayerNames,
            host = firebaseGame.host,
            currentPlayer = firebaseGame.currentPlayer,
        )
    }

    suspend fun gameExists(gameCode: String): Boolean {
        val docRef = db.collection("games").document(gameCode)

        val snapshot = docRef.get().await()

        return snapshot.exists()
    }

    fun updatePlayerOrder(gameCode: String, playerNames: List<String>) {
        val gameDocRef = db.collection("games").document(gameCode)

        gameDocRef.update("orderedPlayerNames", playerNames)
    }

    fun addPlayer(player: Player, gameCode: String) {
        val gameDocRef = db.collection("games").document(gameCode)

        gameDocRef.update("orderedPlayerNames", FieldValue.arrayUnion(player.name))

        db.collection("games").document(gameCode).collection("players").document(player.name)
            .set(player)

    }


    private suspend fun generateValidGameCode(): String {
        var gameCode = generate5DigitAlphaNumericCode()
        var docRef = db.collection("games").document(gameCode)
        var checkGameCode = true
        while (checkGameCode) {
            docRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        gameCode = generate5DigitAlphaNumericCode()
                        docRef = db.collection("games").document(gameCode)
                    } else {
                        checkGameCode = false
                        return@addOnSuccessListener
                    }
                }
                .await()
        }
        return gameCode
    }

    private fun generate5DigitAlphaNumericCode(): String {
        val allowedChars = ('A'..'Z') + ('0'..'9')
        return (1..5)
            .map { allowedChars.random() }
            .joinToString("")
    }
}