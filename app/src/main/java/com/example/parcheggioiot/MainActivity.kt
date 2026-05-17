package com.example.parcheggioiot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.parcheggioiot.navigation.NavGraph
import com.example.parcheggioiot.ui.theme.ParcheggioIotTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ParcheggioIotTheme {
                // Lasciamo che NavGraph gestisca il suo controller internamente
                NavGraph()
            }
        }
    }
}