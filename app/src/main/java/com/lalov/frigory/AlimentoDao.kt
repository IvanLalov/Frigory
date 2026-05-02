package com.lalov.frigory

import androidx.room.*

@Dao
interface AlimentoDao {

    // Gestión del inventario (Nevera)
    @Query("SELECT * FROM tabla_nevera")
    suspend fun obtenerTodos(): List<Alimento>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(alimento: Alimento)

    @Delete
    suspend fun eliminar(alimento: Alimento)

    @Update
    suspend fun actualizar(alimento: Alimento)

    // Gestión de la lista de la compra
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
        val nuevoAlimento = Alimento(
            nombre = item.nombre,
            cantidad = 1,
            fechaCaducidad = "Sin fecha"
        )
        insertar(nuevoAlimento)
        eliminarCompra(item)
    }
}