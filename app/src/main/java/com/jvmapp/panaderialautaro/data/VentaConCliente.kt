package com.jvmapp.panaderialautaro.data

data class VentaConCliente(
    val id: Int,
    val clienteId: Int,
    val tipoPrecioId: Int,
    val clienteNombre: String,
    val fecha: Long,
    val kgVarilla: Double,
    val kgBollo: Double,
    val kgCriollo: Double,
    val otroMonto: Double,
    val total: Double,
    val pagado: Double,
    val diferencia: Double
)