package com.jvmapp.panaderialautaro.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Switch
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.jvmapp.panaderialautaro.R
import com.jvmapp.panaderialautaro.data.AppDatabase
import com.jvmapp.panaderialautaro.data.Cliente
import com.jvmapp.panaderialautaro.data.TipoPrecio
import kotlinx.coroutines.launch

class FragmentCliente : Fragment(R.layout.fragment_clientes) {

    private val db by lazy { AppDatabase.getDatabase(requireContext()) }

    private lateinit var etNombre: EditText
    private lateinit var btnGuardar: Button
    private lateinit var spnPrecio: Spinner
    private lateinit var spnEditarCliente: Spinner
    private lateinit var spnPrecioEditar: Spinner
    private lateinit var btnEditarNombre: Button
    private lateinit var swActivo: Switch

    private var listaClientes: List<Cliente> = emptyList()
    private var listaTiposPrecio: List<TipoPrecio> = emptyList()
    private var actualizandoSwitch = false
    private var actualizandoSpinner = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etNombre = view.findViewById(R.id.et_nombre)
        btnGuardar = view.findViewById(R.id.btn_guardar_cliente)
        spnPrecio = view.findViewById(R.id.spn_precio)
        spnEditarCliente = view.findViewById(R.id.spn_editar_cliente)
        spnPrecioEditar = view.findViewById(R.id.spn_precio_editar)
        btnEditarNombre = view.findViewById(R.id.btn_cambiar_nombre)
        swActivo = view.findViewById(R.id.sw_activo)

        cargarDatos()

        btnGuardar.setOnClickListener { guardarCliente() }

        btnEditarNombre.setOnClickListener { editarNombreCliente() }

        // Al cambiar cliente en el spinner de editar, actualiza el switch y el tipo de precio
        spnEditarCliente.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (listaClientes.isEmpty() || actualizandoSpinner) return
                val cliente = listaClientes[position]

                actualizandoSwitch = true
                swActivo.isChecked = cliente.activo
                actualizandoSwitch = false

                // Preselecciona el tipo de precio del cliente
                val indexTipo = listaTiposPrecio.indexOfFirst { it.id == cliente.tipoPrecioId }
                if (indexTipo >= 0) {
                    actualizandoSpinner = true
                    spnPrecioEditar.setSelection(indexTipo)
                    actualizandoSpinner = false
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Al cambiar el tipo de precio del cliente editado, lo guarda
        spnPrecioEditar.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (listaClientes.isEmpty() || actualizandoSpinner) return
                val cliente = listaClientes[spnEditarCliente.selectedItemPosition]
                val tipoPrecio = listaTiposPrecio[position]
                if (cliente.tipoPrecioId != tipoPrecio.id) {
                    viewLifecycleOwner.lifecycleScope.launch {
                        db.clienteDao().actualizar(cliente.copy(tipoPrecioId = tipoPrecio.id))
                        cargarDatos()
                        Toast.makeText(requireContext(), "Precio actualizado", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        swActivo.setOnCheckedChangeListener { _, isChecked ->
            if (actualizandoSwitch) return@setOnCheckedChangeListener
            val cliente = listaClientes[spnEditarCliente.selectedItemPosition]
            viewLifecycleOwner.lifecycleScope.launch {
                db.clienteDao().actualizar(cliente.copy(activo = isChecked))
                cargarDatos()
            }
        }
    }

    private fun cargarDatos() {
        viewLifecycleOwner.lifecycleScope.launch {
            // Carga tipos de precio en ambos spinners
            listaTiposPrecio = db.tipoPrecioDao().obtenerTodos()
            val nombresTipo = listaTiposPrecio.map { it.nombre }
            val adapterTipo = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, nombresTipo)
            adapterTipo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spnPrecio.adapter = adapterTipo

            val adapterTipoEditar = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, nombresTipo)
            adapterTipoEditar.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spnPrecioEditar.adapter = adapterTipoEditar

            // Carga clientes
            listaClientes = db.clienteDao().obtenerTodos()
            val nombres = listaClientes.map {
                "${it.nombre} (${if (it.activo) "Activo" else "Inactivo"})"
            }
            val adapterClientes = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, nombres)
            adapterClientes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            actualizandoSpinner = true
            spnEditarCliente.adapter = adapterClientes
            actualizandoSpinner = false

            // Preselecciona tipo de precio del primer cliente
            if (listaClientes.isNotEmpty()) {
                actualizandoSwitch = true
                swActivo.isChecked = listaClientes[0].activo
                actualizandoSwitch = false

                val indexTipo = listaTiposPrecio.indexOfFirst { it.id == listaClientes[0].tipoPrecioId }
                if (indexTipo >= 0) {
                    actualizandoSpinner = true
                    spnPrecioEditar.setSelection(indexTipo)
                    actualizandoSpinner = false
                }
            }
        }
    }

    private fun guardarCliente() {
        val nombre = etNombre.text.toString().trim()
        if (nombre.isEmpty()) {
            Toast.makeText(requireContext(), "Ingrese un nombre", Toast.LENGTH_SHORT).show()
            return
        }

        val tipoPrecioSeleccionado = listaTiposPrecio[spnPrecio.selectedItemPosition]

        viewLifecycleOwner.lifecycleScope.launch {
            db.clienteDao().insertar(
                Cliente(
                    nombre = nombre,
                    tipoPrecioId = tipoPrecioSeleccionado.id,
                    activo = true
                )
            )
            Toast.makeText(requireContext(), "Cliente guardado", Toast.LENGTH_SHORT).show()
            etNombre.text.clear()
            cargarDatos()
        }
    }

    private fun editarNombreCliente() {
        if (listaClientes.isEmpty()) return
        val cliente = listaClientes[spnEditarCliente.selectedItemPosition]
        val editText = EditText(requireContext())
        editText.setText(cliente.nombre)

        AlertDialog.Builder(requireContext())
            .setTitle("Editar nombre")
            .setView(editText)
            .setPositiveButton("Guardar") { _, _ ->
                val nuevoNombre = editText.text.toString().trim()
                if (nuevoNombre.isEmpty()) return@setPositiveButton
                viewLifecycleOwner.lifecycleScope.launch {
                    db.clienteDao().actualizar(cliente.copy(nombre = nuevoNombre))
                    cargarDatos()
                    Toast.makeText(requireContext(), "Nombre actualizado", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}