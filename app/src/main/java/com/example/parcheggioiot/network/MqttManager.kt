// MqttManager.kt
package com.example.parcheggioiot.network

import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient
import com.hivemq.client.mqtt.MqttClient
import java.util.UUID

object MqttManager {
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
            .buildAsync()
    }

    private var isConnected = false

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
                    onError(throwable)
                }
            }
    }
}