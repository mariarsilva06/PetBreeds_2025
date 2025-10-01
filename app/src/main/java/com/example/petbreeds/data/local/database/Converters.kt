package com.example.petbreeds.data.local.database

import androidx.room.TypeConverter
import com.example.model.PetType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromPetType(petType: PetType): String {
        return petType.name
    }

    @TypeConverter
    fun toPetType(petType: String): PetType {
        return PetType.valueOf(petType)
    }

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return gson.fromJson(value, object : TypeToken<List<String>>() {}.type)
    }
}