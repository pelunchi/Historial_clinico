package com.example.historialclinico.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.historialclinico.R
import com.example.historialclinico.data.database.ExpedienteRepository
import com.example.historialclinico.data.database.UserRepository
import com.example.historialclinico.ui.activities.MainActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.recyclerview.widget.RecyclerView
import com.example.historialclinico.data.database.ConsultaRepository
import com.example.historialclinico.ui.adapters.ConsultasAdapter

class InicioFragment : Fragment() {

    private lateinit var tvDoctorNombre: TextView
    private lateinit var tvConsultasHoy: TextView
    private lateinit var tvTotalPacientes: TextView
    private lateinit var etBuscar: EditText
    private lateinit var btnNuevoPaciente: Button
    private lateinit var llUltimasConsultas: RecyclerView

    private val expedienteRepo = ExpedienteRepository()
    private val userRepo = UserRepository()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_inicio, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvDoctorNombre     = view.findViewById(R.id.doctor_nombre)
        tvConsultasHoy     = view.findViewById(R.id.tvConsultasHoy)
        tvTotalPacientes   = view.findViewById(R.id.tvTotalPacientes)
        etBuscar           = view.findViewById(R.id.etBuscar)
        btnNuevoPaciente   = view.findViewById(R.id.btnNuevoPaciente)
        llUltimasConsultas = view.findViewById(R.id.llUltimasConsultas)
        llUltimasConsultas.layoutManager = LinearLayoutManager(requireContext())

        // ── Nombre del doctor desde Firebase ──────────────────────────
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val perfil = userRepo.obtenerPerfil()
                tvDoctorNombre.text = if (!perfil?.nombre.isNullOrBlank())
                    "Dr. ${perfil!!.nombre}" else "Doctor"
            } catch (_: Exception) {
                tvDoctorNombre.text = "Doctor"
            }
        }

        // ── Estadísticas y últimas consultas desde Firebase ───────────
        val consultaRepo = ConsultaRepository()

        viewLifecycleOwner.lifecycleScope.launch {
            consultaRepo.listarTodasFlow().collectLatest { lista ->

                val ultimas = lista
                    .sortedByDescending { it.fechaTimestamp }
                    .take(5)

                llUltimasConsultas.adapter = ConsultasAdapter(ultimas) { consulta ->
                    // navegación opcional
                }
            }
        }

        // ── Buscar ─────────────────────────────────────────────────────
        etBuscar.setOnEditorActionListener { _, _, _ ->
            val query = etBuscar.text.toString().trim()
            if (query.isNotEmpty()) {
                (requireActivity() as MainActivity).selectNavItem(R.id.nav_pacientes)
            }
            true
        }

        // ── Nuevo paciente ─────────────────────────────────────────────
        btnNuevoPaciente.setOnClickListener {
            (requireActivity() as MainActivity).navegarAExpediente(pacienteId = null)
        }
    }

    private fun agregarItem(nombre: String, info: String) {
        val item = layoutInflater.inflate(R.layout.item_paciente, llUltimasConsultas, false)
        item.findViewById<TextView>(R.id.tvNombrePaciente).text = nombre
        item.findViewById<TextView>(R.id.tvInfoPaciente).text   = info
        item.findViewById<TextView>(R.id.tvAvatarInitials).text =
            nombre.split(" ").map { it.firstOrNull() ?: ' ' }.take(2).joinToString("")
        llUltimasConsultas.addView(item)
    }

    private fun formatFecha(ts: Long): String {
        val fmt  = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val cal  = Calendar.getInstance().apply { timeInMillis = ts }
        val hoy  = Calendar.getInstance()
        val ayer = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        return when {
            mismaFecha(cal, hoy)  -> "Hoy, ${fmt.format(Date(ts))}"
            mismaFecha(cal, ayer) -> "Ayer, ${fmt.format(Date(ts))}"
            else -> SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(ts))
        }
    }

    private fun mismaFecha(a: Calendar, b: Calendar) =
        a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
                a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)
}

private fun MainActivity.selectNavItem(navPacientes: Int) {}
