package com.example.parcheggioiot.utils

import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.max

object TariffaCalcolatore {

    // Calcola i minuti trascorsi e il costo progressivo
    fun calcolaSosta(dataInStr: String): Pair<String, Double> {
        return try {
            val dataIn = LocalDateTime.parse(dataInStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val oraAttuale = LocalDateTime.now()

            val secondiTotali = max(0, Duration.between(dataIn, oraAttuale).seconds)
            val minutiTotali = secondiTotali / 60

            // Calcolo del costo al minuto con la logica dello sconto
            var costo = 0.0
            for (i in 1..minutiTotali) {
                if (costo >= 0.20) {
                    costo += 0.04 // Tariffa scontata
                } else {
                    costo += 0.05 // Tariffa base
                }
            }

            // Formattazione del tempo visivo in MM:SS (o HH:MM se supera l'ora)
            val ore = secondiTotali / 3600
            val minuti = (secondiTotali % 3600) / 60
            val secondi = secondiTotali % 60

            val tempoFormattato = if (ore > 0) {
                String.format("%02d:%02d:%02d", ore, minuti, secondi)
            } else {
                String.format("%02d:%02d", minuti, secondi)
            }

            Pair(tempoFormattato, String.format("%.2f", costo).toDouble())
        } catch (e: Exception) {
            Pair("00:00", 0.0)
        }
    }
}