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
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.historialclinico.R
import com.example.historialclinico.data.models.Paciente
import com.example.historialclinico.ui.adapters.ConsultaSimulada
import com.example.historialclinico.ui.adapters.ConsultasAdapter
import com.google.android.material.button.MaterialButton
import com.example.historialclinico.ui.fragments.ConsultaFragment

class PacientePerfilFragment : Fragment() {

    private lateinit var paciente: Paciente

    private lateinit var btnExpediente: Button
    private lateinit var btnConsultas: Button
    private lateinit var layoutExpediente: LinearLayout
    private lateinit var layoutConsultas: LinearLayout
    private lateinit var btnEditarExpediente: MaterialButton
    private lateinit var btnNuevaConsulta: MaterialButton

    private val colorVerde      = 0xFF1D9E75.toInt()
    private val colorVerdeTexto = 0xFF2E7D32.toInt()
    private val colorFondoClaro = 0xFFE8F5E9.toInt()
    private val colorBlanco     = 0xFFFFFFFF.toInt()

    companion object {
        private const val ARG_PACIENTE = "arg_paciente"

        fun newInstance(paciente: Paciente): PacientePerfilFragment {
            val fragment = PacientePerfilFragment()
            val args = Bundle()
            args.putSerializable(ARG_PACIENTE, paciente)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        paciente = arguments?.getSerializable(ARG_PACIENTE) as? Paciente ?: Paciente()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_paciente_perfil, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        enlazarVistas(view)
        llenarDatosPaciente(view)
        configurarListaConsultas(view)
        configurarBotones()
        mostrarExpediente() // Expediente activo por defecto
    }

    private fun enlazarVistas(view: View) {
        btnExpediente       = view.findViewById(R.id.btnExpediente)
        btnConsultas        = view.findViewById(R.id.btnConsultas)
        layoutExpediente    = view.findViewById(R.id.layoutExpediente)
        layoutConsultas     = view.findViewById(R.id.layoutConsultas)
        btnEditarExpediente = view.findViewById(R.id.btnEditarExpediente)
        btnNuevaConsulta    = view.findViewById(R.id.btnNuevaConsulta)
    }

    private fun llenarDatosPaciente(view: View) {
        val iniciales = paciente.nombre
            .split(" ")
            .take(2)
            .joinToString("") { it.first().toString() }
            .uppercase()

        view.findViewById<TextView>(R.id.tvAvatarInitials).text = iniciales
        view.findViewById<TextView>(R.id.tvNombrePaciente).text = paciente.nombre
        view.findViewById<TextView>(R.id.tvInfoPaciente).text   =
            "${paciente.edad} años | ${paciente.sexo} | ${paciente.tipoSangre}"
    }

    // ----------------------------------------------------------------
    // RecyclerView con datos simulados
    // ----------------------------------------------------------------
    private fun configurarListaConsultas(view: View) {
        val consultasSimuladas = listOf(
            ConsultaSimulada("1", "08 Abril 2026",      "10:30 AM", "Dolor de cabeza recurrente"),
            ConsultaSimulada("2", "02 Enero 2026",      "2:04 PM",  "Control de presión arterial"),
            ConsultaSimulada("3", "10 Octubre 2025",    "6:24 PM",  "Dolor de garganta regular"),
            ConsultaSimulada("4", "26 Septiembre 2025", "12:14 PM", "Control de presión arterial"),
            ConsultaSimulada("5", "16 Junio 2025",      "10:45 AM", "123456789012345678901234567890123456789012345678901234567890")
        )

        val rv = view.findViewById<RecyclerView>(R.id.rvConsultas)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = ConsultasAdapter(consultasSimuladas) { consulta ->
            // Por ahora solo muestra un Toast — aquí irá la navegación después
            Toast.makeText(requireContext(), "Consulta: ${consulta.motivo}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun configurarBotones() {
        view?.findViewById<ImageButton>(R.id.btnBack)?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        btnExpediente.setOnClickListener { mostrarExpediente() }
        btnConsultas.setOnClickListener  { mostrarConsultas()  }

        btnEditarExpediente.setOnClickListener { }
        btnNuevaConsulta.setOnClickListener {
            val fragment = ConsultaFragment().apply {
                arguments = Bundle().apply {
                    putSerializable("paciente", paciente)
                }
            }
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

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