package com.example.historialclinico.data.models

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Expediente(
    var id: String = "",

    // Ficha
    var nombre: String = "",
    var edad: Int = 0,
    var sexo: String = "",
    var fechaNacimiento: String = "",
    var tipoSangre: String = "",
    var curp: String = "",
    var direccion: String = "",
    var telefono: String = "",
    var correo: String = "",

    // AHF
    var ahfDiabetes: Boolean = false,
    var ahfHipertension: Boolean = false,
    var ahfCancer: Boolean = false,
    var ahfCardio: Boolean = false,
    var ahfObesidad: Boolean = false,
    var ahfRenal: Boolean = false,
    var ahfOtros: String = "",

    // APP
    var appCirugias: String = "",
    var appAlergias: String = "",
    var appCronicas: String = "",
    var appHospitalizaciones: String = "",
    var appOtros: String = "",

    // PA
    var paInicio: String = "",
    var paDescripcion: String = "",
    var paEvolucion: String = "",

    // EF
    var efTalla: Double = 0.0,
    var efPeso: Double = 0.0,
    var efImc: Double = 0.0,
    var efPresionArterial: String = "",
    var efFrecCardiaca: Int = 0,
    var efFrecRespiratoria: Int = 0,
    var efTemperatura: Double = 0.0,
    var efSpo2: Double = 0.0,
    var efObservaciones: String = "",

    // Tratamiento
    var tratMedicamentos: String = "",
    var tratDosis: String = "",
    var tratTerapias: String = "",
    var tratNotas: String = "",
    var tratProximaConsulta: String = "",

    // Avatar
    var avatarColorIndex: Int = -1,   // ← NUEVO: -1 = no asignado aún

    // Metadatos
    var userId: String = "",
    var fechaCreacion: Long = System.currentTimeMillis(),
    var fechaActualizacion: Long = System.currentTimeMillis()
) {
    fun calcularImc(): Double {
        if (efTalla <= 0 || efPeso <= 0) return 0.0
        val m = efTalla / 100.0
        return efPeso / (m * m)
    }

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