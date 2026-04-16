package com.jvmapp.panaderialautaro.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jvmapp.panaderialautaro.R
import com.jvmapp.panaderialautaro.VentaAdapter
import com.jvmapp.panaderialautaro.data.AppDatabase
import com.jvmapp.panaderialautaro.data.Cliente
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date

class FragmentHistorial : Fragment(R.layout.fragment_historial) {

    private val db by lazy {
        AppDatabase.Companion.getDatabase(requireContext())
    }

    private lateinit var rvVentas: RecyclerView
    private lateinit var spClientes: Spinner
    private var listaClientes: List<Cliente> = emptyList()
    private lateinit var adapter: VentaAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvVentas = view.findViewById(R.id.rvVentas)
        spClientes = view.findViewById(R.id.spn_clientes_historial)

        adapter = VentaAdapter(emptyList())
        rvVentas.layoutManager = LinearLayoutManager(requireContext())
        rvVentas.adapter = adapter

        cargarVentas()
        cargarClientes()

        spClientes.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val cliente = listaClientes[position]
                cargarVentasPorCliente(cliente.id)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}

        }
    }
    private fun cargarVentas() {
        viewLifecycleOwner.lifecycleScope.launch {

            val lista = db.ventaDao().obtenerVentaConCliente()

            lista.forEach {
                Log.d("HISTORIAL_DEBUG", "${it.clienteNombre} - ${it.total} - id:${it.clienteId}")
            }

            adapter.actualizarLista(lista)

        }
    }

    private fun cargarClientes() {
        viewLifecycleOwner.lifecycleScope.launch {
            listaClientes = db.clienteDao().obtenerClientesActivos()

            val nombres = listaClientes.map { it.nombre }

            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                nombres
            )

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_item)
            spClientes.adapter = adapter
        }
    }

    private fun cargarVentasPorCliente(clienteId: Int) {
        viewLifecycleOwner.lifecycleScope.launch {

            val lista = db.ventaDao().obtenerVentasConClientePorCliente(clienteId)
            adapter.actualizarLista(lista)

        }
    }

}