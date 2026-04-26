package com.example.historialclinico.ui.adapters

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.historialclinico.R
import com.example.historialclinico.data.models.Paciente
import com.example.historialclinico.databinding.ItemPacienteBinding
import kotlin.math.absoluteValue

class PacientesAdapter(
    private val lista: List<Paciente>,
    private val onItemClick: (Paciente) -> Unit,
    private val onItemLongClick: (Paciente) -> Unit = {}
) : RecyclerView.Adapter<PacientesAdapter.PacienteViewHolder>() {

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

            val partes = mutableListOf<String>()
            if (paciente.edad > 0) partes.add("${paciente.edad} años")
            if (paciente.sexo.isNotBlank()) partes.add(paciente.sexo)
            if (paciente.tipoSangre.isNotBlank()) partes.add(paciente.tipoSangre)
            binding.tvInfoPaciente.text = partes.joinToString(" | ")

            val color = ContextCompat.getColor(binding.root.context, paciente.avatarColorRes)
            binding.tvAvatarInitials.backgroundTintList = ColorStateList.valueOf(color)

            binding.root.setOnClickListener { onItemClick(paciente) }
            binding.root.setOnLongClickListener { onItemLongClick(paciente); true }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PacienteViewHolder {
        val binding = ItemPacienteBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PacienteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PacienteViewHolder, position: Int) =
        holder.bind(lista[position])

    override fun getItemCount() = lista.size
}
