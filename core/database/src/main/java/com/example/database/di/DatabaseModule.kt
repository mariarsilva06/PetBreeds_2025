package com.example.database.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.example.database.entity.PetEntity
import com.example.database.dao.PetDao
import com.example.database.PetDatabase


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