package com.tcc.tarasulandroid.di

import com.tcc.tarasulandroid.data.LanguageManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface LanguageManagerEntryPoint {
    fun languageManager(): LanguageManager
}
