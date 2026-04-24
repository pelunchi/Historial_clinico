package com.example.historialclinico.ui.utils

import com.example.historialclinico.R
import kotlin.math.absoluteValue

object AvatarColorHelper {

    private val colores = listOf(
        R.color.avatar_yellow,
        R.color.avatar_purple,
        R.color.avatar_red,
        R.color.avatar_blue,
        R.color.avatar_green
    )

    /**
     * Devuelve siempre el mismo color para el mismo ID.
     * Usa el hashCode del ID para que sea consistente entre sesiones.
     */
    fun colorParaId(id: String): Int =
        colores[id.hashCode().absoluteValue % colores.size]

    /** Mantiene compatibilidad con usos anteriores, pero ahora usa el ID. */
    fun colorAleatorio(): Int = colores.random()
}
