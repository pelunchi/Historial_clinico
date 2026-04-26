package com.example.historialclinico.ui.utils

import com.example.historialclinico.R
import kotlin.math.absoluteValue

object AvatarColorHelper {

    private val avatarColors = listOf(
        R.color.avatar_yellow,
        R.color.avatar_purple,
        R.color.avatar_red,
        R.color.avatar_blue,
        R.color.avatar_green
    )

    fun avatarColorRes(position: Int): Int = avatarColors[position % avatarColors.size]

    fun avatarIniciales(nombre: String): String =
        nombre.split(" ").take(2).joinToString("") { it.firstOrNull()?.toString() ?: "" }.uppercase()
}
