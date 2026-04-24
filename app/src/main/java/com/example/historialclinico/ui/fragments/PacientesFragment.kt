package com.example.historialclinico.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.historialclinico.R
import com.example.historialclinico.data.database.ConsultaRepository
import com.example.historialclinico.data.database.ExpedienteRepository
import com.example.historialclinico.data.models.Expediente
import com.example.historialclinico.data.models.Paciente
import com.example.historialclinico.databinding.FragmentPacientesBinding
import com.example.historialclinico.ui.activities.MainActivity
import com.example.historialclinico.ui.adapters.PacientesAdapter
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

class PacientesFragment : Fragment() {

    private var _binding: FragmentPacientesBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: PacientesAdapter
    private val listaPacientes = mutableListOf<Paciente>()
    private val listaFiltrada  = mutableListOf<Paciente>()

    private val expRepo     = ExpedienteRepository()
    private val consultaRepo = ConsultaRepository()

    private val avatarColors = listOf(
        R.color.avatar_yellow,
        R.color.avatar_purple,
        R.color.avatar_red,
        R.color.avatar_blue,
        R.color.avatar_green
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPacientesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configurarRecyclerView()
        configurarBusqueda()
        configurarBotones()
        cargarDesdeFirebase()
    }

    // ── Firebase ───────────────────────────────────────────────────────

    private fun cargarDesdeFirebase() {
        viewLifecycleOwner.lifecycleScope.launch {
            expRepo.listarFlow().collectLatest { expedientes ->
                listaPacientes.clear()
                listaPacientes.addAll(expedientes.map { it.toPaciente() })
                filtrarPacientes(binding.etBuscar.text.toString())
            }
        }
    }

    private fun Expediente.toPaciente() = Paciente(
        id             = id,
        nombre         = nombre,
        edad           = edad,
        sexo           = if (sexo.startsWith("F", ignoreCase = true)) "F" else "M",
        tipoSangre     = tipoSangre,   // ← pasa el tipo de sangre real
        avatarColorRes = avatarColors[id.hashCode().absoluteValue % avatarColors.size]
    )

    // ── RecyclerView ───────────────────────────────────────────────────

    private fun configurarRecyclerView() {
        adapter = PacientesAdapter(
            lista         = listaFiltrada,
            onItemClick   = { paciente ->
                val fragment = PacientePerfilFragment.newInstance(paciente)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit()
            },
            onItemLongClick = { paciente ->
                mostrarDialogoEliminar(paciente)
            }
        )
        binding.rvPacientes.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPacientes.adapter = adapter
    }

    // ── Eliminar ───────────────────────────────────────────────────────

    /**
     * Muestra un AlertDialog para confirmar la eliminación.
     * Se activa tanto por long-press en la tarjeta como por btnEliminar.
     */
    private fun mostrarDialogoEliminar(paciente: Paciente? = null) {
        if (listaPacientes.isEmpty()) {
            Snackbar.make(requireView(), "No hay pacientes para eliminar", Snackbar.LENGTH_SHORT).show()
            return
        }

        if (paciente != null) {
            // Eliminar el paciente específico (vino de long-press)
            confirmarEliminar(paciente)
        } else {
            // Vino del botón "Eliminar paciente" → mostrar lista para seleccionar
            val nombres = listaPacientes.map { it.nombre }.toTypedArray()
            var seleccionado = 0

            AlertDialog.Builder(requireContext())
                .setTitle("Selecciona el paciente a eliminar")
                .setSingleChoiceItems(nombres, 0) { _, which -> seleccionado = which }
                .setPositiveButton("Continuar") { _, _ ->
                    confirmarEliminar(listaPacientes[seleccionado])
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun confirmarEliminar(paciente: Paciente) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar paciente")
            .setMessage("¿Eliminar a ${paciente.nombre}?\n\nSe borrarán el expediente y todas sus consultas. Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ -> eliminarPaciente(paciente) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarPaciente(paciente: Paciente) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Eliminar expediente
                expRepo.eliminar(paciente.id)
                // Eliminar todas sus consultas
                try { consultaRepo.eliminarTodas(paciente.id) } catch (_: Exception) {}
                Snackbar.make(requireView(), "Paciente eliminado", Snackbar.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Snackbar.make(requireView(), "Error al eliminar: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    // ── Búsqueda ───────────────────────────────────────────────────────

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
    }

    // ── Botones ────────────────────────────────────────────────────────

    private fun configurarBotones() {
        binding.btnNuevoPaciente.setOnClickListener {
            (requireActivity() as MainActivity).navegarAExpediente(pacienteId = null)
        }
        // Botón "Eliminar paciente" → abre selector de pacientes
        binding.btnEliminar.setOnClickListener {
            mostrarDialogoEliminar(paciente = null)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
