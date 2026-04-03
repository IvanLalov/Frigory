package com.lalov.frigory

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Alimento::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun alimentoDao(): AlimentoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "frigory_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}