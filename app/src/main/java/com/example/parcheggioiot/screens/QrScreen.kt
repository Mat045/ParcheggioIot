// QRScreen.kt
package com.example.parcheggioiot.screens


import androidx.activity.compose.BackHandler
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import kotlin.math.roundToInt


@Composable
fun QRScreen(navController: NavController, targaUtente: String) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }


    // Stati di controllo operazione
    var statoOperazione by remember { mutableStateOf("In attesa di scansione") }


    // Stati del Cronometro e del Calcolo Economico Locale
    var secondiTrascorsi by remember { mutableStateOf(0) }
    var timerAttivo by remember { mutableStateOf(false) }
    var costoAttuale by remember { mutableStateOf(0.0) }
    var sostaTerminata by remember { mutableStateOf(false) }


    // Timestamp di riferimento per evitare derive del delay(1000)
    var tempoInizioSosta by remember { mutableStateOf(0L) }


    val SfondoScuro = Color(0xFF121824)
    val SuperficieCard = Color(0xFF1E2638)
    val BluPrimario = Color(0xFF3B82F6)
    val RossoErrore = Color(0xFFEF4444)
    val GialloTimer = Color(0xFFFBBF24)


    // Funzioni di utilità locali per slegarsi dai crash del parser di stringhe date
    fun calcolaCostoLocali(secondi: Int): Double {
        val minuti = secondi / 60
        val costo = if (minuti <= 4) {
            minuti * 0.05
        } else {
            0.20 + ((minuti - 4) * 0.04)
        }
        return (costo * 100.0).roundToInt() / 100.0
    }


    fun formattaTempoLocale(secondi: Int): String {
        val h = secondi / 3600
        val m = (secondi % 3600) / 60
        val s = secondi % 60
        return if (h > 0) {
            String.format("%02d:%02d:%02d", h, m, s)
        } else {
            String.format("%02d:%02d", m, s)
        }
    }


    // Blocco del tasto back fisico se l'utente è dentro il parcheggio con timer attivo
    BackHandler(enabled = timerAttivo) { }


    // Effetto di update del cronometro agganciato al clock di sistema
    LaunchedEffect(timerAttivo) {
        if (timerAttivo) {
            tempoInizioSosta = System.currentTimeMillis() - (secondiTrascorsi * 1000L)
            while (timerAttivo) {
                val tempoAttuale = System.currentTimeMillis()
                secondiTrascorsi = ((tempoAttuale - tempoInizioSosta) / 1000L).toInt()
                costoAttuale = calcolaCostoLocali(secondiTrascorsi)
                delay(500) // Controlla due volte al secondo per massima precisione visiva
            }
        }
    }


    // Ascolto dei canali di risposta MQTT dal Raspberry
    LaunchedEffect(Unit) {
        MqttManager.connettiInBackground(
            onSuccess = {
                MqttManager.client.subscribeWith()
                    .topicFilter("parcheggio/app/accessi/risposta")
                    .callback { publish ->
                        val payload = String(publish.payloadAsBytes)
                        val json = JSONObject(payload)
                        val successo = json.optBoolean("successo", false)
                        val msg = json.getString("messaggio")


                        coroutineScope.launch(Dispatchers.Main) {
                            statoOperazione = msg
                            snackbarHostState.showSnackbar(msg)
                            // Se il server rifiuta l'ingresso (es: auto non sul sensore), spegni il timer
                            if (!successo) {
                                timerAttivo = false
                                secondiTrascorsi = 0
                                costoAttuale = 0.0
                            }
                        }
                    }
                    .send()


                MqttManager.client.subscribeWith()
                    .topicFilter("parcheggio/app/checkout/risposta")
                    .callback { publish ->
                        val payload = String(publish.payloadAsBytes)
                        val json = JSONObject(payload)
                        val successo = json.optBoolean("successo", false)
                        val msg = json.getString("messaggio")


                        coroutineScope.launch(Dispatchers.Main) {
                            statoOperazione = msg
                            snackbarHostState.showSnackbar(msg)
                            // Se il checkout fallisce lato server (es: auto non sul sensore d'uscita), riavvia il timer
                            if (!successo) {
                                timerAttivo = true
                                sostaTerminata = false
                            }
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
            Column(
                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Layout Dinamico: Se la sosta è in corso o terminata, mostra il Timer gigante
                if (timerAttivo || sostaTerminata) {
                    Text(text = formattaTempoLocale(secondiTrascorsi), fontSize = 64.sp, fontWeight = FontWeight.Bold, color = GialloTimer)
                    Text(text = "€ ${String.format("%.2f", costoAttuale)}", fontSize = 28.sp, color = Color.White, modifier = Modifier.padding(bottom = 24.dp))
                } else {
                    Text("Accesso Varco", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Simulazione scansione QR Code", fontSize = 14.sp, color = Color.LightGray, modifier = Modifier.padding(bottom = 32.dp))
                }


                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SuperficieCard),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            color = SfondoScuro.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                        ) {
                            Text(text = statoOperazione, color = BluPrimario, fontSize = 15.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center, modifier = Modifier.padding(14.dp))
                        }


                        // TASTO SIMULAZIONE INGRESSO
                        if (!timerAttivo && !sostaTerminata) {
                            Button(
                                onClick = {
                                    // Avvia istantaneamente il rendering locale per non dare latenze visive
                                    timerAttivo = true
                                    statoOperazione = "Invio richiesta ingresso..."


                                    val jsonReq = JSONObject().apply {
                                        put("azione", "CHECKIN")
                                        put("codice_qr", "PARCHEGGIO_INGRESSO")
                                        put("targa", targaUtente)
                                        put("cf", "RSSMRA80A01F205X")
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
                                Text("INQUADRA QR INGRESSO", fontWeight = FontWeight.Bold)
                            }
                        }


                        // TASTO SIMULAZIONE USCITA
                        if (timerAttivo) {
                            Button(
                                onClick = {
                                    timerAttivo = false
                                    sostaTerminata = true
                                    statoOperazione = "Invio richiesta uscita..."


                                    val jsonReq = JSONObject().apply {
                                        put("azione", "CHECKOUT")
                                        put("codice_qr", "PARCHEGGIO_USCITA")
                                        put("targa", targaUtente)
                                        put("cf", "RSSMRA80A01F205X")
                                        put("costo_finale", costoAttuale)
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
                        }


                        // TASTO DI RITORNO DISPONIBILE SOLO A SOSTA CHIUSA
                        if (sostaTerminata) {
                            Button(
                                onClick = { navController.popBackStack() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(54.dp)
                            ) {
                                Text("TORNA ALLA HOME", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }


                        if (!timerAttivo && !sostaTerminata) {
                            Spacer(modifier = Modifier.height(16.dp))
                            TextButton(onClick = { navController.popBackStack() }) {
                                Text("Annulla e torna indietro", color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}

