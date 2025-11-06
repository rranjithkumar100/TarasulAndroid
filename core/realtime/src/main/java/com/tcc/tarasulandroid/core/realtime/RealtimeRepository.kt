
package com.tcc.tarasulandroid.core.realtime

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RealtimeRepository @Inject constructor(
    private val socketClient: SocketClient
) {

    fun connect() {
        socketClient.connect()
    }

    fun disconnect() {
        socketClient.disconnect()
    }

    fun isConnected(): Boolean {
        return socketClient.isConnected()
    }

    fun onNewMessage(): Flow<Any> {
        return socketClient.on("chat:new_message")
    }

    fun sendPing() {
        socketClient.emit("ping", "ping")
    }

    fun onPong(): Flow<Any> {
        return socketClient.on("pong")
    }
}
