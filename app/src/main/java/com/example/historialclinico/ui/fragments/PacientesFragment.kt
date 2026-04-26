package com.example.historialclinico.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.historialclinico.R
import com.example.historialclinico.data.database.ConsultaRepository
import com.example.historialclinico.data.database.ExpedienteRepository
import com.example.historialclinico.data.models.Paciente
import com.example.historialclinico.databinding.FragmentPacientesBinding
import com.example.historialclinico.ui.activities.MainActivity
import com.example.historialclinico.ui.adapters.PacientesAdapter
import com.example.historialclinico.ui.viewmodel.AppViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PacientesFragment : Fragment() {

    private var _binding: FragmentPacientesBinding? = null
    private val binding get() = _binding!!

    private val vm: AppViewModel by activityViewModels()

    private lateinit var adapter: PacientesAdapter
    private val listaPacientes = mutableListOf<Paciente>()
    private val listaFiltrada  = mutableListOf<Paciente>()

    private val expRepo      = ExpedienteRepository()
    private val consultaRepo = ConsultaRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPacientesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configurarRecyclerView()
        configurarBusqueda()
        configurarBotones()

        viewLifecycleOwner.lifecycleScope.launch {
            vm.expedientes.collectLatest {
                listaPacientes.clear()
                listaPacientes.addAll(vm.pacientesConColor())
                filtrarPacientes(binding.etBuscar.text.toString())
            }
        }
    }

    private fun configurarRecyclerView() {
        adapter = PacientesAdapter(
            lista           = listaFiltrada,
            onItemClick     = { paciente ->
                val fragment = PacientePerfilFragment.newInstance(paciente)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit()
            },
            onItemLongClick = { paciente -> mostrarDialogoEliminar(paciente) }
        )
        binding.rvPacientes.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPacientes.adapter = adapter
    }

    private fun mostrarDialogoEliminar(paciente: Paciente? = null) {
        if (listaPacientes.isEmpty()) {
            Snackbar.make(requireView(), "No hay pacientes para eliminar", Snackbar.LENGTH_SHORT).show()
            return
        }
        if (paciente != null) {
            // Desde long click — confirmar solo ese
            confirmarEliminar(listOf(paciente))
        } else {
            // Desde botón — selección múltiple
            val nombres = listaPacientes.map { it.nombre }.toTypedArray()
            val seleccionados = mutableListOf<Paciente>()

            AlertDialog.Builder(requireContext())
                .setTitle("Selecciona pacientes a eliminar")
                .setMultiChoiceItems(nombres, null) { _, which, isChecked ->
                    if (isChecked) seleccionados.add(listaPacientes[which])
                    else seleccionados.remove(listaPacientes[which])
                }
                .setPositiveButton("Continuar") { _, _ ->
                    if (seleccionados.isEmpty()) {
                        Snackbar.make(requireView(), "No seleccionaste ningún paciente", Snackbar.LENGTH_SHORT).show()
                    } else {
                        confirmarEliminar(seleccionados)
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun confirmarEliminar(pacientes: List<Paciente>) {
        val mensaje = if (pacientes.size == 1)
            "¿Eliminar a ${pacientes[0].nombre}?\n\nSe borrarán el expediente y todas sus consultas."
        else
            "¿Eliminar a ${pacientes.size} pacientes?\n\nSe borrarán sus expedientes y todas sus consultas."

        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar paciente${if (pacientes.size > 1) "s" else ""}")
            .setMessage(mensaje)
            .setPositiveButton("Eliminar") { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    var errores = 0
                    pacientes.forEach { p ->
                        try {
                            expRepo.eliminar(p.id)
                            try { consultaRepo.eliminarTodas(p.id) } catch (_: Exception) {}
                        } catch (_: Exception) {
                            errores++
                        }
                    }
                    val msg = if (errores == 0)
                        if (pacientes.size == 1) "Paciente eliminado" else "${pacientes.size} pacientes eliminados"
                    else
                        "Se eliminaron ${pacientes.size - errores} de ${pacientes.size} pacientes"
                    Snackbar.make(requireView(), msg, Snackbar.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun configurarBusqueda() {
        binding.etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { filtrarPacientes(s.toString()) }
        })
    }

    private fun filtrarPacientes(query: String) {
        listaFiltrada.clear()
        listaFiltrada.addAll(
            if (query.isBlank()) listaPacientes
            else listaPacientes.filter { it.nombre.contains(query, ignoreCase = true) }
        )
        adapter.notifyDataSetChanged()

        // Mostrar u ocultar estado vacío
        binding.layoutSinPacientes.visibility = if (listaFiltrada.isEmpty()) View.VISIBLE else View.GONE
        binding.rvPacientes.visibility        = if (listaFiltrada.isEmpty()) View.GONE   else View.VISIBLE
    }

    private fun configurarBotones() {
        binding.btnNuevoPaciente.setOnClickListener {
            (requireActivity() as MainActivity).navegarAExpediente(pacienteId = null)
        }
        binding.btnEliminar.setOnClickListener {
            mostrarDialogoEliminar(paciente = null)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}