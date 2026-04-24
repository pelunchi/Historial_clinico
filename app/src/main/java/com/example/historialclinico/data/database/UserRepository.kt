package com.example.historialclinico.data.database

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

data class UserProfile(
    val uid: String = "",
    val nombre: String = "",
    val email: String = ""
)

/**
 * Estructura en Firebase:
 *   users/
 *     {uid}/
 *       uid, nombre, email
 */
class UserRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db: DatabaseReference = FirebaseDatabase.getInstance().reference

    private fun refUsuario(): DatabaseReference {
        val uid = auth.currentUser?.uid
            ?: throw IllegalStateException("No hay sesión activa")
        return db.child("users").child(uid)
    }

    suspend fun guardarPerfil(nombre: String) {
        val uid   = auth.currentUser?.uid   ?: return
        val email = auth.currentUser?.email ?: ""
        refUsuario().setValue(mapOf("uid" to uid, "nombre" to nombre, "email" to email)).await()
    }

    suspend fun obtenerPerfil(): UserProfile? {
        val snap = refUsuario().get().await()
        if (!snap.exists()) return null
        return UserProfile(
            uid    = snap.child("uid").getValue(String::class.java)    ?: "",
            nombre = snap.child("nombre").getValue(String::class.java) ?: "",
            email  = snap.child("email").getValue(String::class.java)  ?: ""
        )
    }
}
