package com.example.historialclinico.data.database

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.example.historialclinico.data.models.Expediente
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ExpedienteRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseDatabase.getInstance().reference

    private fun refUsuario(): DatabaseReference {
        val uid = auth.currentUser?.uid
            ?: throw IllegalStateException("No hay sesión activa en Firebase")
        return db.child("expedientes").child(uid)
    }

    suspend fun guardar(expediente: Expediente): String {
        val esNuevo = expediente.id.isBlank()
        val ref = if (esNuevo) refUsuario().push()
        else refUsuario().child(expediente.id)
        val id = ref.key ?: throw Exception("Error generando ID")

        val colorIndex = if (esNuevo && expediente.avatarColorIndex < 0) {
            // Obtener el color del último paciente creado
            val snapshot = refUsuario().get().await()
            val ultimoColor = snapshot.children
                .mapNotNull { it.getValue(Expediente::class.java) }
                .maxByOrNull { it.fechaCreacion }
                ?.avatarColorIndex ?: -1

            // Elegir aleatoriamente entre los 4 colores restantes
            val coloresDisponibles = (0..4).filter { it != ultimoColor }
            coloresDisponibles.random()
        } else {
            expediente.avatarColorIndex
        }

        val datos = expediente.copy(
            id               = id,
            userId           = auth.currentUser?.uid ?: "",
            avatarColorIndex = colorIndex
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
                    .sortedBy { it.nombre.lowercase() }   // ← cambia esta línea
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

    /** Lee todos los expedientes una sola vez (sin listener) */
    suspend fun listarUnaVez(): List<Expediente> {
        val snapshot = refUsuario().get().await()
        return snapshot.children
            .mapNotNull { it.getValue(Expediente::class.java) }
    }

    /** Actualiza solo el campo avatarColorIndex sin tocar el resto del expediente */
    suspend fun actualizarColorIndex(expedienteId: String, colorIndex: Int) {
        refUsuario().child(expedienteId).child("avatarColorIndex")
            .setValue(colorIndex).await()
    }

}