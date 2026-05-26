// HistoryScreen.kt
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
import com.example.parcheggioiot.network.MqttManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

/**
 * Schermata dello storico che mostra l'elenco delle soste effettuate dall'utente.
 * Invia una richiesta al broker e attende l'array JSON di risposta sul canale dedicato.
 */
@Composable
fun HistoryScreen(navController: NavController, targaUtente: String) {
    val listaPermanenze = remember { mutableStateListOf<JSONObject>() }
    var inCaricamento by remember { mutableStateOf(true) }

    // Gestione del ciclo di vita per sottoscrizione e successiva pulizia dei topic
    DisposableEffect(Unit) {
        MqttManager.connettiInBackground(
            onSuccess = {
                // Registrazione del listener per ricevere il flusso dello storico
                MqttManager.client.subscribeWith()
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

                // Inoltro della query per richiedere lo storico legato alla targa attuale
                val jsonReq = JSONObject().apply {
                    put("targa", targaUtente)
                }
                MqttManager.client.publishWith()
                    .topic("parcheggio/app/storico/richiesta")
                    .payload(jsonReq.toString().toByteArray())
                    .send()
            },
            onError = {
                inCaricamento = false
            }
        )

        // Cancellazione della sottoscrizione quando la schermata viene rimossa dal display
        onDispose {
            MqttManager.client.unsubscribeWith()
                .topicFilter("parcheggio/app/storico/risposta")
                .send()
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
                                Text("ID Sessione: ${per.optString("id_sessione")}", style = MaterialTheme.typography.bodyLarge)
                                Text("Ingresso: ${per.optString("data_ingresso")}", style = MaterialTheme.typography.bodyMedium)
                                Text("Stato Logico: ${per.optString("stato")}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}