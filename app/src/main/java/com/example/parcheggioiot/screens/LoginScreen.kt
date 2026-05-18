package com.example.parcheggioiot.screens

import androidx.compose.foundation.background
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
import com.hivemq.client.mqtt.MqttClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.UUID

@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Palette Colori Personalizzata Dark Blue
    val SfondoScuro = Color(0xFF121824)    // Blu/Grigio molto scuro
    val SuperficieCard = Color(0xFF1E2638) // Grigio bluastro per i pannelli
    val BluPrimario = Color(0xFF3B82F6)    // Blu moderno per i bottoni principali

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
                mqttClient.subscribeWith()
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
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = SfondoScuro // Sfondo generale scuro
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            // Card centrale contenitiva per un effetto "Glow/Floating"
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
                                coroutineScope.launch(Dispatchers.IO) {
                                    val jsonReq = JSONObject().apply {
                                        put("azione", "LOGIN")
                                        put("mail", email)
                                        put("password", pass)
                                    }
                                    mqttClient.publishWith()
                                        .topic("parcheggio/app/login")
                                        .payload(jsonReq.toString().toByteArray())
                                        .send()
                                }
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