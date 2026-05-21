// ParkingScreen.kt
package com.example.parcheggioiot.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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

@Composable
fun ParkingScreen(navController: NavController) {
    val coroutineScope = rememberCoroutineScope()

    var a1Occupato by remember { mutableStateOf(false) }
    var a2Occupato by remember { mutableStateOf(false) }

    val SfondoScuro = Color(0xFF121824)
    val SuperficieCard = Color(0xFF1E2638)
    val VerdeNeon = Color(0xFF10B981)
    val RossoNeon = Color(0xFFEF4444)

    LaunchedEffect(Unit) {
        MqttManager.connettiInBackground(
            onSuccess = {
                MqttManager.client.subscribeWith()
                    .topicFilter("parcheggio/app/posti/risposta")
                    .callback { publish ->
                        val payload = String(publish.payloadAsBytes)
                        try {
                            val json = JSONObject(payload)
                            if (json.getString("azione") == "STATO_POSTI_RISPOSTA") {
                                val statoA1 = json.optString("A1", "0").trim()
                                val statoA2 = json.optString("A2", "0").trim()

                                coroutineScope.launch(Dispatchers.Main) {
                                    a1Occupato = (statoA1 == "1")
                                    a2Occupato = (statoA2 == "1")
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    .send()

                val jsonReq = JSONObject().apply {
                    put("azione", "RICHIEDI_STATO_POSTI")
                }
                MqttManager.client.publishWith()
                    .topic("parcheggio/app/posti/richiesta")
                    .payload(jsonReq.toString().toByteArray())
                    .send()
            },
            onError = {
                it.printStackTrace()
            }
        )
    }

    val righe = listOf("A", "B", "C", "D", "E", "F", "G", "H", "I")

    Scaffold(containerColor = SfondoScuro) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Mappa Parcheggio",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "I posti reali interconnessi sono A1 e A2",
                fontSize = 13.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = SuperficieCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    righe.forEachIndexed { i, label ->
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            for (j in 1..5) {
                                val nomePosto = "$label$j"

                                val coloreSfondo = when (nomePosto) {
                                    "A1" -> if (a1Occupato) RossoNeon else VerdeNeon
                                    "A2" -> if (a2Occupato) RossoNeon else VerdeNeon
                                    else -> RossoNeon
                                }

                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 3.3.dp, vertical = 2.dp)
                                        .size(52.dp, 34.dp)
                                        .background(coloreSfondo, shape = RoundedCornerShape(6.dp))
                                        .border(1.dp, Color(0xFF121824), shape = RoundedCornerShape(6.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = nomePosto,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        val space = if (i == 0 || i == 2 || i == 5) 24.dp else 0.dp
                        if (space > 0.dp) {
                            Spacer(modifier = Modifier.height(space))
                        }
                    }
                }
            }
        }
    }
}