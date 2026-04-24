package com.example.historialclinico.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageButton
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.historialclinico.R
import com.example.historialclinico.data.database.ExpedienteRepository
import com.example.historialclinico.data.models.Expediente
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import android.app.DatePickerDialog

class ExpedienteFragment : Fragment() {

    private val repo = ExpedienteRepository()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private var expedienteId: String = ""

    // Ficha
    private lateinit var etNombre: TextInputEditText
    private lateinit var etEdad: TextInputEditText
    private lateinit var actvSexo: AutoCompleteTextView
    private lateinit var etFechaNac: TextInputEditText
    private lateinit var etTipoSangre: TextInputEditText
    private lateinit var etCurp: TextInputEditText
    private lateinit var etDireccion: TextInputEditText
    private lateinit var etTelefono: TextInputEditText
    private lateinit var etCorreo: TextInputEditText

    // AHF
    private lateinit var cbDiabetes: MaterialCheckBox
    private lateinit var cbHipertension: MaterialCheckBox
    private lateinit var cbCancer: MaterialCheckBox
    private lateinit var cbCardio: MaterialCheckBox
    private lateinit var cbObesidad: MaterialCheckBox
    private lateinit var cbRenal: MaterialCheckBox
    private lateinit var etAhfOtros: TextInputEditText

    // APP
    private lateinit var etCirugias: TextInputEditText
    private lateinit var etAlergias: TextInputEditText
    private lateinit var etCronicas: TextInputEditText
    private lateinit var etHospitalizaciones: TextInputEditText
    private lateinit var etAppOtros: TextInputEditText

    // EF
    private lateinit var etTalla: TextInputEditText
    private lateinit var etPeso: TextInputEditText
    private lateinit var etImc: TextInputEditText
    private lateinit var etPa: TextInputEditText
    private lateinit var etFc: TextInputEditText
    private lateinit var etFr: TextInputEditText
    private lateinit var etTemp: TextInputEditText
    private lateinit var etSpo2: TextInputEditText
    private lateinit var etEfObs: TextInputEditText

    // UI
    private lateinit var btnGuardar: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_expediente, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupSexoDropdown()
        setupDatePicker()
        setupImcCalculo()

        view.findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        btnGuardar.setOnClickListener { guardar() }

