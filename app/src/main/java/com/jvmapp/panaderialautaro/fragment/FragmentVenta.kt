package com.jvmapp.panaderialautaro.fragment

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.jvmapp.panaderialautaro.R
import com.jvmapp.panaderialautaro.data.AppDatabase
import com.jvmapp.panaderialautaro.data.Cliente
import com.jvmapp.panaderialautaro.data.TipoPrecio
import com.jvmapp.panaderialautaro.data.Venta
import kotlinx.coroutines.launch

class FragmentVenta : Fragment(R.layout.fragment_venta) {

    private var precioVarilla = 0.0
    private var precioBollo = 0.0
    private var precioCriollo = 0.0

    private lateinit var etVarilla: EditText
    private lateinit var etBollo: EditText
    private lateinit var etCriollo: EditText
    private lateinit var etOtro: EditText
    private lateinit var etPago: EditText
    private lateinit var tvTotal: TextView
    private lateinit var tvDiferencia: TextView
    private lateinit var tvDeuda: TextView
    private lateinit var tvDeudaCliente: TextView
    private lateinit var btnConfirmar: Button
    private lateinit var btnPagoTotal: Button
    private lateinit var spClientes: Spinner
    private lateinit var spTipoPrecio: Spinner

    private val db by lazy { AppDatabase.getDatabase(requireContext()) }

    private var listaClientes: List<Cliente> = emptyList()
    private var listaTiposPrecio: List<TipoPrecio> = emptyList()

