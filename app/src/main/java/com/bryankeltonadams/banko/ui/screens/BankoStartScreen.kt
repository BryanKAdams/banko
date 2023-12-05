package com.bryankeltonadams.banko.ui.screens

import android.graphics.fonts.FontFamily
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


data class BankoStartScreenUiState(
    val userName: String = "",
    var gameCode: String = "",
    var joinedGameCode: String = "",
    val errorMessage: String? = null,
    val joinedGameIsValid: Boolean = false,
)

@Composable
fun BankoStartScreen(
    bankoStartScreenViewModel: BankoStartScreenViewModel,
    onShowSnackBar: (String) -> Unit = {},
    navigateToGame: (String) -> Unit = {}
) {

    val bankoStartScreenUiState by bankoStartScreenViewModel.bankoStartScreenUiState.collectAsState()

    val errorMessage = bankoStartScreenUiState.errorMessage

    val joinedGameIsValid = bankoStartScreenUiState.joinedGameIsValid



    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            bankoStartScreenViewModel.resetErrorMessage()
            bankoStartScreenUiState.errorMessage?.let {
                onShowSnackBar(it)
            }
        }
    }

    LaunchedEffect(joinedGameIsValid) {
        if (joinedGameIsValid) {
            bankoStartScreenViewModel.resetGameValidity()
            navigateToGame(bankoStartScreenUiState.joinedGameCode)
        }
    }


    BankoStartScreen(
        uiState = bankoStartScreenUiState,
        onUpdateUserName = bankoStartScreenViewModel::onUpdateUserName,
        onUpdateGameCode = bankoStartScreenViewModel::onUpdateGameCode,
        onCreateGame = bankoStartScreenViewModel::createGame,
        onJoinGame = bankoStartScreenViewModel::joinGame,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulesBottomSheet(bottomSheetState: SheetState, onDismissRequest: () -> Unit = {}) {
    ModalBottomSheet(
        modifier = Modifier.safeDrawingPadding(),
        onDismissRequest = onDismissRequest,
        sheetState = bottomSheetState
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            item {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 48.sp, text = "¡Banko! Rules",
                    textAlign = TextAlign.Center,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Default
                )
            }
            item {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 18.sp,
                    text = "Players:\n" +
                            "2 or more",

                    textAlign = TextAlign.Center,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Default
                )
            }
            item {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 18.sp,
                    text = "How to Win:\n" + "The player with the most BANKO points by the end of the game wins!",
                    textAlign = TextAlign.Center,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Default
                )
            }
            item {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 18.sp,
                    text = "How to Play:\n" +
                            "Players take turns rolling two dice, adding the total value of the dice to the round's total score. In the first three turns of each round, 7s are worth 70 points, and doubles are worth only their face value. After the 3rd roll, a 7 ends the round, but doubles now DOUBLE the point total! Players keep rolling or banking until someone rolls a 7 or all players have banked. \n",
                    textAlign = TextAlign.Center,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Default
                )
            }
            item {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 18.sp,
                    text = "Calling “BANKO”" + "\n" +
                            "Anyone can call “BANKO” to lock in their points and at any point in the game. Once you call BANKO you can no longer roll dice or score points until the next round.",
                    textAlign = TextAlign.Center,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Default
                )
            }
            item {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 18.sp, text = "5. If you win, you will be given a new card",
                    textAlign = TextAlign.Center,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Default
                )
            }
            item {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 18.sp, text = "6. The first person to win 5 times wins the game!",
                    textAlign = TextAlign.Center,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Default
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankoStartScreen(
    uiState: BankoStartScreenUiState,
    onUpdateUserName: (String) -> Unit,
    onUpdateGameCode: (String) -> Unit,
    onCreateGame: () -> Unit,
    onJoinGame: () -> Unit = {},
) {
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }

    if (showBottomSheet) {
        RulesBottomSheet(bottomSheetState = bottomSheetState, onDismissRequest = {
            showBottomSheet = false
        })
    }

    Scaffold(
        Modifier
            .fillMaxSize()
            .safeDrawingPadding(),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .safeDrawingPadding(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable {
                            showBottomSheet = true

                        }, text = "Rules"
                )
            }

        }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .consumeWindowInsets(it)
                .safeDrawingPadding()
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                fontSize = 48.sp, text = "¡Banko!",
                textAlign = TextAlign.Center,
            )
            TextField(modifier = Modifier.fillMaxWidth(),
                value = uiState.userName,
                onValueChange = onUpdateUserName,
                placeholder = {
                    Text(text = "Enter your name. i.e. Jeff")
                })
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = uiState.gameCode,
                onValueChange = onUpdateGameCode,
                placeholder = {
                    Text(text = "5 Digit Game Code To Join a Game")
                })
            Button(
                enabled = uiState.gameCode.length == 5 && uiState.userName.isNotEmpty(),
                onClick = onJoinGame
            ) {
                Text(text = "Join Game")
            }
            Button(
                enabled = uiState.gameCode.isEmpty() && uiState.userName.isNotEmpty(),
                onClick = onCreateGame
            ) {
                Text(text = "Create Game")
            }

        }
    }

}

@Composable
@Preview
fun BankoStartScreenPreview() {
    BankoStartScreen(
        uiState = BankoStartScreenUiState(),
        onUpdateUserName = {},
        onUpdateGameCode = {},
        onCreateGame = {},
        onJoinGame = {},
    )
}