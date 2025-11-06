package com.tcc.tarasulandroid.di

import com.tcc.tarasulandroid.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.socket.client.IO
import io.socket.client.Socket
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RealtimeModule {

    @Provides
    @Singleton
    fun provideSocket(): Socket {
        return IO.socket(BuildConfig.BASE_SOCKET_URL)
    }
}
