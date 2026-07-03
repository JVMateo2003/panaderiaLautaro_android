package com.jvmapp.panaderialautaro.fragment

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.jvmapp.panaderialautaro.R
import com.jvmapp.panaderialautaro.data.AppDatabase
import com.jvmapp.panaderialautaro.data.TipoPrecio
import kotlinx.coroutines.launch

class FragmentPrecios : Fragment(R.layout.fragment_precios) {

    private val db by lazy { AppDatabase.getDatabase(requireContext()) }

    private lateinit var etNombrePrecio: EditText
    private lateinit var etPrecioVarillaBollo: EditText
    private lateinit var etPrecioCriollo: EditText
    private lateinit var btnGuardarNuevo: Button

    private lateinit var spnTiposPrecio: Spinner
    private lateinit var etEditarPrecioVarillaBollo: EditText
    private lateinit var etEditarPrecioCriollo: EditText
    private lateinit var btnActualizar: Button

    private var listaTiposPrecio: List<TipoPrecio> = emptyList()
    private var actualizandoSpinner = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etNombrePrecio = view.findViewById(R.id.et_nombre_precio)
        etPrecioVarillaBollo = view.findViewById(R.id.et_precio_varilla_bollo)
        etPrecioCriollo = view.findViewById(R.id.et_precio_criollo)
        btnGuardarNuevo = view.findViewById(R.id.btn_guardar_nuevo_precio)

        spnTiposPrecio = view.findViewById(R.id.spn_tipos_precio)
        etEditarPrecioVarillaBollo = view.findViewById(R.id.et_editar_precio_varilla_bollo)
        etEditarPrecioCriollo = view.findViewById(R.id.et_editar_precio_criollo)
        btnActualizar = view.findViewById(R.id.btn_actualizar_precio)

        cargarTiposPrecio()

        btnGuardarNuevo.setOnClickListener { guardarNuevoPrecio() }
        btnActualizar.setOnClickListener { actualizarPrecio() }

        spnTiposPrecio.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (actualizandoSpinner || listaTiposPrecio.isEmpty()) return
                val tipo = listaTiposPrecio[position]
                etEditarPrecioVarillaBollo.setText(tipo.precioVarilla.toString())
                etEditarPrecioCriollo.setText(tipo.precioCriollo.toString())
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun cargarTiposPrecio() {
        viewLifecycleOwner.lifecycleScope.launch {
            listaTiposPrecio = db.tipoPrecioDao().obtenerTodos()
            val nombres = listaTiposPrecio.map { it.nombre }
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, nombres)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            actualizandoSpinner = true
            spnTiposPrecio.adapter = adapter
            actualizandoSpinner = false

            // Precarga los precios del primer tipo
            if (listaTiposPrecio.isNotEmpty()) {
                etEditarPrecioVarillaBollo.setText(listaTiposPrecio[0].precioVarilla.toString())
                etEditarPrecioCriollo.setText(listaTiposPrecio[0].precioCriollo.toString())
            }
        }
    }

    private fun guardarNuevoPrecio() {
        val nombre = etNombrePrecio.text.toString().trim()
        val precioVB = etPrecioVarillaBollo.text.toString().toDoubleOrNull()
        val precioCriollo = etPrecioCriollo.text.toString().toDoubleOrNull()

        if (nombre.isEmpty()) {
            Toast.makeText(requireContext(), "Ingrese un nombre", Toast.LENGTH_SHORT).show()
            return
        }
        if (precioVB == null || precioCriollo == null) {
            Toast.makeText(requireContext(), "Ingrese precios válidos", Toast.LENGTH_SHORT).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            db.tipoPrecioDao().insertar(
                TipoPrecio(
                    nombre = nombre,
                    precioVarilla = precioVB,
                    precioBollo = precioVB,
                    precioCriollo = precioCriollo
                )
            )
            Toast.makeText(requireContext(), "Tipo de precio guardado", Toast.LENGTH_SHORT).show()
            etNombrePrecio.text.clear()
            etPrecioVarillaBollo.text.clear()
            etPrecioCriollo.text.clear()
            cargarTiposPrecio()
        }
    }

    private fun actualizarPrecio() {
        if (listaTiposPrecio.isEmpty()) return

        val tipo = listaTiposPrecio[spnTiposPrecio.selectedItemPosition]
        val precioVB = etEditarPrecioVarillaBollo.text.toString().toDoubleOrNull()
        val precioCriollo = etEditarPrecioCriollo.text.toString().toDoubleOrNull()

        if (precioVB == null || precioCriollo == null) {
            Toast.makeText(requireContext(), "Ingrese precios válidos", Toast.LENGTH_SHORT).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            db.tipoPrecioDao().actualizar(
                tipo.copy(
                    precioVarilla = precioVB,
                    precioBollo = precioVB,
                    precioCriollo = precioCriollo
                )
            )
            Toast.makeText(requireContext(), "Precio actualizado", Toast.LENGTH_SHORT).show()
            cargarTiposPrecio()
        }
    }
}