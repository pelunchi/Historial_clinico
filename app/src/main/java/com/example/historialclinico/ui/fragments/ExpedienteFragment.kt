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
import android.widget.EditText

class ExpedienteFragment : Fragment() {

    private val repo = ExpedienteRepository()
    private val vm: AppViewModel by activityViewModels()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    private lateinit var tilFechaNac: com.google.android.material.textfield.TextInputLayout
    private lateinit var etFechaDia: android.widget.EditText
    private lateinit var etFechaMes: android.widget.EditText
    private lateinit var etFechaAnio: android.widget.EditText

    private var expedienteId: String = ""
    private var esNuevo = true
    private var avatarColorIndexActual: Int = -1

    private lateinit var etNombre: TextInputEditText
    private lateinit var etEdad: TextInputEditText
    private lateinit var actvSexo: AutoCompleteTextView
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

    // ── Lógica de edad automática ──────────────────────────────────────

    /**
     * Construye la fecha "dd/MM/yyyy" desde los tres campos y devuelve
     * la edad calculada. Si la fecha está incompleta devuelve 0.
     */
    private fun edadDesdeCampos(): Int {
        val d = etFechaDia.text.toString().padStart(2, '0')
        val m = etFechaMes.text.toString().padStart(2, '0')
        val a = etFechaAnio.text.toString()
        if (d == "00" || m == "00" || a.length < 4) return 0
        return vm.calcularEdadDesde("$d/$m/$a")
    }

    /**
     * Actualiza el campo edad y su estado habilitado/deshabilitado
     * según si hay una fecha de nacimiento completa y válida.
     *   - Con fecha válida  → calcula la edad, muestra, deshabilita el campo
     *   - Sin fecha válida  → habilita el campo para edición manual
     */
    private fun actualizarCampoEdad() {
        val edad = edadDesdeCampos()
        if (edad > 0) {
            // Fecha completa y válida → mostrar edad calculada, deshabilitar campo
            etEdad.setText(edad.toString())
            etEdad.isEnabled = false
            etEdad.alpha = 0.6f
        } else {
            // Fecha incompleta/vacía → dejar editable
            etEdad.isEnabled = true
            etEdad.alpha = 1.0f
            // Limpiar solo si el campo mostraba una edad calculada previamente
            // (si el año se borró, limpiar para no dejar dato inconsistente)
            val anio = etFechaAnio.text.toString()
            val dia  = etFechaDia.text.toString()
            val mes  = etFechaMes.text.toString()
            if (dia.isBlank() && mes.isBlank() && anio.isBlank()) {
                etEdad.setText("")
            }
        }
    }

    // ── Carga y llenado ───────────────────────────────────────────────

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
        avatarColorIndexActual = exp.avatarColorIndex
        etNombre.setText(exp.nombre)
        actvSexo.setText(exp.sexo, false)

        val partes = exp.fechaNacimiento.split("/")
        if (partes.size == 3) {
            etFechaDia.setText(partes[0])
            etFechaMes.setText(partes[1])
            etFechaAnio.setText(partes[2])
            // La fecha está completa: actualizarCampoEdad() calculará y deshabilitará
        }

        // Siempre mostrar la edad que viene del expediente (ya recalculada por el ViewModel)
        etEdad.setText(if (exp.edad > 0) exp.edad.toString() else "")

        // Aplicar estado correcto del campo según si hay fecha
        actualizarCampoEdad()

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

    // ── Guardar ───────────────────────────────────────────────────────

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

        val fechaNacimiento = run {
            val d = etFechaDia.text.toString().padStart(2, '0')
            val m = etFechaMes.text.toString().padStart(2, '0')
            val a = etFechaAnio.text.toString()
            if (d == "00" || m == "00" || a.isEmpty()) "" else "$d/$m/$a"
        }

        // Si hay fecha válida, usar edad calculada; si no, usar lo que escribió el doctor
        val edadCalculada = vm.calcularEdadDesde(fechaNacimiento)
        val edadFinal = if (edadCalculada > 0) edadCalculada
        else etEdad.text.toString().toIntOrNull() ?: 0

        val expediente = Expediente(
            id                   = expedienteId,
            nombre               = nombre,
            edad                 = edadFinal,
            sexo                 = sexo,
            fechaNacimiento      = fechaNacimiento,
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
            avatarColorIndex     = avatarColorIndexActual
        )

        btnGuardar.isEnabled = false
        btnGuardar.text = "Guardando..."

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                expedienteId = repo.guardar(expediente)
                Snackbar.make(requireView(), "✅ Guardado", Snackbar.LENGTH_SHORT).show()
                delay(500)

