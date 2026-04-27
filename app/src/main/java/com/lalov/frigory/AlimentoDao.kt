package com.lalov.frigory

import androidx.room.*

@Dao
interface AlimentoDao {

    // --- INVENTARIO (NEVERA) ---
    @Query("SELECT * FROM tabla_nevera")
    suspend fun obtenerTodos(): List<Alimento>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(alimento: Alimento) // Añadido suspend

    @Delete
    suspend fun eliminar(alimento: Alimento) // Añadido suspend

    @Update
    suspend fun actualizar(alimento: Alimento) // Añadido suspend

    // --- LISTA DE LA COMPRA ---
    @Query("SELECT * FROM lista_compra")
    suspend fun obtenerCompra(): List<CompraItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarCompra(item: CompraItem)

    @Delete
    suspend fun eliminarCompra(item: CompraItem)

    @Update
    suspend fun actualizarCompra(item: CompraItem)

    @Transaction
    suspend fun moverAInventario(item: CompraItem) {
        // Convertimos el item de compra en un Alimento para la nevera
        val nuevoAlimento = Alimento(
            nombre = item.nombre,
            cantidad = 1, // Por defecto 1, luego el usuario puede editarlo
            fechaCaducidad = "Sin fecha" // O una fecha por defecto
        )
        insertar(nuevoAlimento)
        eliminarCompra(item)
    }
}