package com.tuevento.tueventofinal.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuevento.tueventofinal.data.model.RolUsuario
import com.tuevento.tueventofinal.data.model.UsuarioRequest
import com.tuevento.tueventofinal.data.model.UsuarioResponse
import com.tuevento.tueventofinal.data.remote.UsuarioRepository
import com.tuevento.tueventofinal.util.ValidationUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class RegisterState {
    object Idle    : RegisterState()
    object Loading : RegisterState()
    data class Success(val user: UsuarioResponse) : RegisterState()
    data class Error(val message: String) : RegisterState()
}

class RegisterViewModel(
    private val repository: UsuarioRepository = UsuarioRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val uiState: StateFlow<RegisterState> = _uiState

    // FIX B-2: Añadir parámetro `rol` que coincide con la llamada en RegisterScreen
    fun register(
        nombre: String,
        apellido: String,
        email: String,
        password: String,
        telefono: String,
        rol: RolUsuario = RolUsuario.USUARIO
    ) {
        when {
            !ValidationUtils.isNotBlank(nombre) -> {
                _uiState.value = RegisterState.Error("El nombre es obligatorio"); return
            }
            !ValidationUtils.isNotBlank(apellido) -> {
                _uiState.value = RegisterState.Error("El apellido es obligatorio"); return
            }
            !ValidationUtils.isValidEmail(email) -> {
                _uiState.value = RegisterState.Error("Ingresa un correo válido"); return
            }
            password.length < 6 -> {
                _uiState.value = RegisterState.Error("La contraseña debe tener al menos 6 caracteres"); return
            }
            telefono.isNotBlank() && !ValidationUtils.isValidPhone(telefono) -> {
                _uiState.value = RegisterState.Error("El teléfono debe tener 10 dígitos"); return
            }
        }

        viewModelScope.launch {
            _uiState.value = RegisterState.Loading
            try {
                val request = UsuarioRequest(
                    nombre   = nombre.trim(),
                    apellido = apellido.trim(),
                    email    = email.trim().lowercase(),
                    password = password,
                    telefono = telefono.trim().ifBlank { null },
                    rol      = rol // Enviar el rol seleccionado
                )
                val response = repository.createUsuario(request)
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = RegisterState.Success(response.body()!!)
                } else {
                    _uiState.value = RegisterState.Error(
                        when (response.code()) {
                            409  -> "Ya existe una cuenta con ese correo."
                            400  -> "Datos inválidos. Verifica los campos."
                            else -> "Error al registrar (${response.code()})"
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.value = RegisterState.Error("Error de conexión: ${e.message}")
            }
        }
    }

    fun resetState() { _uiState.value = RegisterState.Idle }
}
