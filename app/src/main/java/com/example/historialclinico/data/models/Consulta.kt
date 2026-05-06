package com.example.historialclinico.data.models

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Consulta(
    var id: String = "",
    var expedienteId: String = "",   // = pacienteId en la lista
    var userId: String = "",

    // Fecha y hora (guardadas como String legible + timestamp para ordenar)
    var fecha: String = "",
    var hora: String = "",
    var fechaTimestamp: Long = System.currentTimeMillis(),

    // Motivo y clínica
    var motivo: String = "",
    var sintomas: String = "",
    var diagnostico: String = "",
    var resumen: String = "",
    var indicaciones: String = "",
    var receta: String = "",

    // Exploración física de la consulta
    var efTalla: Double = 0.0,
    var efPeso: Double = 0.0,
    var efImc: Double = 0.0,
    var efPresionArterial: String = "",
    var efFrecCardiaca: Int = 0,
    var efFrecRespiratoria: Int = 0,
    var efTemperatura: Double = 0.0,
    var efSpo2: Double = 0.0,
    var efObservaciones: String = ""
) {
    fun categoriaImc(): String = when {
        efImc <= 0   -> "—"
        efImc < 18.5 -> "Bajo peso"
        efImc < 25.0 -> "Peso normal"
        efImc < 30.0 -> "Sobrepeso"
        efImc < 35.0 -> "Obesidad I"
        efImc < 40.0 -> "Obesidad II"
        else          -> "Obesidad III"
    }
}

