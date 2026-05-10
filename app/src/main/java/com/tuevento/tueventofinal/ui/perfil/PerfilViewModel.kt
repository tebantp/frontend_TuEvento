package com.tuevento.tueventofinal.ui.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuevento.tueventofinal.data.model.UsuarioRequest
import com.tuevento.tueventofinal.data.model.UsuarioResponse
import com.tuevento.tueventofinal.data.remote.NetworkModule
import com.tuevento.tueventofinal.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

sealed class PerfilState {
    object Idle : PerfilState()
    object Loading : PerfilState()
    object Success : PerfilState()
    data class Error(val message: String) : PerfilState()
}

class PerfilViewModel(private val sessionManager: SessionManager) : ViewModel() {

    private val _uiState = MutableStateFlow<PerfilState>(PerfilState.Idle)
    val uiState: StateFlow<PerfilState> = _uiState

    private val _usuario = MutableStateFlow<UsuarioResponse?>(sessionManager.getUser())
    val usuario: StateFlow<UsuarioResponse?> = _usuario

    fun actualizarPerfil(id: Long, request: UsuarioRequest) {
        viewModelScope.launch {
            _uiState.value = PerfilState.Loading
            try {
                val response = NetworkModule.apiService.updateUsuario(id, request)
                if (response.isSuccessful && response.body() != null) {
                    val updatedUser = response.body()!!
                    sessionManager.saveSession(updatedUser)
                    _usuario.value = updatedUser
                    _uiState.value = PerfilState.Success
                } else {
                    _uiState.value = PerfilState.Error("Error al actualizar: ${response.message()}")
                }
            } catch (e: Exception) {
                _uiState.value = PerfilState.Error(e.message ?: "Error de red")
            }
        }
    }

    fun subirFoto(id: Long, part: MultipartBody.Part) {
        // ELIMINADO: POST /api/usuarios/{id}/foto no existe en el backend.
        // Se omite la subida por ahora para evitar errores 404.
    }

    fun logout() {
        sessionManager.logout()
    }

    fun resetState() {
        _uiState.value = PerfilState.Idle
    }
}
