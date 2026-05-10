package com.tuevento.tueventofinal.ui.notificaciones

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuevento.tueventofinal.data.model.NotificacionResponse
import com.tuevento.tueventofinal.data.remote.NotificacionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class NotificacionState {
    object Loading : NotificacionState()
    data class Success(val notificaciones: List<NotificacionResponse>) : NotificacionState()
    data class Error(val message: String) : NotificacionState()
}

class NotificacionViewModel(
    private val repository: NotificacionRepository = NotificacionRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<NotificacionState>(NotificacionState.Loading)
    val uiState: StateFlow<NotificacionState> = _uiState

    fun fetchNotificaciones(usuarioId: Long) {
        viewModelScope.launch {
            _uiState.value = NotificacionState.Loading
            try {
                val list = repository.getNotificacionesByUsuario(usuarioId)
                _uiState.value = NotificacionState.Success(list)
            } catch (e: Exception) {
                _uiState.value = NotificacionState.Error(e.message ?: "Error al cargar notificaciones")
            }
        }
    }

    fun marcarComoLeida(id: Long, usuarioId: Long) {
        viewModelScope.launch {
            try {
                repository.marcarLeida(id)
                fetchNotificaciones(usuarioId)
            } catch (e: Exception) {
                // Silently fail or handle error
            }
        }
    }
}
