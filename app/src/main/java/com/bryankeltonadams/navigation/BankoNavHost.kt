package com.bryankeltonadams.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.bryankeltonadams.banko.GameRepository
import com.bryankeltonadams.banko.ui.screens.BankoGameScreen
import com.bryankeltonadams.banko.ui.screens.BankoStartScreen
import com.bryankeltonadams.banko.ui.screens.BankoStartScreenViewModel


private val banko_start_screen = "banko_start_screen"
private val banko_game_screen = "banko_game_screen"

@Composable
fun BankoNavHost(
    navController: NavHostController,
    onShowSnackbar: (String) -> Unit = {}
) {
    NavHost(
        modifier = Modifier.fillMaxSize(),
        navController = navController,
        startDestination = banko_start_screen
    ) {
        composable(banko_start_screen) {
            BankoStartScreen(
                bankoStartScreenViewModel = hiltViewModel(),
                onShowSnackBar = onShowSnackbar,
                navigateToGame = {
                    navController.navigate("${banko_game_screen}/$it")
                })
        }
        composable(
            route = "$banko_game_screen/{gameCode}",
        ) {
            val gameCode = it.arguments?.getString("gameCode")
            if (gameCode != null) {
                BankoGameScreen(hiltViewModel(), navigateBack = {
                    navController.popBackStack()
                })
            }
        }
    }

}