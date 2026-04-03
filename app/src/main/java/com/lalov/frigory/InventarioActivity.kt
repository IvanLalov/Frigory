package com.lalov.frigory

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InventarioActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private lateinit var rvInventario: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventario)

        database = AppDatabase.getDatabase(this)

        rvInventario = findViewById(R.id.rvInventario)
        rvInventario.layoutManager = LinearLayoutManager(this)

        val btnAdd = findViewById<Button>(R.id.btnAnadirProducto)
        btnAdd.setOnClickListener {
            val intent = Intent(this, BuscarAlimentoActivity::class.java)
            startActivity(intent)
        }

        cargarDatos()
    }

    override fun onResume() {
        super.onResume()
        cargarDatos()
    }

    private fun cargarDatos() {
        CoroutineScope(Dispatchers.IO).launch {
            val lista = database.alimentoDao().obtenerTodos()
            withContext(Dispatchers.Main) {
                // El AlimentoAdapter se encargará de pintar de rojo los agotados
                rvInventario.adapter = AlimentoAdapter(
                    listaAlimentos = lista,
                    onClick = { alimento -> mostrarDialogoEdicion(alimento) },
                    onLongClick = { alimento -> mostrarDialogoBorrar(alimento) }
                )
            }
        }
    }

    private fun mostrarDialogoEdicion(alimento: Alimento) {
        val view = layoutInflater.inflate(R.layout.dialog_edicion, null)
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setView(view)

        val dialog = builder.create()
        dialog.show()

        val tvNombre = view.findViewById<TextView>(R.id.tvNombreDialogo)
        val tvCant = view.findViewById<TextView>(R.id.tvCantidadDialogo)
        val btnMas = view.findViewById<Button>(R.id.btnMas)
        val btnMenos = view.findViewById<Button>(R.id.btnMenos)
        val btnCerrar = view.findViewById<Button>(R.id.btnCerrarDialogo)

        tvNombre.text = alimento.nombre
        tvCant.text = alimento.cantidad.toString()

        // --- FUNCIÓN DE AYUDA PARA EL COLOR ---
        fun actualizarColorCero() {
            tvCant.text = alimento.cantidad.toString() // Solo el número, sin paréntesis

            if (alimento.cantidad == 0) {
                tvCant.setTextColor(android.graphics.Color.RED)
                // Opcional: puedes cambiar el título del diálogo para avisar
                tvNombre.text = "${alimento.nombre} (AGOTADO)"
            } else {
                tvCant.setTextColor(android.graphics.Color.BLACK)
                tvNombre.text = alimento.nombre
            }
        }

        // Ponemos el color correcto al abrir el diálogo por primera vez
        actualizarColorCero()

        btnMas.setOnClickListener {
            alimento.cantidad++
            modificarCantidad(alimento, 0) // Guardamos el cambio en la DB
            actualizarColorCero() // Refrescamos el texto y el color en el Pop-up
        }

        btnMenos.setOnClickListener {
            if (alimento.cantidad > 0) {
                alimento.cantidad--
                modificarCantidad(alimento, 0)
                actualizarColorCero()
            }
        }

        btnCerrar.setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun mostrarDialogoBorrar(alimento: Alimento) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Eliminar producto")
            .setMessage("¿Seguro que quieres quitar el ${alimento.nombre} de la nevera?")
            .setPositiveButton("Eliminar") { _, _ ->
                CoroutineScope(Dispatchers.IO).launch {
                    database.alimentoDao().eliminar(alimento)
                    cargarDatos()
                }
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun modificarCantidad(alimento: Alimento, cambio: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val nuevaCantidad = alimento.cantidad + cambio
            if (nuevaCantidad >= 0) {
                alimento.cantidad = nuevaCantidad
                database.alimentoDao().actualizar(alimento)
                cargarDatos()
            }
        }
    }
}