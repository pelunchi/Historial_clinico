package com.example.historialclinico.ui.fragments

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.historialclinico.R
import com.example.historialclinico.data.models.Consulta
import com.example.historialclinico.data.models.Expediente
import com.example.historialclinico.data.models.Paciente
import com.example.historialclinico.ui.activities.MainActivity
import com.example.historialclinico.ui.viewmodel.AppViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class InicioFragment : Fragment() {

    private val vm: AppViewModel by activityViewModels()

    private lateinit var tvDoctorNombre: TextView

    private lateinit var layoutSinDatos: LinearLayout
    private lateinit var tvConsultasHoy: TextView
    private lateinit var tvTotalPacientes: TextView
    private lateinit var etBuscar: EditText
    private lateinit var btnNuevoPaciente: Button
    private lateinit var rvUltimasConsultas: RecyclerView

    private data class ItemInicio(val paciente: Paciente, val consulta: Consulta)

    private val todosLosItems = mutableListOf<ItemInicio>()

    private inner class InicioAdapter(
        private val items: List<ItemInicio>
    ) : RecyclerView.Adapter<InicioAdapter.VH>() {

        inner class VH(val view: View) : RecyclerView.ViewHolder(view)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            VH(LayoutInflater.from(parent.context).inflate(R.layout.item_paciente, parent, false))

        override fun getItemCount() = items.size

        override fun onBindViewHolder(holder: VH, position: Int) {
            val paciente = items[position].paciente
            val consulta = items[position].consulta
            val v        = holder.view

            v.findViewById<TextView>(R.id.tvNombrePaciente).text = paciente.nombre
            v.findViewById<TextView>(R.id.tvInfoPaciente).text = buildString {
                if (consulta.fecha.isNotBlank()) append(consulta.fecha)
                if (consulta.hora.isNotBlank())  append("  ${consulta.hora}")
            }

            val tvIniciales = v.findViewById<TextView>(R.id.tvAvatarInitials)
            tvIniciales.text = paciente.iniciales
            val color = ContextCompat.getColor(requireContext(), paciente.avatarColorRes)
            tvIniciales.backgroundTintList = ColorStateList.valueOf(color)

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

        layoutSinDatos = view.findViewById(R.id.layoutSinDatos)
        tvDoctorNombre     = view.findViewById(R.id.doctor_nombre)
        tvConsultasHoy     = view.findViewById(R.id.tvConsultasHoy)
        tvTotalPacientes   = view.findViewById(R.id.tvTotalPacientes)
        etBuscar           = view.findViewById(R.id.etBuscar)
        btnNuevoPaciente   = view.findViewById(R.id.btnNuevoPaciente)
        rvUltimasConsultas = view.findViewById(R.id.llUltimasConsultas)
        rvUltimasConsultas.layoutManager = LinearLayoutManager(requireContext())

        viewLifecycleOwner.lifecycleScope.launch {
            vm.perfil.collectLatest { perfil ->
                tvDoctorNombre.text =
                    if (!perfil?.nombre.isNullOrBlank()) "Dr(a). ${perfil!!.nombre}" else "Doctor(a)"
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            vm.expedientes.collectLatest { lista ->
                tvTotalPacientes.text = lista.size.toString()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            vm.consultas.collectLatest { consultas ->
                val fmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                tvConsultasHoy.text = consultas.count { it.fecha == fmt.format(Date()) }.toString()

                // pacientesConColor() ya tiene el índice correcto — mismo que PacientesFragment
                val pacientesMap = vm.pacientesConColor().associateBy { it.id }

                val items = consultas
                    .take(8)
                    .mapNotNull { consulta ->
                        val paciente = pacientesMap[consulta.expedienteId] ?: return@mapNotNull null
                        ItemInicio(paciente, consulta)
                    }

                todosLosItems.clear()
                todosLosItems.addAll(items)
                aplicarFiltro(etBuscar.text.toString())
            }
        }

        etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { aplicarFiltro(s.toString()) }
        })

        btnNuevoPaciente.setOnClickListener {
            (requireActivity() as MainActivity).navegarAExpediente(pacienteId = null)
        }
    }

    private fun aplicarFiltro(query: String) {
        val filtrados = if (query.isBlank()) todosLosItems
        else todosLosItems.filter {
            it.paciente.nombre.contains(query, ignoreCase = true) ||
                    it.consulta.motivo.contains(query, ignoreCase = true)
        }

        val sinPacientes = vm.expedientes.value.isEmpty()
        val sinConsultas = todosLosItems.isEmpty()

        layoutSinDatos.visibility          = if (sinPacientes) View.VISIBLE else View.GONE
        rvUltimasConsultas.visibility      = if (sinPacientes) View.GONE    else View.VISIBLE
        view?.findViewById<View>(R.id.resumenCartas)?.visibility          = if (sinPacientes) View.GONE else View.VISIBLE
        view?.findViewById<View>(R.id.ultimasConsultasTitulo)?.visibility = if (sinPacientes || sinConsultas) View.GONE else View.VISIBLE  // ← oculta si no hay pacientes O no hay consultas

        if (!sinPacientes) rvUltimasConsultas.adapter = InicioAdapter(filtrados)
    }
}