package com.bryankeltonadams.banko.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bryankeltonadams.banko.GameRepository
import com.bryankeltonadams.banko.UserPreferencesRepository
import com.bryankeltonadams.data.model.Player
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BankoStartScreenViewModel
@Inject constructor(
    private val gameRepository: GameRepository,
    private val userPreferencesRepository: UserPreferencesRepository

) : ViewModel() {

    init {
        viewModelScope.launch {
            val sessionPreferences = userPreferencesRepository.sessionPreferencesFlow.first()
            _bankoStartScreenUiState.value = bankoStartScreenUiState.value.copy(
                userName = sessionPreferences.name
            )
        }
    }

    private val _bankoStartScreenUiState = MutableStateFlow(BankoStartScreenUiState())
    val bankoStartScreenUiState = _bankoStartScreenUiState.asStateFlow()

    fun onUpdateUserName(userName: String) {
        _bankoStartScreenUiState.value = _bankoStartScreenUiState.value.copy(userName = userName)
    }

    fun onUpdateGameCode(gameCode: String) {
        _bankoStartScreenUiState.value = _bankoStartScreenUiState.value.copy(gameCode = gameCode)
    }

    private fun generateValidGameCode(): String {
        var gameCode = generate5DigitAlphaNumericCode()
        viewModelScope.launch {
            var checkGameCode = gameRepository.gameExists(gameCode)
            while (checkGameCode) {
                gameCode = generate5DigitAlphaNumericCode()
                checkGameCode = gameRepository.gameExists(gameCode)
            }
        }
        return gameCode
    }

    private fun generate5DigitAlphaNumericCode(): String {
        val allowedChars = ('A'..'Z') + ('0'..'9')
        return (1..5)
            .map { allowedChars.random() }
            .joinToString("")
    }

    fun createGame() {
        viewModelScope.launch {
            val userName = _bankoStartScreenUiState.value.userName
            val gameCode =
                gameRepository.createGame(gameCode = generateValidGameCode(), name = userName)

            userPreferencesRepository.setPreferences(userName, gameCode)

            _bankoStartScreenUiState.value = bankoStartScreenUiState.value.copy(
                joinedGameIsValid = true,
                joinedGameCode = gameCode
            )
        }
    }

    fun resetErrorMessage() {
        _bankoStartScreenUiState.value = _bankoStartScreenUiState.value.copy(errorMessage = null)
    }

    fun joinGame() {
        viewModelScope.launch {
            _bankoStartScreenUiState.value.joinedGameCode = _bankoStartScreenUiState.value.gameCode
            _bankoStartScreenUiState.value.gameCode = ""
            if (gameRepository.gameExists(_bankoStartScreenUiState.value.joinedGameCode)) {
                val player =
                    Player(_bankoStartScreenUiState.value.userName, 0)
                gameRepository.addPlayer(
                    player,
                    _bankoStartScreenUiState.value.joinedGameCode
                )
                userPreferencesRepository.setPreferences(
                    _bankoStartScreenUiState.value.userName,
                    _bankoStartScreenUiState.value.joinedGameCode
                )
                _bankoStartScreenUiState.value = bankoStartScreenUiState.value.copy(
                    joinedGameIsValid = true
                )
            } else {
                _bankoStartScreenUiState.value = bankoStartScreenUiState.value.copy(
                    errorMessage = "Game does not exist"
                )
            }
        }
    }

    fun resetGameValidity() {
        _bankoStartScreenUiState.value = _bankoStartScreenUiState.value.copy(
            joinedGameIsValid = false
        )
    }

}