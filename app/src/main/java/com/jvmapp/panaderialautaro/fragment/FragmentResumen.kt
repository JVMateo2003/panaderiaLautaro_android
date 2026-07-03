package com.jvmapp.panaderialautaro.fragment

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import com.jvmapp.panaderialautaro.R
import com.jvmapp.panaderialautaro.data.AppDatabase
import com.jvmapp.panaderialautaro.data.VentaConCliente
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class FragmentResumen : Fragment(R.layout.fragment_resumen) {

    private val db by lazy { AppDatabase.getDatabase(requireContext()) }

    private lateinit var tvVarilla: TextView
    private lateinit var tvBollo: TextView
    private lateinit var tvCriollo: TextView
    private lateinit var tvSobro: TextView
    private lateinit var tvKgVendidos: TextView
    private lateinit var tvTotalVendido: TextView
    private lateinit var tvEfectivo: TextView
    private lateinit var rvDeudasDia: RecyclerView
    private lateinit var btnDescargar: Button

    private lateinit var deudaAdapter: DeudaAdapter

    // Guardamos los datos para usarlos al generar el PDF
    private var kgVarilla = 0.0
    private var kgBollo = 0.0
    private var kgCriollo = 0.0
    private var sobroVarilla = 0.0
    private var sobroBollo = 0.0
    private var sobroCriollo = 0.0
    private var ventasDelDia: List<VentaConCliente> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvVarilla = view.findViewById(R.id.tv_resumen_varilla)
        tvBollo = view.findViewById(R.id.tv_resumen_bollo)
        tvCriollo = view.findViewById(R.id.tv_resumen_criollo)
        tvSobro = view.findViewById(R.id.tv_resumen_sobro)
        tvKgVendidos = view.findViewById(R.id.tv_resumen_kg_vendidos)
        tvTotalVendido = view.findViewById(R.id.tv_resumen_total_vendido)
        tvEfectivo = view.findViewById(R.id.tv_resumen_efectivo)
        rvDeudasDia = view.findViewById(R.id.rv_deudas_dia)
        btnDescargar = view.findViewById(R.id.btn_descargar_resumen)

        deudaAdapter = DeudaAdapter(emptyList())
        rvDeudasDia.layoutManager = LinearLayoutManager(requireContext())
        rvDeudasDia.adapter = deudaAdapter

        cargarResumen()

        btnDescargar.setOnClickListener { generarYCompartirPDF() }
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

    private fun cargarResumen() {
        viewLifecycleOwner.lifecycleScope.launch {
            val inicio = inicioDelDia()
            val fin = finDelDia()

            // Producción
            val produccion = db.produccionDao().obtenerDelDia(inicio, fin)
            if (produccion != null) {
                kgVarilla = produccion.kgVarilla
                kgBollo = produccion.kgBollo
                kgCriollo = produccion.kgCriollo
                sobroVarilla = produccion.sobroVarilla
                sobroBollo = produccion.sobroBollo
                sobroCriollo = produccion.sobroCriollo

                tvVarilla.text = "Varilla: ${kgVarilla} kg  |  Sobró: ${sobroVarilla} kg"
                tvBollo.text = "Bollo: ${kgBollo} kg  |  Sobró: ${sobroBollo} kg"
                tvCriollo.text = "Criollo: ${kgCriollo} kg  |  Sobró: ${sobroCriollo} kg"
                val totalSobro = sobroVarilla + sobroBollo + sobroCriollo
                tvSobro.text = "Total sobró: ${"%.2f".format(totalSobro)} kg"
            } else {
                tvVarilla.text = "Varilla: sin datos"
                tvBollo.text = "Bollo: sin datos"
                tvCriollo.text = "Criollo: sin datos"
                tvSobro.text = "Sobró: sin datos"
            }

            // Ventas
            ventasDelDia = db.ventaDao().obtenerVentasDelDia(inicio, fin)

            val kgTotales = ventasDelDia.sumOf { it.kgVarilla + it.kgBollo + it.kgCriollo }
            val totalVendido = ventasDelDia.sumOf { it.total }
            val efectivo = ventasDelDia.sumOf { if (it.pagado > 0) it.pagado else 0.0 }

            tvKgVendidos.text = "Kg vendidos: ${"%.2f".format(kgTotales)} kg"
            tvTotalVendido.text = "Total vendido: $${"%.2f".format(totalVendido)}"
            tvEfectivo.text = "Efectivo cobrado: $${"%.2f".format(efectivo)}"

            // Deudas
            val deudas = ventasDelDia
                .filter { it.diferencia < 0 }
                .map { Pair(it.clienteNombre, -it.diferencia) }
            deudaAdapter.actualizarLista(deudas)
        }
    }

    private fun generarYCompartirPDF() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val archivo = withContext(Dispatchers.IO) { generarPDF() }
                compartirPDF(archivo)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error al generar PDF: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun generarPDF(): File {
        val fecha = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
        val fechaTitulo = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

        val dir = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val archivo = File(dir, "resumen_$fecha.pdf")

        val writer = PdfWriter(archivo)
        val pdfDoc = PdfDocument(writer)
        val document = Document(pdfDoc)

        // Título
        document.add(
            Paragraph("Panadería Lautaro")
                .setFontSize(22f)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
        )
        document.add(
            Paragraph("Resumen del día - $fechaTitulo")
                .setFontSize(14f)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20f)
        )

        // Sección Producción
        document.add(
            Paragraph("PRODUCCIÓN")
                .setFontSize(16f)
                .setBold()
                .setMarginTop(10f)
        )

        val tablaProduccion = Table(UnitValue.createPercentArray(floatArrayOf(2f, 1f, 1f)))
            .setWidth(UnitValue.createPercentValue(100f))

        // Header
        listOf("Producto", "Salió (kg)", "Sobró (kg)").forEach {
            tablaProduccion.addHeaderCell(
                Cell().add(Paragraph(it).setBold())
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY)
            )
        }

        tablaProduccion.addCell(Cell().add(Paragraph("Varilla")))
        tablaProduccion.addCell(Cell().add(Paragraph("$kgVarilla")))
        tablaProduccion.addCell(Cell().add(Paragraph("$sobroVarilla")))

        tablaProduccion.addCell(Cell().add(Paragraph("Bollo")))
        tablaProduccion.addCell(Cell().add(Paragraph("$kgBollo")))
        tablaProduccion.addCell(Cell().add(Paragraph("$sobroBollo")))

        tablaProduccion.addCell(Cell().add(Paragraph("Criollo")))
        tablaProduccion.addCell(Cell().add(Paragraph("$kgCriollo")))
        tablaProduccion.addCell(Cell().add(Paragraph("$sobroCriollo")))

        document.add(tablaProduccion)

        // Sección Ventas
        document.add(
            Paragraph("VENTAS")
                .setFontSize(16f)
                .setBold()
                .setMarginTop(20f)
        )

        val tablaVentas = Table(UnitValue.createPercentArray(floatArrayOf(2f, 1f, 1f, 1f, 1f, 1f, 1f, 1f)))
            .setWidth(UnitValue.createPercentValue(100f))

        listOf("Cliente", "Varilla", "Bollo", "Criollo", "Otro $", "Total", "Pagó", "Deuda").forEach {
            tablaVentas.addHeaderCell(
                Cell().add(Paragraph(it).setBold().setFontSize(9f))
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY)
            )
        }

        var totalKg = 0.0
        var totalVendido = 0.0
        var totalPagado = 0.0
        var totalDeuda = 0.0

        ventasDelDia.forEach { venta ->
            tablaVentas.addCell(Cell().add(Paragraph(venta.clienteNombre).setFontSize(9f)))
            tablaVentas.addCell(Cell().add(Paragraph("${"%.2f".format(venta.kgVarilla)}").setFontSize(9f)))
            tablaVentas.addCell(Cell().add(Paragraph("${"%.2f".format(venta.kgBollo)}").setFontSize(9f)))
            tablaVentas.addCell(Cell().add(Paragraph("${"%.2f".format(venta.kgCriollo)}").setFontSize(9f)))
            tablaVentas.addCell(Cell().add(Paragraph("${"%.2f".format(venta.otroMonto)}").setFontSize(9f)))
            tablaVentas.addCell(Cell().add(Paragraph("${"%.2f".format(venta.total)}").setFontSize(9f)))
            tablaVentas.addCell(Cell().add(Paragraph("${"%.2f".format(venta.pagado)}").setFontSize(9f)))

            val deuda = if (venta.diferencia < 0) -venta.diferencia else 0.0
            tablaVentas.addCell(
                Cell().add(Paragraph("${"%.2f".format(deuda)}").setFontSize(9f))
            )

            totalKg += venta.kgVarilla + venta.kgBollo + venta.kgCriollo
            totalVendido += venta.total
            totalPagado += venta.pagado
            totalDeuda += deuda
        }

        // Fila de totales
        tablaVentas.addCell(Cell().add(Paragraph("TOTAL").setBold().setFontSize(9f)).setBackgroundColor(ColorConstants.LIGHT_GRAY))
        tablaVentas.addCell(Cell().add(Paragraph("${"%.2f".format(totalKg)}").setBold().setFontSize(9f)).setBackgroundColor(ColorConstants.LIGHT_GRAY))
        tablaVentas.addCell(Cell().add(Paragraph("").setFontSize(9f)).setBackgroundColor(ColorConstants.LIGHT_GRAY))
        tablaVentas.addCell(Cell().add(Paragraph("").setFontSize(9f)).setBackgroundColor(ColorConstants.LIGHT_GRAY))
        tablaVentas.addCell(Cell().add(Paragraph("").setFontSize(9f)).setBackgroundColor(ColorConstants.LIGHT_GRAY))
        tablaVentas.addCell(Cell().add(Paragraph("${"%.2f".format(totalVendido)}").setBold().setFontSize(9f)).setBackgroundColor(ColorConstants.LIGHT_GRAY))
        tablaVentas.addCell(Cell().add(Paragraph("${"%.2f".format(totalPagado)}").setBold().setFontSize(9f)).setBackgroundColor(ColorConstants.LIGHT_GRAY))
        tablaVentas.addCell(Cell().add(Paragraph("${"%.2f".format(totalDeuda)}").setBold().setFontSize(9f)).setBackgroundColor(ColorConstants.LIGHT_GRAY))

        document.add(tablaVentas)

        document.close()
        return archivo
    }

    private fun compartirPDF(archivo: File) {
        val uri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            archivo
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(Intent.createChooser(intent, "Compartir resumen"))
    }

    inner class DeudaAdapter(private var lista: List<Pair<String, Double>>) :
        RecyclerView.Adapter<DeudaAdapter.DeudaViewHolder>() {

        inner class DeudaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvNombre: TextView = view.findViewById(R.id.tv_deuda_nombre)
            val tvMonto: TextView = view.findViewById(R.id.tv_deuda_monto)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeudaViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_deuda, parent, false)
            return DeudaViewHolder(view)
        }

        override fun onBindViewHolder(holder: DeudaViewHolder, position: Int) {
            val (nombre, monto) = lista[position]
            holder.tvNombre.text = nombre
            holder.tvMonto.text = "$${"%.2f".format(monto)}"
        }

        override fun getItemCount() = lista.size

        fun actualizarLista(nuevaLista: List<Pair<String, Double>>) {
            lista = nuevaLista
            notifyDataSetChanged()
        }
    }
}