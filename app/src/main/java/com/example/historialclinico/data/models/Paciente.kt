package com.example.historialclinico.data.models

import com.example.historialclinico.R
import java.io.Serializable

data class Paciente(
    val id: String = "",
    val nombre: String = "",
    val edad: Int = 0,
    val sexo: String = "",
    val tipoSangre: String = "",
    val avatarColorRes: Int = R.color.avatar_blue
) : Serializable {

    val iniciales: String
        get() {
            val partes = nombre.trim().split(" ").filter { it.isNotBlank() }
            return when {
                partes.size >= 2 -> "${partes[0].first()}${partes[1].first()}".uppercase()
                partes.isNotEmpty() -> partes[0].take(2).uppercase()
                else -> "?"
            }
        }
}