package com.example.parcheggioiot.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.parcheggioiot.network.MqttManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

// Mantiene lo stato della sosta tra i cambi di schermata dell'app
object StatoSostaCondiviso {
    var dataIniziale: String by mutableStateOf("")
}

@Composable
fun HomeScreen(navController: NavController, nomeUtente: String, targaUtente: String) {
    val coroutineScope = rememberCoroutineScope()

    val SfondoScuro = Color(0xFF121824)
    val SuperficieCard = Color(0xFF1E2638)
    val BluPrimario = Color(0xFF3B82F6)
    val GialloAttenzione = Color(0xFFFBBF24)

    LaunchedEffect(Unit) {
        MqttManager.connettiInBackground(
            onSuccess = {
                // Ascolta la risposta di avvenuto checkin
                MqttManager.client.subscribeWith()
                    .topicFilter("parcheggio/app/accessi/risposta")
                    .callback { publish ->
                        val payload = String(publish.payloadAsBytes)
                        try {
                            val json = JSONObject(payload)
                            if (json.getBoolean("successo")) {
                                val dataIn = json.getString("data_in")
                                coroutineScope.launch(Dispatchers.Main) {
                                    StatoSostaCondiviso.dataIniziale = dataIn
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    .send()

                // Ascolta la risposta di avvenuto checkout per resettare lo stato
                MqttManager.client.subscribeWith()
                    .topicFilter("parcheggio/app/checkout/risposta")
                    .callback { publish ->
                        val payload = String(publish.payloadAsBytes)
                        try {
                            val json = JSONObject(payload)
                            if (json.getBoolean("successo")) {
                                coroutineScope.launch(Dispatchers.Main) {
                                    StatoSostaCondiviso.dataIniziale = ""
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    .send()
            },
            onError = { it.printStackTrace() }
        )
    }

    Scaffold(containerColor = SfondoScuro) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Benvenuto, $nomeUtente!", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Color.White)

            if (targaUtente.isNotBlank()) {
                Text("Targa associata: $targaUtente", fontSize = 16.sp, color = Color.LightGray, modifier = Modifier.padding(top = 8.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Il box compare solo se il valore di dataIniziale è valorizzato dal checkin
            if (StatoSostaCondiviso.dataIniziale.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SuperficieCard)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("SOSTA ATTIVA", color = GialloAttenzione, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Ingresso registrato il:", color = Color.LightGray, fontSize = 12.sp)
                        Text(StatoSostaCondiviso.dataIniziale, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { navController.navigate("parking") },
                colors = ButtonDefaults.buttonColors(containerColor = BluPrimario),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(54.dp)
            ) {
                Text("VEDI MAPPA PARCHEGGIO", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { navController.navigate("qr?targaUtente=$targaUtente") },
                colors = ButtonDefaults.buttonColors(containerColor = BluPrimario),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(54.dp)
            ) {
                Text("APRI GESTIONE QR (INGRESSO/USCITA)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { navController.navigate("history?targaUtente=$targaUtente") },
                colors = ButtonDefaults.buttonColors(containerColor = BluPrimario),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(54.dp)
            ) {
                Text("VISUALIZZA STORICO SOSTE", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(40.dp))

            TextButton(
                onClick = {
                    navController.navigate("login") { popUpTo("home") { inclusive = true } }
                }
            ) {
                Text("Effettua il Logout", color = Color.Red, fontSize = 16.sp)
            }
        }
    }
}