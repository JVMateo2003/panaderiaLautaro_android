package com.jvmapp.panaderialautaro.fragment

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.jvmapp.panaderialautaro.R
import com.jvmapp.panaderialautaro.data.AppDatabase
import com.jvmapp.panaderialautaro.data.Produccion
import kotlinx.coroutines.launch
import java.util.Calendar

class FragmentProduccion : Fragment(R.layout.fragment_produccion) {

    private val db by lazy { AppDatabase.getDatabase(requireContext()) }

    private lateinit var etVarilla: EditText
    private lateinit var etBollo: EditText
    private lateinit var etCriollo: EditText
    private lateinit var etOtro: EditText
    private lateinit var etSobroVarilla: EditText
    private lateinit var etSobroBollo: EditText
    private lateinit var etSobroCriollo: EditText
    private lateinit var btnGuardar: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etVarilla = view.findViewById(R.id.et_produccion_varilla)
        etBollo = view.findViewById(R.id.et_produccion_bollo)
        etCriollo = view.findViewById(R.id.et_produccion_criollo)
        etOtro = view.findViewById(R.id.et_produccion_otro)
        etSobroVarilla = view.findViewById(R.id.et_produccion_sobro_varilla)
        etSobroBollo = view.findViewById(R.id.et_produccion_sobro_bollo)
        etSobroCriollo = view.findViewById(R.id.et_produccion_sobro_criollo)
        btnGuardar = view.findViewById(R.id.btn_produccion_guardar)

        cargarProduccionDelDia()

        btnGuardar.setOnClickListener { guardarProduccion() }
    }

    private fun evaluarExpresion(expresion: String): Double {
        return try {
            net.objecthunter.exp4j.ExpressionBuilder(expresion).build().evaluate()
        } catch (e: Exception) { 0.0 }
    }

    private fun inicioDelDia(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun finDelDia(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }

    // Si ya existe produccion del dia, carga los valores para poder editarlos
    private fun cargarProduccionDelDia() {
        viewLifecycleOwner.lifecycleScope.launch {
            val produccion = db.produccionDao().obtenerDelDia(inicioDelDia(), finDelDia())
            produccion?.let {
                etVarilla.setText(it.kgVarilla.toString())
                etBollo.setText(it.kgBollo.toString())
                etCriollo.setText(it.kgCriollo.toString())
                etOtro.setText(it.otroMonto.toString())
                etSobroVarilla.setText(it.sobroVarilla.toString())
                etSobroBollo.setText(it.sobroBollo.toString())
                etSobroCriollo.setText(it.sobroCriollo.toString())
            }
        }
    }

    private fun guardarProduccion() {
        val kgVarilla = evaluarExpresion(etVarilla.text.toString())
        val kgBollo = evaluarExpresion(etBollo.text.toString())
        val kgCriollo = evaluarExpresion(etCriollo.text.toString())
        val otroMonto = evaluarExpresion(etOtro.text.toString())
        val sobroVarilla = evaluarExpresion(etSobroVarilla.text.toString())
        val sobroBollo = evaluarExpresion(etSobroBollo.text.toString())
        val sobroCriollo = evaluarExpresion(etSobroCriollo.text.toString())

        viewLifecycleOwner.lifecycleScope.launch {
            val produccionExistente = db.produccionDao().obtenerDelDia(inicioDelDia(), finDelDia())

            if (produccionExistente != null) {
                // Si ya existe la del dia, la actualiza
                db.produccionDao().actualizar(
                    produccionExistente.copy(
                        kgVarilla = kgVarilla,
                        kgBollo = kgBollo,
                        kgCriollo = kgCriollo,
                        otroMonto = otroMonto,
                        sobroVarilla = sobroVarilla,
                        sobroBollo = sobroBollo,
                        sobroCriollo = sobroCriollo
                    )
                )
                Toast.makeText(requireContext(), "Producción actualizada", Toast.LENGTH_SHORT).show()
            } else {
                // Si no existe, crea una nueva
                db.produccionDao().insertar(
                    Produccion(
                        fecha = System.currentTimeMillis(),
                        kgVarilla = kgVarilla,
                        kgBollo = kgBollo,
                        kgCriollo = kgCriollo,
                        otroMonto = otroMonto,
                        sobroVarilla = sobroVarilla,
                        sobroBollo = sobroBollo,
                        sobroCriollo = sobroCriollo
                    )
                )
                Toast.makeText(requireContext(), "Producción guardada", Toast.LENGTH_SHORT).show()
            }
        }
    }
}