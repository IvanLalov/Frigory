package com.lalov.frigory

import androidx.room.*

@Dao
interface AlimentoDao {

    @Query("SELECT * FROM tabla_nevera")
    fun obtenerTodos(): List<Alimento>

    @Insert
    fun insertar(alimento: Alimento)

    @Delete
    fun eliminar(alimento: Alimento)

    @Update
    fun actualizar(alimento: Alimento)
}