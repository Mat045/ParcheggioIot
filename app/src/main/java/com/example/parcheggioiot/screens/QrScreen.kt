// QRScreen.kt
package com.example.parcheggioiot.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.parcheggioiot.network.MqttManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * Schermata per la simulazione e la scansione dei QR Code di ingresso e uscita.
 * Gestisce la sottoscrizione ai canali di risposta per gli accessi e i pagamenti.
 */
@Composable
fun QRScreen(navController: NavController, targaUtente: String) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var statoOperazione by remember { mutableStateOf("In attesa di scansione") }

    // Ciclo di vita legato alla presenza della schermata nella gerarchia dei componenti
    DisposableEffect(Unit) {
        MqttManager.connettiInBackground(
            onSuccess = {
                // Sottoscrizione al topic per i responsi di check-in (ingressi)
                MqttManager.client.subscribeWith()
                    .topicFilter("parcheggio/app/accessi/risposta")
                    .callback { publish ->
                        val payload = String(publish.payloadAsBytes)
                        val json = JSONObject(payload)
                        val msg = json.getString("messaggio")

                        coroutineScope.launch(Dispatchers.Main) {
                            statoOperazione = msg
                            snackbarHostState.showSnackbar(msg)
                        }
                    }
                    .send()

                // Sottoscrizione al topic per i responsi di check-out (pagamenti/uscite)
                MqttManager.client.subscribeWith()
                    .topicFilter("parcheggio/app/checkout/risposta")
                    .callback { publish ->
                        val payload = String(publish.payloadAsBytes)
                        val json = JSONObject(payload)
                        val successo = json.getBoolean("successo")
                        val msg = json.getString("messaggio")

                        coroutineScope.launch(Dispatchers.Main) {
                            if (successo) {
                                val costo = json.optDouble("costo", 0.0)
                                statoOperazione = "$msg - Pagato: €$costo"
                            } else {
                                statoOperazione = msg
                            }
                            snackbarHostState.showSnackbar(msg)
                        }
                    }
                    .send()
            },
            onError = { e ->
                e.printStackTrace()
            }
        )

        // Rimozione controllata delle sottoscrizioni all'uscita dalla schermata
        onDispose {
            MqttManager.client.unsubscribeWith()
                .topicFilter("parcheggio/app/accessi/risposta")
                .send()
            MqttManager.client.unsubscribeWith()
                .topicFilter("parcheggio/app/checkout/risposta")
                .send()
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
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Text("Pannello Simulazione QR Code", style = MaterialTheme.typography.titleLarge)

                Text(text = "Stato Corrente: $statoOperazione", style = MaterialTheme.typography.bodyMedium)

                Button(
                    onClick = {
                        val jsonReq = JSONObject().apply {
                            put("azione", "CHECKIN")
                            put("codice_qr", "PARCHEGGIO_INGRESSO")
                            put("targa", targaUtente)
                        }
                        MqttManager.client.publishWith()
                            .topic("parcheggio/accessi")
                            .payload(jsonReq.toString().toByteArray())
                            .send()
                    },
                    modifier = Modifier.fillMaxWidth(0.8f).height(50.dp)
                ) {
                    Text("Inquadra QR Ingresso")
                }

                Button(
                    onClick = {
                        val jsonReq = JSONObject().apply {
                            put("azione", "CHECKOUT")
                            put("codice_qr", "PARCHEGGIO_USCITA")
                            put("targa", targaUtente)
                        }
                        MqttManager.client.publishWith()
                            .topic("parcheggio/app/checkout")
                            .payload(jsonReq.toString().toByteArray())
                            .send()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth(0.8f).height(50.dp)
                ) {
                    Text("Inquadra QR Uscita (Richiedi Sblocco)")
                }
            }
        }
    }
}