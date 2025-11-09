package com.tcc.tarasulandroid.di

import android.content.Context
import com.tcc.tarasulandroid.data.db.AppDatabase
import com.tcc.tarasulandroid.data.db.ContactsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }
    
    @Provides
    @Singleton
    fun provideContactsDao(database: AppDatabase): ContactsDao {
        return database.contactsDao()
    }
    
    @Provides
    @Singleton
    fun provideMessagesDao(database: AppDatabase): com.tcc.tarasulandroid.data.db.MessagesDao {
        return database.messagesDao()
    }
    
    @Provides
    @Singleton
    fun provideMediaDao(database: AppDatabase): com.tcc.tarasulandroid.data.db.MediaDao {
        return database.mediaDao()
    }
}
