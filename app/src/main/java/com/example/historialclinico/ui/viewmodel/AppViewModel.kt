package com.example.historialclinico.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.historialclinico.R
import com.example.historialclinico.data.database.ConsultaRepository
import com.example.historialclinico.data.database.ExpedienteRepository
import com.example.historialclinico.data.database.UserRepository
import com.example.historialclinico.data.database.UserProfile
import com.example.historialclinico.data.models.Consulta
import com.example.historialclinico.data.models.Expediente
import com.example.historialclinico.data.models.Paciente
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppViewModel : ViewModel() {

    private val expRepo      = ExpedienteRepository()
    private val consultaRepo = ConsultaRepository()
    private val userRepo     = UserRepository()

    private val avatarColors = listOf(
        R.color.avatar_yellow,
        R.color.avatar_purple,
        R.color.avatar_red,
        R.color.avatar_blue,
        R.color.avatar_green
    )

    private val _expedientes = MutableStateFlow<List<Expediente>>(emptyList())
    val expedientes: StateFlow<List<Expediente>> = _expedientes.asStateFlow()

    private val _consultas = MutableStateFlow<List<Consulta>>(emptyList())
    val consultas: StateFlow<List<Consulta>> = _consultas.asStateFlow()

    private val _perfil = MutableStateFlow<UserProfile?>(null)
    val perfil: StateFlow<UserProfile?> = _perfil.asStateFlow()

    private val _consultasPorPaciente = MutableStateFlow<Map<String, List<Consulta>>>(emptyMap())
    val consultasPorPaciente: StateFlow<Map<String, List<Consulta>>> =
        _consultasPorPaciente.asStateFlow()

    // ── Control de inicio ──────────────────────────────────────────────
    private var iniciado = false

    /**
     * Llamar desde MainActivity DESPUÉS de confirmar auth.currentUser != null.
     * Reemplaza el init{} para evitar el crash "No hay sesión activa".
     */
    fun iniciarSync() {
        if (iniciado) return
        if (FirebaseAuth.getInstance().currentUser == null) return
        iniciado = true

        viewModelScope.launch {
            try {
                expRepo.listarFlow().collectLatest { lista ->
                    _expedientes.value = lista
                }
            } catch (_: Exception) {}
        }
        viewModelScope.launch {
            try {
                consultaRepo.listarTodasFlow().collectLatest { lista ->
                    _consultas.value = lista
                    _consultasPorPaciente.value = lista.groupBy { it.expedienteId }
                }
            } catch (_: Exception) {}
        }
        viewModelScope.launch {
            try { _perfil.value = userRepo.obtenerPerfil() } catch (_: Exception) {}
        }
        viewModelScope.launch {
            try { migrarColoresAvatar() } catch (_: Exception) {}
        }
    }

    /** Llama esto al cerrar sesión para limpiar el caché y permitir reiniciar en el próximo login */
    fun limpiarYReiniciar() {
        iniciado = false
        _expedientes.value          = emptyList()
        _consultas.value            = emptyList()
        _consultasPorPaciente.value = emptyMap()
        _perfil.value               = null
    }

    // ── Funciones existentes — sin cambios ─────────────────────────────

    fun pacientesConColor(): List<Paciente> =
        _expedientes.value.map { exp ->
            val colorRes = if (exp.avatarColorIndex >= 0)
                avatarColors[exp.avatarColorIndex % avatarColors.size]
            else
                R.color.avatar_blue
            Paciente(
                id             = exp.id,
                nombre         = exp.nombre,
                edad           = exp.edad,
                sexo           = when {
                    exp.sexo.startsWith("F", ignoreCase = true) -> "F"
                    exp.sexo.startsWith("M", ignoreCase = true) -> "M"
                    else -> ""
                },
                tipoSangre     = exp.tipoSangre,
                avatarColorRes = colorRes
            )
        }

    fun pacienteConColor(expedienteId: String): Paciente? =
        pacientesConColor().find { it.id == expedienteId }

    fun expedientesMap(): Map<String, Expediente> =
        _expedientes.value.associateBy { it.id }

    fun consultasDeFlow(expedienteId: String): List<Consulta> =
        _consultasPorPaciente.value[expedienteId] ?: emptyList()

    fun recargarPerfil() {
        viewModelScope.launch {
            try { _perfil.value = userRepo.obtenerPerfil() } catch (_: Exception) {}
        }
    }

    fun eliminarConsulta(expedienteId: String, consultaId: String) {
        viewModelScope.launch {
            consultaRepo.eliminar(expedienteId, consultaId)
            _consultasPorPaciente.update { mapa ->
                val actualizada = mapa[expedienteId]?.filter { it.id != consultaId } ?: emptyList()
                mapa + (expedienteId to actualizada)
            }
        }
    }

    private suspend fun migrarColoresAvatar() {
        val lista = expRepo.listarUnaVez()
        val sinColor = lista.filter { it.avatarColorIndex < 0 }
        sinColor.forEachIndexed { index, exp ->
            expRepo.actualizarColorIndex(exp.id, index % 5)
        }
    }
}
