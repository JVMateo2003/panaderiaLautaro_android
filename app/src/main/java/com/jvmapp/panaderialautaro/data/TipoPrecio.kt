package com.jvmapp.panaderialautaro.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tipos_precio")
data class TipoPrecio(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val precioVarilla: Double,
    val precioBollo: Double,
    val precioCriollo: Double
)