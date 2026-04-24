package com.example.historialclinico.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.historialclinico.R
import com.example.historialclinico.data.database.UserRepository
import com.example.historialclinico.ui.activities.LoginActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private val userRepo = UserRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_profile, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        // Email siempre disponible localmente
        view.findViewById<TextView>(R.id.tvUserEmail).text = user?.email ?: "—"

        // Nombre del doctor desde Firebase
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val perfil = userRepo.obtenerPerfil()
                val tvNombre = view.findViewById<TextView?>(R.id.tvUserNombre)
                if (!perfil?.nombre.isNullOrBlank()) {
                    tvNombre?.text = "Dr. ${perfil!!.nombre}"
                }
            } catch (_: Exception) { /* sin nombre, no pasa nada */ }
        }

        // Cerrar sesión
        view.findViewById<MaterialButton>(R.id.btnLogout).setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Cerrar Sesión")
                .setMessage("¿Estás seguro?")
                .setPositiveButton("Sí") { _, _ ->
                    auth.signOut()
                    startActivity(
                        Intent(requireContext(), LoginActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                    )
                }
                .setNegativeButton("No", null)
                .show()
        }
    }
}