                if (esNuevo) {
                    var paciente: Paciente? = null
                    repeat(10) {
                        paciente = vm.pacienteConColor(expedienteId)
                        if (paciente != null) return@repeat
                        delay(200)
                    }
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

    // ── Setup de vistas ───────────────────────────────────────────────

    private fun setupSexoDropdown() {
        actvSexo.setAdapter(ArrayAdapter(requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            listOf("Masculino", "Femenino")))
    }

    private fun setupDatePicker() {
        val colorActivo   = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.section_ficha)
        val colorInactivo = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.borde_formulario_gris)

        val activarTil = { hasFocus: Boolean ->
            val color = if (hasFocus) colorActivo else colorInactivo
            val states = arrayOf(
                intArrayOf(android.R.attr.state_focused),
                intArrayOf(-android.R.attr.state_focused),
                intArrayOf()
            )
            val colors = intArrayOf(color, color, color)
            tilFechaNac.setBoxStrokeColorStateList(
                android.content.res.ColorStateList(states, colors)
            )
            tilFechaNac.boxStrokeColor = color
            tilFechaNac.invalidate()
        }

        etFechaDia.filters = arrayOf(android.text.InputFilter { source, _, _, dest, _, _ ->
            val resultado = (dest.toString() + source.toString()).toIntOrNull() ?: return@InputFilter ""
            if (resultado > 31) "" else null
        }, android.text.InputFilter.LengthFilter(2))

        etFechaMes.filters = arrayOf(android.text.InputFilter { source, _, _, dest, _, _ ->
            val resultado = (dest.toString() + source.toString()).toIntOrNull() ?: return@InputFilter ""
            if (resultado > 12) "" else null
        }, android.text.InputFilter.LengthFilter(2))

        etFechaAnio.filters = arrayOf(android.text.InputFilter.LengthFilter(4))

        etFechaDia.setOnFocusChangeListener  { _, f -> if (f) activarTil(true) else if (!etFechaMes.hasFocus() && !etFechaAnio.hasFocus()) activarTil(false) }
        etFechaMes.setOnFocusChangeListener  { _, f -> if (f) activarTil(true) else if (!etFechaDia.hasFocus() && !etFechaAnio.hasFocus()) activarTil(false) }
        etFechaAnio.setOnFocusChangeListener { _, f -> if (f) activarTil(true) else if (!etFechaDia.hasFocus() && !etFechaMes.hasFocus()) activarTil(false) }

        // Watcher compartido: recalcula edad cuando cualquier campo de fecha cambia
        val fechaWatcher = object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                actualizarCampoEdad()
            }
        }

        // Al escribir 2 dígitos en DIA → salta a MES
        etFechaDia.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                if ((s?.length ?: 0) == 2) etFechaMes.requestFocus()
                actualizarCampoEdad()
            }
        })

        // Al escribir 2 dígitos en MES → salta a AÑO
        etFechaMes.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                if ((s?.length ?: 0) == 2) etFechaAnio.requestFocus()
                actualizarCampoEdad()
            }
        })

        // AÑO: recalcula al llegar a 4 dígitos o al borrar
        etFechaAnio.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                actualizarCampoEdad()
            }
        })

        // Backspace en MES vacío → regresa a DIA
        etFechaMes.setOnKeyListener { _, keyCode, event ->
            if (event.action == android.view.KeyEvent.ACTION_DOWN &&
                keyCode == android.view.KeyEvent.KEYCODE_DEL &&
                etFechaMes.text.isNullOrEmpty()) {
                etFechaDia.requestFocus()
                etFechaDia.setSelection(etFechaDia.text?.length ?: 0)
                true
            } else false
        }

        // Backspace en AÑO vacío → regresa a MES
        etFechaAnio.setOnKeyListener { _, keyCode, event ->
            if (event.action == android.view.KeyEvent.ACTION_DOWN &&
                keyCode == android.view.KeyEvent.KEYCODE_DEL &&
                etFechaAnio.text.isNullOrEmpty()) {
                etFechaMes.requestFocus()
                etFechaMes.setSelection(etFechaMes.text?.length ?: 0)
                true
            } else false
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

    private fun initViews(v: View) {
        etNombre            = v.findViewById(R.id.etNombre)
        etEdad              = v.findViewById(R.id.etEdad)
        actvSexo            = v.findViewById(R.id.actvSexo)

        tilFechaNac = v.findViewById(R.id.tilFechaNac)
        etFechaDia  = v.findViewById(R.id.etFechaDia)
        etFechaMes  = v.findViewById(R.id.etFechaMes)
        etFechaAnio = v.findViewById(R.id.etFechaAnio)

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