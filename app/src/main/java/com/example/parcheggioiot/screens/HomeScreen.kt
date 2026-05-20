package com.example.parcheggioiot.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun HomeScreen(navController: NavController, nomeUtente: String, targaUtente: String) {

    val SfondoScuro = Color(0xFF121824)
    val BluPrimario = Color(0xFF3B82F6)

    Scaffold(
        containerColor = SfondoScuro
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Sezione di Benvenuto
            Text(
                text = "Benvenuto, $nomeUtente!",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            if (targaUtente.isNotBlank()) {
                Text(
                    text = "Targa associata: $targaUtente",
                    fontSize = 16.sp,
                    color = Color.LightGray,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // 1. NUOVO PULSANTE: Mappa del Parcheggio (Spostato qui)
            Button(
                onClick = {
                    navController.navigate("parking")
                },
                colors = ButtonDefaults.buttonColors(containerColor = BluPrimario),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text("VEDI MAPPA PARCHEGGIO", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. PULSANTE: Gestione QR
            Button(
                onClick = {
                    navController.navigate("qr?targaUtente=$targaUtente")
                },
                colors = ButtonDefaults.buttonColors(containerColor = BluPrimario),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text("APRI GESTIONE QR (INGRESSO/USCITA)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. PULSANTE: Storico Soste
            Button(
                onClick = {
                    navController.navigate("history?targaUtente=$targaUtente")
                },
                colors = ButtonDefaults.buttonColors(containerColor = BluPrimario),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text("VISUALIZZA STORICO SOSTE", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Pulsante di Logout
            TextButton(
                onClick = {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            ) {
                Text("Effettua il Logout", color = Color.Red, fontSize = 16.sp)
            }
        }
    }
}