package com.tuevento.tueventofinal.ui.eventos

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuevento.tueventofinal.data.model.EstadoEvento
import com.tuevento.tueventofinal.data.model.EventoRequest
import com.tuevento.tueventofinal.data.model.EventoResponse
import com.tuevento.tueventofinal.data.model.ImagenEventoRequest
import com.tuevento.tueventofinal.data.remote.ImagenRepository
import com.tuevento.tueventofinal.data.remote.NetworkModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class EventoCrearState {
    object Idle    : EventoCrearState()
    object Loading : EventoCrearState()
    // imageUrl viaja junto al evento para propagar al ViewModel compartido
    data class Success(val evento: EventoResponse, val imageUrl: String) : EventoCrearState()
    data class Error(val message: String) : EventoCrearState()
}

class EventoCrearViewModel(
    private val imagenRepository: ImagenRepository = ImagenRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<EventoCrearState>(EventoCrearState.Idle)
    val uiState: StateFlow<EventoCrearState> = _uiState

    fun crearEvento(organizadorId: Long, request: EventoRequest, imageUrl: String = "") {
        viewModelScope.launch {
            _uiState.value = EventoCrearState.Loading
            Log.d("EventoCrear", "POST /api/eventos — organizadorId=$organizadorId")
            try {
                val validatedRequest = request.copy(
                    estado   = EstadoEvento.PUBLICADO,
                    latitud  = request.latitud  ?: 4.6097,
                    longitud = request.longitud ?: -74.0817
                )

                val response = NetworkModule.apiService.createEvento(organizadorId, validatedRequest)

                if (response.isSuccessful && response.body() != null) {
                    val eventoCreado = response.body()!!
                    Log.d("EventoCrear", "✅ Evento creado — id=${eventoCreado.id}, titulo=${eventoCreado.titulo}")

                    // Guardar imagen en el backend (no-blocking: si falla, el evento ya está creado)
                    if (imageUrl.isNotBlank() && imageUrl.startsWith("http")) {
                        try {
                            val imgRes = imagenRepository.addImagenEvento(
                                ImagenEventoRequest(
                                    eventoId    = eventoCreado.id,
                                    url         = imageUrl,
                                    descripcion = "Imagen principal",
                                    orden       = 1
                                )
                            )
                            if (imgRes.isSuccessful) {
                                Log.d("EventoCrear", "✅ Imagen persistida en backend")
                            } else {
                                Log.w("EventoCrear", "⚠️ Imagen no guardada en backend: ${imgRes.code()} — se usará localmente")
                            }
                        } catch (e: Exception) {
                            Log.w("EventoCrear", "⚠️ Error al persistir imagen: ${e.message} — se usará localmente")
                            // No emitir error: el evento sí se creó. La URL vivirá en el mapa local.
                        }
                    }

                    // Emitir éxito con el ID real del backend
                    _uiState.value = EventoCrearState.Success(eventoCreado, imageUrl)

                } else {
                    val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                    Log.e("EventoCrear", "❌ Servidor rechazó el evento: ${response.code()} — $errorBody")
                    _uiState.value = EventoCrearState.Error(
                        when (response.code()) {
                            400  -> "Datos inválidos: $errorBody"
                            404  -> "Organizador no encontrado (id=$organizadorId)"
                            409  -> "Ya existe un evento con ese nombre"
                            else -> "Error ${response.code()}: $errorBody"
                        }
                    )
                }
            } catch (e: Exception) {
                Log.e("EventoCrear", "❌ Error de conexión: ${e.message}", e)
                _uiState.value = EventoCrearState.Error(
                    "Sin conexión con el servidor. Verifica que el backend esté activo."
                )
            }
        }
    }

    fun resetState() { _uiState.value = EventoCrearState.Idle }
}
