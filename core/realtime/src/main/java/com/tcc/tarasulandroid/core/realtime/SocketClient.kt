
package com.tcc.tarasulandroid.core.realtime

import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URISyntaxException

class SocketClient(private val url: String) {

    private var socket: Socket? = null
    private val scope = CoroutineScope(Dispatchers.IO + Job())

    private val _events = MutableSharedFlow<Pair<String, Any>>()
    val events = _events.asSharedFlow()

    private var reconnectJob: Job? = null

    fun connect() {
        if (isConnected()) return

        try {
            val options = IO.Options()
            options.reconnection = false
            socket = IO.socket(url, options)
        } catch (e: URISyntaxException) {
            e.printStackTrace()
            return
        }

        socket?.on(Socket.EVENT_CONNECT) {
            println("Socket connected")
            reconnectJob?.cancel()
        }

        socket?.on(Socket.EVENT_DISCONNECT) {
            println("Socket disconnected")
            scheduleReconnect()
        }

        socket?.on(Socket.EVENT_CONNECT_ERROR) { _ ->
            println("Socket connection error")
            scheduleReconnect()
        }

        socket?.on("ping") { _ ->
            scope.launch {
                emit("pong", "pong")
            }
        }

        socket?.on("chat:new_message") { args ->
            scope.launch {
                if (args.isNotEmpty()) {
                    val data = args[0] as JSONObject
                    _events.emit("chat:new_message" to data)
                }
            }
        }

        socket?.connect()
    }

    fun disconnect() {
        reconnectJob?.cancel()
        socket?.disconnect()
    }

    fun isConnected(): Boolean {
        return socket?.connected() ?: false
    }

    fun on(event: String): Flow<Any> {
        return flow {
            events.collect { (e, data) ->
                if (e == event) {
                    emit(data)
                }
            }
        }
    }

    fun emit(event: String, payload: Any) {
        if (isConnected()) {
            socket?.emit(event, payload)
        }
    }

    private fun scheduleReconnect(initialDelay: Long = 1000L) {
        if (reconnectJob?.isActive == true) return

        reconnectJob = scope.launch {
            var delayMs = initialDelay
            while (!isConnected()) {
                delay(delayMs)
                delayMs = (delayMs * 2).coerceAtMost(60000L) // exponential backoff up to 1 minute
                socket?.connect()
            }
        }
    }
}
