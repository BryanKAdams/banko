package com.bryankeltonadams.banko.ui.screens

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bryankeltonadams.banko.GameRepository
import com.bryankeltonadams.banko.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class BankoGameScreenViewModel
@Inject
constructor(
    private val savedStateHandle: SavedStateHandle,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val gameRepository: GameRepository

) : ViewModel() {

    private var _uiState = MutableStateFlow(BankoGameScreenUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = uiState.value.copy(
                gameCode = userPreferencesRepository.sessionPreferencesFlow.first().gameCode,
                playerName = userPreferencesRepository.sessionPreferencesFlow.first().name,
            )
            gameRepository.getGame(uiState.value.gameCode).collect { domainGame ->
                val sortedPlayersByPlayerOrder = domainGame.players.sortedBy { player ->
                    domainGame.orderedPlayerNames.indexOf(player.name)
                }
                domainGame.players = sortedPlayersByPlayerOrder
                _uiState.value = uiState.value.copy(game = domainGame)
            }
        }

    }

    fun onGameFinished() {
        viewModelScope.launch {
            gameRepository.deleteGame(_uiState.value.gameCode)
        }
    }

    private fun rollDice(): Pair<Int, Int> {
        val dice1 = Random.nextInt(1, 7) // Generates a random number between 1 and 6 (inclusive)
        val dice2 = Random.nextInt(1, 7)
        return dice1 to dice2
    }

    fun onBank() {
        viewModelScope.launch {
            gameRepository.bankPoints(
                gameCode = _uiState.value.gameCode,
                currentPlayer = _uiState.value.playerName,
                remainingPlayers = _uiState.value.game?.round?.activeOrderedPlayerNames?.minus(
                    _uiState.value.playerName
                ) ?: emptyList(),
                currentScore = _uiState.value.game?.round?.currentPoints ?: 0,
                fullOrderedPlayerList = _uiState.value.game?.orderedPlayerNames ?: emptyList(),
            )

        }
    }


    fun onDiceRolled(manuallyEntered: Int? = null) {

        val (dieOne, dieTwo) = rollDice()

        viewModelScope.launch {
            val playerPosition =
                _uiState.value.game?.orderedPlayerNames?.indexOf(_uiState.value.playerName)
            // nextPlayerPosition taking into account end of array starting at 0 again
            // check if playerPosition is last in array
            val nextPlayerPosition =
                if (playerPosition == _uiState.value.game?.orderedPlayerNames?.size?.minus(
                        1
                    )
                ) {
                    0
                } else {
                    playerPosition?.plus(1)
                }
            val nextPlayer = nextPlayerPosition?.let {
                _uiState.value.game?.round?.activeOrderedPlayerNames?.getOrNull(
                    it
                ) ?: _uiState.value.game?.round?.activeOrderedPlayerNames?.get(0)
            }

            gameRepository.rollDice(
                gameCode = _uiState.value.gameCode,
                currentRoll = _uiState.value.game?.round?.roll ?: 0,
                nextPlayer = nextPlayer!!,
                dice = Pair(dieOne, dieTwo),
                roundNum = _uiState.value.game?.round?.roundNum ?: 0,
                currentScore = _uiState.value.game?.round?.currentPoints ?: 0,
                activeOrderedPlayerNames = _uiState.value.game?.round?.activeOrderedPlayerNames
                    ?: emptyList(),
                fullOrderedPlayerNames = _uiState.value.game?.orderedPlayerNames ?: emptyList(),
                manuallyEntered = manuallyEntered,
            )
        }
    }

    fun updatePlayerOrder() {
        viewModelScope.launch {
            _uiState.value.game?.orderedPlayerNames?.reversed()
                ?.let { reversed ->
                    gameRepository.updatePlayerOrder(
                        gameCode = _uiState.value.gameCode,
                        reversed
                    )
                }
        }
    }

    fun onStartGame() {
        viewModelScope.launch {
            gameRepository.startGame(
                _uiState.value.gameCode,
                activeOrderedPlayerNames = _uiState.value.game?.orderedPlayerNames ?: emptyList()
            )

        }
    }

}
