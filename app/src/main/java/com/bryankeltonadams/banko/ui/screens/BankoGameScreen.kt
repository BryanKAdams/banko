package com.bryankeltonadams.banko.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bryankeltonadams.data.model.DomainGame
import com.bryankeltonadams.data.model.Player
import com.google.firebase.Timestamp

data class BankoGameScreenUiState(
    val gameCode: String = "",
    val playerName: String = "",
    val game: DomainGame? = null,
)

@Composable
fun BankoGameScreen(
    bankoGameScreenViewModel: BankoGameScreenViewModel, navigateBack: () -> Unit = {}
) {
    val uiState by bankoGameScreenViewModel.uiState.collectAsState()
    BankoGameScreen(
        uiState = uiState,
        onStartGame = bankoGameScreenViewModel::onStartGame,
        updatePlayerOrder = bankoGameScreenViewModel::updatePlayerOrder,
        onDiceRolled = bankoGameScreenViewModel::onDiceRolled,
        onBank = bankoGameScreenViewModel::onBank,
        onGameFinished = { bankoGameScreenViewModel.onGameFinished(); navigateBack() },
        onManuallyEnteredDice = bankoGameScreenViewModel::onDiceRolled,
    )
}

@Composable
fun FourByThreeBoxGrid(rollNumber: Int = 1, onManuallyEnteredDice: (Int) -> Unit = {}) {
    val rollNumberGreaterThan3 = rollNumber > 3
    Column(
        modifier = Modifier
            .height(200.dp)
            .padding(
                WindowInsets.navigationBars
                    .only(WindowInsetsSides.Bottom)
                    .asPaddingValues()
            )
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)
        ) {
            Button(
                onClick = { onManuallyEnteredDice(2) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(3.dp),
                enabled = !rollNumberGreaterThan3
            ) {
                Text(text = "2")
            }
            Button(
                onClick = { onManuallyEnteredDice(3) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(3.dp)
            ) {
                Text(text = "3")
            }
            Button(
                onClick = { onManuallyEnteredDice(4) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(3.dp)
            ) {
                Text(text = "4")
            }
            Button(
                onClick = { onManuallyEnteredDice(5) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(3.dp)
            ) {
                Text(text = "5")
            }
        }
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)
        ) {
            Button(
                onClick = { onManuallyEnteredDice(6) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(3.dp)
            ) {
                Text(text = "6")
            }
            Button(
                onClick = { onManuallyEnteredDice(7) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(3.dp),
                colors = ButtonDefaults.buttonColors(
                    if (rollNumberGreaterThan3) Color.Red else ButtonDefaults.buttonColors().containerColor,
                )

            ) {
                Text(text = "7")
            }
            Button(
                onClick = { onManuallyEnteredDice(8) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(3.dp)
            ) {
                Text(text = "8")
            }
            Button(
                onClick = { onManuallyEnteredDice(9) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(3.dp)
            ) {
                Text(text = "9")
            }
        }
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)
        ) {
            Button(
                onClick = { onManuallyEnteredDice(10) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(3.dp)
            ) {
                Text(text = "10")
            }
            Button(
                onClick = { onManuallyEnteredDice(11) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(3.dp),
            ) {
                Text(text = "11")
            }
            Button(
                onClick = { onManuallyEnteredDice(12) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(3.dp),
                enabled = !rollNumberGreaterThan3
            ) {
                Text(text = "12")
            }
            Button(
                onClick = { onManuallyEnteredDice(13) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(3.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(text = "Doubles", fontSize = 8.sp, maxLines = 1)
            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankoGameScreen(
    uiState: BankoGameScreenUiState,
    onStartGame: () -> Unit = {},
    updatePlayerOrder: () -> Unit = {},
    onDiceRolled: () -> Unit = {},
    onBank: () -> Unit = {},
    onGameFinished: () -> Unit = {},
    onManuallyEnteredDice: (Int) -> Unit = {},
) {


    if (uiState.game?.round?.roundNum != null && uiState.game.round.roundNum > uiState.game.endRoundNum) {
        LaunchedEffect(Unit) {
            onGameFinished()
        }
    }
    val selfPlayer = uiState.game?.players?.firstOrNull { it.name == uiState.playerName }
    val hostPlayer = uiState.game?.host?.let { host ->
        uiState.game.players.firstOrNull { it.name == host }
    }
    val currentTurnsPlayer = uiState.game?.currentPlayer?.let { currentPlayer ->
        uiState.game.players.firstOrNull { it.name == currentPlayer }
    }
    val isHost = hostPlayer?.name == uiState.playerName
    val isStarted = uiState.game?.round != null

    val isSelfActive =
        uiState.game?.round?.activeOrderedPlayerNames?.contains(uiState.playerName)
            ?: false

    val bottomSheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.Expanded

    )
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = bottomSheetState
    )
    BottomSheetScaffold(
        scaffoldState = bottomSheetScaffoldState,
        sheetPeekHeight = 148.dp,
        sheetContent = {
            Column {
                if (!isStarted && isHost) {
                    Button(
                        modifier = Modifier
                            .height(48.dp)
                            .fillMaxWidth(),
                        onClick = onStartGame
                    ) {
                        Text(text = "Start Game", style = MaterialTheme.typography.labelLarge)
                    }

                }
                if (isStarted) {
                    Button(
                        enabled = isSelfActive,
                        modifier = Modifier
                            .height(48.dp)
                            .fillMaxWidth(),
                        onClick = onBank,
                    ) {
                        Text(text = "¡Banko!", style = MaterialTheme.typography.labelLarge)
                    }
                    Spacer(modifier = Modifier.height(48.dp))
                    if (currentTurnsPlayer == selfPlayer) {
                        FourByThreeBoxGrid(
                            rollNumber = uiState.game?.round?.roll ?: 0,
                            onManuallyEnteredDice = onManuallyEnteredDice
                        )

                    }
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .safeDrawingPadding()
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column() {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "Game Code: ${uiState.gameCode}",
                    textAlign = TextAlign.Center,
                    fontSize = 24.sp,
                )
                if (isHost) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = "Hi ${uiState.playerName} you are the host",
                        textAlign = TextAlign.Center,
                        fontSize = 24.sp,
                    )
                } else {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = "${hostPlayer?.name} is the host",
                        textAlign = TextAlign.Center,
                        fontSize = 24.sp,
                    )
                }
            }
            Column() {
                uiState.game?.players?.forEach {
                    val isPlayerActive =
                        uiState.game.round?.activeOrderedPlayerNames?.contains(it.name)
                            ?: false
                    Row(
                        modifier = Modifier
                            .background(
                                if (isPlayerActive) Color.Red else Color.Gray
                            )
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = it.name,
                            textAlign = TextAlign.Center,
                            fontSize = 24.sp,
                        )
                        if (it == hostPlayer) {
                            Icon(
                                modifier = Modifier,
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color.Yellow
                            )
                        }

                        if (isStarted) {
                            Text(
                                text = it.points.toString(),
                                fontSize = 24.sp,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                    }
                }
            }
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 32.dp)
            )
            {
                if (!isStarted) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(200.dp),
                        strokeWidth = 20.dp
                    )
                    Text(text = "Waiting for host to start game")
                } else {
                    Text(text = "Round: ${uiState.game?.round?.roundNum} / ${uiState.game?.endRoundNum}")
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(
                            16.dp,
                            Alignment.CenterHorizontally
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .background(Color.Black),
                        ) {
                            Text(
                                text = uiState.game?.round?.dieOne.toString(),
                                modifier = Modifier.align(Alignment.Center),
                                color = Color.White
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .background(Color.Black)
                        ) {
                            Text(
                                text = uiState.game?.round?.dieTwo.toString(),
                                modifier = Modifier.align(Alignment.Center),
                                color = Color.White
                            )
                        }
                    }
                    if (selfPlayer == currentTurnsPlayer) {
                        Text(text = "It's your turn")
                        Button(onClick = { onDiceRolled() }) {
                            Text(text = "Roll Dice")

                        }
                    } else {
                        Text(text = "It's ${currentTurnsPlayer?.name}'s turn")
                    }
                    Text(text = uiState.game?.round?.currentPoints.toString())

                }
            }
        }
    }

}


@Preview
@Composable
fun BankoGameScreenPreview() {
    BankoGameScreen(
        BankoGameScreenUiState(
            "1234",
            "Bryan",
            DomainGame(
                Timestamp.now(),
                joinCode = "1234",
                players = listOf(
                    Player("Bryan", 0),
                    Player("Sarah", 0),
                    Player("Maddie", 0),
                ),
                round = null,
                host = "Bryan",
                currentPlayer = "Bryan"
            )
        )
    )
}
