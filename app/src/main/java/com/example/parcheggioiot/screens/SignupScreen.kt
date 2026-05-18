package com.example.parcheggioiot.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.hivemq.client.mqtt.MqttClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.UUID

@Composable
fun SignupScreen(navController: NavController) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var nome by remember { mutableStateOf("") }
    var cognome by remember { mutableStateOf("") }
    var mail by remember { mutableStateOf("") }
    var cf by remember { mutableStateOf("") }
    var targa by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val SfondoScuro = Color(0xFF121824)
    val SuperficieCard = Color(0xFF1E2638)
    val BluPrimario = Color(0xFF3B82F6)

    val isFormValid = nome.isNotBlank() &&
            cognome.isNotBlank() &&
            mail.contains("@") &&
            cf.length == 16 &&
            targa.length == 7 &&
            password.length >= 5

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
                            if (json.getString("azione") == "SIGNUP_RISPOSTA") {
                                if (json.getBoolean("successo")) {
                                    coroutineScope.launch(Dispatchers.Main) {
                                        navController.navigate("home?nomeUtente=$nome") {
                                            popUpTo("signup") { inclusive = true }
                                        }
                                    }
                                } else {
                                    coroutineScope.launch(Dispatchers.Main) {
                                        snackbarHostState.showSnackbar("Errore durante la registrazione!")
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
        containerColor = SfondoScuro
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            Text("Registrazione", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Crea il tuo profilo IoT", fontSize = 14.sp, color = Color.LightGray)

            Spacer(modifier = Modifier.height(10.dp))

            val inputColors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = BluPrimario,
                unfocusedBorderColor = Color.Gray,
                focusedLabelColor = BluPrimario,
                unfocusedLabelColor = Color.LightGray
            )

            OutlinedTextField(value = nome, onValueChange = { nome = it }, label = { Text("Nome") }, colors = inputColors, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            OutlinedTextField(value = cognome, onValueChange = { cognome = it }, label = { Text("Cognome") }, colors = inputColors, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            OutlinedTextField(value = mail, onValueChange = { mail = it }, label = { Text("Email") }, colors = inputColors, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))

            OutlinedTextField(
                value = cf,
                onValueChange = { if (it.length <= 16) cf = it.uppercase() },
                label = { Text("Codice Fiscale (16 caratteri)") },
                colors = inputColors,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                isError = cf.isNotEmpty() && cf.length != 16
            )

            OutlinedTextField(
                value = targa,
                onValueChange = { if (it.length <= 7) targa = it.uppercase() },
                label = { Text("Targa (7 caratteri)") },
                colors = inputColors,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                isError = targa.isNotEmpty() && targa.length != 7
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password (min. 5 caratteri)") },
                colors = inputColors,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                isError = password.isNotEmpty() && password.length < 5
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (isFormValid) {
                        coroutineScope.launch(Dispatchers.IO) {
                            val jsonReq = JSONObject().apply {
                                put("azione", "SIGNUP")
                                put("nome", nome)
                                put("cognome", cognome)
                                put("mail", mail)
                                put("cf", cf)
                                put("targa", targa)
                                put("password", password)
                            }
                            mqttClient.publishWith()
                                .topic("parcheggio/app/signup")
                                .payload(jsonReq.toString().toByteArray())
                                .send()
                        }
                    }
                },
                enabled = isFormValid,
                colors = ButtonDefaults.buttonColors(containerColor = BluPrimario, disabledContainerColor = Color.DarkGray),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("REGISTRATI", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (isFormValid) Color.White else Color.Gray)
            }
        }
    }
}