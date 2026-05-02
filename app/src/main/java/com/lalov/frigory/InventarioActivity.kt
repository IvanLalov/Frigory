package com.lalov.frigory

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.Calendar

class InventarioActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private lateinit var rvInventario: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_inventario)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarInventario)
        setSupportActionBar(toolbar)

        database = AppDatabase.getDatabase(this)
        rvInventario = findViewById(R.id.rvInventario)
        rvInventario.layoutManager = LinearLayoutManager(this)

        // Botón principal para añadir productos
        findViewById<Button>(R.id.btnAnadirProducto).setOnClickListener {
            startActivity(Intent(this, BuscarAlimentoActivity::class.java))
        }

        // Botón "Píldora" para la lista de la compra
        findViewById<Button>(R.id.btnVerListaCompra).setOnClickListener {
            startActivity(Intent(this, ListaCompraActivity::class.java))
        }

        cargarDatos()
    }

    override fun onResume() {
        super.onResume()
        cargarDatos()
    }

    private fun cargarDatos() {
        lifecycleScope.launch {
            val lista = database.alimentoDao().obtenerTodos()
            rvInventario.adapter = AlimentoAdapter(
                listaAlimentos = lista,
                onClick = { alimento -> mostrarDialogoEdicion(alimento) },
                onLongClick = { alimento -> mostrarOpcionesAlimento(alimento) }
            )
        }
    }

    private fun mostrarOpcionesAlimento(alimento: Alimento) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_opciones_alimento, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogView.findViewById<TextView>(R.id.tvTituloOpciones).text = alimento.nombre

        dialogView.findViewById<Button>(R.id.btnAnadirALaCompra).setOnClickListener {
            lifecycleScope.launch {
                database.alimentoDao().insertarCompra(CompraItem(nombre = alimento.nombre))
                Toast.makeText(this@InventarioActivity, "Añadido a la lista", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }

        dialogView.findViewById<Button>(R.id.btnEliminar).setOnClickListener {
            lifecycleScope.launch {
                database.alimentoDao().eliminar(alimento)
                cargarDatos()
                dialog.dismiss()
            }
        }

        dialogView.findViewById<Button>(R.id.btnEditarFecha).setOnClickListener {
            dialog.dismiss()
            val cal = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                val nuevaFecha = "${String.format("%02d", day)}/${String.format("%02d", month + 1)}/$year"
                lifecycleScope.launch {
                    alimento.fechaCaducidad = nuevaFecha
                    database.alimentoDao().actualizar(alimento)
                    cargarDatos()
                }
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        dialogView.findViewById<Button>(R.id.btnNoOpciones).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun mostrarDialogoEdicion(alimento: Alimento) {
        val view = layoutInflater.inflate(R.layout.dialog_edicion, null)
        val dialog = AlertDialog.Builder(this).setView(view).create()

        val tvNombre = view.findViewById<TextView>(R.id.tvNombreDialogo)
        val tvCant = view.findViewById<TextView>(R.id.tvCantidadDialogo)

        tvNombre.text = alimento.nombre
        tvCant.text = alimento.cantidad.toString()

        fun actualizarInterfaz() {
            tvCant.text = alimento.cantidad.toString()
            tvCant.setTextColor(if (alimento.cantidad == 0) Color.RED else Color.BLACK)
        }

        view.findViewById<Button>(R.id.btnMas).setOnClickListener {
            if (alimento.cantidad < 999) {
                alimento.cantidad++
                actualizarPersistencia(alimento)
                actualizarInterfaz()
            }
        }

        view.findViewById<Button>(R.id.btnMenos).setOnClickListener {
            if (alimento.cantidad > 0) {
                alimento.cantidad--
                actualizarPersistencia(alimento)
                actualizarInterfaz()
                if (alimento.cantidad == 0) comprobarAlertas(alimento)
            }
        }

        view.findViewById<Button>(R.id.btnCerrarDialogo).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun actualizarPersistencia(alimento: Alimento) {
        lifecycleScope.launch {
            database.alimentoDao().actualizar(alimento)
            rvInventario.adapter?.notifyDataSetChanged()
        }
    }

    private fun comprobarAlertas(alimento: Alimento) {
        AlertDialog.Builder(this)
            .setTitle("Aviso de stock")
            .setMessage("${alimento.nombre} se ha agotado. ¿Añadir a la lista de la compra?")
            .setPositiveButton("Sí") { _, _ ->
                lifecycleScope.launch {
                    database.alimentoDao().insertarCompra(CompraItem(nombre = alimento.nombre))
                }
            }
            .setNegativeButton("No", null)
            .show()
    }
}