package com.example.historialclinico.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.historialclinico.R
import com.example.historialclinico.data.database.ConsultaRepository
import com.example.historialclinico.data.models.Consulta
import com.example.historialclinico.data.models.Paciente
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ConsultaFragment : Fragment() {

    private var paciente: Paciente? = null
    private var consultaId: String = ""
    private val repo = ConsultaRepository()

    private lateinit var etFecha: TextInputEditText
    private lateinit var etHora: TextInputEditText
    private lateinit var etMotivo: TextInputEditText
    private lateinit var etSintomas: TextInputEditText
    private lateinit var etDiagnostico: TextInputEditText
    private lateinit var etResumen: TextInputEditText
    private lateinit var etIndicaciones: TextInputEditText
    private lateinit var etReceta: TextInputEditText
    private lateinit var etTalla: TextInputEditText
    private lateinit var etPeso: TextInputEditText
    private lateinit var etImc: TextInputEditText
    private lateinit var etPa: TextInputEditText
    private lateinit var etFc: TextInputEditText
    private lateinit var etFr: TextInputEditText
    private lateinit var etTemp: TextInputEditText
    private lateinit var etSpo2: TextInputEditText
    private lateinit var etObs: TextInputEditText
    private lateinit var tvEstado: TextView
    private lateinit var btnGuardar: MaterialButton

    companion object {
        fun newInstance(paciente: Paciente, consultaId: String? = null) =
            ConsultaFragment().apply {
                arguments = Bundle().apply {
                    putSerializable("paciente", paciente)
                    putString("consultaId", consultaId)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        paciente   = arguments?.getSerializable("paciente") as? Paciente
        consultaId = arguments?.getString("consultaId") ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_formulario_consulta, container, false)  // ← nombre actualizado

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupImcCalculo()

        view.findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        if (consultaId.isBlank()) {
            // Nueva consulta — fecha y hora automáticas
            val ahora = Date()
            etFecha.setText(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(ahora))
            etHora.setText(SimpleDateFormat("hh:mm a", Locale.getDefault()).format(ahora))
        } else {
            cargarConsultaExistente()
        }

        btnGuardar.setOnClickListener { guardar() }
    }

    // ── Cargar existente ───────────────────────────────────────────────

    private fun cargarConsultaExistente() {
        val expId = paciente?.id ?: return
        setEstado("⏳ Cargando...", "#1976D2")
        btnGuardar.isEnabled = false

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val c = repo.obtener(expId, consultaId)
                if (c != null) poblarFormulario(c) else setEstado("⚠️ No encontrada", "#F57C00")
            } catch (e: Exception) {
                setEstado("❌ Error: ${e.message}", "#D32F2F")
            } finally {
                tvEstado.visibility  = View.GONE
                btnGuardar.isEnabled = true
            }
        }
    }

    private fun poblarFormulario(c: Consulta) {
        etFecha.setText(c.fecha)
        etHora.setText(c.hora)
        etMotivo.setText(c.motivo)
        etSintomas.setText(c.sintomas)
        etDiagnostico.setText(c.diagnostico)
        etResumen.setText(c.resumen)
        etIndicaciones.setText(c.indicaciones)
        etReceta.setText(c.receta)
        if (c.efTalla > 0) etTalla.setText(c.efTalla.toString())
        if (c.efPeso  > 0) etPeso.setText(c.efPeso.toString())
        etPa.setText(c.efPresionArterial)
        if (c.efFrecCardiaca    > 0) etFc.setText(c.efFrecCardiaca.toString())
        if (c.efFrecRespiratoria > 0) etFr.setText(c.efFrecRespiratoria.toString())
        if (c.efTemperatura     > 0) etTemp.setText(c.efTemperatura.toString())
        if (c.efSpo2            > 0) etSpo2.setText(c.efSpo2.toString())
        etObs.setText(c.efObservaciones)
    }

    // ── Guardar ────────────────────────────────────────────────────────

    private fun guardar() {
        val expId = paciente?.id
        if (expId.isNullOrBlank()) {
            Snackbar.make(requireView(), "Error: paciente no identificado", Snackbar.LENGTH_LONG).show()
            return
        }
        if (etMotivo.text.isNullOrBlank()) {
            Snackbar.make(requireView(), "El motivo de consulta es obligatorio", Snackbar.LENGTH_LONG).show()
            return
        }

        val talla = etTalla.text.toString().toDoubleOrNull() ?: 0.0
        val peso  = etPeso.text.toString().toDoubleOrNull()  ?: 0.0
        val imc   = if (talla > 0 && peso > 0) { val m = talla/100.0; peso/(m*m) } else 0.0

        val consulta = Consulta(
            id                 = consultaId,
            expedienteId       = expId,
            fecha              = etFecha.text.toString(),
            hora               = etHora.text.toString(),
            motivo             = etMotivo.text.toString().trim(),
            sintomas           = etSintomas.text.toString(),
            diagnostico        = etDiagnostico.text.toString(),
            resumen            = etResumen.text.toString(),
            indicaciones       = etIndicaciones.text.toString(),
            receta             = etReceta.text.toString(),
            efTalla            = talla,
            efPeso             = peso,
            efImc              = imc,
            efPresionArterial  = etPa.text.toString(),
            efFrecCardiaca     = etFc.text.toString().toIntOrNull() ?: 0,
            efFrecRespiratoria = etFr.text.toString().toIntOrNull() ?: 0,
            efTemperatura      = etTemp.text.toString().toDoubleOrNull() ?: 0.0,
            efSpo2             = etSpo2.text.toString().toDoubleOrNull() ?: 0.0,
            efObservaciones    = etObs.text.toString()
        )

        setEstado("⏳ Guardando...", "#1976D2")
        btnGuardar.isEnabled = false

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                consultaId = repo.guardar(consulta)
                setEstado("✅ Guardado", "#2E7D32")
                delay(800)
                parentFragmentManager.popBackStack()  // ← regresa a consultas al guardar
            } catch (e: Exception) {
                setEstado("❌ Error: ${e.message}", "#D32F2F")
                btnGuardar.isEnabled = true
            }
        }
    }

    private fun setupImcCalculo() {
        val calc = {
            val t = etTalla.text.toString().toDoubleOrNull() ?: 0.0
            val p = etPeso.text.toString().toDoubleOrNull()  ?: 0.0
            if (t > 0 && p > 0) { val m = t/100.0; etImc.setText(String.format("%.1f", p/(m*m))) }
            else etImc.setText("")
        }
        etTalla.addTextChangedListener { calc() }
        etPeso.addTextChangedListener  { calc() }
    }

    private fun setEstado(texto: String, color: String) {
        tvEstado.visibility = View.VISIBLE
        tvEstado.text = texto
        tvEstado.setTextColor(android.graphics.Color.parseColor(color))
    }

    private fun initViews(v: View) {
        etFecha        = v.findViewById(R.id.etFechaConsulta)
        etHora         = v.findViewById(R.id.etHoraConsulta)
        etMotivo       = v.findViewById(R.id.etMotivoConsulta)
        etSintomas     = v.findViewById(R.id.etSintomas)
        etDiagnostico  = v.findViewById(R.id.etDiagnostico)
        etResumen      = v.findViewById(R.id.etResumenConsulta)
        etIndicaciones = v.findViewById(R.id.etIndicaciones)
        etReceta       = v.findViewById(R.id.etReceta)
        etTalla        = v.findViewById(R.id.etEfTallaConsulta)
        etPeso         = v.findViewById(R.id.etEfPesoConsulta)
        etImc          = v.findViewById(R.id.etEfImcConsulta)
        etPa           = v.findViewById(R.id.etEfPaConsulta)
        etFc           = v.findViewById(R.id.etEfFcConsulta)
        etFr           = v.findViewById(R.id.etEfFr)
        etTemp         = v.findViewById(R.id.etEfTempConsulta)
        etSpo2         = v.findViewById(R.id.etEfSpo2Consulta)
        etObs          = v.findViewById(R.id.etEfObsConsulta)
        btnGuardar     = v.findViewById(R.id.btnGuardarConsulta)

        tvEstado = TextView(requireContext()).apply {
            textSize = 14f
            setPadding(0, 16, 0, 0)
            visibility = View.GONE
        }
        try {
            (v.findViewById<MaterialButton>(R.id.btnGuardarConsulta).parent as? ViewGroup)
                ?.addView(tvEstado)
        } catch (_: Exception) {}
    }
}
