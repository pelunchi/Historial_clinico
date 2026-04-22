package com.example.historialclinico.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.historialclinico.R

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("historial_prefs", Context.MODE_PRIVATE)

    fun saveUserId(userId: Int) = prefs.edit().putInt("user_id", userId).apply()
    fun getUserId(): Int = prefs.getInt("user_id", -1)

    fun setLoggedIn(value: Boolean) = prefs.edit().putBoolean("is_logged_in", value).apply()
    fun isLoggedIn(): Boolean = prefs.getBoolean("is_logged_in", false)

    fun saveTheme(themeId: Int) = prefs.edit().putInt("theme", themeId).apply()
    fun getTheme(): Int = prefs.getInt("theme", 0)

    fun clearAll() = prefs.edit().clear().apply()
}
