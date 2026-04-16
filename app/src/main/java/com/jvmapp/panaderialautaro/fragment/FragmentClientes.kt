package com.jvmapp.panaderialautaro.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Switch
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.jvmapp.panaderialautaro.R
import com.jvmapp.panaderialautaro.data.AppDatabase
import com.jvmapp.panaderialautaro.data.Cliente
import kotlinx.coroutines.launch

class FragmentCliente : Fragment(R.layout.fragment_clientes) {

    private val db by lazy {
        AppDatabase.getDatabase(requireContext())
    }

    private lateinit var etNombre: EditText
    private lateinit var btnGuardar: Button
    private lateinit var spEditarCliente: Spinner
    private lateinit var btnEditarNombre: Button
    private lateinit var swActivo: Switch

    private var listaClientes: List<Cliente> = emptyList()
    private var actualizandoSwitch = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etNombre = view.findViewById(R.id.et_nombre)
        btnGuardar = view.findViewById(R.id.btn_guardar_cliente)
        spEditarCliente = view.findViewById(R.id.spn_editar_cliente)
        btnEditarNombre = view.findViewById(R.id.btn_cambiar_nombre)
        swActivo = view.findViewById(R.id.sw_activo)

        btnGuardar.setOnClickListener {
            guardarCliente()
        }

        btnEditarNombre.setOnClickListener {

            val cliente = listaClientes[spEditarCliente.selectedItemPosition]

            val editText = EditText(requireContext())
            editText.setText(cliente.nombre)

            AlertDialog.Builder(requireContext())
                .setTitle("Editar cliente")
                .setView(editText)
                .setPositiveButton("Guardar") { _, _ ->

                    val nuevoNombre = editText.text.toString()

                    viewLifecycleOwner.lifecycleScope.launch {
                        val actualizado = cliente.copy(nombre = nuevoNombre)
                        db.clienteDao().actualizar(actualizado)
                        cargarClientes()
                    }
                }
                .show()
        }

        spEditarCliente.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                val cliente = listaClientes[position]

                actualizandoSwitch = true
                swActivo.isChecked = cliente.activo
                actualizandoSwitch = false
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        swActivo.setOnCheckedChangeListener { _, isChecked ->

            if (actualizandoSwitch) return@setOnCheckedChangeListener

            val cliente = listaClientes[spEditarCliente.selectedItemPosition]

            viewLifecycleOwner.lifecycleScope.launch {
                val actualizado = cliente.copy(activo = isChecked)
                db.clienteDao().actualizar(actualizado)
                cargarClientes()
            }
        }

        cargarClientes()

    }

    private fun guardarCliente() {

        val nombre = etNombre.text.toString().trim()

        if (nombre.isEmpty()) {
            Toast.makeText(requireContext(), "Ingrese un nombre", Toast.LENGTH_SHORT).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {

            db.clienteDao().insertar(
                Cliente(
                    nombre = nombre,
                    activo = true,
                    tipoPrecio = "normal")
            )
            val clientes = db.clienteDao().obtenerClientesActivos()
            Log.d("CLIENTES", clientes.toString())

            Toast.makeText(requireContext(), "Cliente guardado", Toast.LENGTH_SHORT).show()
            etNombre.text.clear()

            cargarClientes()
        }
    }

    private fun cargarClientes() {
        viewLifecycleOwner.lifecycleScope.launch {

            listaClientes = db.clienteDao().obtenerTodos()

            val nombres = listaClientes.map {
                "${it.nombre} (${if (it.activo) "Activo" else "Inactivo"})"
            }

            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                nombres
            )

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spEditarCliente.adapter = adapter

            if (listaClientes.isNotEmpty()) {
                actualizandoSwitch = true
                swActivo.isChecked = listaClientes[0].activo
                actualizandoSwitch = false
            }
        }
    }
}