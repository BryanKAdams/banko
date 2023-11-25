package com.bryankeltonadams.banko.ui.screens

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bryankeltonadams.banko.GameRepository
import com.bryankeltonadams.banko.UserPreferencesRepository
import com.bryankeltonadams.data.model.Round
import com.bryankeltonadams.data.model.Setting
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class BankoGameScreenViewModel
@Inject constructor(
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

    fun updatePlayerBankList(playerName: String) {
        _uiState.value =
            uiState.value.copy(bankList = _uiState.value.bankList.toMutableList().apply {
                if (contains(playerName)) {
                    remove(playerName)
                } else {
                    add(playerName)
                }
            })
    }


    fun onGlobalBankClicked() {
        _uiState.value.game?.currentPlayer?.let {
            onBank(
                currentPlayer = it, playerNames = _uiState.value.bankList
            )
        }
        _uiState.value = uiState.value.copy(bankList = emptyList())

    }

    fun onSettingChanged(setting: Setting) {
        viewModelScope.launch {
            gameRepository.updateSetting(
                gameCode = _uiState.value.gameCode, settingToUpdate = setting
            )
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

    fun onBank(currentPlayer: String, playerNames: List<String>) {
        viewModelScope.launch {
            gameRepository.bankPoints(
                gameCode = _uiState.value.gameCode,
                currentPlayer = currentPlayer,
                bankingPlayers = playerNames,
                remainingPlayers = _uiState.value.game?.round?.activeOrderedPlayerNames?.minus(
                    playerNames.toSet()
                ) ?: emptyList(),
                currentPoints = _uiState.value.game?.round?.currentPoints ?: 0,
                fullOrderedPlayerList = _uiState.value.game?.orderedPlayerNames ?: emptyList(),
            )

        }
    }


    fun onDiceRolled(manuallyEntered: Int? = null) {

        val (dieOne, dieTwo) = rollDice()

        viewModelScope.launch {
            val playerPosition =
                _uiState.value.game?.round?.activeOrderedPlayerNames?.indexOf(_uiState.value.game?.currentPlayer)
            val nextPlayerPosition = (playerPosition?.plus(1)
                ?: 0) % (_uiState.value.game?.round!!.activeOrderedPlayerNames.size)
            val nextPlayer =
                _uiState.value.game?.round?.activeOrderedPlayerNames?.getOrNull(nextPlayerPosition)
                    ?: _uiState.value.game?.round?.activeOrderedPlayerNames?.get(0)


            val isBeforeFirstThreeRolls = _uiState.value.game!!.round!!.currentRoll <= 3
            var cumulativeDiceValue = dieOne + dieTwo
            if (manuallyEntered != null) {
                cumulativeDiceValue = manuallyEntered
            }
            var pointValueToAdd = when {
                isBeforeFirstThreeRolls && cumulativeDiceValue == 7 -> 70
                !isBeforeFirstThreeRolls && cumulativeDiceValue == 7 -> 0
                !isBeforeFirstThreeRolls && (dieOne == dieTwo || manuallyEntered == 13) -> _uiState.value.game?.round?.currentPoints
                    ?: 0

                else -> cumulativeDiceValue
            }

            var finalRoundNum = _uiState.value.game?.round?.roundNum ?: 0
            var finalRoll = _uiState.value.game?.round?.currentRoll ?: 0
            var finalActiveOrderedPlayerNames = _uiState.value.game?.round?.activeOrderedPlayerNames
            var nextPlayerName = nextPlayer

            if (pointValueToAdd == 0) {
                finalRoundNum++
                finalRoll = 1
                finalActiveOrderedPlayerNames = _uiState.value.game?.orderedPlayerNames

                val newPlayerPosition =
                    finalActiveOrderedPlayerNames?.indexOf(_uiState.value.game?.currentPlayer)
                val newNextPlayerPosition = (newPlayerPosition?.plus(1)
                    ?: 0) % (finalActiveOrderedPlayerNames?.size!!)
                nextPlayerName = finalActiveOrderedPlayerNames.getOrNull(newNextPlayerPosition)
                    ?: finalActiveOrderedPlayerNames[0]
            } else {
                pointValueToAdd += _uiState.value.game?.round?.currentPoints!!
                finalRoll++
            }

            val currentRound = Round(
                finalRoundNum,
                pointValueToAdd,
                if (manuallyEntered != null) 0 else dieOne,
                if (manuallyEntered != null) 0 else dieTwo,
                finalRoll,
                activeOrderedPlayerNames = finalActiveOrderedPlayerNames!!
            )
            gameRepository.rollDice(
                gameCode = _uiState.value.gameCode,
                round = currentRound,
                nextPlayerName = nextPlayerName!!
            )
        }
    }

    fun updatePlayerOrder() {
        viewModelScope.launch {
            _uiState.value.game?.orderedPlayerNames?.reversed()?.let { reversed ->
                gameRepository.updatePlayerOrder(
                    gameCode = _uiState.value.gameCode, reversed
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
