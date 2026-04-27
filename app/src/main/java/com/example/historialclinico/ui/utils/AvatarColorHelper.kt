package com.example.historialclinico.ui.utils

import com.example.historialclinico.R

object AvatarColorHelper {

    private val colores = listOf(
        R.color.avatar_yellow,
        R.color.avatar_purple,
        R.color.avatar_red,
        R.color.avatar_blue,
        R.color.avatar_green
    )

    fun colorAleatorio(): Int = colores.random()
}