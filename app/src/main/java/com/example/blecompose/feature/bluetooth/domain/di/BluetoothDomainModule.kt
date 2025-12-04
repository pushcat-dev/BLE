package com.example.blecompose.feature.bluetooth.domain.di

import com.example.blecompose.feature.bluetooth.domain.repository.BluetoothRepository
import com.example.blecompose.feature.bluetooth.domain.repository.BluetoothRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BluetoothDomainModule {

    @Binds
    @Singleton
    abstract fun bindBluetoothRepository(
        impl: BluetoothRepositoryImpl
    ): BluetoothRepository
}