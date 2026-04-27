package com.lalov.frigory

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ListaCompraActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private lateinit var adapter: CompraAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // CONTROL DE SEGURIDAD: Si no hay usuario, cerramos y mandamos al Login
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_lista_compra)

        database = AppDatabase.getDatabase(this)

        val etNuevo = findViewById<EditText>(R.id.etNuevoItemCompra)
        val btnAdd = findViewById<Button>(R.id.btnAnadirCompra)
        val rv = findViewById<RecyclerView>(R.id.rvListaCompra)

        rv.layoutManager = LinearLayoutManager(this)

        // Inicializamos el adapter con una lista vacía y los callbacks
        adapter = CompraAdapter(mutableListOf(),
            onCheckChanged = { item -> actualizarItem(item) },
            onDelete = { item -> borrarItem(item) }
        )
        rv.adapter = adapter

        btnAdd.setOnClickListener {
            val nombre = etNuevo.text.toString().trim()

            if (nombre.isNotEmpty()) {
                insertarItem(nombre)
                etNuevo.text.clear()
            } else {

                Toast.makeText(this, "Por favor, escribe un producto", Toast.LENGTH_SHORT).show()
            }
        }

        cargarLista()
    }

    private fun cargarLista() {
        CoroutineScope(Dispatchers.IO).launch {
            val lista = database.alimentoDao().obtenerCompra()
            withContext(Dispatchers.Main) {
                // Actualizamos los datos del adapter de forma segura en el hilo principal
                adapter.actualizar(lista)
            }
        }
    }

    private fun insertarItem(nombre: String) {
        CoroutineScope(Dispatchers.IO).launch {
            database.alimentoDao().insertarCompra(CompraItem(nombre = nombre))
            withContext(Dispatchers.Main) {
                // Feedback visual de éxito
                Toast.makeText(this@ListaCompraActivity, "$nombre añadido", Toast.LENGTH_SHORT).show()
                cargarLista()
            }
        }
    }

    private fun actualizarItem(item: CompraItem) {
        CoroutineScope(Dispatchers.IO).launch {
            database.alimentoDao().actualizarCompra(item)

            cargarLista()
        }
    }

    private fun borrarItem(item: CompraItem) {
        CoroutineScope(Dispatchers.IO).launch {
            database.alimentoDao().eliminarCompra(item)
            withContext(Dispatchers.Main) {
                Toast.makeText(this@ListaCompraActivity, "Eliminado de la lista", Toast.LENGTH_SHORT).show()
                cargarLista()
            }
        }
    }
}