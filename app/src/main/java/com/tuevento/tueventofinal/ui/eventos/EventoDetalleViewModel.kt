package com.tuevento.tueventofinal.ui.eventos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuevento.tueventofinal.data.model.EventoResponse
import com.tuevento.tueventofinal.data.model.FeedbackResponse
import com.tuevento.tueventofinal.data.model.ImagenEventoResponse
import com.tuevento.tueventofinal.data.model.InscripcionResponse
import com.tuevento.tueventofinal.data.remote.EventoRepository
import com.tuevento.tueventofinal.data.remote.FeedbackRepository
import com.tuevento.tueventofinal.data.remote.ImagenRepository
import com.tuevento.tueventofinal.data.remote.InscripcionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class EventoDetalleState {
    object Loading : EventoDetalleState()
    data class Success(
        val evento: EventoResponse,
        val feedback: List<FeedbackResponse> = emptyList(),
        val userInscripcion: InscripcionResponse? = null,
        val imagenes: List<ImagenEventoResponse> = emptyList()
    ) : EventoDetalleState()
    data class Error(val message: String) : EventoDetalleState()
}

class EventoDetalleViewModel(
    private val repository: EventoRepository = EventoRepository(),
    private val feedbackRepo: FeedbackRepository = FeedbackRepository(),
    private val inscripcionRepo: InscripcionRepository = InscripcionRepository(),
    private val imagenRepo: ImagenRepository = ImagenRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<EventoDetalleState>(EventoDetalleState.Loading)
    val uiState: StateFlow<EventoDetalleState> = _uiState

    fun fetchEvento(eventoId: Long, usuarioId: Long) {
        viewModelScope.launch {
            _uiState.value = EventoDetalleState.Loading
            try {
                val response = repository.getEventoById(eventoId)
                val feedback = feedbackRepo.getFeedbackByEvento(eventoId)
                val imagenes = imagenRepo.getImagenesByEvento(eventoId)
                val inscripciones = inscripcionRepo.getInscripcionesByUsuario(usuarioId)
                val userInscripcion = inscripciones.find { it.eventoId == eventoId }

                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = EventoDetalleState.Success(
                        evento = response.body()!!,
                        feedback = feedback,
                        userInscripcion = userInscripcion,
                        imagenes = imagenes
                    )
                } else {
                    _uiState.value = EventoDetalleState.Error("Error al cargar detalle")
                }
            } catch (e: Exception) {
                _uiState.value = EventoDetalleState.Error(e.message ?: "Error desconocido")
            }
        }
    }
}
