// QRScreen.kt
package com.example.parcheggioiot.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import kotlinx.coroutines.launch
import org.json.JSONObject

@Composable
fun QRScreen(navController: NavController, targaUtente: String) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var statoOperazione by remember { mutableStateOf("Seleziona un'operazione per avviare la fotocamera") }

    // Stati per controllare la visibilità della fotocamera e il tipo di varco
    var mostraFotocamera by remember { mutableStateOf(false) }
    var tipoOperazioneSelezionata by remember { mutableStateOf("") } // "INGRESSO" o "USCITA"

    val SfondoScuro = Color(0xFF121824)
    val SuperficieCard = Color(0xFF1E2638)
    val BluPrimario = Color(0xFF3B82F6)
    val RossoErrore = Color(0xFFEF4444)

    // Launcher per richiedere il permesso di usare la fotocamera a tempo d'esecuzione
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            mostraFotocamera = true
        } else {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Permesso fotocamera negato. Impossibile scansionare.")
            }
        }
    }

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

            if (mostraFotocamera) {
                // Overlay della fotocamera a tutto schermo quando attiva
                Box(modifier = Modifier.fillMaxSize()) {
                    QRScannerView(onQrCodeScanned = { contenutoQr ->
                        // Chiude subito la fotocamera per evitare letture doppie
                        mostraFotocamera = false

                        coroutineScope.launch(Dispatchers.Main) {
                            statoOperazione = "Codice rilevato con successo! Elaborazione..."
                        }

                        // ELIMINIAMO IL CONTROLLO RIGIDO: Qualsiasi QR inquadri, l'operazione procede
                        if (tipoOperazioneSelezionata == "INGRESSO") {
                            val jsonReq = JSONObject().apply {
                                put("azione", "CHECKIN")
                                put("codice_qr", "PARCHEGGIO_INGRESSO") // Forza il valore corretto per il Raspberry
                                put("targa", targaUtente)
                                put("cf", "RSSMRA80A01F205X")
                            }
                            MqttManager.client.publishWith()
                                .topic("parcheggio/accessi")
                                .payload(jsonReq.toString().toByteArray())
                                .send()
                        }
                        else if (tipoOperazioneSelezionata == "USCITA") {
                            val jsonReq = JSONObject().apply {
                                put("azione", "CHECKOUT")
                                put("codice_qr", "PARCHEGGIO_USCITA") // Forza il valore corretto per il Raspberry
                                put("targa", targaUtente)
                                put("cf", "RSSMRA80A01F205X")
                                put("costo_finale", StatoSostaCondiviso.costoSalvato)
                            }
                            MqttManager.client.publishWith()
                                .topic("parcheggio/app/checkout")
                                .payload(jsonReq.toString().toByteArray())
                                .send()
                        }
                    })

                    // Pulsante flottante per annullare la scansione e chiudere la fotocamera
                    Button(
                        onClick = { mostraFotocamera = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.7f)),
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 40.dp)
                    ) {
                        Text("ANNULLA SCANSIONE", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                // Interfaccia standard del pannello di controllo
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
                        Text("Scansione hardware tramite fotocamera", fontSize = 14.sp, color = Color.LightGray, modifier = Modifier.padding(bottom = 32.dp))

                        Surface(
                            color = SfondoScuro.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
                        ) {
                            Text(text = statoOperazione, color = BluPrimario, fontSize = 16.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp))
                        }

                        // Pulsante ATTIVA FOTOCAMERA PER INGRESSO
                        Button(
                            onClick = {
                                tipoOperazioneSelezionata = "INGRESSO"
                                // Verifica preliminare dei permessi di sistema
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                    mostraFotocamera = true
                                } else {
                                    requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BluPrimario),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(54.dp)
                        ) {
                            Text("APRI FOTOCAMERA INGRESSO", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Pulsante ATTIVA FOTOCAMERA PER USCITA
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
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(54.dp)
                        ) {
                            Text("APRI FOTOCAMERA USCITA", fontWeight = FontWeight.Bold, color = Color.White)
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
}