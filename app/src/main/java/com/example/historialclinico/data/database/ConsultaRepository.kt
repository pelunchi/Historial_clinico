package com.example.historialclinico.data.database

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.example.historialclinico.data.models.Consulta
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Estructura en Firebase:
 *   consultas/
 *     {uid}/
 *       {expedienteId}/
 *         {consultaId}/ ← campos de Consulta
 */
class ConsultaRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db: DatabaseReference = FirebaseDatabase.getInstance().reference

    private fun uid() = auth.currentUser?.uid
        ?: throw IllegalStateException("No hay sesión activa")

    private fun refUsuario(): DatabaseReference =
        db.child("consultas").child(uid())

    private fun refPaciente(expedienteId: String): DatabaseReference =
        refUsuario().child(expedienteId)

    // ── Guardar ────────────────────────────────────────────────────────

    suspend fun guardar(consulta: Consulta): String {
        val ref = if (consulta.id.isBlank())
            refPaciente(consulta.expedienteId).push()
        else
            refPaciente(consulta.expedienteId).child(consulta.id)

        val id = ref.key ?: throw Exception("Error generando ID de consulta")
        ref.setValue(
            consulta.copy(
                id             = id,
                userId         = uid(),
                fechaTimestamp = System.currentTimeMillis()
            )
        ).await()
        return id
    }

    // ── Leer una ────────────────────────────────────────────────────────

    suspend fun obtener(expedienteId: String, consultaId: String): Consulta? {
        val snap = refPaciente(expedienteId).child(consultaId).get().await()
        return snap.getValue(Consulta::class.java)
    }

    // ── Flow de un paciente ─────────────────────────────────────────────

    fun listarFlow(expedienteId: String): Flow<List<Consulta>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lista = snapshot.children
                    .mapNotNull { it.getValue(Consulta::class.java) }
                    .sortedByDescending { it.fechaTimestamp }
                trySend(lista)
            }
            override fun onCancelled(error: DatabaseError) { close(error.toException()) }
        }
        refPaciente(expedienteId).addValueEventListener(listener)
        awaitClose { refPaciente(expedienteId).removeEventListener(listener) }
    }

    /**
     * Flow en tiempo real con TODAS las consultas del doctor,
     * sin importar a qué paciente pertenecen.
     *
     * Cómo funciona:
     *  - Escucha consultas/{uid}/ completo (un nivel arriba del expedienteId)
     *  - Itera cada nodo hijo (= cada paciente) y luego cada consulta dentro
     *  - Aplana todo en una lista única ordenada por fechaTimestamp desc
     */
    fun listarTodasFlow(): Flow<List<Consulta>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val todas = mutableListOf<Consulta>()
                // snapshot = consultas/{uid}/
                //   ↳ child = {expedienteId}/
                //       ↳ child = {consultaId}/ ← aquí vive cada Consulta
                for (pacienteSnap in snapshot.children) {
                    for (consultaSnap in pacienteSnap.children) {
                        consultaSnap.getValue(Consulta::class.java)?.let { todas.add(it) }
                    }
                }
                trySend(todas.sortedByDescending { it.fechaTimestamp })
            }
            override fun onCancelled(error: DatabaseError) { close(error.toException()) }
        }
        refUsuario().addValueEventListener(listener)
        awaitClose { refUsuario().removeEventListener(listener) }
    }

    // ── Eliminar ────────────────────────────────────────────────────────

    suspend fun eliminar(expedienteId: String, consultaId: String) {
        refPaciente(expedienteId).child(consultaId).removeValue().await()
    }

    suspend fun eliminarTodas(expedienteId: String) {
        refPaciente(expedienteId).removeValue().await()
    }
}

