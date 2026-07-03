package com.jvmapp.panaderialautaro.adapter

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jvmapp.panaderialautaro.R
import com.jvmapp.panaderialautaro.data.VentaConCliente
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class VentaAdapter(
    private var lista: List<VentaConCliente>,
    private val onEliminar: (VentaConCliente) -> Unit
) : RecyclerView.Adapter<VentaAdapter.VentaViewHolder>() {

    class VentaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tv_item_nombre)
        val tvFecha: TextView = view.findViewById(R.id.tv_item_fecha)
        val tvTotal: TextView = view.findViewById(R.id.tv_item_total)
        val tvPagado: TextView = view.findViewById(R.id.tv_item_pagado)
        val tvDiferencia: TextView = view.findViewById(R.id.tv_item_diferencia)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VentaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_venta, parent, false)
        return VentaViewHolder(view)
    }

    override fun onBindViewHolder(holder: VentaViewHolder, position: Int) {
        val venta = lista[position]
        val fecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(venta.fecha))

        holder.tvNombre.text = venta.clienteNombre
        holder.tvFecha.text = fecha
        holder.tvTotal.text = "Total: ${"%.2f".format(venta.total)}"
        holder.tvPagado.text = "Pagado: ${"%.2f".format(venta.pagado)}"

        val diff = venta.diferencia
        if (diff < 0) {
            holder.tvDiferencia.text = "Deuda: ${"%.2f".format(-diff)}"
            holder.tvDiferencia.setTextColor(0xFFE53935.toInt())
        } else {
            holder.tvDiferencia.text = "Vuelto: ${"%.2f".format(diff)}"
            holder.tvDiferencia.setTextColor(0xFF43A047.toInt())
        }

        // Mantener presionado para eliminar
        holder.itemView.setOnLongClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Eliminar venta")
                .setMessage("¿Seguro que querés eliminar la venta de ${venta.clienteNombre}?")
                .setPositiveButton("Eliminar") { _, _ -> onEliminar(venta) }
                .setNegativeButton("Cancelar", null)
                .show()
            true
        }
    }

    override fun getItemCount() = lista.size

    fun actualizarLista(nuevaLista: List<VentaConCliente>) {
        lista = nuevaLista
        notifyDataSetChanged()
    }
}