        // Si viene un ID, cargar el expediente existente
        val idExistente = arguments?.getString("pacienteId")
        if (!idExistente.isNullOrBlank()) {
            expedienteId = idExistente
            cargarExpedienteExistente(idExistente)
        }
    }

    // ── Cargar desde Firebase ──────────────────────────────────────────

    private fun cargarExpedienteExistente(id: String) {
        btnGuardar.isEnabled = false
        btnGuardar.text = "Cargando..."

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val exp = repo.obtener(id)
                if (exp != null) poblarFormulario(exp)
            } catch (e: Exception) {
                Snackbar.make(requireView(), "Error al cargar: ${e.message}", Snackbar.LENGTH_LONG).show()
            } finally {
                btnGuardar.isEnabled = true
                btnGuardar.text = " + Guardar Paciente"
            }
        }
    }

    private fun poblarFormulario(exp: Expediente) {
        etNombre.setText(exp.nombre)
        etEdad.setText(if (exp.edad > 0) exp.edad.toString() else "")
        actvSexo.setText(exp.sexo, false)
        etFechaNac.setText(exp.fechaNacimiento)
        etTipoSangre.setText(exp.tipoSangre)
        etCurp.setText(exp.curp)
        etDireccion.setText(exp.direccion)
        etTelefono.setText(exp.telefono)
        etCorreo.setText(exp.correo)

        cbDiabetes.isChecked     = exp.ahfDiabetes
        cbHipertension.isChecked = exp.ahfHipertension
        cbCancer.isChecked       = exp.ahfCancer
        cbCardio.isChecked       = exp.ahfCardio
        cbObesidad.isChecked     = exp.ahfObesidad
        cbRenal.isChecked        = exp.ahfRenal
        etAhfOtros.setText(exp.ahfOtros)

        etCirugias.setText(exp.appCirugias)
        etAlergias.setText(exp.appAlergias)
        etCronicas.setText(exp.appCronicas)
        etHospitalizaciones.setText(exp.appHospitalizaciones)
        etAppOtros.setText(exp.appOtros)

        if (exp.efTalla > 0) etTalla.setText(exp.efTalla.toString())
        if (exp.efPeso  > 0) etPeso.setText(exp.efPeso.toString())

    }

    // ── Guardar ────────────────────────────────────────────────────────

    private fun guardar() {
        if (etNombre.text.isNullOrBlank()) {
            Snackbar.make(requireView(), "El nombre es obligatorio", Snackbar.LENGTH_LONG).show()
            return
        }

        val talla = etTalla.text.toString().toDoubleOrNull() ?: 0.0
        val peso  = etPeso.text.toString().toDoubleOrNull()  ?: 0.0
        val imc   = if (talla > 0 && peso > 0) { val m = talla / 100.0; peso / (m * m) } else 0.0

        val expediente = Expediente(
            id                   = expedienteId,
            nombre               = etNombre.text.toString().trim(),
            edad                 = etEdad.text.toString().toIntOrNull() ?: 0,
            sexo                 = actvSexo.text.toString(),
            fechaNacimiento      = etFechaNac.text.toString(),
            tipoSangre           = etTipoSangre.text.toString().trim().uppercase(),
            curp                 = etCurp.text.toString().trim().uppercase(),
            direccion            = etDireccion.text.toString().trim(),
            telefono             = etTelefono.text.toString().trim(),
            correo               = etCorreo.text.toString().trim().lowercase(),
            ahfDiabetes          = cbDiabetes.isChecked,
            ahfHipertension      = cbHipertension.isChecked,
            ahfCancer            = cbCancer.isChecked,
            ahfCardio            = cbCardio.isChecked,
            ahfObesidad          = cbObesidad.isChecked,
            ahfRenal             = cbRenal.isChecked,
            ahfOtros             = etAhfOtros.text.toString(),
            appCirugias          = etCirugias.text.toString(),
            appAlergias          = etAlergias.text.toString(),
            appCronicas          = etCronicas.text.toString(),
            appHospitalizaciones = etHospitalizaciones.text.toString(),
            appOtros             = etAppOtros.text.toString(),
            efTalla              = talla,
            efPeso               = peso,
            efImc                = imc,
        )

        btnGuardar.isEnabled = false
        btnGuardar.text = "Guardando..."

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                expedienteId = repo.guardar(expediente)
                Snackbar.make(requireView(), "✅ Guardado correctamente", Snackbar.LENGTH_LONG).show()
            } catch (e: Exception) {
                Snackbar.make(requireView(), "❌ Error: ${e.message}", Snackbar.LENGTH_LONG).show()
            } finally {
                btnGuardar.isEnabled = true
                btnGuardar.text = " + Guardar Paciente"
            }
        }
    }

    // ── Setup ──────────────────────────────────────────────────────────

    private fun setupSexoDropdown() {
        actvSexo.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line,
                listOf("Masculino", "Femenino"))
        )
    }

    private fun setupDatePicker() {
        etFechaNac.setOnClickListener { mostrarDatePicker(etFechaNac) }
        etFechaNac.setOnFocusChangeListener { _, focused ->
            if (focused) mostrarDatePicker(etFechaNac)
        }
    }

    private fun mostrarDatePicker(campo: TextInputEditText) {
        val cal = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, y, m, d ->
            cal.set(y, m, d)
            campo.setText(dateFormat.format(cal.time))
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun setupImcCalculo() {
        val calc = {
            val t = etTalla.text.toString().toDoubleOrNull() ?: 0.0
            val p = etPeso.text.toString().toDoubleOrNull()  ?: 0.0
            if (t > 0 && p > 0) {
                val m = t / 100.0
                etImc.setText(String.format("%.1f", p / (m * m)))
            } else etImc.setText("")
        }
        etTalla.addTextChangedListener { calc() }
        etPeso.addTextChangedListener  { calc() }
    }

    // ── Init vistas ────────────────────────────────────────────────────

    private fun initViews(v: View) {
        etNombre         = v.findViewById(R.id.etNombre)
        etEdad           = v.findViewById(R.id.etEdad)
        actvSexo         = v.findViewById(R.id.actvSexo)
        etFechaNac       = v.findViewById(R.id.etFechaNac)
        etTipoSangre     = v.findViewById(R.id.etTipoSangre)
        etCurp           = v.findViewById(R.id.etCurp)
        etDireccion      = v.findViewById(R.id.etDireccion)
        etTelefono       = v.findViewById(R.id.etTelefono)
        etCorreo         = v.findViewById(R.id.etCorreo)
        cbDiabetes       = v.findViewById(R.id.cbAhfDiabetes)
        cbHipertension   = v.findViewById(R.id.cbAhfHipertension)
        cbCancer         = v.findViewById(R.id.cbAhfCancer)
        cbCardio         = v.findViewById(R.id.cbAhfCardio)
        cbObesidad       = v.findViewById(R.id.cbAhfObesidad)
        cbRenal          = v.findViewById(R.id.cbAhfRenal)
        etAhfOtros       = v.findViewById(R.id.etAhfOtros)
        etCirugias       = v.findViewById(R.id.etAppCirugias)
        etAlergias       = v.findViewById(R.id.etAppAlergias)
        etCronicas       = v.findViewById(R.id.etAppCronicas)
        etHospitalizaciones = v.findViewById(R.id.etAppHospitalizaciones)
        etAppOtros       = v.findViewById(R.id.etAppOtros)
        etTalla          = v.findViewById(R.id.etEfTalla)
        etPeso           = v.findViewById(R.id.etEfPeso)
        etImc            = v.findViewById(R.id.etEfImc)
        btnGuardar       = v.findViewById(R.id.btnGuardar)
    }
}
 