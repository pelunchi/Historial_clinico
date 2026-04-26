package com.example.historialclinico.ui.fragments

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.historialclinico.R
import com.example.historialclinico.data.database.ConsultaRepository
import com.example.historialclinico.data.models.Consulta
import com.example.historialclinico.data.models.Paciente
import com.example.historialclinico.ui.activities.MainActivity
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

class VerConsultaFragment : Fragment() {

    private var paciente: Paciente? = null
    private var consultaId: String = ""

    private val repo = ConsultaRepository()

    private val avatarColors = listOf(
        R.color.avatar_yellow,
        R.color.avatar_purple,
        R.color.avatar_red,
        R.color.avatar_blue,
        R.color.avatar_green
    )

    companion object {
        fun newInstance(paciente: Paciente, consultaId: String) =
            VerConsultaFragment().apply {
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
    ): View = inflater.inflate(R.layout.fragment_ver_consulta, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        llenarHeader(view)
        cargarConsulta(view)

        view.findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        view.findViewById<MaterialButton>(R.id.btnEditarConsulta).setOnClickListener {
            paciente?.let { p ->
                (requireActivity() as MainActivity).navegarAConsulta(p, consultaId)
            }
        }
    }

    // ── Header del paciente ────────────────────────────────────────────

    private fun llenarHeader(view: View) {
        val p = paciente ?: return

        val iniciales = p.nombre.split(" ")
            .take(2).joinToString("") { it.firstOrNull()?.toString() ?: "" }.uppercase()

        val tvIniciales = view.findViewById<TextView>(R.id.tvAvatarInitials)
        tvIniciales.text = iniciales

        val colorRes = avatarColors[p.id.hashCode().absoluteValue % avatarColors.size]
        val color    = ContextCompat.getColor(requireContext(), colorRes)
        tvIniciales.backgroundTintList = ColorStateList.valueOf(color)

        view.findViewById<TextView>(R.id.tvNombrePaciente).text = p.nombre

        val tipoSangre = if (p.tipoSangre.isNotBlank()) " | ${p.tipoSangre}" else ""
        view.findViewById<TextView>(R.id.tvInfoPaciente).text =
            "${p.edad} años | ${p.sexo}$tipoSangre"
    }

    // ── Cargar datos desde Firebase ────────────────────────────────────

    private fun cargarConsulta(view: View) {
        val expId = paciente?.id ?: return

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val consulta = repo.obtener(expId, consultaId) ?: return@launch
                llenarVistas(view, consulta)
            } catch (_: Exception) {}
        }
    }

    private fun llenarVistas(view: View, c: Consulta) {
        // Información de consulta
        set(view, R.id.tvFechaConsulta,    c.fecha.ifBlank { "—" })
        set(view, R.id.tvHoraConsulta,     c.hora.ifBlank { "—" })
        set(view, R.id.tvMotivoConsulta,   c.motivo.ifBlank { "—" })
        set(view, R.id.tvSintomasConsulta, c.sintomas.ifBlank { "—" })
        set(view, R.id.tvDiagnosticoConsulta, c.diagnostico.ifBlank { "—" })
        set(view, R.id.tvNotasConsulta,    c.resumen.ifBlank { "—" })
        set(view, R.id.tvIndicacionesConsulta, c.indicaciones.ifBlank { "—" })
        set(view, R.id.tvRecetaConsulta,   c.receta.ifBlank { "—" })

        // Exploración física
        set(view, R.id.tvAlturaConsulta,   if (c.efTalla > 0) "${c.efTalla} cm" else "—")
        set(view, R.id.tvPesoConsulta,     if (c.efPeso  > 0) "${c.efPeso} kg"  else "—")
        set(view, R.id.tvIMCConsulta,      if (c.efImc   > 0) String.format("%.1f", c.efImc) else "—")
        set(view, R.id.tvPresionConsulta,  c.efPresionArterial.ifBlank { "—" })
        set(view, R.id.tvFCConsulta,       if (c.efFrecCardiaca    > 0) "${c.efFrecCardiaca} lpm" else "—")
        set(view, R.id.tvTempConsulta,     if (c.efTemperatura     > 0) "${c.efTemperatura} °C"   else "—")
        set(view, R.id.tvSpOConsulta,      if (c.efSpo2            > 0) "${c.efSpo2} %"           else "—")
        set(view, R.id.tvFrecRespiratoriaConsulta,
            if (c.efFrecRespiratoria > 0) "${c.efFrecRespiratoria} rpm" else "—")
        set(view, R.id.tvObservacionesFisicasConsulta, c.efObservaciones.ifBlank { "—" })
    }

    private fun set(view: View, id: Int, text: String) {
        try { view.findViewById<TextView>(id)?.text = text } catch (_: Exception) {}
    }
}
