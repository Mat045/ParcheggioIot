package com.example.parcheggioiot.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.hivemq.client.mqtt.MqttClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.UUID

@Composable
fun QRScreen(navController: NavController, targaUtente: String) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var scansioneCompletata by remember { mutableStateOf(false) }

    val mqttClient = remember {
        MqttClient.builder()
            .useMqttVersion3()
            .identifier(UUID.randomUUID().toString())
            .serverHost("a67b59331e4e42e8bf7557cd181f3aee.s1.eu.hivemq.cloud")
            .serverPort(8883)
            .sslWithDefaultConfig()
            .simpleAuth()
            .username("esp32")
            .password("Iot12345678".toByteArray())
            .applySimpleAuth()
            .buildAsync()
    }

    LaunchedEffect(Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                mqttClient.connect().get()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Simulazione Scansione QR Code", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (!scansioneCompletata) {
                            val risultatoQr = "PARCHEGGIO_INGRESSO"

                            if (risultatoQr == "PARCHEGGIO_INGRESSO") {
                                scansioneCompletata = true
                                coroutineScope.launch(Dispatchers.IO) {
                                    val jsonReq = JSONObject().apply {
                                        put("azione", "CHECKIN")
                                        put("targa", targaUtente)
                                    }
                                    mqttClient.publishWith()
                                        .topic("parcheggio/accessi")
                                        .payload(jsonReq.toString().toByteArray())
                                        .send()

                                    launch(Dispatchers.Main) {
                                        snackbarHostState.showSnackbar("QR Valido! Check-in inviato alla sbarra.")
                                    }
                                }
                            }
                        }
                    },
                    enabled = !scansioneCompletata
                ) {
                    Text(if (scansioneCompletata) "Scansione Effettuata" else "Inquadra QR (Simula)")
                }
            }
        }
    }
}