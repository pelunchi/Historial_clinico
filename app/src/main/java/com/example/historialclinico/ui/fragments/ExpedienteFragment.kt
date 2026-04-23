package com.example.historialclinico.ui.fragments

import android.widget.ImageButton
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.historialclinico.R
import com.example.historialclinico.data.database.ExpedienteRepository
import com.example.historialclinico.data.models.Expediente
import com.example.historialclinico.utils.PreferencesManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ExpedienteFragment : Fragment() {

    private val repo = ExpedienteRepository()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private var expedienteId: String = ""

    // Ficha
    private lateinit var etNombre: TextInputEditText
    private lateinit var etEdad: TextInputEditText
    private lateinit var actvSexo: AutoCompleteTextView
    private lateinit var etFechaNac: TextInputEditText
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
    private lateinit var etPresion: TextInputEditText
    private lateinit var etFc: TextInputEditText
    private lateinit var etFr: TextInputEditText
    private lateinit var etTemp: TextInputEditText
    private lateinit var etSpo2: TextInputEditText
    private lateinit var etEfObs: TextInputEditText

    // Tratamiento
    private lateinit var etMeds: TextInputEditText
    private lateinit var etDosis: TextInputEditText
    private lateinit var etTerapias: TextInputEditText
    private lateinit var etNotas: TextInputEditText
    private lateinit var etProxima: TextInputEditText

    // UI
    private lateinit var tvEstado: TextView
    private lateinit var btnGuardar: MaterialButton
    private lateinit var btnBack: ImageButton


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_expediente, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupSexoDropdown()
        setupDatePickers()
        setupImcCalculo()
        btnGuardar.setOnClickListener { guardar() }
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun initViews(v: View) {
        etNombre        = v.findViewById(R.id.etNombre)
        etEdad          = v.findViewById(R.id.etEdad)
        actvSexo        = v.findViewById(R.id.actvSexo)
        etFechaNac      = v.findViewById(R.id.etFechaNac)
        etCurp          = v.findViewById(R.id.etCurp)
        etDireccion     = v.findViewById(R.id.etDireccion)
        etTelefono      = v.findViewById(R.id.etTelefono)
        etCorreo        = v.findViewById(R.id.etCorreo)
        cbDiabetes      = v.findViewById(R.id.cbAhfDiabetes)
        cbHipertension  = v.findViewById(R.id.cbAhfHipertension)
        cbCancer        = v.findViewById(R.id.cbAhfCancer)
        cbCardio        = v.findViewById(R.id.cbAhfCardio)
        cbObesidad      = v.findViewById(R.id.cbAhfObesidad)
        cbRenal         = v.findViewById(R.id.cbAhfRenal)
        etAhfOtros      = v.findViewById(R.id.etAhfOtros)
        etCirugias      = v.findViewById(R.id.etAppCirugias)
        etAlergias      = v.findViewById(R.id.etAppAlergias)
        etCronicas      = v.findViewById(R.id.etAppCronicas)
        etHospitalizaciones = v.findViewById(R.id.etAppHospitalizaciones)
        etAppOtros      = v.findViewById(R.id.etAppOtros)

        etTalla         = v.findViewById(R.id.etEfTalla)
        etPeso          = v.findViewById(R.id.etEfPeso)
        etImc           = v.findViewById(R.id.etEfImc)
        etPresion       = v.findViewById(R.id.etEfPa)
        etFc            = v.findViewById(R.id.etEfFc)
        etFr            = v.findViewById(R.id.etEfFr)
        etTemp          = v.findViewById(R.id.etEfTemp)
        etSpo2          = v.findViewById(R.id.etEfSpo2)
        etEfObs         = v.findViewById(R.id.etEfObs)

        btnGuardar      = v.findViewById(R.id.btnGuardar)
        btnBack         = v.findViewById(R.id.btnBack)
    }

    private fun setupSexoDropdown() {
        val opciones = listOf("Masculino", "Femenino", "Otro")
        actvSexo.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, opciones)
        )
    }

    private fun setupDatePickers() {
        listOf(etFechaNac).forEach { campo ->
            campo.setOnClickListener { mostrarDatePicker(campo) }
            campo.setOnFocusChangeListener { _, focused -> if (focused) mostrarDatePicker(campo) }
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
            val talla = etTalla.text.toString().toDoubleOrNull() ?: 0.0
            val peso  = etPeso.text.toString().toDoubleOrNull()  ?: 0.0
            if (talla > 0 && peso > 0) {
                val m = talla / 100.0
                etImc.setText(String.format("%.1f", peso / (m * m)))
            } else etImc.setText("")
        }
        etTalla.addTextChangedListener { calc() }
        etPeso.addTextChangedListener  { calc() }
    }

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
            efPresionArterial    = etPresion.text.toString(),
            efFrecCardiaca       = etFc.text.toString().toIntOrNull() ?: 0,
            efFrecRespiratoria   = etFr.text.toString().toIntOrNull() ?: 0,
            efTemperatura        = etTemp.text.toString().toDoubleOrNull() ?: 0.0,
            efSpo2               = etSpo2.text.toString().toDoubleOrNull() ?: 0.0,

        )

        setEstado("⏳ Guardando...", "#1976D2")
        btnGuardar.isEnabled = false

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                expedienteId = repo.guardar(expediente)
                setEstado("✅ Guardado correctamente", "#2E7D32")
            } catch (e: Exception) {
                setEstado("❌ Error: ${e.message}", "#D32F2F")
            } finally {
                btnGuardar.isEnabled = true
            }
        }
    }

    private fun setEstado(texto: String, color: String) {
        tvEstado.visibility = View.VISIBLE
        tvEstado.text = texto
        tvEstado.setTextColor(android.graphics.Color.parseColor(color))
    }
}
