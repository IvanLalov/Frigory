package com.lalov.frigory

import android.graphics.Paint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CompraAdapter(
    private var items: List<CompraItem>,
    private val onCheckChanged: (CompraItem) -> Unit,
    private val onDelete: (CompraItem) -> Unit
) : RecyclerView.Adapter<CompraAdapter.CompraViewHolder>() {

    class CompraViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cb: CheckBox = view.findViewById(R.id.cbComprado)
        val tv: TextView = view.findViewById(R.id.tvNombreItemCompra)
        val btnBorrar: ImageButton = view.findViewById(R.id.btnBorrarCompra)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompraViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_compra, parent, false)
        return CompraViewHolder(view)
    }

    override fun onBindViewHolder(holder: CompraViewHolder, position: Int) {
        val item = items[position]

        holder.cb.setOnCheckedChangeListener(null)
        holder.tv.text = item.nombre
        holder.cb.isChecked = item.marcado

        actualizarEstiloTexto(holder.tv, item.marcado)

        holder.cb.setOnCheckedChangeListener { _, isChecked ->
            item.marcado = isChecked
            actualizarEstiloTexto(holder.tv, isChecked)
            onCheckChanged(item)
        }

        holder.btnBorrar.setOnClickListener { onDelete(item) }
    }

    private fun actualizarEstiloTexto(tv: TextView, marcado: Boolean) {
        if (marcado) {
            tv.paintFlags = tv.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            tv.setTextColor(Color.GRAY)
        } else {
            tv.paintFlags = tv.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            tv.setTextColor(Color.BLACK)
        }
    }

    override fun getItemCount() = items.size

    fun actualizar(nuevaLista: List<CompraItem>) {
        items = nuevaLista
        notifyDataSetChanged()
    }
}