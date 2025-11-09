package com.tcc.tarasulandroid.feature.home.ui

import com.tcc.tarasulandroid.feature.home.model.Message

data class HomeUiState(
    val messages: List<Message> = emptyList(),
    val lastEvent: String = "",
    val isDarkTheme: Boolean = false
)