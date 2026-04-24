package com.example.historialclinico.data.models

data class ConsultaConPaciente(
    val nombrePaciente: String,
    val fecha: String,
    val hora: String,
    val timestamp: Long
)