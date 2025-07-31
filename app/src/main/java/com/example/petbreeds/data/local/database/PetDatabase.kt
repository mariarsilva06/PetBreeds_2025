package com.example.petbreeds.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.petbreeds.data.local.dao.PetDao
import com.example.petbreeds.data.local.entity.PetEntity

@Database(
    entities = [PetEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class PetDatabase : RoomDatabase() {
    abstract fun petDao(): PetDao
}
