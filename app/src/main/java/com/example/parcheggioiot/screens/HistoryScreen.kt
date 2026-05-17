package com.example.parcheggioiot.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.hivemq.client.mqtt.MqttClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

@Composable
fun HistoryScreen(navController: NavController, targaUtente: String) {
    val coroutineScope = rememberCoroutineScope()
    val listaPermanenze = remember { mutableStateListOf<JSONObject>() }
    var inCaricamento by remember { mutableStateOf(true) }

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
                    .topicFilter("parcheggio/app/storico/risposta")
                    .callback { publish ->
                        val payload = String(publish.payloadAsBytes)
                        try {
                            val jsonArray = JSONArray(payload)
                            listaPermanenze.clear()
                            for (i in 0 until jsonArray.length()) {
                                listaPermanenze.add(jsonArray.getJSONObject(i))
                            }
                            inCaricamento = false
                        } catch (e: Exception) {
                            e.printStackTrace()
                            inCaricamento = false
                        }
                    }
                    .send()

                val jsonReq = JSONObject().apply {
                    put("targa", targaUtente)
                }
                mqttClient.publishWith()
                    .topic("parcheggio/app/storico/richiesta")
                    .payload(jsonReq.toString().toByteArray())
                    .send()

            } catch (e: Exception) {
                e.printStackTrace()
                inCaricamento = false
            }
        }
    }

    Scaffold { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (inCaricamento) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (listaPermanenze.isEmpty()) {
                Text("Nessuna sosta registrata per questa targa.", modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(listaPermanenze) { per ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Targa: ${per.optString("targa")}", style = MaterialTheme.typography.bodyLarge)
                                Text("Ingresso: ${per.optString("data_ingresso")}", style = MaterialTheme.typography.bodyMedium)
                                Text("Stato: ${per.optString("stato")}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}