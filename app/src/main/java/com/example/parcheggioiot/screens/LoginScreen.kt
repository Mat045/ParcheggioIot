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
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

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
                                        // Passiamo sia il nome che la targa alla Home dell'app
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
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))
            TextField(
                value = pass,
                onValueChange = { pass = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(20.dp))

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
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Login")
            }

            TextButton(onClick = { navController.navigate("signup") }) {
                Text("Non sei ancora iscritto? Fai il Signup")
            }
        }
    }
}