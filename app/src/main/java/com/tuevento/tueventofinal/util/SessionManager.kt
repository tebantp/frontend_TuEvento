package com.tuevento.tueventofinal.util

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.tuevento.tueventofinal.data.model.UsuarioResponse
import com.tuevento.tueventofinal.data.remote.NetworkModule

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREF_NAME        = "TuEventoPrefs"
        private const val KEY_USER         = "user_session"
        private const val KEY_ROLE         = "user_role"
        private const val KEY_TOKEN        = "auth_token"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }

    // El backend NO emite JWT actualmente.
    // Cuando lo implemente, pasar el token como parámetro y se inyectará en Retrofit.
    fun saveSession(user: UsuarioResponse, token: String? = null) {
        val userJson = gson.toJson(user)
        prefs.edit().apply {
            putString(KEY_USER, userJson)
            putString(KEY_ROLE, user.rol.name)
            putBoolean(KEY_IS_LOGGED_IN, true)
            token?.let { putString(KEY_TOKEN, it) }
            apply()
        }
        // Propagar token al interceptor de Retrofit
        NetworkModule.setAuthToken(token)
    }

    fun restoreToken() {
        // Llamar en el arranque de la app (MainActivity/Application) para restaurar
        // el token en el interceptor después de un reinicio de la app.
        val token = prefs.getString(KEY_TOKEN, null)
        NetworkModule.setAuthToken(token)
    }

    fun getRole(): String? = prefs.getString(KEY_ROLE, null)

    fun getUser(): UsuarioResponse? {
        val userJson = prefs.getString(KEY_USER, null) ?: return null
        return try {
            gson.fromJson(userJson, UsuarioResponse::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    fun logout() {
        prefs.edit().clear().apply()
        NetworkModule.setAuthToken(null)
    }
}
