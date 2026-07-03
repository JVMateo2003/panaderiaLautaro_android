package com.jvmapp.panaderialautaro.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface TipoPrecioDao {
    @Insert
    suspend fun insertar(tipoPrecio: TipoPrecio)

    @Update
    suspend fun actualizar(tipoPrecio: TipoPrecio)

    @Query("SELECT * FROM tipos_precio")
    suspend fun obtenerTodos(): List<TipoPrecio>

    @Query("SELECT * FROM tipos_precio WHERE id = :id")
    suspend fun obtenerPorId(id: Int): TipoPrecio?
}