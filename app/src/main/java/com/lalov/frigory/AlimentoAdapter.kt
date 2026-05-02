package com.lalov.frigory

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class AlimentoAdapter(
    private var listaAlimentos: List<Alimento>,
    private val onClick: (Alimento) -> Unit,
    private val onLongClick: (Alimento) -> Unit
) : RecyclerView.Adapter<AlimentoAdapter.AlimentoViewHolder>() {

    class AlimentoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreAlimento)
        val tvCantidad: TextView = view.findViewById(R.id.tvCantidadAlimento)
        val tvFecha: TextView = view.findViewById(R.id.tvFechaAlimento)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlimentoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_alimento, parent, false)
        return AlimentoViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlimentoViewHolder, position: Int) {
        val alimento = listaAlimentos[position]
        holder.tvNombre.text = alimento.nombre

        // Gestión visual de caducidad
        try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val fechaCaducidad = sdf.parse(alimento.fechaCaducidad)

            val calHoy = Calendar.getInstance()
            calHoy.set(Calendar.HOUR_OF_DAY, 0)
            calHoy.set(Calendar.MINUTE, 0)
            calHoy.set(Calendar.SECOND, 0)
            calHoy.set(Calendar.MILLISECOND, 0)
            val hoy = calHoy.time

            if (fechaCaducidad != null) {
                val diferencia = fechaCaducidad.time - hoy.time
                val diasRestantes = (diferencia / (1000 * 60 * 60 * 24)).toInt()

                when {
                    diasRestantes < 0 -> {
                        holder.tvFecha.text = "CADUCADO (${alimento.fechaCaducidad})"
                        holder.tvFecha.setTextColor(Color.RED)
                    }
                    diasRestantes <= 3 -> {
                        holder.tvFecha.text = "Caduca pronto: ${alimento.fechaCaducidad}"
                        holder.tvFecha.setTextColor(Color.parseColor("#FF9800"))
                    }
                    else -> {
                        holder.tvFecha.text = "Caduca: ${alimento.fechaCaducidad}"
                        holder.tvFecha.setTextColor(Color.parseColor("#757575"))
                    }
                }
            }
        } catch (e: Exception) {
            holder.tvFecha.text = "Sin fecha de caducidad"
            holder.tvFecha.setTextColor(Color.GRAY)
        }

        // Gestión visual de stock
        if (alimento.cantidad == 0) {
            holder.tvCantidad.text = "AGOTADO"
            holder.tvCantidad.setTextColor(Color.RED)
            holder.itemView.setBackgroundColor(Color.parseColor("#FFF0F0"))
        } else {
            holder.tvCantidad.text = "Cant: ${alimento.cantidad}"
            holder.tvCantidad.setTextColor(Color.BLACK)
            holder.itemView.setBackgroundColor(Color.WHITE)
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