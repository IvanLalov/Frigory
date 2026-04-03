package com.lalov.frigory

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.lalov.frigory.AlimentoAdapter

class InventarioActivity : AppCompatActivity() {

    private lateinit var adapter: AlimentoAdapter
    private val listaAlimentos = mutableListOf<Alimento>()
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventario)

        // Inicializamos la base de datos de Room
        database = AppDatabase.getDatabase(this)

        // Configuramos la lista (RecyclerView)
        val rv = findViewById<RecyclerView>(R.id.recyclerViewInventario)
        rv.layoutManager = LinearLayoutManager(this)
        adapter = AlimentoAdapter(listaAlimentos)
        rv.adapter = adapter

        // Botón para añadir producto de prueba
        val btnAdd = findViewById<Button>(R.id.btnAnadirProducto)
        btnAdd.setOnClickListener {
            insertarProductoDePrueba()
        }

        // Cargamos los datos guardados al entrar
        cargarDatos()
    }

    private fun cargarDatos() {
        CoroutineScope(Dispatchers.IO).launch {
            val alimentos = database.alimentoDao().obtenerTodos()
            withContext(Dispatchers.Main) {
                listaAlimentos.clear()
                listaAlimentos.addAll(alimentos)
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun insertarProductoDePrueba() {
        CoroutineScope(Dispatchers.IO).launch {
            val nuevo = Alimento(nombre = "Producto de Prueba", cantidad = 1, fechaCaducidad = "2026-12-31")
            database.alimentoDao().insertar(nuevo)
            cargarDatos()
        }
    }
}