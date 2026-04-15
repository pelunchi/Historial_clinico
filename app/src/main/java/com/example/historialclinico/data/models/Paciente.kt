package com.example.historialclinico.data.models

import com.example.historialclinico.R

data class Paciente(
    val id: String,
    val nombre: String,
    val edad: Int,
    val sexo: String,        // "M" o "F"
    val tipoSangre: String,
    val avatarColorRes: Int = R.color.avatar_blue // valor por defecto

) {
    // Genera las iniciales para el avatar a partir del nombre
    val iniciales: String
        get() {
            val partes = nombre.trim().split(" ")
            return when {
                partes.size >= 2 -> "${partes[0].first()}${partes[1].first()}".uppercase()
                partes.isNotEmpty() -> partes[0].take(2).uppercase()
                else -> "?"
            }
        }
}