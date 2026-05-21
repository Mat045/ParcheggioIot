package com.example.parcheggioiot.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.parcheggioiot.network.MqttManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

@Composable
fun QRScreen(navController: NavController, targaUtente: String) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var statoOperazione by remember { mutableStateOf("In attesa di scansione") }

    val SfondoScuro = Color(0xFF121824)
    val SuperficieCard = Color(0xFF1E2638)
    val BluPrimario = Color(0xFF3B82F6)
    val RossoErrore = Color(0xFFEF4444)

    LaunchedEffect(Unit) {
        MqttManager.connettiInBackground(
            onSuccess = {
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

                MqttManager.client.subscribeWith()
                    .topicFilter("parcheggio/app/checkout/risposta")
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
            },
            onError = { e -> e.printStackTrace() }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = SfondoScuro
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SuperficieCard)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Accesso Varco", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Simulazione scansione QR Code", fontSize = 14.sp, color = Color.LightGray, modifier = Modifier.padding(bottom = 32.dp))

                    Surface(
                        color = SfondoScuro.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
                    ) {
                        Text(text = statoOperazione, color = BluPrimario, fontSize = 16.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp))
                    }

                    // Pulsante INGRESSO (Genera data_in su Raspberry)
                    Button(
                        onClick = {
                            val jsonReq = JSONObject().apply {
                                put("azione", "CHECKIN")
                                put("codice_qr", "PARCHEGGIO_INGRESSO")
                                put("targa", targaUtente)
                                put("cf", "RSSMRA80A01F205X") // Sostituisci o passa il CF reale dell'utente se salvato
                            }
                            MqttManager.client.publishWith()
                                .topic("parcheggio/accessi")
                                .payload(jsonReq.toString().toByteArray())
                                .send()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BluPrimario),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(54.dp)
                    ) {
                        Text("INQUADRA QR INGRESSO",fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Pulsante USCITA (Invia il costo calcolato dall'app al Raspberry per chiudere la sessione)
                    Button(
                        onClick = {
                            val jsonReq = JSONObject().apply {
                                put("azione", "CHECKOUT")
                                put("codice_qr", "PARCHEGGIO_USCITA")
                                put("targa", targaUtente)
                                put("cf", "RSSMRA80A01F205X")
                                put("costo_finale", StatoSostaCondiviso.costoSalvato) // Passa il prezzo calcolato dall'app
                            }
                            MqttManager.client.publishWith()
                                .topic("parcheggio/app/checkout")
                                .payload(jsonReq.toString().toByteArray())
                                .send()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RossoErrore),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(54.dp)
                    ) {
                        Text("INQUADRA QR USCITA", fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("Torna alla Home", color = Color.Gray)
                    }
                }
            }
        }
    }
}