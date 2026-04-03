package com.lalov.frigory

import android.util.Log // Importante para que funcione el Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AlimentoAdapter(
    private var listaAlimentos: List<Alimento>,
    private val onClick: (Alimento) -> Unit,
    private val onLongClick: (Alimento) -> Unit
) : RecyclerView.Adapter<AlimentoAdapter.AlimentoViewHolder>() {

    class AlimentoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreAlimento)
        val tvCantidad: TextView = view.findViewById(R.id.tvCantidadAlimento)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlimentoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_alimento, parent, false)
        return AlimentoViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlimentoViewHolder, position: Int) {
        val alimento = listaAlimentos[position]
        holder.tvNombre.text = alimento.nombre

        if (alimento.cantidad == 0) {
            holder.tvCantidad.text = "¡AGOTADO!"
            holder.tvCantidad.setTextColor(android.graphics.Color.RED)
            // Opcional: fondo suave para que llame la atención
            holder.itemView.setBackgroundColor(android.graphics.Color.parseColor("#FFF0F0"))
        } else {
            holder.tvCantidad.text = "Cant: ${alimento.cantidad}"
            holder.tvCantidad.setTextColor(android.graphics.Color.BLACK)
            holder.itemView.setBackgroundColor(android.graphics.Color.WHITE)
        }

        holder.itemView.setOnClickListener { onClick(alimento) }
        holder.itemView.setOnLongClickListener {
            onLongClick(alimento)
            true
        }
    }

    override fun getItemCount() = listaAlimentos.size

    fun actualizarLista(nuevaLista: List<Alimento>) {
        listaAlimentos = nuevaLista
        notifyDataSetChanged()
    }
}