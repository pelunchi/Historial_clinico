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
import androidx.fragment.app.Fragment
import com.example.historialclinico.R
import com.example.historialclinico.data.models.Paciente
import com.google.android.material.button.MaterialButton

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

    private fun configurarBotones() {
        view?.findViewById<ImageButton>(R.id.btnBack)?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        btnExpediente.setOnClickListener { mostrarExpediente() }
        btnConsultas.setOnClickListener  { mostrarConsultas()  }

        // Por ahora sin acción — se implementan después
        btnEditarExpediente.setOnClickListener { }
        btnNuevaConsulta.setOnClickListener    { }
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