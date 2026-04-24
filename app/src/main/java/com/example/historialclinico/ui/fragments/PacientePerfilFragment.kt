package com.example.historialclinico.ui.fragments

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.historialclinico.R
import com.example.historialclinico.data.database.ConsultaRepository
import com.example.historialclinico.data.database.ExpedienteRepository
import com.example.historialclinico.data.models.Expediente
import com.example.historialclinico.data.models.Paciente
import com.example.historialclinico.ui.activities.MainActivity
import com.example.historialclinico.ui.adapters.ConsultasAdapter
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

class PacientePerfilFragment : Fragment() {

    private lateinit var paciente: Paciente

    private lateinit var btnExpediente: Button
    private lateinit var btnConsultas: Button
    private lateinit var layoutExpediente: LinearLayout
    private lateinit var layoutConsultas: LinearLayout
    private lateinit var btnEditarExpediente: MaterialButton
    private lateinit var btnNuevaConsulta: MaterialButton
    private lateinit var rvConsultas: RecyclerView

    private val expRepo      = ExpedienteRepository()
    private val consultaRepo = ConsultaRepository()

    private val avatarColors = listOf(
        R.color.avatar_yellow,
        R.color.avatar_purple,
        R.color.avatar_red,
        R.color.avatar_blue,
        R.color.avatar_green
    )

    private val colorVerde      = 0xFF1D9E75.toInt()
    private val colorVerdeTexto = 0xFF2E7D32.toInt()
    private val colorFondoClaro = 0xFFE8F5E9.toInt()
    private val colorBlanco     = 0xFFFFFFFF.toInt()