    // Flag para evitar loop entre los dos spinners
    private var actualizandoSpinner = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
        initListeners()
        cargarDatos()
    }

    private fun initView(view: View) {
        etVarilla = view.findViewById(R.id.et_varilla)
        etBollo = view.findViewById(R.id.et_bollo)
        etCriollo = view.findViewById(R.id.et_criollo)
        etOtro = view.findViewById(R.id.et_otro)
        etPago = view.findViewById(R.id.et_pago)
        tvTotal = view.findViewById(R.id.tv_total)
        tvDiferencia = view.findViewById(R.id.tv_diferencia)
        tvDeuda = view.findViewById(R.id.tv_deuda)
        tvDeudaCliente = view.findViewById(R.id.tv_deuda_cliente)
        btnConfirmar = view.findViewById(R.id.btn_calcular_venta)
        btnPagoTotal = view.findViewById(R.id.btn_pago_total)
        spClientes = view.findViewById(R.id.sp_clientes)
        spTipoPrecio = view.findViewById(R.id.sp_tipo_precio)
    }

    private fun initListeners() {
        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { calcular() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        etVarilla.addTextChangedListener(watcher)
        etBollo.addTextChangedListener(watcher)
        etCriollo.addTextChangedListener(watcher)
        etOtro.addTextChangedListener(watcher)
        etPago.addTextChangedListener(watcher)

        btnConfirmar.setOnClickListener { guardarVenta() }
        btnPagoTotal.setOnClickListener { pagarTotal() }

        // Al cambiar cliente, preselecciona su tipo de precio
        spClientes.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (listaClientes.isNotEmpty() && !actualizandoSpinner) {
                    val cliente = listaClientes[position]
                    calcularDeudaCliente(cliente.id)
                    val indexTipo = listaTiposPrecio.indexOfFirst { it.id == cliente.tipoPrecioId }
                    if (indexTipo >= 0) {
                        actualizandoSpinner = true
                        spTipoPrecio.setSelection(indexTipo)
                        actualizandoSpinner = false
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Al cambiar tipo de precio, actualiza precios y recalcula
        spTipoPrecio.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (listaTiposPrecio.isNotEmpty()) {
                    val tipo = listaTiposPrecio[position]
                    precioVarilla = tipo.precioVarilla
                    precioBollo = tipo.precioBollo
                    precioCriollo = tipo.precioCriollo
                    calcular()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun evaluarExpresion(expresion: String): Double {
        return try {
            net.objecthunter.exp4j.ExpressionBuilder(expresion).build().evaluate()
        } catch (e: Exception) { 0.0 }
    }

    private fun calcular() {
        val varilla = evaluarExpresion(etVarilla.text.toString())
        val bollo = evaluarExpresion(etBollo.text.toString())
        val criollo = evaluarExpresion(etCriollo.text.toString())
        val otro = evaluarExpresion(etOtro.text.toString())
        val pago = evaluarExpresion(etPago.text.toString())

        val total = (varilla * precioVarilla) + (bollo * precioBollo) + (criollo * precioCriollo) + otro
        tvTotal.text = "Total: ${"%.2f".format(total)}"

        val diferencia = pago - total
        if (diferencia >= 0) {
            tvDiferencia.text = "Vuelto: ${"%.2f".format(diferencia)}"
            tvDeuda.text = "Deuda: 0"
        } else {
            tvDiferencia.text = "Vuelto: 0"
            tvDeuda.text = "Debe: ${"%.2f".format(-diferencia)}"
        }
    }

    private fun guardarVenta() {
        val varilla = evaluarExpresion(etVarilla.text.toString())
        val bollo = evaluarExpresion(etBollo.text.toString())
        val criollo = evaluarExpresion(etCriollo.text.toString())
        val otro = evaluarExpresion(etOtro.text.toString())
        val pago = evaluarExpresion(etPago.text.toString())

        val total = (varilla * precioVarilla) + (bollo * precioBollo) + (criollo * precioCriollo) + otro
        val diferencia = pago - total

        viewLifecycleOwner.lifecycleScope.launch {
            if (listaClientes.isEmpty()) {
                Toast.makeText(requireContext(), "No hay clientes", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val posicion = spClientes.selectedItemPosition
            if (posicion < 0) {
                Toast.makeText(requireContext(), "Seleccione un cliente", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val cliente = listaClientes[posicion]
            val tipoPrecio = listaTiposPrecio[spTipoPrecio.selectedItemPosition]

            Log.d("VENTA_DEBUG", "Cliente: ${cliente.nombre} - Precio: ${tipoPrecio.nombre}")

            db.ventaDao().insertar(
                Venta(
                    clienteId = cliente.id,
                    tipoPrecioId = tipoPrecio.id,
                    fecha = System.currentTimeMillis(),
                    kgVarilla = varilla,
                    kgBollo = bollo,
                    kgCriollo = criollo,
                    otroMonto = otro,
                    total = total,
                    pagado = pago,
                    diferencia = diferencia
                )
            )

            Toast.makeText(requireContext(), "Venta guardada", Toast.LENGTH_SHORT).show()

            etVarilla.text.clear()
            etBollo.text.clear()
            etCriollo.text.clear()
            etOtro.text.clear()
            etPago.text.clear()

            calcular()
            calcularDeudaCliente(cliente.id)
        }
    }

    private fun cargarDatos() {
        viewLifecycleOwner.lifecycleScope.launch {
            listaTiposPrecio = db.tipoPrecioDao().obtenerTodos()
            val nombresTipo = listaTiposPrecio.map { it.nombre }
            val adapterTipo = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, nombresTipo)
            adapterTipo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spTipoPrecio.adapter = adapterTipo

            listaClientes = db.clienteDao().obtenerClientesActivos()
            val nombres = listaClientes.map { it.nombre }
            val adapterClientes = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, nombres)
            adapterClientes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spClientes.adapter = adapterClientes

            if (listaClientes.isNotEmpty()) {
                calcularDeudaCliente(listaClientes[0].id)
            }
        }
    }

    private fun calcularDeudaCliente(clienteId: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            val ventas = db.ventaDao().obtenerVentasDeCliente(clienteId)
            val pagos = db.pagoDao().obtenerPagosDeCliente(clienteId)

            val deudaVentas = ventas.sumOf { if (it.diferencia < 0) -it.diferencia else 0.0 }
            val totalPagos = pagos.filter { it.tipo == "transferencia" }.sumOf { it.monto }
            val deudaReal = deudaVentas - totalPagos

            tvDeudaCliente.text = "Deuda: ${"%.2f".format(if (deudaReal > 0) deudaReal else 0.0)}"
            tvDeudaCliente.setTextColor(if (deudaReal > 0) Color.RED else Color.GREEN)
        }
    }

    private fun pagarTotal() {
        val varilla = evaluarExpresion(etVarilla.text.toString())
        val bollo = evaluarExpresion(etBollo.text.toString())
        val criollo = evaluarExpresion(etCriollo.text.toString())
        val otro = evaluarExpresion(etOtro.text.toString())
        val total = (varilla * precioVarilla) + (bollo * precioBollo) + (criollo * precioCriollo) + otro
        etPago.setText(total.toString())
        calcular()
    }
}