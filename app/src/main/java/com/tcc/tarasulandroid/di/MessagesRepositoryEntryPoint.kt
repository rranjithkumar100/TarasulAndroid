package com.tcc.tarasulandroid.di

import com.tcc.tarasulandroid.data.MessagesRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface MessagesRepositoryEntryPoint {
    fun messagesRepository(): MessagesRepository
}
