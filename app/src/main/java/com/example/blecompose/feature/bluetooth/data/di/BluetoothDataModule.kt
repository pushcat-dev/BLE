package com.example.blecompose.feature.bluetooth.data.di

import com.example.blecompose.core.bluetooth.repository.MultiTapRepository
import com.example.blecompose.feature.bluetooth.data.datasource.BluetoothDataSource
import com.example.blecompose.feature.bluetooth.data.datasource.BluetoothDataSourceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BluetoothDataModule {

    @Provides
    @Singleton
    fun provideBluetoothDataSource(
        coreRepo: MultiTapRepository
    ): BluetoothDataSource =
        BluetoothDataSourceImpl(coreRepo)
}