package com.jvmapp.panaderialautaro.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "ventas",
    foreignKeys = [
        ForeignKey(
            entity = Cliente::class,
            parentColumns = ["id"],
            childColumns = ["clienteId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Venta (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val clienteId: Int,
    val fecha: Long,
    val kilos: Double,
    val precioPorKilo: Double,
    val total: Double,
    val pagado: Double,
    val diferencia: Double
)