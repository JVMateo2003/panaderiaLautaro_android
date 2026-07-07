package com.jvmapp.panaderialautaro.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PagoDao {
    @Insert
    suspend fun insertar(pago: Pago)

    @Query("SELECT * FROM pagos WHERE clienteId = :clienteId ORDER BY fecha DESC")
    suspend fun obtenerPagosDeCliente(clienteId: Int): List<Pago>

    @Query("SELECT * FROM pagos WHERE fecha BETWEEN :inicio AND :fin")
    suspend fun obtenerPagosDelDia(inicio: Long, fin: Long): List<Pago>

    @Query("SELECT * FROM pagos")
    suspend fun obtenerTodosLosClientes(): List<Pago>
}