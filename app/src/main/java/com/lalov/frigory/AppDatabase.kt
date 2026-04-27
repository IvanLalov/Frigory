package com.lalov.frigory

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// 1. Cambia la versión a 2 y añade CompraItem::class
@Database(entities = [Alimento::class, CompraItem::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun alimentoDao(): AlimentoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "frigory_db"
                )
                    .fallbackToDestructiveMigration() // 2. Añade esto para que no explote al cambiar la versión
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}