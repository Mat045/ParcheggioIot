package com.example.parcheggioiot.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.parcheggioiot.screens.*

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "start"
    ) {
        composable("start") { StartScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("signup") { SignupScreen(navController) }
        composable("parking") { ParkingScreen(navController) }

        // ABBIAMO UNIFICATO LA HOME: Ora gestisce sia "home" che "home?nomeUtente=Qualcosa"
        composable(
            route = "home?nomeUtente={nomeUtente}",
            arguments = listOf(
                navArgument("nomeUtente") {
                    type = NavType.StringType
                    nullable = true          // Dice ad Android che il nome può mancare
                    defaultValue = "Utente"  // Se manca, usa "Utente" in automatico
                }
            )
        ) { backStackEntry ->
            val nome = backStackEntry.arguments?.getString("nomeUtente") ?: "Utente"
            HomeScreen(navController = navController, nomeUtente = nome)
        }

        // composable("qr_cam") { QRScreen(navController) }
        // composable("history") { HistoryScreen(navController) }
    }
}