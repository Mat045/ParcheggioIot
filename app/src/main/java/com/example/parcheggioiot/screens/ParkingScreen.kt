package com.example.parcheggioiot.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun ParkingScreen(navController: NavController) {
    val righe = listOf("A", "B", "C", "D", "E", "F", "G", "H", "I")
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        righe.forEachIndexed { i, label ->
            Row {
                for (j in 1..5) {
                    Box(modifier = Modifier.size(50.dp, 30.dp).border(1.dp, Color.Black).background(Color.LightGray), contentAlignment = Alignment.Center) {
                        Text("$label$j")
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }
            }
            // Spaziature speciali richieste: dopo riga 1, 3, 5
            val space = if (i == 0 || i == 2 || i == 5) 30.dp else 4.dp
            Spacer(modifier = Modifier.height(space))
        }
    }
}