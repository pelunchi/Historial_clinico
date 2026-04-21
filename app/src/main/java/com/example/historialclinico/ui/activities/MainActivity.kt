package com.example.historialclinico.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.historialclinico.R
import com.example.historialclinico.ui.fragments.ExpedienteFragment
import com.example.historialclinico.ui.fragments.InicioFragment
import com.example.historialclinico.ui.fragments.PacientesFragment
import com.example.historialclinico.ui.fragments.ProfileFragment
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var currentSelectedId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        // Estado inicial
        if (savedInstanceState == null) {
            selectItem(R.id.nav_inicio)
        }

        // Clicks
        findViewById<LinearLayout>(R.id.navInicio).setOnClickListener {
            selectItem(R.id.nav_inicio)
        }

        findViewById<LinearLayout>(R.id.navPacientes).setOnClickListener {
            selectItem(R.id.nav_pacientes)
        }

        findViewById<LinearLayout>(R.id.navAjustes).setOnClickListener {
            selectItem(R.id.nav_ajustes)
        }
    }

    private fun selectItem(itemId: Int) {
        if (currentSelectedId == itemId) return

        currentSelectedId = itemId
        updateUI(itemId)
        navigate(itemId)
    }

    // 🔥 ESTA ES LA FUNCIÓN QUE TE FALTABA
    private fun navigate(itemId: Int) {
        val fragment: Fragment = when (itemId) {
            R.id.nav_inicio -> InicioFragment()
            R.id.nav_pacientes -> PacientesFragment()
            R.id.nav_ajustes -> ProfileFragment()
            else -> InicioFragment()
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    // 🔥 AQUÍ YA NO USAMOS binding NI funciones que no existen
    fun navegarAExpediente(pacienteId: String? = null) {

        // Cambia visualmente al tab de pacientes
        selectItem(R.id.nav_pacientes)

        // Abre Expediente encima
        val fragment = ExpedienteFragment().apply {
            arguments = Bundle().apply {
                putString("pacienteId", pacienteId)
            }
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack("expediente")
            .commit()
    }

    private fun updateUI(itemId: Int) {
        val active = getColor(R.color.nav_active)
        val inactive = getColor(R.color.nav_unselected)

        val labels = mapOf(
            R.id.nav_inicio to findViewById<TextView>(R.id.navInicioLabel),
            R.id.nav_pacientes to findViewById<TextView>(R.id.navPacientesLabel),
            R.id.nav_ajustes to findViewById<TextView>(R.id.navAjustesLabel)
        )

        val icons = mapOf(
            R.id.nav_inicio to findViewById<TextView>(R.id.navInicioIcon),
            R.id.nav_pacientes to findViewById<TextView>(R.id.navPacientesIcon),
            R.id.nav_ajustes to findViewById<TextView>(R.id.navAjustesIcon)
        )

        // Reset
        labels.values.forEach { it.setTextColor(inactive) }
        icons.values.forEach { it.setTextColor(inactive) }

        // Activar seleccionado
        labels[itemId]?.setTextColor(active)
        icons[itemId]?.setTextColor(active)
    }
}