package com.jvmapp.panaderialautaro.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ProduccionDao {
    @Insert
    suspend fun insertar(produccion: Produccion)

    @Update
    suspend fun actualizar(produccion: Produccion)

    @Query("SELECT * FROM produccion WHERE fecha BETWEEN :inicio AND :fin LIMIT 1")
    suspend fun obtenerDelDia(inicio: Long, fin: Long): Produccion?

    @Query("SELECT * FROM produccion ORDER BY fecha DESC")
    suspend fun obtenerTodas(): List<Produccion>
}