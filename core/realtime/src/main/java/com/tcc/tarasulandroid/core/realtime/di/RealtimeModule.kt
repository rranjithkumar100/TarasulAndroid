
package com.tcc.tarasulandroid.core.realtime.di

import com.tcc.tarasulandroid.core.realtime.BuildConfig
import com.tcc.tarasulandroid.core.realtime.SocketClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RealtimeModule {

    @Provides
    @Singleton
    fun provideSocketClient(): SocketClient {
        return SocketClient(BuildConfig.BASE_SOCKET_URL)
    }
}
