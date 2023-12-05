package com.bryankeltonadams.banko

import android.content.ContentValues.TAG
import android.util.Log
import com.bryankeltonadams.data.model.DomainGame
import com.bryankeltonadams.data.model.FirebaseGame
import com.bryankeltonadams.data.model.Player
import com.bryankeltonadams.data.model.Round
import com.bryankeltonadams.data.model.Setting
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

    suspend fun finishGame(gameCode: String) {
        val gameDocRef = db.collection("games").document(gameCode)

        gameDocRef.update("finished", true).await()
    }

    suspend fun updateSetting(gameCode: String, settingToUpdate: Setting) {
        val gameDocRef = db.collection("games").document(gameCode)

        // Fetch the current array of settings
        val documentSnapshot = gameDocRef.get().await()

        // Check if the document exists
        if (documentSnapshot.exists()) {
            val game = documentSnapshot.toObject<FirebaseGame>()
            // Get the current data as a Map

            // Get the current array of settings
            val currentSettings =
                (game?.settings as? MutableList<Setting> ?: emptyList()).toMutableList()

            // Find the index of the setting to update
            val indexToUpdate = currentSettings.indexOfFirst { it.name == settingToUpdate.name }

            // If the setting is found, update its value
            if (indexToUpdate != -1) {
                currentSettings[indexToUpdate] = settingToUpdate
            } else {
                // Handle the case where the setting is not found
                // or you may choose to add it as a new setting
                currentSettings.add(settingToUpdate)
            }

            // Update the entire document with the modified array
            gameDocRef.update("settings", currentSettings).await()
        } else {
            // Handle the case where the document doesn't exist
            println("Document does not exist.")
        }
    }

    suspend fun bankPoints(
        gameCode: String,
        currentPlayer: String,
        bankingPlayers: List<String>,
        remainingPlayers: List<String>,
        currentPoints: Int,
        fullOrderedPlayerList: List<String>,
    ) {
        val gameDocRef = db.collection("games").document(gameCode)

        bankingPlayers.forEach { bankingPlayer ->

            val bankingPlayerDocRef = gameDocRef.collection("players").document(bankingPlayer)
            bankingPlayerDocRef.update("points", FieldValue.increment(currentPoints.toDouble()))
                .await()

        }

        if (remainingPlayers.isEmpty()) {
            gameDocRef.update(
                "round.roundNum", FieldValue.increment(1),
                "round.currentPoints", 0,
                "round.dieOne", 0,
                "round.dieTwo", 0,
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
            var nextPlayerInOrderedList = currentPlayer
            if (currentPlayer in bankingPlayers) {
                nextPlayerInOrderedList =
                    remainingPlayers.getOrNull(myIndexInOrderedList + 1)
                        ?: remainingPlayers[0]
            }
            gameDocRef.update(
                "round.activeOrderedPlayerNames", remainingPlayers,
                "currentPlayer", nextPlayerInOrderedList
            ).await()
        }
    }

    suspend fun rollDice(
        gameCode: String,
        round: Round,
        nextPlayerName: String
    ) {
        val gameDocRef = db.collection("games").document(gameCode)

        gameDocRef.update(
            "round.dieOne", 0,
            "round.dieTwo", 0
        ).await()
        gameDocRef.update("round", round).await()
        gameDocRef.update("currentPlayer", nextPlayerName).await()
    }


    fun startGame(gameCode: String, activeOrderedPlayerNames: List<String>) {
        val gameDocRef = db.collection("games").document(gameCode)

        gameDocRef.update("round", Round(1, 0, 0, 0, 1, activeOrderedPlayerNames))
    }

    fun createGame(gameCode: String, name: String): String {
        val player = Player(
            name = name,
            points = 0,
            hostCreated = true
        )


        val game = FirebaseGame(
            dateLastModified = Timestamp.now(),
            joinCode = gameCode,
            endRoundNum = 10,
            orderedPlayerNames = listOf(name),
            host = name,
            currentPlayer = name,
            settings = listOf(
                Setting(
                    "Host can roll or bank for other players with devices.",
                    "true"
                )
            )
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
            settings = firebaseGame.settings,
            finished = firebaseGame.finished
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
}