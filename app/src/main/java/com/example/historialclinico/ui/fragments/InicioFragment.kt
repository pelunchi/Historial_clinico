package com.example.historialclinico.ui.fragments

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.historialclinico.R
import com.example.historialclinico.data.database.ConsultaRepository
import com.example.historialclinico.data.database.ExpedienteRepository
import com.example.historialclinico.data.database.UserRepository
import com.example.historialclinico.data.models.Consulta
import com.example.historialclinico.data.models.Expediente
import com.example.historialclinico.data.models.Paciente
import com.example.historialclinico.ui.activities.MainActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.absoluteValue

class InicioFragment : Fragment() {

    private lateinit var tvDoctorNombre: TextView
    private lateinit var tvConsultasHoy: TextView
    private lateinit var tvTotalPacientes: TextView
    private lateinit var etBuscar: EditText
    private lateinit var btnNuevoPaciente: Button
    private lateinit var rvUltimasConsultas: RecyclerView

    private val expedienteRepo = ExpedienteRepository()
    private val consultaRepo   = ConsultaRepository()
    private val userRepo       = UserRepository()

    private val avatarColors = listOf(
        R.color.avatar_yellow,
        R.color.avatar_purple,
        R.color.avatar_red,
        R.color.avatar_blue,
        R.color.avatar_green
    )

    // Modelo temporal para cada fila del RecyclerView
    private data class ItemInicio(val expediente: Expediente, val consulta: Consulta)

    // Adapter inline — no necesita archivo separado
    private inner class InicioAdapter(
        private val items: List<ItemInicio>
    ) : RecyclerView.Adapter<InicioAdapter.VH>() {

        inner class VH(val view: View) : RecyclerView.ViewHolder(view)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            VH(LayoutInflater.from(parent.context).inflate(R.layout.item_paciente, parent, false))

        override fun getItemCount() = items.size

        override fun onBindViewHolder(holder: VH, position: Int) {
            val exp      = items[position].expediente
            val consulta = items[position].consulta
            val v        = holder.view

            v.findViewById<TextView>(R.id.tvNombrePaciente).text = exp.nombre
            v.findViewById<TextView>(R.id.tvInfoPaciente).text = buildString {
                if (consulta.fecha.isNotBlank()) append(consulta.fecha)
                if (consulta.hora.isNotBlank()) append("  ${consulta.hora}")
            }

            val iniciales = exp.nombre.split(" ")
                .take(2).joinToString("") { it.firstOrNull()?.toString() ?: "" }.uppercase()
            val tvIniciales = v.findViewById<TextView>(R.id.tvAvatarInitials)
            tvIniciales.text = iniciales

            val colorRes = avatarColors[exp.id.hashCode().absoluteValue % avatarColors.size]
            tvIniciales.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(requireContext(), colorRes))

            val paciente = Paciente(
                id             = exp.id,
                nombre         = exp.nombre,
                edad           = exp.edad,
                sexo           = if (exp.sexo.startsWith("F", ignoreCase = true)) "F" else "M",
                tipoSangre     = exp.tipoSangre,
                avatarColorRes = colorRes
            )
            v.setOnClickListener {
                (requireActivity() as MainActivity).navegarAVerConsulta(paciente, consulta.id)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_inicio, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvDoctorNombre     = view.findViewById(R.id.doctor_nombre)
        tvConsultasHoy     = view.findViewById(R.id.tvConsultasHoy)
        tvTotalPacientes   = view.findViewById(R.id.tvTotalPacientes)
        etBuscar           = view.findViewById(R.id.etBuscar)
        btnNuevoPaciente   = view.findViewById(R.id.btnNuevoPaciente)
        rvUltimasConsultas = view.findViewById(R.id.llUltimasConsultas)

        rvUltimasConsultas.layoutManager = LinearLayoutManager(requireContext())

        // Nombre del doctor
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val perfil = userRepo.obtenerPerfil()
                tvDoctorNombre.text =
                    if (!perfil?.nombre.isNullOrBlank()) "Dr. ${perfil!!.nombre}" else "Doctor"
            } catch (_: Exception) { tvDoctorNombre.text = "Doctor" }
        }

        // Total de pacientes
        viewLifecycleOwner.lifecycleScope.launch {
            expedienteRepo.listarFlow().collectLatest { lista ->
                tvTotalPacientes.text = lista.size.toString()
            }
        }

        // Pacientes con consulta reciente
        viewLifecycleOwner.lifecycleScope.launch {
            val expedientesCache = mutableMapOf<String, Expediente>()

            // Caché de expedientes actualizado en tiempo real
            launch {
                expedienteRepo.listarFlow().collectLatest { lista ->
                    expedientesCache.clear()
                    lista.forEach { expedientesCache[it.id] = it }
                }
            }

            // Consultas → cruzar con caché → mostrar
            consultaRepo.listarTodasFlow().collectLatest { consultas ->
                val fmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                tvConsultasHoy.text = consultas.count { it.fecha == fmt.format(Date()) }.toString()

                val items = consultas
                    .map { it.expedienteId }
                    .distinct()
                    .take(5)
                    .mapNotNull { expId ->
                        val exp = expedientesCache[expId] ?: return@mapNotNull null
                        ItemInicio(exp, consultas.first { it.expedienteId == expId })
                    }

                rvUltimasConsultas.adapter = InicioAdapter(items)
            }
        }

        etBuscar.setOnEditorActionListener { _, _, _ ->
            if (etBuscar.text.toString().isNotEmpty())
                (requireActivity() as MainActivity).selectNavItem(R.id.nav_pacientes)
            true
        }

        btnNuevoPaciente.setOnClickListener {
            (requireActivity() as MainActivity).navegarAExpediente(pacienteId = null)
        }
    }
}
