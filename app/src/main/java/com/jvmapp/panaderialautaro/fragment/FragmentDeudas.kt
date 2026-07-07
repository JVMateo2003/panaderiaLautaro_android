package com.jvmapp.panaderialautaro.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jvmapp.panaderialautaro.R
import com.jvmapp.panaderialautaro.data.AppDatabase
import com.jvmapp.panaderialautaro.data.SaldoCliente
import kotlinx.coroutines.launch

class FragmentDeudas : Fragment(R.layout.fragment_deudas) {

    private val db by lazy { AppDatabase.getDatabase(requireContext()) }
    private lateinit var rvDeudas: RecyclerView
    private lateinit var adapter: SaldoAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvDeudas = view.findViewById(R.id.rv_deudas)
        adapter = SaldoAdapter(emptyList())
        rvDeudas.layoutManager = LinearLayoutManager(requireContext())
        rvDeudas.adapter = adapter

        cargarSaldos()
    }

    private fun cargarSaldos() {
        viewLifecycleOwner.lifecycleScope.launch {
            val saldosVentas = db.ventaDao().obtenerSaldoPorCliente()
            val pagos = db.pagoDao().obtenerTodosLosClientes()

            // Combina saldo de ventas con transferencias
            val saldosFinales = saldosVentas.map { saldo ->
                val transferencias = pagos
                    .filter { it.clienteId == saldo.clienteId && it.tipo == "transferencia" }
                    .sumOf { it.monto }
                saldo.copy(saldoVentas = saldo.saldoVentas + transferencias)
            }

            // Ordena: primero los que deben, después los que tienen saldo a favor
            val ordenados = saldosFinales.sortedBy { it.saldoVentas }
            adapter.actualizarLista(ordenados)
        }
    }

    inner class SaldoAdapter(private var lista: List<SaldoCliente>) :
        RecyclerView.Adapter<SaldoAdapter.SaldoViewHolder>() {

        inner class SaldoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvNombre: TextView = view.findViewById(R.id.tv_saldo_nombre)
            val tvMonto: TextView = view.findViewById(R.id.tv_saldo_monto)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SaldoViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_saldo, parent, false)
            return SaldoViewHolder(view)
        }

        override fun onBindViewHolder(holder: SaldoViewHolder, position: Int) {
            val saldo = lista[position]
            holder.tvNombre.text = saldo.clienteNombre

            when {
                saldo.saldoVentas < 0 -> {
                    holder.tvMonto.text = "Debe: $${"%.2f".format(-saldo.saldoVentas)}"
                    holder.tvMonto.setTextColor(Color.RED)
                }
                saldo.saldoVentas > 0 -> {
                    holder.tvMonto.text = "A favor: $${"%.2f".format(saldo.saldoVentas)}"
                    holder.tvMonto.setTextColor(Color.GREEN)
                }
                else -> {
                    holder.tvMonto.text = "Sin deuda"
                    holder.tvMonto.setTextColor(Color.WHITE)
                }
            }
        }

        override fun getItemCount() = lista.size

        fun actualizarLista(nuevaLista: List<SaldoCliente>) {
            lista = nuevaLista
            notifyDataSetChanged()
        }
    }
}