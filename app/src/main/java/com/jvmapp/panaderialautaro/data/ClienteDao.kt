package com.jvmapp.panaderialautaro.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ClienteDao {
    @Insert
    suspend fun insertar(cliente: Cliente)

    @Update
    suspend fun actualizar(cliente: Cliente)

    @Query("SELECT * FROM clientes WHERE activo = 1")
    suspend fun obtenerClientesActivos(): List<Cliente>

    @Query("SELECT * FROM clientes WHERE id = :id")
    suspend fun obtenerPorId(id: Int): Cliente?

    @Query("SELECT * FROM clientes")
    suspend fun obtenerTodos(): List<Cliente>
}