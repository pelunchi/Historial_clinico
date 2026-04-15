package com.example.historialclinico.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.historialclinico.data.models.Paciente
import com.example.historialclinico.databinding.ItemPacienteBinding
import android.content.res.ColorStateList
import androidx.core.content.ContextCompat
import kotlin.math.absoluteValue
import com.example.historialclinico.R

class PacientesAdapter(
    private val lista: List<Paciente>,
    private val onItemClick: (Paciente) -> Unit
) : RecyclerView.Adapter<PacientesAdapter.PacienteViewHolder>() {

    // Lista de colores de avatar disponibles
    private val avatarColors = listOf(
        R.color.avatar_yellow,
        R.color.avatar_purple,
        R.color.avatar_red,
        R.color.avatar_blue,
        R.color.avatar_green
    )
    inner class PacienteViewHolder(private val binding: ItemPacienteBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(paciente: Paciente) {
            binding.tvAvatarInitials.text = paciente.iniciales
            binding.tvNombrePaciente.text = paciente.nombre
            binding.tvInfoPaciente.text = "${paciente.edad} años | ${paciente.sexo} | ${paciente.tipoSangre}"

            // Color basado en el id del paciente para que no cambie al hacer scroll
            val colorRes = avatarColors[paciente.id.hashCode().absoluteValue % avatarColors.size]
            val color = ContextCompat.getColor(binding.root.context, colorRes)
            binding.tvAvatarInitials.backgroundTintList = ColorStateList.valueOf(color)

            binding.root.setOnClickListener { onItemClick(paciente) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PacienteViewHolder {
        val binding = ItemPacienteBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PacienteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PacienteViewHolder, position: Int) {
        holder.bind(lista[position])
    }

    override fun getItemCount() = lista.size
}