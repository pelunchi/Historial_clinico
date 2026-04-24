package com.example.historialclinico.data.database

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.example.historialclinico.data.models.Expediente
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Estructura en Firebase:
 *   expedientes/
 *     {uid_medico}/
 *       {id_expediente}/ ← campos del Expediente
 */
class ExpedienteRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseDatabase.getInstance().reference

    private fun refUsuario(): DatabaseReference {
        val uid = auth.currentUser?.uid
            ?: throw IllegalStateException("No hay sesión activa en Firebase")
        return db.child("expedientes").child(uid)
    }

    suspend fun guardar(expediente: Expediente): String {
        val ref = if (expediente.id.isBlank()) refUsuario().push()
                  else refUsuario().child(expediente.id)
        val id = ref.key ?: throw Exception("Error generando ID")
        val datos = expediente.copy(
            id                 = id,
            userId             = auth.currentUser?.uid ?: "",
        )
        ref.setValue(datos).await()
        return id
    }

    suspend fun obtener(idExpediente: String): Expediente? {
        val snapshot = refUsuario().child(idExpediente).get().await()
        return snapshot.getValue(Expediente::class.java)
    }

    fun listarFlow(): Flow<List<Expediente>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lista = snapshot.children
                    .mapNotNull { it.getValue(Expediente::class.java) }
                    .sortedByDescending { it.fechaActualizacion }
                trySend(lista)
            }
            override fun onCancelled(error: DatabaseError) { close(error.toException()) }
        }
        refUsuario().addValueEventListener(listener)
        awaitClose { refUsuario().removeEventListener(listener) }
    }

    suspend fun eliminar(idExpediente: String) {
        refUsuario().child(idExpediente).removeValue().await()
    }
}
