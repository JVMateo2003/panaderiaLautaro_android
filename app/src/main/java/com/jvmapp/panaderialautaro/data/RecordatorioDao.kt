package com.jvmapp.panaderialautaro.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface RecordatorioDao {
    @Insert
    suspend fun insertar(recordatorio: Recordatorio)

    @Query("SELECT * FROM recordatorios ORDER BY fecha DESC")
    suspend fun obtenerTodos(): List<Recordatorio>

    @Query("DELETE FROM recordatorios WHERE id = :id")
    suspend fun eliminar(id: Int)
}