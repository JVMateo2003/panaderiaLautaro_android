package com.jvmapp.panaderialautaro.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Cliente::class, Venta::class],
    version = 2
)
abstract class AppDatabase :RoomDatabase() {
    abstract fun clienteDao(): ClienteDao
    abstract fun ventaDao(): VentaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "panaderia_db"
                )
                    .fallbackToDestructiveMigration() // 🔥 importante por version 2
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}