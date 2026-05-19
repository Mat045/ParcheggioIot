package com.example.parcheggioiot.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.parcheggioiot.network.MqttManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(navController: NavController, nomeUtente: String, targaUtente: String) {
    val coroutineScope = rememberCoroutineScope()

    var statoPostoA1 by remember { mutableStateOf("LIBERO") }
    var statoPostoA2 by remember { mutableStateOf("LIBERO") }

    LaunchedEffect(Unit) {
        MqttManager.connettiInBackground(
            onSuccess = {
                MqttManager.client.subscribeWith()
                    .topicFilter("parcheggio/posto1")
                    .callback { publish ->
                        val payload = String(publish.payloadAsBytes).trim()
                        statoPostoA1 = payload
                    }
                    .send()

                MqttManager.client.subscribeWith()
                    .topicFilter("parcheggio/posto2")
                    .callback { publish ->
                        val payload = String(publish.payloadAsBytes).trim()
                        statoPostoA2 = payload
                    }
                    .send()
            },
            onError = {
                    e -> e.printStackTrace()
            }
        )
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Benvenuto, $nomeUtente!",
                fontSize = 28.sp,
                style = MaterialTheme.typography.headlineMedium
            )

            if (targaUtente.isNotBlank()) {
                Text(
                    text = "Targa associata: $targaUtente",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            Text(text = "Stato Parcheggi in Tempo Reale:", fontSize = 18.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(if (statoPostoA1 == "OCCUPATO") Color.Red else Color.Green, shape = CircleShape)
                    )
                    Text(text = "Posto A1", modifier = Modifier.padding(top = 4.dp))
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(if (statoPostoA2 == "OCCUPATO") Color.Red else Color.Green, shape = CircleShape)
                    )
                    Text(text = "Posto A2", modifier = Modifier.padding(top = 4.dp))
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {
                    navController.navigate("qr?targaUtente=$targaUtente")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Apri Gestione QR (Ingresso/Uscita)")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    navController.navigate("history?targaUtente=$targaUtente")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Visualizza Storico Soste")
            }

            Spacer(modifier = Modifier.height(32.dp))

            TextButton(
                onClick = {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            ) {
                Text("Logout")
            }
        }
    }
}