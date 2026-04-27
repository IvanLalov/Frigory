package com.lalov.frigory

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tabla_nevera")
data class Alimento(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    var cantidad: Int,
    var fechaCaducidad: String = "Sin fecha",
    val stockMinimo: Int = 1
)