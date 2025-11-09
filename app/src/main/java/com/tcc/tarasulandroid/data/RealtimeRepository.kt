package com.tcc.tarasulandroid.data

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RealtimeRepository @Inject constructor() {

    val lastEvent: Flow<String> = flow {
        var count = 0
        while (true) {
            emit("Pong: ${count++}")
            delay(1000)
        }
    }
}