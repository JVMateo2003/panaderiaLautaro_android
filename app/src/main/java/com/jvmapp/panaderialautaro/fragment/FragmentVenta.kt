package com.jvmapp.panaderialautaro.fragment

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.jvmapp.panaderialautaro.R
import com.jvmapp.panaderialautaro.data.AppDatabase
import com.jvmapp.panaderialautaro.data.Cliente
import com.jvmapp.panaderialautaro.data.Venta
import kotlinx.coroutines.launch

class FragmentVenta : Fragment(R.layout.fragment_venta) {

    // precios (después los vamos a traer desde configuración)
    private val precioVarilla = 2350.0
    private val precioBollo = 2350.0
    private val precioCriollo = 4400.0

    private lateinit var etVarilla : EditText
    private lateinit var etBollo : EditText
    private lateinit var etCriollo : EditText
    private lateinit var etOtro : EditText
    private lateinit var etPago : EditText
    private lateinit var tvTotal : TextView
    private lateinit var tvDiferencia : TextView
    private lateinit var tvDeuda : TextView

    private lateinit var btnConfirmar : Button
    private val db by lazy {
        AppDatabase.getDatabase(requireContext())
    }

    private lateinit var spClientes : Spinner
    private var listaClientes: List<Cliente> = emptyList()

    private lateinit var tvDeudaCliente: TextView

    private lateinit var btnPagoTotal: Button


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView(view)
        initListener()
        cargarClientes()
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

        btnConfirmar = view.findViewById(R.id.btn_calcular_venta)

        spClientes = view.findViewById(R.id.sp_clientes)

        tvDeudaCliente = view.findViewById(R.id.tv_deuda_cliente)

        btnPagoTotal = view.findViewById(R.id.btn_pago_total)
    }

    private fun initListener() {

        // listeners (cuando el usuario escribe)
        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                calcular()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        etVarilla.addTextChangedListener(watcher)
        etBollo.addTextChangedListener(watcher)
        etCriollo.addTextChangedListener(watcher)
        etOtro.addTextChangedListener(watcher)
        etPago.addTextChangedListener(watcher)

        btnConfirmar.setOnClickListener{
            guardarVenta()
        }

        spClientes.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {

                if (listaClientes.isNotEmpty()) {
                    val cliente = listaClientes[position]

                    calcularDeudaCliente(cliente.id)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        btnPagoTotal.setOnClickListener { pagarTotal() }
    }

    private fun evaluarExpresion(expresion: String): Double{
        return try {
            net.objecthunter.exp4j.ExpressionBuilder(expresion)
                .build()
                .evaluate()
        }catch (e: Exception){
            0.0
        }
    }

    // función para calcular
    fun calcular() {

        val varilla = evaluarExpresion(etVarilla.text.toString())
        val bollo = evaluarExpresion(etBollo.text.toString())
        val criollo = evaluarExpresion(etCriollo.text.toString())
        val otro = evaluarExpresion(etOtro.text.toString())
        val pago = evaluarExpresion(etPago.text.toString())

        val total =
            (varilla * precioVarilla) +
                    (bollo * precioBollo) +
                    (criollo * precioCriollo) +
                    otro

        tvTotal.text = "Total: $total"

        val diferencia = pago - total

        if (diferencia >= 0) {
            tvDiferencia.text = "Vuelto: $diferencia"
            tvDeuda.text = "Deuda: 0"
        } else {
            tvDiferencia.text = "Vuelto: 0"
            tvDeuda.text = "Deuda: ${-diferencia}"
        }
    }

    fun guardarVenta() {

        val varilla = evaluarExpresion(etVarilla.text.toString())
        val bollo = evaluarExpresion(etBollo.text.toString())
        val criollo = evaluarExpresion(etCriollo.text.toString())
        val otro = evaluarExpresion(etOtro.text.toString())
        val pago = evaluarExpresion(etPago.text.toString())

        val total = (varilla * precioVarilla) +
                (bollo * precioBollo) +
                (criollo * precioCriollo) +
                otro
        val diferencia = pago - total

        viewLifecycleOwner.lifecycleScope.launch {

            if (listaClientes.isNotEmpty()) {
                val posicion = spClientes.selectedItemPosition
                val cliente = listaClientes[posicion]

                if (posicion == -1) {
                    Toast.makeText(requireContext(), "Seleccione un cliente", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                Log.d("VENTA_DEBUG", "Cliente seleccionado: ${cliente.nombre} - ID: ${cliente.id}")

                db.ventaDao().insertar(
                    Venta(
                        clienteId = cliente.id,
                        fecha = System.currentTimeMillis(),
                        kilos = varilla + bollo + criollo,
                        precioPorKilo = 0.0,
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

            }else {
                Toast.makeText(requireContext(), "No hay clientes", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun cargarClientes(){

        viewLifecycleOwner.lifecycleScope.launch {

            listaClientes = db.clienteDao().obtenerClientesActivos()

            val nombres = listaClientes.map { it.nombre }

            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                nombres
            )

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spClientes.adapter = adapter

            if (listaClientes.isNotEmpty()) {
                calcularDeudaCliente(listaClientes[0].id)
            }
        }
    }

    private fun calcularDeudaCliente(clienteId: Int){
        viewLifecycleOwner.lifecycleScope.launch {
            val ventas = db.ventaDao().obtenerVentasDeCliente(clienteId)

            val deudaTotal = ventas.sumOf {
                if (it.diferencia < 0) -it.diferencia else 0.0
            }

            tvDeudaCliente.text = "Deuda actual: $${"%.2f".format(deudaTotal)}"

            if (deudaTotal > 0) {
                tvDeudaCliente.setTextColor(Color.RED)
            } else {
                tvDeudaCliente.setTextColor(Color.GREEN)
            }
        } 
    }

    private fun pagarTotal(){
        val varilla = evaluarExpresion(etVarilla.text.toString())
        val bollo = evaluarExpresion(etBollo.text.toString())
        val criollo = evaluarExpresion(etCriollo.text.toString())
        val otro = evaluarExpresion(etOtro.text.toString())

        val total =
            (varilla * precioVarilla) +
                    (bollo * precioBollo) +
                    (criollo * precioCriollo) +
                    otro

        etPago.setText(total.toString())

        calcular()
    }
}