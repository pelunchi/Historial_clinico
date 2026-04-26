package com.example.historialclinico.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.historialclinico.data.models.Consulta
import com.example.historialclinico.databinding.ItemConsultaBinding

class ConsultasAdapter(
    private val lista: List<Consulta>,
    private val onItemClick: (Consulta) -> Unit,
    private val onItemLongClick: (Consulta) -> Unit = {}
) : RecyclerView.Adapter<ConsultasAdapter.ConsultaViewHolder>() {

    inner class ConsultaViewHolder(private val binding: ItemConsultaBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(consulta: Consulta) {
            binding.tvConsultationTitle.text  = "Consulta – ${consulta.fecha}"
            binding.tvConsultationReason.text = consulta.motivo.ifBlank { "Sin motivo registrado" }
            binding.tvConsultationTime.text   = consulta.hora
            binding.root.setOnClickListener { onItemClick(consulta) }
            binding.root.setOnLongClickListener {
                onItemLongClick(consulta)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConsultaViewHolder {
        val binding = ItemConsultaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ConsultaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ConsultaViewHolder, position: Int) =
        holder.bind(lista[position])

    override fun getItemCount() = lista.size
}
