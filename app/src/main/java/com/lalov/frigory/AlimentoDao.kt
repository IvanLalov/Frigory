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

    // --- FUNCIONES PARA LA LISTA DE LA COMPRA ---
    @Query("SELECT * FROM lista_compra")
    suspend fun obtenerCompra(): List<CompraItem>

    @Insert
    suspend fun insertarCompra(item: CompraItem)

    @Delete
    suspend fun eliminarCompra(item: CompraItem)

    @Update
    suspend fun actualizarCompra(item: CompraItem)
}