package com.example.historialclinico.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.historialclinico.R
import com.example.historialclinico.ui.activities.MainActivity
import com.example.historialclinico.utils.PreferencesManager

class InicioFragment : Fragment() {

    private lateinit var tvDoctorNombre: TextView
    private lateinit var tvConsultasHoy: TextView
    private lateinit var tvTotalPacientes: TextView
    private lateinit var etBuscar: EditText
    private lateinit var btnNuevoPaciente: Button
    private lateinit var llUltimasConsultas: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_inicio, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 🔥 Theme dinámico (lo que ya tenías)
        val prefs = PreferencesManager(requireContext())

        // 🔗 Bind de vistas
        tvDoctorNombre = view.findViewById(R.id.doctor_nombre)
        tvConsultasHoy = view.findViewById(R.id.tvConsultasHoy)
        tvTotalPacientes = view.findViewById(R.id.tvTotalPacientes)
        etBuscar = view.findViewById(R.id.etBuscar)
        btnNuevoPaciente = view.findViewById(R.id.btnNuevoPaciente)
        llUltimasConsultas = view.findViewById(R.id.llUltimasConsultas)

        // 🧠 Datos mock (luego aquí conectas DB)
        cargarDatos()

        // 🔍 Buscar (simple por ahora)
        etBuscar.setOnEditorActionListener { _, _, _ ->
            val query = etBuscar.text.toString()
            Toast.makeText(requireContext(), "Buscando: $query", Toast.LENGTH_SHORT).show()
            true
        }

        // ➕ Botón nuevo paciente
        btnNuevoPaciente.setOnClickListener {
            (requireActivity() as MainActivity)
                .navegarAExpediente(pacienteId = null)
        }
    }

    private fun cargarDatos() {
        // Nombre doctor
        tvDoctorNombre.text = "Dr. Juan Pérez"

        // Stats (simulados)
        tvConsultasHoy.text = "12"
        tvTotalPacientes.text = "87"

        // Lista simulada
        agregarPaciente("María González", "Hoy, 10:30 AM")
        agregarPaciente("Carlos López", "Ayer, 5:00 PM")
        // Lista simulada
        agregarPaciente("María González", "Hoy, 10:30 AM")
        agregarPaciente("Carlos López", "Ayer, 5:00 PM")
        // Lista simulada
        agregarPaciente("María González", "Hoy, 10:30 AM")
        agregarPaciente("Carlos López", "Ayer, 5:00 PM")// Lista simulada
        agregarPaciente("María González", "Hoy, 10:30 AM")
        agregarPaciente("Carlos López", "Ayer, 5:00 PM")
        // Lista simulada
        agregarPaciente("María González", "Hoy, 10:30 AM")
        agregarPaciente("Carlos López", "Ayer, 5:00 PM")// Lista simulada
        agregarPaciente("María González", "Hoy, 10:30 AM")
        agregarPaciente("Carlos López", "Ayer, 5:00 PM")


    }

    private fun agregarPaciente(nombre: String, info: String) {

        val item = layoutInflater.inflate(
            R.layout.item_paciente,
            llUltimasConsultas,
            false
        )

        val tvNombre = item.findViewById<TextView>(R.id.tvNombrePaciente)
        val tvInfo = item.findViewById<TextView>(R.id.tvInfoPaciente)
        val tvAvatar = item.findViewById<TextView>(R.id.tvAvatarInitials)

        tvNombre.text = nombre
        tvInfo.text = info

        // Iniciales
        tvAvatar.text = nombre.split(" ")
            .map { it.first() }
            .take(2)
            .joinToString("")

        llUltimasConsultas.addView(item)
    }


}