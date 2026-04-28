package com.example.historialclinico.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.historialclinico.R
import com.example.historialclinico.ui.activities.LoginActivity
import com.example.historialclinico.ui.viewmodel.AppViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private val vm: AppViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_profile, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val auth = FirebaseAuth.getInstance()
        view.findViewById<TextView>(R.id.tvUserEmail).text = auth.currentUser?.email ?: "—"

        // Nombre desde caché del ViewModel — sin esperar red
        viewLifecycleOwner.lifecycleScope.launch {
            vm.perfil.collectLatest { perfil ->
                val nombre = if (!perfil?.nombre.isNullOrBlank()) "Dr. ${perfil.nombre}" else "Doctor"
                view.findViewById<TextView>(R.id.tvUserNombre).text = nombre
                view.findViewById<TextView>(R.id.tvUserRol).text    = nombre
            }
        }

        view.findViewById<MaterialButton>(R.id.btnLogout).setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Cerrar Sesión")
                .setMessage("¿Estás seguro?")
                .setPositiveButton("Sí") { _, _ ->
                    auth.signOut()
                    startActivity(Intent(requireContext(), LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                }
                .setNegativeButton("No", null)
                .show()
        }
    }
}
