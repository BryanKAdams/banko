package com.bryankeltonadams.banko.core


import com.bryankeltonadams.banko.GameRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {


    @Singleton
    @Provides
    fun provideAuthRepository(): GameRepository = GameRepository()

}
