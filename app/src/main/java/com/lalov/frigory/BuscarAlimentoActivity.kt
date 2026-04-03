package com.lalov.frigory

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.lalov.frigory.R

class BuscarAlimentoActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buscar_alimento)

        database = AppDatabase.getDatabase(this)

        val etBuscador = findViewById<EditText>(R.id.etBuscador)
        val btnBuscar = findViewById<Button>(R.id.btnBuscarAPI)
        val rv = findViewById<RecyclerView>(R.id.rvResultadosAPI)

        rv.layoutManager = LinearLayoutManager(this)

        btnBuscar.setOnClickListener {
            val texto = etBuscador.text.toString()
            if (texto.isNotEmpty()) {
                buscarEnInternet(texto, rv)
            }
        }
    }

    private fun buscarEnInternet(termino: String, rv: RecyclerView) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val respuesta = RetrofitClient.instance.buscarAlimento(termino)
                val lista = respuesta.productos

                withContext(Dispatchers.Main) {
                    rv.adapter = ResultadosAdapter(lista) { productoSeleccionado ->
                        guardarEnBaseDeDatos(productoSeleccionado)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@BuscarAlimentoActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun guardarEnBaseDeDatos(prod: ProductoAPI) {
        CoroutineScope(Dispatchers.IO).launch {
            val nuevo = Alimento(
                nombre = prod.nombre ?: "Desconocido",
                cantidad = 1,
                fechaCaducidad = "Sin fecha"
            )
            database.alimentoDao().insertar(nuevo)
            withContext(Dispatchers.Main) {
                Toast.makeText(this@BuscarAlimentoActivity, "¡Guardado en la nevera!", Toast.LENGTH_SHORT).show()
                finish() // Volvemos atrás automáticamente
            }
        }
    }
}