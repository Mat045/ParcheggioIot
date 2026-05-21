package com.example.parcheggioiot.utils

import java.time.Instant
import java.time.Duration
import kotlin.math.max

object TariffaCalcolatore {

    fun calcolaSosta(dataInStr: String): Pair<String, Double> {
        return try {
            // Parsing flessibile che accetta qualsiasi stringa ISO del Raspberry (con o senza millisecondi/Z)
            val istanteIn = Instant.parse(dataInStr.let {
                if (!it.contains("Z") && !it.contains("+")) "${it}Z" else it
            })
            val istanteAttuale = Instant.now()

            val secondiTotali = max(0, Duration.between(istanteIn, istanteAttuale).seconds)
            val minutiTotali = secondiTotali / 60

            // LOGICA DI CALCOLO SOLDI CORRETTA:
            // Ogni minuto aggiunge 0.05. Se i minuti accumulati portano il costo oltre i 0.20€,
            // la soglia viene superata al 4° minuto (4 * 0.05 = 0.20).
            // Dal 5° minuto in poi, ogni minuto successivo costa 0.04.

            val costo = if (minutiTotali <= 4) {
                minutiTotali * 0.05
            } else {
                0.20 + ((minutiTotali - 4) * 0.04)
            }

            // Formattazione del tempo visivo (MM:SS)
            val ore = secondiTotali / 3600
            val minuti = (secondiTotali % 3600) / 60
            val secondi = secondiTotali % 60

            val tempoFormattato = if (ore > 0) {
                String.format("%02d:%02d:%02d", ore, minuti, secondi)
            } else {
                String.format("%02d:%02d", minuti, secondi)
            }

            // Arrotonda a 2 cifre decimali prima di restituire il Double
            val costoArrotondato = Math.round(costo * 100.0) / 100.0

            Pair(tempoFormattato, costoArrotondato)
        } catch (e: Exception) {
            e.printStackTrace()
            Pair("00:00", 0.0)
        }
    }
}