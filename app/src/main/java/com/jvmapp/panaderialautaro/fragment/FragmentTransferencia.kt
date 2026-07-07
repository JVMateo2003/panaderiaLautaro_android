package com.jvmapp.panaderialautaro.fragment

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.jvmapp.panaderialautaro.data.Pago
import kotlinx.coroutines.launch

class FragmentTransferencia : Fragment(R.layout.fragment_transferencia) {

    private val db by lazy { AppDatabase.getDatabase(requireContext()) }

    private lateinit var spnCliente: Spinner
    private lateinit var tvDeudaActual: TextView
    private lateinit var etMonto: EditText
    private lateinit var tvDeudaRestante: TextView
    private lateinit var btnConfirmar: Button

    private var listaClientes: List<Cliente> = emptyList()
    private var deudaActual = 0.0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        spnCliente = view.findViewById(R.id.spn_cliente_transferencia)
        tvDeudaActual = view.findViewById(R.id.tv_deuda_actual_transferencia)
        etMonto = view.findViewById(R.id.et_monto_transferencia)
        tvDeudaRestante = view.findViewById(R.id.tv_deuda_restante)
        btnConfirmar = view.findViewById(R.id.btn_confirmar_transferencia)

        cargarClientes()

        spnCliente.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (listaClientes.isNotEmpty()) {
                    calcularDeuda(listaClientes[position].id)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        etMonto.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { actualizarDeudaRestante() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        btnConfirmar.setOnClickListener { confirmarTransferencia() }
    }

    private fun cargarClientes() {
        viewLifecycleOwner.lifecycleScope.launch {
            listaClientes = db.clienteDao().obtenerClientesActivos()
            val nombres = listaClientes.map { it.nombre }
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, nombres)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spnCliente.adapter = adapter

            if (listaClientes.isNotEmpty()) {
                calcularDeuda(listaClientes[0].id)
            }
        }
    }

    private fun calcularDeuda(clienteId: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            val ventas = db.ventaDao().obtenerVentasDeCliente(clienteId)
            val pagos = db.pagoDao().obtenerPagosDeCliente(clienteId)

            val saldoVentas = ventas.sumOf { it.diferencia }
            val totalTransferencias = pagos.filter { it.tipo == "transferencia" }.sumOf { it.monto }
            val saldoReal = saldoVentas + totalTransferencias

            deudaActual = if (saldoReal < 0) -saldoReal else 0.0

            when {
                saldoReal < 0 -> {
                    tvDeudaActual.text = "Deuda: $${"%.2f".format(-saldoReal)}"
                    tvDeudaActual.setTextColor(Color.RED)
                }
                saldoReal > 0 -> {
                    tvDeudaActual.text = "Saldo a favor: $${"%.2f".format(saldoReal)}"
                    tvDeudaActual.setTextColor(Color.GREEN)
                }
                else -> {
                    tvDeudaActual.text = "Sin deuda"
                    tvDeudaActual.setTextColor(Color.GREEN)
                }
            }

            etMonto.text.clear()
            actualizarDeudaRestante()
        }
    }

    private fun actualizarDeudaRestante() {
        val monto = etMonto.text.toString().toDoubleOrNull() ?: 0.0
        val restante = (deudaActual - monto).coerceAtLeast(0.0)
        tvDeudaRestante.text = "Deuda restante: $${"%.2f".format(restante)}"
        tvDeudaRestante.setTextColor(if (restante > 0) Color.RED else Color.GREEN)
    }

    private fun confirmarTransferencia() {
        val monto = etMonto.text.toString().toDoubleOrNull()

        if (monto == null || monto <= 0) {
            Toast.makeText(requireContext(), "Ingrese un monto válido", Toast.LENGTH_SHORT).show()
            return
        }

        if (listaClientes.isEmpty()) return
        val cliente = listaClientes[spnCliente.selectedItemPosition]

        viewLifecycleOwner.lifecycleScope.launch {
            db.pagoDao().insertar(
                Pago(
                    clienteId = cliente.id,
                    fecha = System.currentTimeMillis(),
                    monto = monto,
                    tipo = "transferencia"
                )
            )
            Toast.makeText(requireContext(), "Transferencia registrada", Toast.LENGTH_SHORT).show()
            calcularDeuda(cliente.id)
        }
    }
}