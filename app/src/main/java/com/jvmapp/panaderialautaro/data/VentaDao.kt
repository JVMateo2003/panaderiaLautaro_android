package com.jvmapp.panaderialautaro.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface VentaDao {
    @Insert
    suspend fun insertar(venta: Venta)

    @Query("SELECT * FROM ventas WHERE clienteId = :clienteId ORDER BY fecha DESC")
    suspend fun obtenerVentasDeCliente(clienteId: Int): List<Venta>

    @Query("SELECT * FROM ventas ORDER BY fecha DESC")
    suspend fun obtenerTodas(): List<Venta>

    @Query("""
        SELECT ventas.*, clientes.nombre AS clienteNombre 
        FROM ventas 
        INNER JOIN clientes ON ventas.clienteId = clientes.id 
        ORDER BY fecha DESC
    """)
    suspend fun obtenerVentasConCliente(): List<VentaConCliente>

    @Query("""
        SELECT ventas.*, clientes.nombre AS clienteNombre 
        FROM ventas 
        INNER JOIN clientes ON ventas.clienteId = clientes.id 
        WHERE ventas.clienteId = :clienteId 
        ORDER BY fecha DESC
    """)
    suspend fun obtenerVentasConClientePorId(clienteId: Int): List<VentaConCliente>

    @Query("""
        SELECT ventas.*, clientes.nombre AS clienteNombre 
        FROM ventas 
        INNER JOIN clientes ON ventas.clienteId = clientes.id 
        WHERE ventas.fecha BETWEEN :inicio AND :fin
        ORDER BY clientes.nombre ASC
    """)
    suspend fun obtenerVentasDelDia(inicio: Long, fin: Long): List<VentaConCliente>

    @Query("""
    SELECT clientes.id as clienteId, clientes.nombre as clienteNombre, 
    COALESCE(SUM(ventas.diferencia), 0) as saldoVentas
    FROM clientes 
    LEFT JOIN ventas ON clientes.id = ventas.clienteId
    WHERE clientes.activo = 1
    GROUP BY clientes.id
""")
    suspend fun obtenerSaldoPorCliente(): List<SaldoCliente>

    @Query("DELETE FROM ventas WHERE id = :ventaId")
    suspend fun eliminar(ventaId: Int)
}