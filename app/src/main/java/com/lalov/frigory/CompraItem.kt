package com.lalov.frigory

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lista_compra")
data class CompraItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    var cantidad: Int = 1,
    var marcado: Boolean = false // Para saber si ya lo hemos metido en el carrito
)