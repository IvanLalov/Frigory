package com.lalov.frigory

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ResultadosAdapter(
    private val productos: List<ProductoAPI>,
    private val onProductoClick: (ProductoAPI) -> Unit
) : RecyclerView.Adapter<ResultadosAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nombre: TextView = view.findViewById(R.id.tvNombreAlimento)
        val marca: TextView = view.findViewById(R.id.tvCantidadAlimento)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_alimento, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val prod = productos[position]
        holder.nombre.text = prod.nombre ?: "Sin nombre"
        holder.marca.text = prod.marca ?: "Marca desconocida"

        holder.itemView.setOnClickListener { onProductoClick(prod) }
    }

    override fun getItemCount() = productos.size
}