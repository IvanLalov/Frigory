package com.lalov.frigory

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class InventarioActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private lateinit var rvInventario: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // SEGURIDAD: Verificamos sesión antes de cargar nada
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_inventario)

        // Configuración de la Toolbar para que aparezca el menú
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarInventario)
        setSupportActionBar(toolbar)

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

    // --- MENÚ SUPERIOR (CARRITO) ---

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_inventario, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_carrito) {
            Toast.makeText(this, "Abriendo lista de la compra...", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, ListaCompraActivity::class.java)
            startActivity(intent)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    // --- LÓGICA DE DATOS ---

    private fun cargarDatos() {
        CoroutineScope(Dispatchers.IO).launch {
            val lista = database.alimentoDao().obtenerTodos()
            withContext(Dispatchers.Main) {
                rvInventario.adapter = AlimentoAdapter(
                    listaAlimentos = lista,
                    onClick = { alimento -> mostrarDialogoEdicion(alimento) },
                    onLongClick = { alimento -> mostrarOpcionesAlimento(alimento) }
                )
            }
        }
    }

    // --- DIÁLOGO DE GESTIÓN (Al mantener pulsado) ---
    private fun mostrarOpcionesAlimento(alimento: Alimento) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_opciones_alimento, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnEliminar = dialogView.findViewById<Button>(R.id.btnEliminar)
        val btnEditar = dialogView.findViewById<Button>(R.id.btnEditarFecha)
        val btnCompra = dialogView.findViewById<Button>(R.id.btnAnadirALaCompra)
        val btnCancelar = dialogView.findViewById<Button>(R.id.btnNoOpciones)
        val tvTitulo = dialogView.findViewById<TextView>(R.id.tvTituloOpciones)

        tvTitulo.text = alimento.nombre

        btnCancelar.setOnClickListener { dialog.dismiss() }

        // Añadir a la lista de la compra manualmente
        btnCompra.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                database.alimentoDao().insertarCompra(CompraItem(nombre = alimento.nombre))
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@InventarioActivity, "${alimento.nombre} añadido a la lista", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }
        }

        btnEliminar.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                database.alimentoDao().eliminar(alimento)
                withContext(Dispatchers.Main) {
                    cargarDatos()
                    Toast.makeText(this@InventarioActivity, "Producto eliminado", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }
        }

        btnEditar.setOnClickListener {
            dialog.dismiss()
            val cal = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                val nuevaFecha = "${String.format("%02d", day)}/${String.format("%02d", month + 1)}/$year"
                CoroutineScope(Dispatchers.IO).launch {
                    alimento.fechaCaducidad = nuevaFecha
                    database.alimentoDao().actualizar(alimento)
                    withContext(Dispatchers.Main) {
                        cargarDatos()
                        Toast.makeText(this@InventarioActivity, "Fecha actualizada", Toast.LENGTH_SHORT).show()
                    }
                }
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        dialog.show()
    }

    // --- DIÁLOGO DE EDICIÓN DE CANTIDAD (+/-) ---
    private fun mostrarDialogoEdicion(alimento: Alimento) {
        val view = layoutInflater.inflate(R.layout.dialog_edicion, null)
        val builder = AlertDialog.Builder(this)
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

        fun actualizarColorCero() {
            tvCant.text = alimento.cantidad.toString()
            if (alimento.cantidad == 0) {
                tvCant.setTextColor(Color.RED)
                tvNombre.text = "${alimento.nombre} (AGOTADO)"
            } else {
                tvCant.setTextColor(Color.BLACK)
                tvNombre.text = alimento.nombre
            }
        }

        actualizarColorCero()

        btnMas.setOnClickListener {
            // LÍMITE SUPERIOR: 999
            if (alimento.cantidad < 999) {
                alimento.cantidad++
                actualizarBaseDeDatos(alimento)
                actualizarColorCero()
            } else {
                Toast.makeText(this, "Máximo alcanzado (999)", Toast.LENGTH_SHORT).show()
            }
        }

        btnMenos.setOnClickListener {
            if (alimento.cantidad > 0) {
                alimento.cantidad--
                actualizarBaseDeDatos(alimento)
                actualizarColorCero()

                if (alimento.cantidad == 0) {
                    comprobarAlertasYAnadir(alimento)
                }
            }
        }

        btnCerrar.setOnClickListener { dialog.dismiss() }
    }

    private fun actualizarBaseDeDatos(alimento: Alimento) {
        CoroutineScope(Dispatchers.IO).launch {
            database.alimentoDao().actualizar(alimento)
            withContext(Dispatchers.Main) {
                rvInventario.adapter?.notifyDataSetChanged()
            }
        }
    }

    private fun comprobarAlertasYAnadir(alimento: Alimento) {
        val hoy = Calendar.getInstance()
        val formato = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())

        var motivo = ""

        if (alimento.cantidad == 0) {
            motivo = "se ha agotado"
        } else {
            try {
                val fechaCad = formato.parse(alimento.fechaCaducidad)
                if (fechaCad != null) {
                    val calCad = Calendar.getInstance()
                    calCad.time = fechaCad
                    val diff = calCad.timeInMillis - hoy.timeInMillis
                    val diasRestantes = diff / (24 * 60 * 60 * 1000)

                    if (diasRestantes <= 2) {
                        motivo = "está a punto de caducar"
                    }
                }
            } catch (e: Exception) { }
        }

        if (motivo.isNotEmpty()) {
            AlertDialog.Builder(this)
                .setTitle("¡Aviso de Frigory!")
                .setMessage("El producto ${alimento.nombre} $motivo. ¿Quieres añadirlo a la lista de la compra?")
                .setPositiveButton("Sí, añadir") { _, _ ->
                    CoroutineScope(Dispatchers.IO).launch {
                        database.alimentoDao().insertarCompra(CompraItem(nombre = alimento.nombre))
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@InventarioActivity, "Añadido a la compra 🛒", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .setNegativeButton("No", null)
                .show()
        }
    }
}