package com.example.parcheggioiot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.parcheggioiot.navigation.NavGraph
import com.example.parcheggioiot.ui.theme.ParcheggioIotTheme // Metti il nome esatto del tuo tema se è diverso

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ParcheggioIotTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // È FONDAMENTALE che ci sia questo per avviare il NavGraph!
                    NavGraph()
                }
            }
        }
    }
}