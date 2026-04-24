package com.example.historialclinico.ui.adapters

class InicioConsultasAdapter(
    private val lista: List<ConsultaConPaciente>,
    private val onClick: (ConsultaConPaciente) -> Unit
) : RecyclerView.Adapter<InicioConsultasAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemPacienteBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ConsultaConPaciente) {
            binding.tvNombrePaciente.text = item.nombrePaciente
            binding.tvInfoPaciente.text = "${item.fecha} • ${item.hora}"

            // Iniciales
            val iniciales = item.nombrePaciente.split(" ")
                .take(2)
                .joinToString("") { it.first().toString() }

            binding.tvAvatarInitials.text = iniciales

            binding.root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPacienteBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(lista[position])

    override fun getItemCount() = lista.size
}