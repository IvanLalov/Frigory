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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.Calendar

class BuscarAlimentoActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_buscar_alimento)
        database = AppDatabase.getDatabase(this)

        val etBuscador = findViewById<EditText>(R.id.etBuscador)
        val rv = findViewById<RecyclerView>(R.id.rvResultados)
        rv.layoutManager = LinearLayoutManager(this)

        findViewById<Button>(R.id.btnBuscarAPI).setOnClickListener {
            val texto = etBuscador.text.toString().trim()
            if (verificarConexion()) {
                if (texto.isNotEmpty()) ejecutarBusqueda(texto, rv)
            } else {
                Toast.makeText(this, "Sin conexión a internet", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun verificarConexion(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun ejecutarBusqueda(termino: String, rv: RecyclerView) {
        lifecycleScope.launch {
            try {
                val respuesta = RetrofitClient.instance.buscarAlimento(termino)
                val lista = respuesta.productos

                if (lista.isNullOrEmpty()) {
                    Toast.makeText(this@BuscarAlimentoActivity, "Sin resultados", Toast.LENGTH_SHORT).show()
                } else {
                    rv.adapter = ResultadosAdapter(lista) { productoSeleccionado ->
                        mostrarDialogoConfirmacion(productoSeleccionado)
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@BuscarAlimentoActivity, "Error de red", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mostrarDialogoConfirmacion(prod: ProductoAPI) {
        val nombreProd = prod.nombre ?: "Producto"
        val fechaSugerida = calcularFechaSugerida(nombreProd)

        val view = layoutInflater.inflate(R.layout.dialog_confirmacion_alimento, null)
        val dialog = android.app.AlertDialog.Builder(this).setView(view).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        view.findViewById<TextView>(R.id.tvTituloDialogo).text = "¿Añadir $nombreProd?"
        view.findViewById<TextView>(R.id.tvMensajeDialogo).text = "Sugerencia de caducidad: $fechaSugerida"

        view.findViewById<Button>(R.id.btnSi).setOnClickListener {
            guardarAlimento(prod, fechaSugerida)
            dialog.dismiss()
        }

        view.findViewById<Button>(R.id.btnManual).setOnClickListener {
            dialog.dismiss()
            val cal = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                val fechaManual = "${String.format("%02d", day)}/${String.format("%02d", month + 1)}/$year"
                guardarAlimento(prod, fechaManual)
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        view.findViewById<Button>(R.id.btnNo).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun calcularFechaSugerida(nombre: String): String {
        val n = nombre.lowercase()
        val dias = when {
            n.contains("leche") -> 7
            n.contains("huevo") -> 21
            n.contains("yogur") -> 15
            n.contains("carne") || n.contains("pollo") -> 3
            n.contains("pescado") -> 2
            else -> 30
        }
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, dias)
        return "${String.format("%02d", cal.get(Calendar.DAY_OF_MONTH))}/${String.format("%02d", cal.get(Calendar.MONTH) + 1)}/${cal.get(Calendar.YEAR)}"
    }

    private fun guardarAlimento(prod: ProductoAPI, fecha: String) {
        lifecycleScope.launch {
            database.alimentoDao().insertar(Alimento(
                nombre = prod.nombre ?: "Desconocido",
                cantidad = 1,
                fechaCaducidad = fecha
            ))
            finish()
        }
    }
}