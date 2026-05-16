package com.example.parcheggioiot.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.bson.Document
import com.mongodb.kotlin.client.coroutine.MongoClient

@Composable
fun SignupScreen(navController: NavController) {
    val coroutineScope = rememberCoroutineScope()

    var nome by remember { mutableStateOf("") }
    var cognome by remember { mutableStateOf("") }
    var mail by remember { mutableStateOf("") }
    var cf by remember { mutableStateOf("") }
    var targa by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val isFormValid = nome.isNotBlank() &&
            cognome.isNotBlank() &&
            mail.contains("@") &&
            cf.length == 16 &&
            targa.length == 7 &&
            password.length >= 5

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Registrazione", fontSize = 28.sp)

        OutlinedTextField(value = nome, onValueChange = { nome = it }, label = { Text("Nome") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = cognome, onValueChange = { cognome = it }, label = { Text("Cognome") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = mail, onValueChange = { mail = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())

        OutlinedTextField(
            value = cf,
            onValueChange = { if (it.length <= 16) cf = it.uppercase() },
            label = { Text("Codice Fiscale (16 caratteri)") },
            modifier = Modifier.fillMaxWidth(),
            isError = cf.isNotEmpty() && cf.length != 16
        )

        OutlinedTextField(
            value = targa,
            onValueChange = { if (it.length <= 7) targa = it.uppercase() },
            label = { Text("Targa (7 caratteri)") },
            modifier = Modifier.fillMaxWidth(),
            isError = targa.isNotEmpty() && targa.length != 7
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password (min. 5 caratteri)") },
            modifier = Modifier.fillMaxWidth(),
            isError = password.isNotEmpty() && password.length < 5
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (isFormValid) {
                    coroutineScope.launch(Dispatchers.IO) {
                        try {
                            val connectionString = "mongodb+srv://matmartini04_db_user:<db_password>@parcheggiocluster.r45yj7e.mongodb.net/?appName=ParcheggioCluster"
                            val mongoClient = MongoClient.create(connectionString)
                            val database = mongoClient.getDatabase("parcheggio_db")
                            val collection = database.getCollection<Document>("UTENTI")

                            val nuovoUtente = Document()
                                .append("nome", nome)
                                .append("cognome", cognome)
                                .append("mail", mail)
                                .append("cf", cf)
                                .append("targa", targa)
                                .append("spesa_totale", 0.0)
                                .append("password", password)

                            collection.insertOne(nuovoUtente)
                            mongoClient.close()

                            launch(Dispatchers.Main) {
                                // Naviga verso la Home passando il nome inserito
                                navController.navigate("home?nomeUtente=$nome")
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            },
            enabled = isFormValid,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("REGISTRATI")
        }

        TextButton(onClick = { navController.popBackStack() }) {
            Text("Hai già un account? Accedi")
        }
    }
}