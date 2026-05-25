// GestoreSosta.kt
package com.example.parcheggioiot.network

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.roundToInt

object GestoreSosta {
    var secondiTrascorsi by mutableStateOf(0)
    var timerAttivo by mutableStateOf(false)
    var costoAttuale by mutableStateOf(0.0)
    var sostaTerminata by mutableStateOf(false)

    fun calcolaCosto(secondi: Int): Double {
        val minuti = secondi / 60
        val costo = if (minuti <= 4) {
            minuti * 0.05
        } else {
            0.20 + ((minuti - 4) * 0.04)
        }
        return (costo * 100.0).roundToInt() / 100.0
    }

    fun incrementaSecondo() {
        if (timerAttivo) {
            secondiTrascorsi++
            costoAttuale = calcolaCosto(secondiTrascorsi)
        }
    }

    fun resettaSosta() {
        secondiTrascorsi = 0
        timerAttivo = false
        costoAttuale = 0.0
        sostaTerminata = false
    }
}