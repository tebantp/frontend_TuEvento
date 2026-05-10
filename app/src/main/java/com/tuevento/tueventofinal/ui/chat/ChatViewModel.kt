package com.tuevento.tueventofinal.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuevento.tueventofinal.data.model.MensajeChatRequest
import com.tuevento.tueventofinal.data.model.MensajeChatResponse
import com.tuevento.tueventofinal.data.remote.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ChatState {
    object Loading : ChatState()
    data class Success(val mensajes: List<MensajeChatResponse>) : ChatState()
    data class Error(val message: String) : ChatState()
}

class ChatViewModel(
    private val repository: ChatRepository = ChatRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<ChatState>(ChatState.Loading)
    val uiState: StateFlow<ChatState> = _uiState

    fun fetchMensajes(eventoId: Long) {
        viewModelScope.launch {
            _uiState.value = ChatState.Loading
            try {
                val mensajes = repository.getMensajesByEvento(eventoId)
                _uiState.value = ChatState.Success(mensajes)
            } catch (e: Exception) {
                _uiState.value = ChatState.Error(e.message ?: "Error al cargar mensajes")
            }
        }
    }

    fun enviarMensaje(remitenteId: Long, eventoId: Long, contenido: String) {
        viewModelScope.launch {
            try {
                val request = MensajeChatRequest(remitenteId, eventoId, contenido)
                val response = repository.enviarMensaje(request)
                if (response.isSuccessful) {
                    fetchMensajes(eventoId) // Recargar mensajes
                }
            } catch (e: Exception) {
                // Manejar error de envío
            }
        }
    }
}
