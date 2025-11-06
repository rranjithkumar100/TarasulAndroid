package com.tcc.tarasulandroid.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Named("dummy_string")
    fun provideDummyString(): String = "Hello from Hilt!"
}