    companion object {
        private const val ARG_PACIENTE = "arg_paciente"
        fun newInstance(paciente: Paciente) = PacientePerfilFragment().apply {
            arguments = Bundle().apply { putSerializable(ARG_PACIENTE, paciente) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        paciente = arguments?.getSerializable(ARG_PACIENTE) as? Paciente ?: Paciente()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_paciente_perfil, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enlazarVistas(view)
        llenarHeader(view)
        configurarBotones()
        mostrarExpediente()

        if (paciente.id.isNotBlank()) {
            cargarExpediente(view)
            cargarConsultas()
        }
    }

    // ── Header ──────────────────────────────────────────────────────────

    private fun llenarHeader(view: View) {
        val iniciales = paciente.nombre.split(" ")
            .take(2).joinToString("") { it.firstOrNull()?.toString() ?: "" }.uppercase()

        val tvIniciales = view.findViewById<TextView>(R.id.tvAvatarInitials)
        tvIniciales.text = iniciales

        val colorRes = avatarColors[paciente.id.hashCode().absoluteValue % avatarColors.size]
        val color    = ContextCompat.getColor(requireContext(), colorRes)
        tvIniciales.backgroundTintList = ColorStateList.valueOf(color)

        view.findViewById<TextView>(R.id.tvNombrePaciente).text = paciente.nombre
        actualizarSubtitulo(view, paciente.edad, paciente.sexo, paciente.tipoSangre)
    }

    private fun actualizarSubtitulo(view: View, edad: Int, sexo: String, tipoSangre: String) {
        val sangre = if (tipoSangre.isNotBlank()) " | $tipoSangre" else ""
        view.findViewById<TextView>(R.id.tvInfoPaciente).text = "${edad} años | ${sexo}$sangre"
    }

    // ── Expediente ───────────────────────────────────────────────────────

    private fun cargarExpediente(view: View) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val exp = expRepo.obtener(paciente.id) ?: return@launch
                actualizarSubtitulo(view, exp.edad, exp.sexo, exp.tipoSangre)
                llenarDatosExpediente(view, exp)
            } catch (_: Exception) {}
        }
    }

    private fun llenarDatosExpediente(view: View, exp: Expediente) {
        set(view, R.id.tvPhone,  exp.telefono.ifBlank { "—" })
        set(view, R.id.tvEmail,  exp.correo.ifBlank { "—" })
        set(view, R.id.tvFechaNacimiento, exp.fechaNacimiento.ifBlank { "—" })
        set(view, R.id.tvCURP,   exp.curp.ifBlank { "—" })
        set(view, R.id.tvAltura, if (exp.efTalla > 0) "${exp.efTalla} cm" else "—")
        set(view, R.id.tvPeso,   if (exp.efPeso  > 0) "${exp.efPeso} kg"  else "—")
        set(view, R.id.tvIMC,    if (exp.efImc   > 0)
            String.format("%.1f  (${exp.categoriaImc()})", exp.efImc) else "—")

        val ahf = buildString {
            if (exp.ahfDiabetes)     append("Diabetes, ")
            if (exp.ahfHipertension) append("Hipertensión, ")
            if (exp.ahfCancer)       append("Cáncer, ")
            if (exp.ahfCardio)       append("Cardiopatía, ")
            if (exp.ahfObesidad)     append("Obesidad, ")
            if (exp.ahfRenal)        append("Renal, ")
            if (exp.ahfOtros.isNotBlank()) append(exp.ahfOtros)
        }.trimEnd(',', ' ')
        set(view, R.id.tvAntecedentesHeredofamiliares, ahf.ifBlank { "Sin antecedentes" })
        set(view, R.id.tvCirugiasPrevias,          exp.appCirugias.ifBlank { "—" })
        set(view, R.id.tvAlergias,                 exp.appAlergias.ifBlank { "—" })
        set(view, R.id.tvEnfermedadesCronicas,     exp.appCronicas.ifBlank { "—" })
        set(view, R.id.tvHospitalizacionesPrevias, exp.appHospitalizaciones.ifBlank { "—" })
        set(view, R.id.tvOtrosAntecedentes,        exp.appOtros.ifBlank { "—" })
    }

    private fun set(view: View, id: Int, text: String) {
        try { view.findViewById<TextView>(id)?.text = text } catch (_: Exception) {}
    }

    // ── Consultas — click abre VerConsultaFragment ──────────────────────

    private fun cargarConsultas() {
        rvConsultas.layoutManager = LinearLayoutManager(requireContext())
        viewLifecycleOwner.lifecycleScope.launch {
            consultaRepo.listarFlow(paciente.id).collectLatest { consultas ->
                rvConsultas.adapter = ConsultasAdapter(consultas) { consulta ->
                    (requireActivity() as MainActivity)
                        .navegarAVerConsulta(paciente, consulta.id)   // ← ver, no editar
                }
            }
        }
    }

    // ── Vistas ─────────────────────────────────────────────────────────

    private fun enlazarVistas(view: View) {
        btnExpediente       = view.findViewById(R.id.btnExpediente)
        btnConsultas        = view.findViewById(R.id.btnConsultas)
        layoutExpediente    = view.findViewById(R.id.layoutExpediente)
        layoutConsultas     = view.findViewById(R.id.layoutConsultas)
        btnEditarExpediente = view.findViewById(R.id.btnEditarExpediente)
        btnNuevaConsulta    = view.findViewById(R.id.btnNuevaConsulta)
        rvConsultas         = view.findViewById(R.id.rvConsultas)
    }

    // ── Botones ─────────────────────────────────────────────────────────

    private fun configurarBotones() {
        view?.findViewById<ImageButton>(R.id.btnBack)?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        btnExpediente.setOnClickListener { mostrarExpediente() }
        btnConsultas.setOnClickListener  { mostrarConsultas()  }
        btnEditarExpediente.setOnClickListener {
            (requireActivity() as MainActivity).navegarAExpediente(pacienteId = paciente.id)
        }
        btnNuevaConsulta.setOnClickListener {
            (requireActivity() as MainActivity).navegarAConsulta(paciente, consultaId = null)
        }
    }

    // ── Tabs ────────────────────────────────────────────────────────────

    private fun mostrarExpediente() {
        layoutExpediente.visibility    = View.VISIBLE
        layoutConsultas.visibility     = View.GONE
        btnEditarExpediente.visibility = View.VISIBLE
        btnNuevaConsulta.visibility    = View.GONE
        btnExpediente.backgroundTintList = ColorStateList.valueOf(colorVerde)
        btnExpediente.setTextColor(colorBlanco)
        btnConsultas.backgroundTintList  = ColorStateList.valueOf(colorFondoClaro)
        btnConsultas.setTextColor(colorVerdeTexto)
    }

    private fun mostrarConsultas() {
        layoutExpediente.visibility    = View.GONE
        layoutConsultas.visibility     = View.VISIBLE
        btnEditarExpediente.visibility = View.GONE
        btnNuevaConsulta.visibility    = View.VISIBLE
        btnConsultas.backgroundTintList  = ColorStateList.valueOf(colorVerde)
        btnConsultas.setTextColor(colorBlanco)
        btnExpediente.backgroundTintList = ColorStateList.valueOf(colorFondoClaro)
        btnExpediente.setTextColor(colorVerdeTexto)
    }
}
