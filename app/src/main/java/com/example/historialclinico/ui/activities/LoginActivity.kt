package com.example.historialclinico.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.historialclinico.R
import com.example.historialclinico.data.database.UserRepository
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import androidx.core.content.edit

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val userRepo = UserRepository()

    // Pestañas
    private lateinit var btnTabLogin: Button
    private lateinit var btnTabRegister: Button
    private lateinit var layoutLogin: CardView
    private lateinit var layoutRegister: CardView

    // Login
    private lateinit var editTextEmail: TextInputEditText
    private lateinit var editTextPassword: TextInputEditText
    private lateinit var buttonLogin: com.google.android.material.button.MaterialButton
    private lateinit var textViewForgotPassword: TextView

    // Registro
    private lateinit var editTextName: TextInputEditText
    private lateinit var editTextEmailRegister: TextInputEditText
    private lateinit var editTextPasswordRegister: TextInputEditText
    private lateinit var editTextConfirmPassword: TextInputEditText
    private lateinit var buttonRegister: com.google.android.material.button.MaterialButton

    // Biometria
    private lateinit var buttonBiometric: com.google.android.material.button.MaterialButton
    private lateinit var sharedPrefs: android.content.SharedPreferences
    private val PREFS_NAME = "historial_prefs"
    private val KEY_BIOMETRIC = "biometric_enabled"
    private val KEY_EMAIL = "saved_email"
    private val KEY_PASSWORD = "saved_password"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        sharedPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)


        initViews()
        if (auth.currentUser != null) {
            // Mostrar boton de huella si ya esta activado
            if (sharedPrefs.getBoolean(KEY_BIOMETRIC, false)) {
                showBiometricButton()
                showBiometricPrompt()
            } else {
                goToMain()
            }
            return
        }

        // Mostrar boton si no hay sesion activa pero ya activó huella antes
        if (sharedPrefs.getBoolean(KEY_BIOMETRIC, false)) {
            showBiometricButton()
            showBiometricPrompt()
        }

        setupListeners()
        showLoginTab()
    }

    private fun initViews() {
        btnTabLogin              = findViewById(R.id.btnTabLogin)
        btnTabRegister           = findViewById(R.id.btnTabRegister)
        layoutLogin              = findViewById(R.id.layoutLogin)
        layoutRegister           = findViewById(R.id.layoutRegister)
        editTextEmail            = findViewById(R.id.editTextEmail)
        editTextPassword         = findViewById(R.id.editTextPassword)
        buttonLogin              = findViewById(R.id.buttonLogin)
        textViewForgotPassword   = findViewById(R.id.textViewForgotPassword)
        editTextName             = findViewById(R.id.editTextName)
        editTextEmailRegister    = findViewById(R.id.editTextEmailRegister)
        editTextPasswordRegister = findViewById(R.id.editTextPasswordRegister)
        editTextConfirmPassword  = findViewById(R.id.editTextConfirmPassword)
        buttonRegister           = findViewById(R.id.buttonRegister)
        buttonBiometric = findViewById(R.id.buttonBiometric)
    }

    private fun setupListeners() {
        btnTabLogin.setOnClickListener    { showLoginTab() }
        btnTabRegister.setOnClickListener { showRegisterTab() }

        buttonLogin.setOnClickListener {
            val email    = editTextEmail.text.toString().trim()
            val password = editTextPassword.text.toString().trim()
            if (validateLogin(email, password)) login(email, password)
        }

        buttonRegister.setOnClickListener {
            val name     = editTextName.text.toString().trim()
            val email    = editTextEmailRegister.text.toString().trim()
            val password = editTextPasswordRegister.text.toString().trim()
            val confirm  = editTextConfirmPassword.text.toString().trim()
            if (validateRegister(name, email, password, confirm)) register(name, email, password)
        }

        textViewForgotPassword.setOnClickListener {
            val email = editTextEmail.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Escribe tu correo primero", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            auth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    Toast.makeText(this, "Correo de recuperación enviado", Toast.LENGTH_LONG).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    // ── Autenticación Firebase ──────────────────────────────────────────

    private fun login(email: String, password: String)
    {
        buttonLogin.isEnabled = false
        buttonLogin.text = "Iniciando sesión..."

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                Toast.makeText(this, "¡Bienvenido!", Toast.LENGTH_SHORT).show()

                /*
                Si no hay huella activada o el correo es diferente al guardado,
                preguntar si quiere activarla
                */
                val emailGuardado = sharedPrefs.getString(KEY_EMAIL, "")
                if (!sharedPrefs.getBoolean(KEY_BIOMETRIC, false) || emailGuardado != email) {
                    askToEnableBiometric(email, password)
                } else {
                    goToMain()
                }
            }
            .addOnFailureListener { exception ->
                // Mensajes de error específicos
                val mensaje = when (exception) {
                    is FirebaseAuthInvalidUserException -> "No existe una cuenta con ese correo"
                    is FirebaseAuthInvalidCredentialsException -> "Contraseña incorrecta"
                    else -> "Error al iniciar sesión. Verifica tu conexión"
                }
                Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
                buttonLogin.isEnabled = true
                buttonLogin.text = "Iniciar Sesión"
            }
    }

    private fun register(nombre: String, email: String, password: String) {
        buttonRegister.isEnabled = false
        buttonRegister.text = "Creando cuenta..."

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                // Guardar nombre del doctor en Firebase
                CoroutineScope(Dispatchers.IO).launch {
                    try { userRepo.guardarPerfil(nombre) } catch (_: Exception) {}
                }
                Toast.makeText(this, "¡Cuenta creada!", Toast.LENGTH_SHORT).show()
                goToMain()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_LONG).show()
                buttonRegister.isEnabled = true
                buttonRegister.text = "Crear Cuenta"
            }
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    // ── Pestañas ────────────────────────────────────────────────────────

    private fun showLoginTab() {
        layoutLogin.visibility    = View.VISIBLE
        layoutRegister.visibility = View.GONE
        btnTabLogin.setBackgroundResource(R.drawable.tab_selected)
        btnTabLogin.setTextColor(getColor(android.R.color.white))
        btnTabRegister.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        btnTabRegister.setTextColor(android.graphics.Color.parseColor("#888888"))
    }

    private fun showRegisterTab() {
        layoutLogin.visibility    = View.GONE
        layoutRegister.visibility = View.VISIBLE
        btnTabRegister.setBackgroundResource(R.drawable.tab_selected)
        btnTabRegister.setTextColor(getColor(android.R.color.white))
        btnTabLogin.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        btnTabLogin.setTextColor(android.graphics.Color.parseColor("#888888"))
    }

    // ── Validaciones ────────────────────────────────────────────────────

    private fun validateLogin(email: String, password: String): Boolean {
        if (email.isEmpty()) { editTextEmail.error = "Ingresa tu correo"; return false }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.error = "Correo inválido"; return false
        }
        if (password.isEmpty()) { editTextPassword.error = "Ingresa tu contraseña"; return false }
        if (password.length < 6) { editTextPassword.error = "Mínimo 6 caracteres"; return false }
        return true
    }

    private fun validateRegister(name: String, email: String, password: String, confirm: String): Boolean {
        if (name.isEmpty()) { editTextName.error = "Ingresa tu nombre"; return false }
        if (email.isEmpty()) { editTextEmailRegister.error = "Ingresa tu correo"; return false }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmailRegister.error = "Correo inválido"; return false
        }
        if (password.isEmpty() || password.length < 6) {
            editTextPasswordRegister.error = "Mínimo 6 caracteres"; return false
        }
        if (password != confirm) {
            editTextConfirmPassword.error = "Las contraseñas no coinciden"; return false
        }
        return true
    }

    // Funcion para mostrar el boton de huella
    private fun showBiometricButton() {
        buttonBiometric.visibility = View.VISIBLE
        buttonBiometric.setOnClickListener { showBiometricPrompt() }
    }

    // Función principal de la huella
    private fun showBiometricPrompt() {
        val executor = androidx.core.content.ContextCompat.getMainExecutor(this)
        val biometricPrompt = androidx.biometric.BiometricPrompt(
            this, executor,
            object : androidx.biometric.BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: androidx.biometric.BiometricPrompt.AuthenticationResult) {
                    val email    = sharedPrefs.getString(KEY_EMAIL, "") ?: ""
                    val password = sharedPrefs.getString(KEY_PASSWORD, "") ?: ""
                    if (email.isNotEmpty() && password.isNotEmpty()) {
                        login(email, password)
                    }
                }
                override fun onAuthenticationFailed() {
                    Toast.makeText(this@LoginActivity, "Huella no reconocida", Toast.LENGTH_SHORT).show()
                }
            }
        )

        val promptInfo = androidx.biometric.BiometricPrompt.PromptInfo.Builder()
            .setTitle("Inicio de sesion por biometria")
            .setSubtitle("Usa tu huella para entrar")
            .setNegativeButtonText("Usar contraseña")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    // Funcion para preguntar si activa la huella
    private fun askToEnableBiometric(email: String, password: String) {
        com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle("Acceso rápido")
            .setMessage("¿Quieres activar el acceso con huella dactilar?")
            .setPositiveButton("Sí") { _, _ ->
                sharedPrefs.edit {
                    putBoolean(KEY_BIOMETRIC, true)
                        .putString(KEY_EMAIL, email)
                        .putString(KEY_PASSWORD, password)
                }
                goToMain()
            }
            .setNegativeButton("No") { _, _ -> goToMain() }
            .show()
    }
}
