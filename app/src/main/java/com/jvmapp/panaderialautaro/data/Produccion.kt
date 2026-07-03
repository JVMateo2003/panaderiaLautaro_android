package com.jvmapp.panaderialautaro.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "produccion")
data class Produccion(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val fecha: Long,
    val kgVarilla: Double,
    val kgBollo: Double,
    val kgCriollo: Double,
    val otroMonto: Double,
    val sobroVarilla: Double,
    val sobroBollo: Double,
    val sobroCriollo: Double
)