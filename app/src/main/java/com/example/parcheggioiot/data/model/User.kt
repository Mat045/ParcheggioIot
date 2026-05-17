package com.example.parcheggioiot.data.model

// Nessun import di Realm, classe dati pulita (opzionale, non la stiamo usando nel flusso)
class Utente {
    var cf: String = ""
    var nome: String = ""
    var cognome: String = ""
    var mail: String = ""
    var targa: String = ""
    var spesaTotale: Double = 0.0
    var password: String = ""
}