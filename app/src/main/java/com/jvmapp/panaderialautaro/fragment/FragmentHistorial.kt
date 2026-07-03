package com.jvmapp.panaderialautaro.fragment

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jvmapp.panaderialautaro.R
import com.jvmapp.panaderialautaro.adapter.VentaAdapter
import com.jvmapp.panaderialautaro.data.AppDatabase
import com.jvmapp.panaderialautaro.data.Cliente
import kotlinx.coroutines.launch

class FragmentHistorial : Fragment(R.layout.fragment_historial) {

    private val db by lazy { AppDatabase.getDatabase(requireContext()) }

    private lateinit var rvVentas: RecyclerView
    private lateinit var spClientes: Spinner
    private var listaClientes: List<Cliente> = emptyList()
    private lateinit var adapter: VentaAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvVentas = view.findViewById(R.id.rvVentas)
        spClientes = view.findViewById(R.id.spn_clientes_historial)

        adapter = VentaAdapter(emptyList()) { venta ->
            // Al confirmar eliminar
            viewLifecycleOwner.lifecycleScope.launch {
                db.ventaDao().eliminar(venta.id)
                if (listaClientes.isNotEmpty()) {
                    cargarVentasPorCliente(listaClientes[spClientes.selectedItemPosition].id)
                }
            }
        }

        rvVentas.layoutManager = LinearLayoutManager(requireContext())
        rvVentas.adapter = adapter

        cargarClientes()

        spClientes.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (listaClientes.isNotEmpty()) {
                    cargarVentasPorCliente(listaClientes[position].id)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun cargarClientes() {
        viewLifecycleOwner.lifecycleScope.launch {
            listaClientes = db.clienteDao().obtenerTodos()
            val nombres = listaClientes.map { it.nombre }
            val spinnerAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                nombres
            )
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spClientes.adapter = spinnerAdapter

            if (listaClientes.isNotEmpty()) {
                cargarVentasPorCliente(listaClientes[0].id)
            }
        }
    }

    private fun cargarVentasPorCliente(clienteId: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            val lista = db.ventaDao().obtenerVentasConClientePorId(clienteId)
            adapter.actualizarLista(lista)
        }
    }
}