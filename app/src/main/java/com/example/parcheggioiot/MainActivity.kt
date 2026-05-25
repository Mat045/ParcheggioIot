package com.example.parcheggioiot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.example.parcheggioiot.navigation.NavGraph
import com.example.parcheggioiot.network.GestoreSosta
import com.example.parcheggioiot.ui.theme.ParcheggioIotTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ParcheggioIotTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // CRONOMETRO GLOBALE: Gira costantemente in background finché l'applicazione è aperta.
                    LaunchedEffect(Unit) {
                        while (true) {
                            delay(1000) // Aspetta un secondo
                            GestoreSosta.incrementaSecondo() // Aggiorna i calcoli se la sosta è attiva
                        }
                    }

                    // Avvio del NavGraph che gestisce le tue schermate (Login, Home, QRScreen, ecc.)
                    NavGraph()
                }
            }
        }
    }
}