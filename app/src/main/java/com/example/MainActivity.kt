package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.SellServiceScreen
import com.example.ui.screens.TakeServiceScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MarketplaceViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    // Secure our repository-backed ViewModel using our factory
    val mainViewModel: MarketplaceViewModel = viewModel(
        factory = MarketplaceViewModel.provideFactory(
            application = androidx.compose.ui.platform.LocalContext.current.applicationContext as android.app.Application
        )
    )

    NavHost(
        navController = navController,
        startDestination = "login_screen"
    ) {
        composable("login_screen") {
            LoginScreen(
                viewModel = mainViewModel,
                onLoginSuccess = {
                    navController.navigate("dashboard_screen") {
                        popUpTo("login_screen") { inclusive = true }
                    }
                }
            )
        }

        composable("dashboard_screen") {
            DashboardScreen(
                viewModel = mainViewModel,
                onTakeServiceClick = {
                    navController.navigate("take_service_screen")
                },
                onSellServiceClick = {
                    navController.navigate("sell_service_screen")
                },
                onLogout = {
                    navController.navigate("login_screen") {
                        popUpTo("dashboard_screen") { inclusive = true }
                    }
                }
            )
        }

        composable("take_service_screen") {
            TakeServiceScreen(
                viewModel = mainViewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("sell_service_screen") {
            SellServiceScreen(
                viewModel = mainViewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
