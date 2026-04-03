package com.lalov.frigory

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tabla_nevera")
data class Alimento(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    var cantidad: Int,
    val fechaCaducidad: String,
    val stockMinimo: Int = 1
)