package com.example.parcheggioiot.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun HomeScreen(navController: NavController, nomeUtente: String) {
    Box(modifier = Modifier.fillMaxSize().padding(20.dp)) {

        // MOSTRA IL NOME CHE ARRIVA DAL DATABASE / SIGNUP
        Text(
            text = "Benvenuto, $nomeUtente",
            fontSize = 18.sp,
            modifier = Modifier.align(Alignment.TopStart)
        )

        Text("H", modifier = Modifier.align(Alignment.TopEnd).clickable { navController.navigate("history") })

        Text("Vedi Parcheggio", modifier = Modifier.align(Alignment.Center).clickable { navController.navigate("parking") })

        Button(onClick = { navController.navigate("qr_cam") }, modifier = Modifier.align(Alignment.BottomEnd)) {
            Text("QR")
        }

        Button(onClick = { navController.navigate("start") }, modifier = Modifier.align(Alignment.BottomStart)) {
            Text("Logout")
        }
    }
}