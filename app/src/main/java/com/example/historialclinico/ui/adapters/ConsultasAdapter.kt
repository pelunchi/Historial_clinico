package com.example.historialclinico.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.historialclinico.R
import com.example.historialclinico.databinding.ItemConsultaBinding

data class ConsultaSimulada(
    val id: String,
    val fecha: String,
    val hora: String,
    val motivo: String
)

class ConsultasAdapter(
    private val lista: List<ConsultaSimulada>,
    private val onItemClick: (ConsultaSimulada) -> Unit
) : RecyclerView.Adapter<ConsultasAdapter.ConsultaViewHolder>() {

    inner class ConsultaViewHolder(private val binding: ItemConsultaBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(consulta: ConsultaSimulada) {
            binding.tvConsultationTitle.text  = "Consulta – ${consulta.fecha}"
            binding.tvConsultationReason.text = consulta.motivo
            binding.tvConsultationTime.text   = consulta.hora

            binding.root.setOnClickListener {
                onItemClick(consulta)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConsultaViewHolder {
        val binding = ItemConsultaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ConsultaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ConsultaViewHolder, position: Int) {
        holder.bind(lista[position])
    }

    override fun getItemCount() = lista.size
}