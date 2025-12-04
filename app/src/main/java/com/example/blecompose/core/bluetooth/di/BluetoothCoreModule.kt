package com.example.blecompose.core.bluetooth.di

import com.example.blecompose.core.bluetooth.repository.MultiTapRepository
import com.example.blecompose.core.bluetooth.repository.MultiTapRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BluetoothCoreModule {

    @Binds
    @Singleton
    abstract fun bindMultiTapRepository(
        impl: MultiTapRepositoryImpl
    ): MultiTapRepository
}