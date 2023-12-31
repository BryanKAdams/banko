package com.bryankeltonadams.banko.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bryankeltonadams.data.model.DomainGame
import com.bryankeltonadams.data.model.Player
import com.bryankeltonadams.data.model.Setting
import com.google.firebase.Timestamp

data class BankoGameScreenUiState(
    val gameCode: String = "",
    val playerName: String = "",
    val game: DomainGame? = null,
    val bankList: List<String> = emptyList(),
    val isFinished: Boolean = false,
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
        navigateBack = navigateBack,
        onGameFinished = { bankoGameScreenViewModel.onGameFinished() },
        onManuallyEnteredDice = bankoGameScreenViewModel::onDiceRolled,
        onSettingChanged = bankoGameScreenViewModel::onSettingChanged,
        updatePlayerBankList = bankoGameScreenViewModel::updatePlayerBankList,
        onGlobalBankClicked = bankoGameScreenViewModel::onGlobalBankClicked,
        addLocalPlayer = bankoGameScreenViewModel::addLocalPlayer,
    )
}

@Composable
fun FourByThreeBoxGrid(rollNumber: Int = 1, onManuallyEnteredDice: (Int) -> Unit = {}) {
    val rollNumberGreaterThan3 = rollNumber > 3
    val enabledList =
        if (rollNumberGreaterThan3) listOf(3, 4, 5, 6, 7, 8, 9, 10, 11, 13) else listOf(
            2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12
        )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(
                WindowInsets.navigationBars
                    .only(WindowInsetsSides.Bottom)
                    .asPaddingValues()
            )
    ) {
        (2..12 step 4).forEach { row ->
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)
            ) {
                (row until row + 4).forEach { i ->
                    val isEnabled = (i in enabledList) || (i == 13 && rollNumberGreaterThan3)
                    val buttonColor =
                        if (i == 7 && rollNumberGreaterThan3) Color.Red else ButtonDefaults.buttonColors().containerColor

                    Button(
                        onClick = { onManuallyEnteredDice(i) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(3.dp),
                        enabled = isEnabled,
                        colors = ButtonDefaults.buttonColors(buttonColor)
                    ) {
                        if (i == 13) {
                            Text(text = "Doubles", fontSize = 8.sp, maxLines = 1)
                        } else {
                            Text(text = i.toString())
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddPlayerDialog(
    addLocalPlayer: (String) -> Unit = {},
    onDismissRequest: () -> Unit = {}
) {
    val playerName = remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismissRequest, confirmButton = {
            Button(
                onClick = {
                    addLocalPlayer(playerName.value)
                }
            ) {
                Text(text = "Add Local Player")
            }
        },
        title = {
            Column {
                Text(text = "Enter Players Name")
            }
        },
        text = {
            TextField(
                value = playerName.value,
                onValueChange = { playerName.value = it },
                label = { Text(text = "Name") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(text = "Name") },
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankoGameScreen(
    uiState: BankoGameScreenUiState,
    onStartGame: () -> Unit = {},
    updatePlayerOrder: () -> Unit = {},
    onDiceRolled: () -> Unit = {},
    onBank: (String, List<String>) -> Unit = { s: String, strings: List<String> -> },
    navigateBack: () -> Unit = {},
    onGameFinished: () -> Unit = {},
    onManuallyEnteredDice: (Int) -> Unit = {},
    onSettingChanged: (Setting) -> Unit,
    updatePlayerBankList: (String) -> Unit = {},
    onGlobalBankClicked: () -> Unit = {},
    addLocalPlayer: (String) -> Unit = {},
) {

    val showPlayerPickerDialog = remember { mutableStateOf(false) }

    val showAddPlayerDialog = remember { mutableStateOf(false) }

    if (uiState.game?.finished == true) {
        LaunchedEffect(Unit) {
            navigateBack()
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
            if (showPlayerPickerDialog.value) {
                AlertDialog(
                    onDismissRequest = { showPlayerPickerDialog.value = false },
                    confirmButton = {
                        Button(
                            onClick = {
                                onGlobalBankClicked()
                                showPlayerPickerDialog.value = false
                            }
                        ) {
                            Text(text = "¡Banko! for selected players")
                        }
                    },
                    title = { Text(text = "Pick a player to ¡Banko! for") },
                    text = {
                        Column {
                            val nonBankedPlayersThatHostCreated =
                                uiState.game?.players?.filter {
                                    uiState.game.round?.activeOrderedPlayerNames!!.contains(
                                        it.name
                                    )
                                }!!.filter { it.hostCreated }

                            val nonBankedPlayers = uiState.game.players.filter {
                                uiState.game.round?.activeOrderedPlayerNames!!.contains(
                                    it.name
                                )
                            }
                            val playersToUse: List<Player> = if (uiState.game.settings.contains(
                                    Setting(
                                        "Host can roll or bank for other players with devices.",
                                        "true"
                                    )
                                )
                            ) {
                                nonBankedPlayers
                            } else {
                                nonBankedPlayersThatHostCreated
                            }
                            playersToUse.forEach { player ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                        .clickable {
                                            updatePlayerBankList(player.name)
                                        }, // Add clickable modifier here
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = player.name)
                                    Checkbox(
                                        checked = uiState.bankList.contains(player.name),
                                        onCheckedChange = { checkValue ->
                                            updatePlayerBankList(player.name)
                                        },
                                    )
                                }
                            }
                        }
                    },
                )
            }

            if (showAddPlayerDialog.value) {
                AddPlayerDialog(
                    addLocalPlayer = { playerName ->
                        addLocalPlayer(playerName)
                        showAddPlayerDialog.value = false
                    },
                    onDismissRequest = { showAddPlayerDialog.value = false }
                )
            }

            Column {
                if (!isStarted && isHost) {
                    Button(
                        modifier = Modifier
                            .height(48.dp)
                            .fillMaxWidth(), onClick = onStartGame
                    ) {
                        Text(text = "Start Game", style = MaterialTheme.typography.labelLarge)
                    }

                }
                val isHostControlSettingOn =
                    uiState.game?.settings?.firstOrNull { it.name == "Host can roll or bank for other players with devices." }?.value?.toBoolean()
                        ?: false

                val isPlayerActive = isPlayerActive(uiState, uiState.playerName)

                if (uiState.game?.round != null) {

                    val playersWithHostControlThatAreActive =
                        uiState.game?.players?.filter { it.hostCreated }?.map { it.name }
                            ?.intersect(
                                uiState.game.round?.activeOrderedPlayerNames!!.toSet()
                            )?.toList() ?: emptyList()

                    if (isStarted && uiState.game?.round?.roundNum!! <= uiState.game.endRoundNum) {
                        Button(
                            enabled = isPlayerActive || ((isHostControlSettingOn && isHost) || (isHost && playersWithHostControlThatAreActive.isNotEmpty())),
                            modifier = Modifier
                                .height(48.dp)
                                .fillMaxWidth(),
                            onClick = {
                                if (isHost && uiState.game.players.any { it.hostCreated } || (isHostControlSettingOn && isHost)) {
                                    showPlayerPickerDialog.value = true
                                } else {
                                    onBank(uiState.game.currentPlayer, listOf(uiState.playerName))
                                }


                            },
                        ) {
                            Text(text = "¡Banko!", style = MaterialTheme.typography.labelLarge)
                        }
                        Spacer(modifier = Modifier.height(48.dp))
                        if (selfPlayer == currentTurnsPlayer || (isHost && uiState.game?.settings?.firstOrNull { it.name == "Host can roll or bank for other players with devices." }?.value?.toBoolean() == true) || (currentTurnsPlayer?.hostCreated == true)) {
                            FourByThreeBoxGrid(
                                rollNumber = uiState.game?.round?.currentRoll ?: 0,
                                onManuallyEnteredDice = onManuallyEnteredDice
                            )

                        }
                    } else if (isHost && uiState.game.round.roundNum > uiState.game.endRoundNum) {
                        Button(
                            modifier = Modifier
                                .height(48.dp)
                                .fillMaxWidth(),
                            onClick = {
                                onGameFinished()
                            },
                        ) {
                            Text(text = "Finish Game", style = MaterialTheme.typography.labelLarge)
                        }

                    }
                }
            }
        }
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
                uiState.game?.players?.forEach { player ->
                    Row(
                        modifier = Modifier
                            .background(
                                if (isPlayerActive(uiState, player.name)) Color.Red else Color.Gray
                            )
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = player.name,
                            textAlign = TextAlign.Center,
                            fontSize = 24.sp,
                        )
                        if (player == hostPlayer) {
                            Icon(
                                modifier = Modifier,
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color.Yellow
                            )
                        }

                        if (isStarted) {
                            Text(
                                text = player.points.toString(),
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
            ) {
                if (!isStarted) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(200.dp), strokeWidth = 20.dp
                    )
                    Text(text = "Waiting for host to start game")
                    val settings =
                        uiState.game?.settings
                    GameSettingsCheckboxList(
                        settings = settings ?: emptyList(),
                        onSettingChanged = onSettingChanged,
                        isHost = isHost
                    )

                    if (isHost) {
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { showAddPlayerDialog.value = true }) {
                            Text(text = "Add Local Player")
                        }
                    }

                } else if (uiState.game!!.round!!.roundNum <= uiState.game.endRoundNum) {
                    Text(text = "Round: ${uiState.game.round?.roundNum} / ${uiState.game.endRoundNum}")
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(
                            16.dp, Alignment.CenterHorizontally
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
                    if (selfPlayer == currentTurnsPlayer || (isHost && uiState.game.settings.firstOrNull { it.name == "Host can roll or bank for other players with devices." }?.value?.toBoolean() == true) || (currentTurnsPlayer?.hostCreated == true && isHost)
                    ) {
                        if (selfPlayer == currentTurnsPlayer) {
                            Text(text = "It's your turn")
                        } else {
                            Text(text = "It's ${currentTurnsPlayer?.name}'s turn")
                        }
                        Button(onClick = { onDiceRolled() }) {
                            Text(text = "Roll Dice")

                        }
                    } else {
                        Text(text = "It's ${currentTurnsPlayer?.name}'s turn")
                    }
                    Text(text = uiState.game.round?.currentPoints.toString())

                } else {
                    Text(text = "Game Over")
                    Text(text = "Winner: ${uiState.game.players.maxByOrNull { it.points }?.name}")
                }
            }
        }
    }

}

@Composable
private fun GameSettingsCheckboxList(
    modifier: Modifier = Modifier,
    settings: List<Setting>,
    onSettingChanged: (Setting) -> Unit = {},
    isHost: Boolean = false
) {
    val filteredBooleanSettings = settings.filter { it.value == "true" || it.value == "false" }
    Column(modifier = modifier) {
        filteredBooleanSettings.forEach { setting ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clickable(enabled = isHost) {
                        onSettingChanged(
                            Setting(
                                setting.name,
                                setting.value
                                    .toBoolean()
                                    .not()
                                    .toString()
                            )
                        )
                    }, // Add clickable modifier here
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = setting.name)
                Checkbox(
                    checked = setting.value.toBoolean(),
                    onCheckedChange = { checkValue ->
                        onSettingChanged(
                            Setting(
                                setting.name,
                                setting.value
                                    .toBoolean()
                                    .not()
                                    .toString()
                            )
                        )
                    },
                    enabled = isHost
                )
            }
        }
    }
}


private fun isPlayerActive(uiState: BankoGameScreenUiState, playerName: String): Boolean {
    return uiState.game?.round?.activeOrderedPlayerNames?.contains(playerName) ?: false
}


@Preview
@Composable
fun BankoGameScreenPreview() {
    BankoGameScreen(
        BankoGameScreenUiState(
            "1234", "Bryan",
            DomainGame(
                Timestamp.now(), joinCode = "1234", players = listOf(
                    Player("Bryan", 0),
                    Player("Sarah", 0),
                    Player("Maddie", 0),
                ), round = null, host = "Bryan", currentPlayer = "Bryan"
            ),
        ),
        onSettingChanged = {},
    )
}
