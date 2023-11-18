package com.bryankeltonadams.banko

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.bryankeltonadams.banko.ui.theme.BankoTheme
import com.bryankeltonadams.navigation.BankoNavHost
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BankoTheme {
                // A surface container using the 'background' color from the theme
                Box(
                    contentAlignment = Alignment.BottomCenter,
                    modifier = Modifier.fillMaxSize()
                ) {
                    val coroutineScope = rememberCoroutineScope()

                    val snackbarHostState = remember { SnackbarHostState() }

                    val navController = rememberNavController()

                    BankoNavHost(navController = navController, onShowSnackbar =
                    { message ->
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                message,
                                withDismissAction = false,
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                    )
                    SnackbarHost(
                        modifier = Modifier.safeDrawingPadding(),
                        hostState = snackbarHostState
                    ) {
                        Snackbar(
                            snackbarData = it
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BankoTheme {
        Greeting("Android")
    }
}