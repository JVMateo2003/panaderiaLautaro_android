package com.jvmapp.panaderialautaro

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jvmapp.panaderialautaro.data.VentaConCliente
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.Color

class VentaAdapter(
    private var lista: List<VentaConCliente>
) : RecyclerView.Adapter<VentaAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCliente: TextView = view.findViewById(R.id.tvCliente)
        val tvFecha: TextView = view.findViewById(R.id.tvFecha)
        val tvTotal: TextView = view.findViewById(R.id.tvTotal)
        val tvEstado: TextView = view.findViewById(R.id.tvEstado)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_venta, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = lista.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val venta = lista[position]

        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val fecha = sdf.format(Date(venta.fecha))

        holder.tvCliente.text = venta.clienteNombre
        holder.tvFecha.text = "Fecha: $fecha"
        holder.tvTotal.text = "Total: ${venta.total}"

        if (venta.diferencia >= 0) {
            holder.tvEstado.text = "Pagado"
            holder.tvEstado.setTextColor(Color.GREEN)
        } else {
            holder.tvEstado.text = "Debe: ${-venta.diferencia}"
            holder.tvEstado.setTextColor(Color.RED)
        }
    }

    fun actualizarLista(nuevaLista: List<VentaConCliente>) {
        lista = nuevaLista
        notifyDataSetChanged()
    }
}