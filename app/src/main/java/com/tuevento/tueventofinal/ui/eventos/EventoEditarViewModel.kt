package com.tuevento.tueventofinal.ui.eventos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuevento.tueventofinal.data.model.EventoRequest
import com.tuevento.tueventofinal.data.model.EventoResponse
import com.tuevento.tueventofinal.data.model.ImagenEventoRequest
import com.tuevento.tueventofinal.data.remote.ImagenRepository
import com.tuevento.tueventofinal.data.remote.NetworkModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class EventoEditarState {
    object Idle    : EventoEditarState()
    object Loading : EventoEditarState()
    data class Success(val evento: EventoResponse, val imageUrl: String) : EventoEditarState()
    data class Error(val message: String) : EventoEditarState()
}

class EventoEditarViewModel(
    private val imagenRepository: ImagenRepository = ImagenRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<EventoEditarState>(EventoEditarState.Idle)
    val uiState: StateFlow<EventoEditarState> = _uiState

    fun editarEvento(eventoId: Long, request: EventoRequest, imageUrl: String = "") {
        viewModelScope.launch {
            _uiState.value = EventoEditarState.Loading
            try {
                val response = NetworkModule.apiService.updateEvento(eventoId, request)
                if (response.isSuccessful && response.body() != null) {
                    val eventoEditado = response.body()!!

                    // Actualizar imagen si el usuario proporcionó una URL nueva
                    if (imageUrl.isNotBlank() && imageUrl.startsWith("http")) {
                        try {
                            imagenRepository.addImagenEvento(
                                ImagenEventoRequest(
                                    eventoId    = eventoEditado.id,
                                    url         = imageUrl,
                                    descripcion = "Imagen principal",
                                    orden       = 1
                                )
                            )
                        } catch (_: Exception) { /* No bloquear el flujo */ }
                    }

                    _uiState.value = EventoEditarState.Success(eventoEditado, imageUrl)
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                    _uiState.value = EventoEditarState.Error("Error al actualizar: $errorBody")
                }
            } catch (e: Exception) {
                _uiState.value = EventoEditarState.Error("Error de conexión: ${e.message}")
            }
        }
    }

    fun resetState() { _uiState.value = EventoEditarState.Idle }
}
