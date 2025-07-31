package com.example.petbreeds.di

import android.content.Context
import androidx.room.Room
import com.example.petbreeds.data.local.dao.PetDao
import com.example.petbreeds.data.local.database.PetDatabase
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
    fun providePetDatabase(
        @ApplicationContext context: Context
    ): PetDatabase {
        return Room.databaseBuilder(
            context,
            PetDatabase::class.java,
            "pet_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun providePetDao(database: PetDatabase): PetDao {
        return database.petDao()
    }
}