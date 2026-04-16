package com.jvmapp.panaderialautaro.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query


@Dao
interface VentaDao {
    @Insert
    suspend fun insertar(venta: Venta)

    @Query("SELECT * FROM ventas WHERE clienteId = :clienteId")
    suspend fun obtenerVentasDeCliente(clienteId: Int): List<Venta>

    @Query("SELECT * FROM ventas ORDER BY fecha DESC")
    suspend fun obtenerVentas(): List<Venta>

    @Query("""SELECT ventas.*, clientes.nombre AS clienteNombre FROM ventas INNER JOIN clientes on ventas.clienteId = clientes.id""")
    suspend fun obtenerVentaConCliente(): List<VentaConCliente>

    @Query ("""SELECT ventas.*, clientes.nombre AS clienteNombre FROM ventas INNER JOIN clientes ON ventas.clienteId = clientes.id WHERE ventas.clienteId = :clienteId ORDER BY fecha DESC """)
    suspend fun obtenerVentasConClientePorCliente(clienteId: Int): List<VentaConCliente>
}
