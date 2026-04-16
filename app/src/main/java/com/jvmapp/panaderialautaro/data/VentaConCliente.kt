package com.jvmapp.panaderialautaro.data

data class VentaConCliente(
    val id: Int,
    val clienteId: Int,
    val clienteNombre: String,
    val fecha: Long,
    val kilos: Double,
    val precioPorKilo: Double,
    val total: Double,
    val pagado: Double,
    val diferencia: Double
)