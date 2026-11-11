package com.growthaxys.churchmanagements.di

import com.growthaxys.churchmanagements.BuildConfig
import com.growthaxys.churchmanagements.data.remote.SupabaseProvider
import com.growthaxys.churchmanagements.data.repository.AuthRepository
import com.growthaxys.churchmanagements.data.repository.SupabaseAuthRepository
import dagger.Module
import dagger.Provides
import dagger.Binds
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryBindings {
    @Binds @Singleton
    abstract fun bindAuthRepository(impl: SupabaseAuthRepository): AuthRepository
}

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides @Singleton
    fun provideSupabase() = SupabaseProvider.create(
        url = BuildConfig.SUPABASE_URL,
        publishableKey = BuildConfig.SUPABASE_PUBLISHABLE_KEY
    )
}
