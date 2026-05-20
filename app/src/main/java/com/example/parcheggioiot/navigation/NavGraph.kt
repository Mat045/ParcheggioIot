package com.example.parcheggioiot.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.parcheggioiot.screens.*

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginScreen(navController) }
        composable("signup") { SignupScreen(navController) }
        composable("parking") { ParkingScreen(navController) }

        composable(
            route = "home?nomeUtente={nomeUtente}&targaUtente={targaUtente}",
            arguments = listOf(
                navArgument("nomeUtente") { type = NavType.StringType; defaultValue = "Utente" },
                navArgument("targaUtente") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val nome = backStackEntry.arguments?.getString("nomeUtente") ?: "Utente"
            val targa = backStackEntry.arguments?.getString("targaUtente") ?: ""
            HomeScreen(navController, nome, targa)
        }

        composable(
            route = "qr?targaUtente={targaUtente}",
            arguments = listOf(navArgument("targaUtente") { type = NavType.StringType; defaultValue = "" })
        ) { backStackEntry ->
            val targa = backStackEntry.arguments?.getString("targaUtente") ?: ""
            QRScreen(navController, targa)
        }

        composable(
            route = "history?targaUtente={targaUtente}",
            arguments = listOf(navArgument("targaUtente") { type = NavType.StringType; defaultValue = "" })
        ) { backStackEntry ->
            val targa = backStackEntry.arguments?.getString("targaUtente") ?: ""
            HistoryScreen(navController, targa)
        }
    }
}