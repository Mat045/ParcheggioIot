package com.example.parcheggioiot.data.model

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class Utente : RealmObject {
    @PrimaryKey
    var cf: String = ""
    var nome: String = ""
    var cognome: String = ""
    var mail: String = ""
    var targa: String = ""
    var spesaTotale: Double = 0.0
    var password: String = ""
}