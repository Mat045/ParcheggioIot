package com.example.parcheggioiot.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull // IMPORT FONDAMENTALE PER IL Flow di Kotlin
import kotlinx.coroutines.launch
import org.bson.Document
import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoClient

@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

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
                            try {
                                // RICORDATI: Sostituisci le "xxxx" con quelle reali del tuo link di MongoDB Atlas!
                                val connectionString = "mongodb+srv://utente_parcheggio:ParcheggioIot2026@parcheggiocluster.xxxx.mongodb.net/?retryWrites=true&w=majority"
                                val mongoClient = MongoClient.create(connectionString)
                                val database = mongoClient.getDatabase("parcheggio_db")
                                val collection = database.getCollection<Document>("UTENTI")

                                // Cerchiamo l'utente. Grazie all'import "kotlinx.coroutines.flow.firstOrNull", ora funzionerà!
                                val utenteTrovato = collection.find(
                                    Filters.and(
                                        Filters.eq("mail", email),
                                        Filters.eq("password", pass)
                                    )
                                ).firstOrNull()

                                mongoClient.close()

                                if (utenteTrovato != null) {
                                    // Accediamo in modo sicuro al campo "nome" convertendolo in Stringa
                                    val nomeUtente = utenteTrovato["nome"]?.toString() ?: "Utente"

                                    launch(Dispatchers.Main) {
                                        navController.navigate("home?nomeUtente=$nomeUtente")
                                    }
                                } else {
                                    launch(Dispatchers.Main) {
                                        snackbarHostState.showSnackbar("Email o Password errati!")
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                launch(Dispatchers.Main) {
                                    snackbarHostState.showSnackbar("Errore di connessione al database")
                                }
                            }
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