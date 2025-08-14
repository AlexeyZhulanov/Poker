package com.example.poker.di

import com.example.poker.data.storage.AppSettings
import com.example.poker.data.storage.EncryptedAppSettings
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class StorageModule {

    @Binds
    @Singleton
    abstract fun bindAppSettings(impl: EncryptedAppSettings): AppSettings
}