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
        _bankoStartScreenUiState.value = bankoStartScreenUiState.value.copy(userName = userName)
    }

    fun onUpdateGameCode(gameCode: String) {
        _bankoStartScreenUiState.value = bankoStartScreenUiState.value.copy(gameCode = gameCode)
    }


    fun createGame() {
        viewModelScope.launch {
            onUpdateGameCode(gameRepository.createGame(_bankoStartScreenUiState.value.userName))
            userPreferencesRepository.setName(_bankoStartScreenUiState.value.userName)
            userPreferencesRepository.setGameCode(_bankoStartScreenUiState.value.gameCode)
            _bankoStartScreenUiState.value = bankoStartScreenUiState.value.copy(
                joinedGameIsValid = true
            )

        }
    }

    fun resetErrorMessage() {
        _bankoStartScreenUiState.value = bankoStartScreenUiState.value.copy(errorMessage = null)
    }


    fun joinGame() {
        viewModelScope.launch {
            if (gameRepository.gameExists(_bankoStartScreenUiState.value.gameCode)) {
                val player =
                    Player(_bankoStartScreenUiState.value.userName, 0)
                gameRepository.addPlayer(
                    player,
                    _bankoStartScreenUiState.value.gameCode
                )
                userPreferencesRepository.setName(_bankoStartScreenUiState.value.userName)
                userPreferencesRepository.setGameCode(_bankoStartScreenUiState.value.gameCode)
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
        _bankoStartScreenUiState.value = bankoStartScreenUiState.value.copy(
            joinedGameIsValid = false
        )
    }

}