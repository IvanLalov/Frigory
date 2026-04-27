package com.lalov.frigory

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class BuscarAlimentoActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // SEGURIDAD: Verificación de sesión de usuario
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_buscar_alimento)

        database = AppDatabase.getDatabase(this)

        val etBuscador = findViewById<EditText>(R.id.etBuscador)
        val btnBuscar = findViewById<Button>(R.id.btnBuscarAPI)
        val rv = findViewById<RecyclerView>(R.id.rvResultados)

        rv.layoutManager = LinearLayoutManager(this)

        btnBuscar.setOnClickListener {
            val texto = etBuscador.text.toString().trim()

            // CONTROL DE CONECTIVIDAD: Antes de llamar a la API
            if (!tieneInternet()) {
                Toast.makeText(this, "No hay conexión a internet 🌐", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (texto.isNotEmpty()) {
                buscarEnInternet(texto, rv)
            } else {
                Toast.makeText(this, "Escribe algo para buscar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // --- FUNCIÓN DE ESCUDO: Detecta si el móvil tiene datos o Wi-Fi activo ---
    private fun tieneInternet(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun buscarEnInternet(termino: String, rv: RecyclerView) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val respuesta = RetrofitClient.instance.buscarAlimento(termino)
                val lista = respuesta.productos

                withContext(Dispatchers.Main) {
                    if (lista.isNullOrEmpty()) {
                        Toast.makeText(this@BuscarAlimentoActivity, "No se encontraron productos", Toast.LENGTH_SHORT).show()
                    } else {
                        rv.adapter = ResultadosAdapter(lista) { productoSeleccionado ->
                            mostrarConfirmacionYFecha(productoSeleccionado)
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@BuscarAlimentoActivity, "Error al conectar con el servidor", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun mostrarConfirmacionYFecha(prod: ProductoAPI) {
        val nombreProd = prod.nombre ?: "Producto"
        val diasSugeridos = obtenerDiasSugeridos(nombreProd)

        val calSugerida = Calendar.getInstance()
        calSugerida.add(Calendar.DAY_OF_YEAR, diasSugeridos)
        val fechaSugeridaStr = "${String.format("%02d", calSugerida.get(Calendar.DAY_OF_MONTH))}/${String.format("%02d", calSugerida.get(Calendar.MONTH) + 1)}/${calSugerida.get(Calendar.YEAR)}"

        val dialogView = layoutInflater.inflate(R.layout.dialog_confirmacion_alimento, null)
        val builder = android.app.AlertDialog.Builder(this)
        builder.setView(dialogView)

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val tvTitulo = dialogView.findViewById<TextView>(R.id.tvTituloDialogo)
        val tvMensaje = dialogView.findViewById<TextView>(R.id.tvMensajeDialogo)
        val btnNo = dialogView.findViewById<Button>(R.id.btnNo)
        val btnSi = dialogView.findViewById<Button>(R.id.btnSi)
        val btnManual = dialogView.findViewById<Button>(R.id.btnManual)

        tvTitulo.text = "¿Añadir $nombreProd?"
        tvMensaje.text = "Frigory sugiere que caducará el $fechaSugeridaStr. ¿Deseas usar esta fecha?"

        btnNo.setOnClickListener { dialog.dismiss() }

        btnSi.setOnClickListener {
            guardarRealEnBaseDeDatos(prod, fechaSugeridaStr)
            dialog.dismiss()
        }

        btnManual.setOnClickListener {
            dialog.dismiss()
            val cal = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                val fechaManual = "${String.format("%02d", day)}/${String.format("%02d", month + 1)}/$year"
                guardarRealEnBaseDeDatos(prod, fechaManual)
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        dialog.show()
    }

    private fun obtenerDiasSugeridos(nombre: String): Int {
        val n = nombre.lowercase()
        return when {
            n.contains("leche") -> 7
            n.contains("huevo") -> 21
            n.contains("yogur") -> 15
            n.contains("carne") || n.contains("pollo") || n.contains("filete") -> 3
            n.contains("pescado") -> 2
            n.contains("fruta") || n.contains("manzana") || n.contains("platano") -> 10
            n.contains("pan") -> 5
            else -> 30
        }
    }

    private fun guardarRealEnBaseDeDatos(prod: ProductoAPI, fecha: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val nuevo = Alimento(
                nombre = prod.nombre ?: "Desconocido",
                cantidad = 1,
                fechaCaducidad = fecha
            )
            database.alimentoDao().insertar(nuevo)
            withContext(Dispatchers.Main) {
                Toast.makeText(this@BuscarAlimentoActivity, "¡${prod.nombre} añadido!", Toast.LENGTH_SHORT).show()
                finish() // Cerramos la búsqueda al terminar
            }
        }
    }
}