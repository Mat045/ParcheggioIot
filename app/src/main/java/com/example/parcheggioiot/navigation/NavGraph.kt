// NavGraph.kt
package com.example.parcheggioiot.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.parcheggioiot.screens.*

/**
 * Gestore centralizzato del grafo di navigazione dell'applicazione Smart Parking.
 * Definisce i punti di ingresso, le rotte e il passaggio dei parametri tra le schermate.
 */
@Composable
fun NavGraph() {
    val navController = rememberNavController()

    // Configurato "start" come destinazione iniziale all'avvio dell'app
    NavHost(navController = navController, startDestination = "start") {

        // Schermata iniziale di benvenuto e smistamento
        composable("start") { StartScreen(navController) }

        // Schermate principali di autenticazione e gestione stalli
        composable("login") { LoginScreen(navController) }
        composable("signup") { SignupScreen(navController) }
        composable("parking") { ParkingScreen(navController) }

        // Home Screen con parametri opzionali per personalizzare l'interfaccia utente
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

        // Pannello di scansione e simulazione QR Code legato alla targa attiva
        composable(
            route = "qr?targaUtente={targaUtente}",
            arguments = listOf(navArgument("targaUtente") { type = NavType.StringType; defaultValue = "" })
        ) { backStackEntry ->
            val targa = backStackEntry.arguments?.getString("targaUtente") ?: ""
            QRScreen(navController, targa)
        }

        // Elenco storico delle soste effettuate dall'utente corrente
        composable(
            route = "history?targaUtente={targaUtente}",
            arguments = listOf(navArgument("targaUtente") { type = NavType.StringType; defaultValue = "" })
        ) { backStackEntry ->
            val targa = backStackEntry.arguments?.getString("targaUtente") ?: ""
            HistoryScreen(navController, targa)
        }
    }
}