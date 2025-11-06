
package com.tcc.tarasulandroid.feature.debug

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun RealtimeDebugScreen(modifier: Modifier = Modifier) {
    val viewModel: RealtimeDebugViewModel = hiltViewModel()
    val connectionStatus by viewModel.connectionStatus.collectAsState()
    val lastPong by viewModel.lastPong.collectAsState()
    val lastMessage by viewModel.lastMessage.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Connection Status: $connectionStatus")
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Last Pong: $lastPong")
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Last Message: $lastMessage")
        Spacer(modifier = Modifier.height(32.dp))

        Row {
            Button(onClick = { viewModel.connect() }) {
                Text("Connect")
            }
            Button(onClick = { viewModel.disconnect() }) {
                Text("Disconnect")
            }
            Button(onClick = { viewModel.sendPing() }) {
                Text("Send Ping")
            }
        }
    }
}
