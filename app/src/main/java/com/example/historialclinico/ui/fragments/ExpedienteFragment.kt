package com.example.historialclinico.ui.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageButton
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.historialclinico.R
import com.example.historialclinico.data.database.ExpedienteRepository
import com.example.historialclinico.data.models.Expediente
import com.example.historialclinico.data.models.Paciente
import com.example.historialclinico.ui.activities.MainActivity
import com.example.historialclinico.ui.viewmodel.AppViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ExpedienteFragment : Fragment() {

    private val repo = ExpedienteRepository()
    private val vm: AppViewModel by activityViewModels()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private var expedienteId: String = ""
    private var esNuevo = true
    private var avatarColorIndexActual: Int = -1  // ← guarda el color existente

    private lateinit var etNombre: TextInputEditText
    private lateinit var etEdad: TextInputEditText
    private lateinit var actvSexo: AutoCompleteTextView
    private lateinit var etFechaNac: TextInputEditText
    private lateinit var etTipoSangre: TextInputEditText
    private lateinit var etCurp: TextInputEditText
    private lateinit var etDireccion: TextInputEditText
    private lateinit var etTelefono: TextInputEditText
    private lateinit var etCorreo: TextInputEditText
    private lateinit var cbDiabetes: MaterialCheckBox
    private lateinit var cbHipertension: MaterialCheckBox
    private lateinit var cbCancer: MaterialCheckBox
    private lateinit var cbCardio: MaterialCheckBox
    private lateinit var cbObesidad: MaterialCheckBox
    private lateinit var cbRenal: MaterialCheckBox
    private lateinit var etAhfOtros: TextInputEditText
    private lateinit var etCirugias: TextInputEditText
    private lateinit var etAlergias: TextInputEditText
    private lateinit var etCronicas: TextInputEditText
    private lateinit var etHospitalizaciones: TextInputEditText
    private lateinit var etAppOtros: TextInputEditText
    private lateinit var etTalla: TextInputEditText
    private lateinit var etPeso: TextInputEditText
    private lateinit var etImc: TextInputEditText
    private lateinit var etFr: TextInputEditText
    private lateinit var btnGuardar: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
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

        val idExistente = arguments?.getString("pacienteId")
        if (!idExistente.isNullOrBlank()) {
            expedienteId = idExistente
            esNuevo      = false
            cargarDesdeCache(idExistente)
        }

        btnGuardar.setOnClickListener { guardar() }
    }

    private fun cargarDesdeCache(id: String) {
        btnGuardar.isEnabled = false
        btnGuardar.text = "Cargando..."

        val enCache = vm.expedientes.value.find { it.id == id }
        if (enCache != null) {
            poblarFormulario(enCache)
            btnGuardar.isEnabled = true
            btnGuardar.text = " + Guardar Paciente"
            return
        }
        viewLifecycleOwner.lifecycleScope.launch {
            vm.expedientes.collectLatest { lista ->
                val exp = lista.find { it.id == id } ?: return@collectLatest
                poblarFormulario(exp)
                btnGuardar.isEnabled = true
                btnGuardar.text = " + Guardar Paciente"
            }
        }
    }

    private fun poblarFormulario(exp: Expediente) {
        avatarColorIndexActual = exp.avatarColorIndex  // ← guarda el color al cargar
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

    private fun guardar() {
        if (etNombre.text.isNullOrBlank()) {
            Snackbar.make(requireView(), "El nombre es obligatorio", Snackbar.LENGTH_LONG).show()
            return
        }

        val nombre = etNombre.text.toString().trim()
        val sexo   = actvSexo.text.toString()
        val talla  = etTalla.text.toString().toDoubleOrNull() ?: 0.0
        val peso   = etPeso.text.toString().toDoubleOrNull()  ?: 0.0
        val imc    = if (talla > 0 && peso > 0) { val m = talla / 100.0; peso / (m * m) } else 0.0

        val expediente = Expediente(
            id                   = expedienteId,
            nombre               = nombre,
            edad                 = etEdad.text.toString().toIntOrNull() ?: 0,
            sexo                 = sexo,
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
            avatarColorIndex     = avatarColorIndexActual  // ← preserva el color existente
        )

        btnGuardar.isEnabled = false
        btnGuardar.text = "Guardando..."

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                expedienteId = repo.guardar(expediente)
                Snackbar.make(requireView(), "✅ Guardado", Snackbar.LENGTH_SHORT).show()
                delay(500)

                if (esNuevo) {
                    // Nuevo paciente → leer el paciente con color desde el ViewModel
                    // Esperar a que Firebase actualice el caché
                    var paciente: Paciente? = null
                    repeat(10) {
                        paciente = vm.pacienteConColor(expedienteId)
                        if (paciente != null) return@repeat
                        delay(200)
                    }
                    // Fallback si por alguna razón no llegó aún
                    if (paciente == null) {
                        paciente = Paciente(
                            id     = expedienteId,
                            nombre = nombre,
                            edad   = expediente.edad,
                            sexo   = when {
                                sexo.startsWith("F", ignoreCase = true) -> "F"
                                sexo.startsWith("M", ignoreCase = true) -> "M"
                                else -> ""
                            },
                            tipoSangre     = expediente.tipoSangre,
                            avatarColorRes = R.color.avatar_blue
                        )
                    }
                    parentFragmentManager.popBackStack()
                    val fragment = PacientePerfilFragment.newInstance(paciente!!)
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, fragment)
                        .addToBackStack(null)
                        .commit()
                } else {
                    parentFragmentManager.popBackStack()
                }

            } catch (e: Exception) {
                Snackbar.make(requireView(), "❌ Error: ${e.message}", Snackbar.LENGTH_LONG).show()
                btnGuardar.isEnabled = true
                btnGuardar.text = " + Guardar Paciente"
            }
        }
    }

    private fun setupSexoDropdown() {
        actvSexo.setAdapter(ArrayAdapter(requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            listOf("Masculino", "Femenino")))
    }

    private fun setupDatePicker() {
        etFechaNac.setOnClickListener { mostrarDatePicker(etFechaNac) }
        etFechaNac.setOnFocusChangeListener { _, f -> if (f) mostrarDatePicker(etFechaNac) }
    }

    private fun mostrarDatePicker(campo: TextInputEditText) {
        val cal = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, y, m, d ->
            cal.set(y, m, d); campo.setText(dateFormat.format(cal.time))
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
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

    private fun initViews(v: View) {
        etNombre            = v.findViewById(R.id.etNombre)
        etEdad              = v.findViewById(R.id.etEdad)
        actvSexo            = v.findViewById(R.id.actvSexo)
        etFechaNac          = v.findViewById(R.id.etFechaNac)
        etTipoSangre        = v.findViewById(R.id.etTipoSangre)
        etCurp              = v.findViewById(R.id.etCurp)
        etDireccion         = v.findViewById(R.id.etDireccion)
        etTelefono          = v.findViewById(R.id.etTelefono)
        etCorreo            = v.findViewById(R.id.etCorreo)
        cbDiabetes          = v.findViewById(R.id.cbAhfDiabetes)
        cbHipertension      = v.findViewById(R.id.cbAhfHipertension)
        cbCancer            = v.findViewById(R.id.cbAhfCancer)
        cbCardio            = v.findViewById(R.id.cbAhfCardio)
        cbObesidad          = v.findViewById(R.id.cbAhfObesidad)
        cbRenal             = v.findViewById(R.id.cbAhfRenal)
        etAhfOtros          = v.findViewById(R.id.etAhfOtros)
        etCirugias          = v.findViewById(R.id.etAppCirugias)
        etAlergias          = v.findViewById(R.id.etAppAlergias)
        etCronicas          = v.findViewById(R.id.etAppCronicas)
        etHospitalizaciones = v.findViewById(R.id.etAppHospitalizaciones)
        etAppOtros          = v.findViewById(R.id.etAppOtros)
        etTalla             = v.findViewById(R.id.etEfTalla)
        etPeso              = v.findViewById(R.id.etEfPeso)
        etImc               = v.findViewById(R.id.etEfImc)
        etFr                = v.findViewById(R.id.etEfFr)
        btnGuardar          = v.findViewById(R.id.btnGuardar)
    }
}