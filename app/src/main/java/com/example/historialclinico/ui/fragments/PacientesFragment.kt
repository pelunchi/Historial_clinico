package com.example.historialclinico.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.historialclinico.R
import com.example.historialclinico.data.models.Paciente
import com.example.historialclinico.databinding.FragmentPacientesBinding
import com.example.historialclinico.ui.activities.MainActivity
import com.example.historialclinico.ui.adapters.PacientesAdapter
import com.example.historialclinico.ui.utils.AvatarColorHelper

class PacientesFragment : Fragment() {

    private var _binding: FragmentPacientesBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: PacientesAdapter
    private val listaPacientes = mutableListOf<Paciente>()
    private val listaFiltrada  = mutableListOf<Paciente>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPacientesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cargarDatosDePrueba()
        configurarRecyclerView()
        configurarBusqueda()
        configurarBotones()
    }

    private fun cargarDatosDePrueba() {
        listaPacientes.addAll(
            listOf(
                Paciente("1", "María González", 45, "F", "O+",
                    avatarColorRes = AvatarColorHelper.colorAleatorio()),
                Paciente("2", "Carlos Pérez",   32, "M", "A+",
                    avatarColorRes = AvatarColorHelper.colorAleatorio()),
                Paciente("3", "Lucía Ramírez",  28, "F", "B-",
                    avatarColorRes = AvatarColorHelper.colorAleatorio()),
            )
        )
        listaFiltrada.addAll(listaPacientes)
    }

    private fun configurarRecyclerView() {
        adapter = PacientesAdapter(listaFiltrada) { paciente ->
            // 👇 Al hacer click en un paciente, abrimos su perfil
            navegarAPerfilPaciente(paciente)
        }
        binding.rvPacientes.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPacientes.adapter = adapter
    }

    private fun navegarAPerfilPaciente(paciente: Paciente) {
        val fragment = PacientePerfilFragment.newInstance(paciente)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)  // usa el ID de tu contenedor
            .addToBackStack(null)
            .commit()
    }

    private fun configurarBusqueda() {
        binding.etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filtrarPacientes(s.toString())
            }
        })
    }

    private fun filtrarPacientes(query: String) {
        listaFiltrada.clear()
        if (query.isBlank()) {
            listaFiltrada.addAll(listaPacientes)
        } else {
            listaFiltrada.addAll(
                listaPacientes.filter {
                    it.nombre.contains(query, ignoreCase = true)
                }
            )
        }
        adapter.notifyDataSetChanged()
    }

    private fun configurarBotones() {
        binding.btnNuevoPaciente.setOnClickListener {
            (requireActivity() as MainActivity).navegarAExpediente(pacienteId = null)
        }

        binding.btnEliminar.setOnClickListener {
            // lógica de eliminación
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}