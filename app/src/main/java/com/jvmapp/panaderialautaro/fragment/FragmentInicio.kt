package com.jvmapp.panaderialautaro.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jvmapp.panaderialautaro.MainActivity
import com.jvmapp.panaderialautaro.R
import com.jvmapp.panaderialautaro.data.AppDatabase
import com.jvmapp.panaderialautaro.data.Recordatorio
import kotlinx.coroutines.launch

class FragmentInicio : Fragment(R.layout.fragment_inicio) {

    private val db by lazy { AppDatabase.getDatabase(requireContext()) }

    private lateinit var etRecordatorio: EditText
    private lateinit var btnAgregar: Button
    private lateinit var rvRecordatorios: RecyclerView
    private lateinit var adapter: RecordatorioAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<ImageView>(R.id.btnMenu).setOnClickListener {
            (activity as MainActivity).abrirDrawer()
        }
        view.findViewById<Button>(R.id.btnCargaProduccion).setOnClickListener {
            (activity as MainActivity).abrirFragment(FragmentProduccion())
        }
        view.findViewById<Button>(R.id.btnCargarVentas).setOnClickListener {
            (activity as MainActivity).abrirFragment(FragmentVenta())
        }
        view.findViewById<Button>(R.id.btnResumen).setOnClickListener {
            (activity as MainActivity).abrirFragment(FragmentResumen())
        }

        etRecordatorio = view.findViewById(R.id.et_recordatorio)
        btnAgregar = view.findViewById(R.id.btn_agregar_recordatorio)
        rvRecordatorios = view.findViewById(R.id.rv_recordatorios)

        adapter = RecordatorioAdapter(emptyList()) { recordatorio ->
            viewLifecycleOwner.lifecycleScope.launch {
                db.recordatorioDao().eliminar(recordatorio.id)
                cargarRecordatorios()
            }
        }

        rvRecordatorios.layoutManager = LinearLayoutManager(requireContext())
        rvRecordatorios.adapter = adapter

        cargarRecordatorios()

        btnAgregar.setOnClickListener {
            val texto = etRecordatorio.text.toString().trim()
            if (texto.isEmpty()) {
                Toast.makeText(requireContext(), "Escribí un recordatorio", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewLifecycleOwner.lifecycleScope.launch {
                db.recordatorioDao().insertar(Recordatorio(texto = texto))
                etRecordatorio.text.clear()
                cargarRecordatorios()
            }
        }
    }

    private fun cargarRecordatorios() {
        viewLifecycleOwner.lifecycleScope.launch {
            val lista = db.recordatorioDao().obtenerTodos()
            adapter.actualizarLista(lista)
        }
    }

    inner class RecordatorioAdapter(
        private var lista: List<Recordatorio>,
        private val onBorrar: (Recordatorio) -> Unit
    ) : RecyclerView.Adapter<RecordatorioAdapter.RecordatorioViewHolder>() {

        inner class RecordatorioViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvTexto: TextView = view.findViewById(R.id.tv_recordatorio_texto)
            val btnBorrar: Button = view.findViewById(R.id.btn_borrar_recordatorio)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordatorioViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_recordatorio, parent, false)
            return RecordatorioViewHolder(view)
        }

        override fun onBindViewHolder(holder: RecordatorioViewHolder, position: Int) {
            val recordatorio = lista[position]
            holder.tvTexto.text = recordatorio.texto
            holder.btnBorrar.setOnClickListener { onBorrar(recordatorio) }
        }

        override fun getItemCount() = lista.size

        fun actualizarLista(nuevaLista: List<Recordatorio>) {
            lista = nuevaLista
            notifyDataSetChanged()
        }
    }
}