package com.example.network.di

import com.example.common.Constants
import com.example.network.interceptor.AuthInterceptor
import com.example.network.service.CatApiService
import com.example.network.service.DogApiService
import com.example.petbreeds.core.network.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level =
                if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
        }

    @Provides
    @Singleton
    @Named("catOkHttpClient")
    fun provideCatOkHttpClient(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient =
        OkHttpClient
            .Builder()
            .addInterceptor(AuthInterceptor(BuildConfig.CAT_API_KEY))
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

    @Provides
    @Singleton
    @Named("dogOkHttpClient")
    fun provideDogOkHttpClient(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient =
        OkHttpClient
            .Builder()
            .addInterceptor(AuthInterceptor(BuildConfig.DOG_API_KEY))
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

    @Provides
    @Singleton
    @Named("catRetrofit")
    fun provideCatRetrofit(
        @Named("catOkHttpClient") okHttpClient: OkHttpClient,
    ): Retrofit =
        Retrofit
            .Builder()
            .baseUrl(Constants.CAT_API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    @Named("dogRetrofit")
    fun provideDogRetrofit(
        @Named("dogOkHttpClient") okHttpClient: OkHttpClient,
    ): Retrofit =
        Retrofit
            .Builder()
            .baseUrl(Constants.DOG_API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideCatApiService(
        @Named("catRetrofit") retrofit: Retrofit,
    ): CatApiService = retrofit.create(CatApiService::class.java)

    @Provides
    @Singleton
    fun provideDogApiService(
        @Named("dogRetrofit") retrofit: Retrofit,
    ): DogApiService = retrofit.create(DogApiService::class.java)
}
