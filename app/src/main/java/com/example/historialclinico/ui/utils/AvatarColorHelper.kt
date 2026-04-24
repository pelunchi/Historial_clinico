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

    fun colorParaId(id: String): Int =
        colores[id.hashCode().absoluteValue % colores.size]

    fun colorAleatorio(): Int = colores.random()
}
