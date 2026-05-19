package com.example.parcheggioiot.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.parcheggioiot.network.MqttManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val SfondoScuro = Color(0xFF121824)
    val SuperficieCard = Color(0xFF1E2638)
    val BluPrimario = Color(0xFF3B82F6)

    LaunchedEffect(Unit) {
        // Connessione asincrona non bloccante tramite MqttManager centralizzato
        MqttManager.connettiInBackground(
            onSuccess = {
                MqttManager.client.subscribeWith()
                    .topicFilter("parcheggio/app/risposta")
                    .callback { publish ->
                        val payload = String(publish.payloadAsBytes)
                        try {
                            val json = JSONObject(payload)
                            if (json.getString("azione") == "LOGIN_RISPOSTA") {
                                if (json.getBoolean("successo")) {
                                    val nomeUtente = json.getString("nome")
                                    val targaUtente = json.getString("targa")
                                    coroutineScope.launch(Dispatchers.Main) {
                                        navController.navigate("home?nomeUtente=$nomeUtente&targaUtente=$targaUtente")
                                    }
                                } else {
                                    coroutineScope.launch(Dispatchers.Main) {
                                        snackbarHostState.showSnackbar("Email o Password errati!")
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    .send()
            },
            onError = {
                coroutineScope.launch(Dispatchers.Main) {
                    snackbarHostState.showSnackbar("Errore di rete: Broker MQTT offline")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = SfondoScuro
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SuperficieCard)
            ) {
                Column(
                    modifier = Modifier
                        .padding(28.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Smart Parking",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Accedi per gestire il tuo posto",
                        fontSize = 14.sp,
                        color = Color.LightGray,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email", color = Color.LightGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = BluPrimario,
                            unfocusedBorderColor = Color.Gray
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = pass,
                        onValueChange = { pass = it },
                        label = { Text("Password", color = Color.LightGray) },
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = BluPrimario,
                            unfocusedBorderColor = Color.Gray
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Button(
                        onClick = {
                            if (email.isNotBlank() && pass.isNotBlank()) {
                                val jsonReq = JSONObject().apply {
                                    put("azione", "LOGIN")
                                    put("mail", email)
                                    put("password", pass)
                                }
                                MqttManager.client.publishWith()
                                    .topic("parcheggio/app/login")
                                    .payload(jsonReq.toString().toByteArray())
                                    .send()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BluPrimario),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text("LOGIN", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(onClick = { navController.navigate("signup") }) {
                        Text("Non sei iscritto? Fai il Signup", color = BluPrimario)
                    }
                }
            }
        }
    }
}