// QRScreen.kt
package com.example.parcheggioiot.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.parcheggioiot.network.MqttManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import kotlin.math.roundToInt

@Composable
fun QRScreen(navController: NavController, targaUtente: String) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Stati di controllo e gestione interfaccia
    var statoOperazione by remember { mutableStateOf("Seleziona un'operazione per avviare la fotocamera") }
    var mostraFotocamera by remember { mutableStateOf(false) }
    var tipoOperazioneSelezionata by remember { mutableStateOf("") }

    // Stati del Cronometro e Tariffe PROGRESSIVE
    var secondiTrascorsi by remember { mutableStateOf(0) }
    var timerAttivo by remember { mutableStateOf(false) }
    var costoAttuale by remember { mutableStateOf(0.0) }
    var sostaTerminata by remember { mutableStateOf(false) }

    val SfondoScuro = Color(0xFF121824)
    val SuperficieCard = Color(0xFF1E2638)
    val BluPrimario = Color(0xFF3B82F6)
    val RossoErrore = Color(0xFFEF4444)
    val GialloTimer = Color(0xFFFBBF24)

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

    // Tasto indietro bloccato se il timer sta correndo
    BackHandler(enabled = timerAttivo) { }

    // Cronometro al secondo
    LaunchedEffect(timerAttivo) {
        while (timerAttivo) {
            delay(1000)
            secondiTrascorsi++
            costoAttuale = calcolaCostoLocali(secondiTrascorsi)
        }
    }

    // Connessione MQTT e ascolto passivo per aggiornare i messaggi di stato
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
                        }
                    }.send()

                MqttManager.client.subscribeWith()
                    .topicFilter("parcheggio/app/checkout/risposta")
                    .callback { publish ->
                        val payload = String(publish.payloadAsBytes)
                        val json = JSONObject(payload)
                        val msg = json.getString("messaggio")
                        coroutineScope.launch(Dispatchers.Main) {
                            statoOperazione = msg
                        }
                    }.send()
            },
            onError = { e -> e.printStackTrace() }
        )
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) { mostraFotocamera = true }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = SfondoScuro
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {

            if (mostraFotocamera) {
                Box(modifier = Modifier.fillMaxSize()) {
                    QRScannerView(onQrCodeScanned = { contenutoQr ->
                        mostraFotocamera = false

                        // AZIONE INGRESSO IMMEDIATA
                        if (tipoOperazioneSelezionata == "INGRESSO") {
                            // Sblocca subito l'interfaccia e mostra il timer, senza aspettare responsi
                            timerAttivo = true
                            statoOperazione = "Ingresso effettuato! Sosta avviata."

                            val jsonReq = JSONObject().apply {
                                put("azione", "CHECKIN")
                                put("codice_qr", contenutoQr.ifBlank { "PARCHEGGIO_INGRESSO" })
                                put("targa", targaUtente)
                            }
                            MqttManager.client.publishWith()
                                .topic("parcheggio/accessi")
                                .payload(jsonReq.toString().toByteArray())
                                .send()
                        }
                        // AZIONE USCITA IMMEDIATA
                        else if (tipoOperazioneSelezionata == "USCITA") {
                            timerAttivo = false
                            sostaTerminata = true
                            statoOperazione = "Uscita effettuata! Pagamento in corso."

                            val jsonReq = JSONObject().apply {
                                put("azione", "CHECKOUT")
                                put("codice_qr", contenutoQr.ifBlank { "PARCHEGGIO_USCITA" })
                                put("targa", targaUtente)
                                put("costo_finale", costoAttuale)
                            }
                            MqttManager.client.publishWith()
                                .topic("parcheggio/app/checkout")
                                .payload(jsonReq.toString().toByteArray())
                                .send()
                        }
                    })

                    Button(
                        onClick = { mostraFotocamera = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.7f)),
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 40.dp)
                    ) { Text("ANNULLA", color = Color.White) }
                }
            } else {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (timerAttivo || sostaTerminata) {
                        Text(text = formattaTempoLocale(secondiTrascorsi), fontSize = 64.sp, fontWeight = FontWeight.Bold, color = GialloTimer)
                        Text(text = "€ ${String.format("%.2f", costoAttuale)}", fontSize = 28.sp, color = Color.White, modifier = Modifier.padding(bottom = 32.dp))
                    } else {
                        Text("Accesso Varco", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Inquadra il QR per convalidare", fontSize = 14.sp, color = Color.LightGray, modifier = Modifier.padding(bottom = 32.dp))
                    }

                    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = SuperficieCard), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(24.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {

                            Surface(color = SfondoScuro.copy(alpha = 0.5f), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
                                Text(text = statoOperazione, color = BluPrimario, fontSize = 15.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(14.dp))
                            }

                            // Pulsante FOTOCAMERA INGRESSO
                            if (!timerAttivo && !sostaTerminata) {
                                Button(
                                    onClick = {
                                        tipoOperazioneSelezionata = "INGRESSO"
                                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                            mostraFotocamera = true
                                        } else {
                                            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = BluPrimario),
                                    modifier = Modifier.fillMaxWidth().height(54.dp)
                                ) { Text("APRI FOTOCAMERA INGRESSO", fontWeight = FontWeight.Bold) }
                            }

                            // Pulsante FOTOCAMERA USCITA (Ora forzato se il timer è attivo)
                            if (timerAttivo) {
                                Button(
                                    onClick = {
                                        tipoOperazioneSelezionata = "USCITA"
                                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                            mostraFotocamera = true
                                        } else {
                                            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = RossoErrore),
                                    modifier = Modifier.fillMaxWidth().height(54.dp)
                                ) { Text("APRI FOTOCAMERA USCITA", fontWeight = FontWeight.Bold, color = Color.White) }
                            }

                            // Pulsante CHIUDI E TORNA ALLA HOME
                            if (sostaTerminata) {
                                Button(
                                    onClick = { navController.popBackStack() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                                    modifier = Modifier.fillMaxWidth().height(54.dp)
                                ) { Text("TORNA ALLA HOME", fontWeight = FontWeight.Bold) }
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
}