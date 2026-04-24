package com.example.historialclinico.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.historialclinico.R
import com.example.historialclinico.data.models.Paciente
import com.example.historialclinico.ui.fragments.ConsultaFragment
import com.example.historialclinico.ui.fragments.ExpedienteFragment
import com.example.historialclinico.ui.fragments.InicioFragment
import com.example.historialclinico.ui.fragments.PacientesFragment
import com.example.historialclinico.ui.fragments.ProfileFragment
import com.example.historialclinico.ui.fragments.VerConsultaFragment
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
        if (savedInstanceState == null) selectItem(R.id.nav_inicio)

        findViewById<LinearLayout>(R.id.navInicio).setOnClickListener    { selectItem(R.id.nav_inicio) }
        findViewById<LinearLayout>(R.id.navPacientes).setOnClickListener  { selectItem(R.id.nav_pacientes) }
        findViewById<LinearLayout>(R.id.navAjustes).setOnClickListener    { selectItem(R.id.nav_ajustes) }
    }

    private fun selectItem(itemId: Int) {
        if (currentSelectedId == itemId) return
        currentSelectedId = itemId
        updateUI(itemId)
        navigate(itemId)
    }

    fun selectNavItem(itemId: Int) = selectItem(itemId)

    private fun navigate(itemId: Int) {
        val fragment: Fragment = when (itemId) {
            R.id.nav_inicio    -> InicioFragment()
            R.id.nav_pacientes -> PacientesFragment()
            R.id.nav_ajustes   -> ProfileFragment()
            else               -> InicioFragment()
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    /** Abre ExpedienteFragment. Si pacienteId != null, carga expediente existente. */
    fun navegarAExpediente(pacienteId: String? = null) {
        val fragment = ExpedienteFragment().apply {
            arguments = Bundle().apply { putString("pacienteId", pacienteId) }
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack("expediente")
            .commit()
    }

    /** Abre ConsultaFragment (formulario). Si consultaId != null, edita la existente. */
    fun navegarAConsulta(paciente: Paciente, consultaId: String? = null) {
        val fragment = ConsultaFragment.newInstance(paciente, consultaId)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack("consulta")
            .commit()
    }

    /** Abre VerConsultaFragment (modo lectura) para una consulta específica. */
    fun navegarAVerConsulta(paciente: Paciente, consultaId: String) {
        val fragment = VerConsultaFragment.newInstance(paciente, consultaId)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack("ver_consulta")
            .commit()
    }

    private fun updateUI(itemId: Int) {
        val active   = getColor(R.color.nav_active)
        val inactive = getColor(R.color.nav_unselected)

        val labels = mapOf(
            R.id.nav_inicio    to findViewById<TextView>(R.id.navInicioLabel),
            R.id.nav_pacientes to findViewById<TextView>(R.id.navPacientesLabel),
            R.id.nav_ajustes   to findViewById<TextView>(R.id.navAjustesLabel)
        )
        val icons = mapOf(
            R.id.nav_inicio    to findViewById<TextView>(R.id.navInicioIcon),
            R.id.nav_pacientes to findViewById<TextView>(R.id.navPacientesIcon),
            R.id.nav_ajustes   to findViewById<TextView>(R.id.navAjustesIcon)
        )

        labels.values.forEach { it.setTextColor(inactive) }
        icons.values.forEach  { it.setTextColor(inactive) }
        labels[itemId]?.setTextColor(active)
        icons[itemId]?.setTextColor(active)
    }
}
