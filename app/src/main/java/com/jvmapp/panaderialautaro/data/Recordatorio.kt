package com.jvmapp.panaderialautaro.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recordatorios")
data class Recordatorio(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val texto: String,
    val fecha: Long = System.currentTimeMillis()
)