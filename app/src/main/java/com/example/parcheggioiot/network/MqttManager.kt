// MqttManager.kt
package com.example.parcheggioiot.network

import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient
import com.hivemq.client.mqtt.MqttClient
import java.util.UUID

/**
 * Gestore della comunicazione MQTT tramite il client HiveMQ.
 * Gestisce la connessione, la disconnessione e il monitoraggio dello stato della rete.
 */
object MqttManager {

    // Configurazione e inizializzazione pigra (lazy) del client HiveMQ
    val client: Mqtt3AsyncClient by lazy {
        MqttClient.builder()
            .useMqttVersion3()
            .identifier("Android_App_" + UUID.randomUUID().toString().take(8))
            .serverHost("a67b59331e4e42e8bf7557cd181f3aee.s1.eu.hivemq.cloud")
            .serverPort(8883)
            .sslWithDefaultConfig()
            .simpleAuth()
            .username("esp32")
            .password("Iot12345678".toByteArray())
            .applySimpleAuth()
            // Listener per monitorare le disconnessioni improvvise o la perdita di segnale
            .addDisconnectedListener { context ->
                isConnected = false
            }
            .buildAsync()
    }

    // Flag per tracciare lo stato attuale della connessione al broker
    private var isConnected = false

    /**
     * Avvia il processo di connessione al broker MQTT in background.
     * Se il client risulta già connesso, esegue direttamente la callback di successo.
     * * @param onSuccess Callback invocata a connessione stabilita con successo.
     * @param onError Callback invocata in caso di errore di connessione.
     */
    fun connettiInBackground(onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        if (isConnected) {
            onSuccess()
            return
        }

        client.connect()
            .whenComplete { _, throwable ->
                if (throwable == null) {
                    isConnected = true
                    onSuccess()
                } else {
                    isConnected = false
                    onError(throwable)
                }
            }
    }
}