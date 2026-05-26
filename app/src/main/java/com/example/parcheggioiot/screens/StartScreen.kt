// StartScreen.kt
package com.example.parcheggioiot.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

/**
 * Schermata di ingresso iniziale (Welcome Screen) dell'applicazione.
 * Offre i punti di accesso principali per instradare l'utente verso il Login o la Registrazione.
 */
@Composable
fun StartScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("BENVENUTO", fontSize = 40.sp)

        Spacer(modifier = Modifier.height(50.dp))

        // Reindirizzamento al modulo di autenticazione esistente
        Button(onClick = { navController.navigate("login") }) {
            Text("LOGIN")
        }

        // Reindirizzamento al modulo di creazione nuovo profilo IoT
        Button(onClick = { navController.navigate("signup") }) {
            Text("SIGNUP")
        }
    }
}