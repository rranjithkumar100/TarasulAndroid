package com.tcc.tarasulandroid.feature.home.model

data class Message(
    val id: String,
    val from: String,
    val to: String,
    val text: String,
    val time: Long,
    val isQuoted: Boolean = false,
    val attachments: List<String> = emptyList()
)