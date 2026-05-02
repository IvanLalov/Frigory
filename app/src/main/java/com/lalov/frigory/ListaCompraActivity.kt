package com.lalov.frigory

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class ListaCompraActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private lateinit var adapter: CompraAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_lista_compra)
        database = AppDatabase.getDatabase(this)

        val etNuevo = findViewById<EditText>(R.id.etNuevoItemCompra)
        val rv = findViewById<RecyclerView>(R.id.rvListaCompra)
        rv.layoutManager = LinearLayoutManager(this)

        adapter = CompraAdapter(mutableListOf(),
            onCheckChanged = { item -> actualizarItem(item) },
            onDelete = { item -> borrarItem(item) }
        )
        rv.adapter = adapter

        findViewById<Button>(R.id.btnAnadirCompra).setOnClickListener {
            val nombre = etNuevo.text.toString().trim()
            if (nombre.isNotEmpty()) {
                insertarItem(nombre)
                etNuevo.text.clear()
            }
        }

        cargarLista()
    }

    private fun cargarLista() {
        lifecycleScope.launch {
            val lista = database.alimentoDao().obtenerCompra()
            adapter.actualizar(lista)
        }
    }

    private fun insertarItem(nombre: String) {
        lifecycleScope.launch {
            database.alimentoDao().insertarCompra(CompraItem(nombre = nombre))
            cargarLista()
        }
    }

    private fun actualizarItem(item: CompraItem) {
        lifecycleScope.launch {
            database.alimentoDao().actualizarCompra(item)
            cargarLista()
        }
    }

    private fun borrarItem(item: CompraItem) {
        lifecycleScope.launch {
            database.alimentoDao().eliminarCompra(item)
            cargarLista()
        }
    }
}