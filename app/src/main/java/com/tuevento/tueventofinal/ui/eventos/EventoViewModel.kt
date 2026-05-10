package com.tuevento.tueventofinal.ui.eventos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuevento.tueventofinal.data.model.EventoResponse
import com.tuevento.tueventofinal.data.remote.EventoRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class EventoListState {
    object Loading : EventoListState()
    data class Success(val eventos: List<EventoResponse>) : EventoListState()
    data class Error(val message: String) : EventoListState()
}

class EventoViewModel(
    private val repository: EventoRepository = EventoRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<EventoListState>(EventoListState.Loading)
    val uiState: StateFlow<EventoListState> = _uiState

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    // ── Mapa local de URLs de imagen por eventoId ─────────────────────────────
    // Persiste mientras el ViewModel vive. Sobrevive a los GETs del servidor
    // porque el merge se hace por ID — nunca se pierde la URL local.
    private val _imageUrlMap = MutableStateFlow<Map<Long, String>>(emptyMap())
    val imageUrlMap: StateFlow<Map<Long, String>> = _imageUrlMap

    // ── Eventos UI (snackbars de rollback) ────────────────────────────────────
    private val _uiEvents = MutableSharedFlow<String>()
    val uiEvents: SharedFlow<String> = _uiEvents

    // ── FLAG: controla si ya se hizo la carga inicial ─────────────────────────
    // Impide que LaunchedEffect(Unit) en la pantalla relance fetchEventos
    // cada vez que el composable se re-ejecuta al volver del backstack.
    private var initialLoadDone = false

    init { fetchEventos() }

    // ─────────────────────────────────────────────────────────────────────────
    // FETCH PRINCIPAL
    // forceSilent = true → pull-to-refresh (no muestra spinner global)
    // forceInvalidate = true → llamado tras crear/editar para sincronizar
    //   el backend sin perder los datos optimistas locales
    // ─────────────────────────────────────────────────────────────────────────
    fun fetchEventos(forceSilent: Boolean = false, forceInvalidate: Boolean = false) {
        // Si ya cargamos y nadie pide forzar, no relanzar (evita el bug de onResume)
        if (initialLoadDone && !forceSilent && !forceInvalidate) return

        viewModelScope.launch {
            if (!forceSilent) _uiState.value = EventoListState.Loading
            else _isRefreshing.value = true

            try {
                val eventosServidor = repository.getEventos()

                // MERGE: fusiona la lista del servidor con las URLs locales.
                // Los IDs vienen del servidor (son los reales), así que no hay colisión.
                // Si hay eventos optimistas locales que el servidor aún no devuelve,
                // los mantenemos hasta que aparezcan en el GET.
                val estadoActual = _uiState.value
                val eventosLocales: List<EventoResponse> =
                    if (estadoActual is EventoListState.Success) estadoActual.eventos
                    else emptyList()

                // Construir mapa id → evento priorizando la respuesta del servidor
                val serverMap = eventosServidor.associateBy { it.id }
                val localOnlyEvents = eventosLocales.filter { it.id !in serverMap }

                // Eventos locales-only que el servidor aún no confirma: los ponemos al inicio
                val merged = localOnlyEvents + eventosServidor

                _uiState.value = EventoListState.Success(merged)
                initialLoadDone = true

            } catch (e: Exception) {
                if (!forceSilent && _uiState.value is EventoListState.Loading) {
                    _uiState.value = EventoListState.Error(
                        "No se pudo conectar al servidor: ${e.message}"
                    )
                }
                // En pull-to-refresh: no pisar el estado anterior con error
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // AGREGAR EVENTO (Optimistic UI)
    // El evento ya tiene el ID real que devolvió el backend (201 Created).
    // Lo insertamos al frente y luego pedimos un force-refresh en background
    // para que la lista quede sincronizada sin que el usuario lo note.
    // ─────────────────────────────────────────────────────────────────────────
    fun agregarEventoLocal(evento: EventoResponse) {
        _uiState.update { current ->
            when (current) {
                is EventoListState.Success -> {
                    // Deduplicar: si por alguna razón el GET ya lo trajo, no duplicar
                    val sinDuplicado = current.eventos.filter { it.id != evento.id }
                    EventoListState.Success(listOf(evento) + sinDuplicado)
                }
                else -> EventoListState.Success(listOf(evento))
            }
        }
        // Force-refresh en background: sincroniza sin sobrescribir lo optimista
        // porque el merge en fetchEventos respeta los IDs ya presentes
        viewModelScope.launch {
            kotlinx.coroutines.delay(1_500) // dar tiempo al backend a persistir
            fetchEventos(forceSilent = true, forceInvalidate = true)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ACTUALIZAR EVENTO (Optimistic UI)
    // ─────────────────────────────────────────────────────────────────────────
    fun actualizarEventoLocal(eventoActualizado: EventoResponse) {
        _uiState.update { current ->
            if (current is EventoListState.Success) {
                EventoListState.Success(
                    current.eventos.map {
                        if (it.id == eventoActualizado.id) eventoActualizado else it
                    }
                )
            } else current
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ELIMINAR EVENTO (Optimistic UI con rollback)
    // Quita el evento de la lista inmediatamente.
    // Si el servidor falla, restaura el snapshot anterior y notifica.
    // ─────────────────────────────────────────────────────────────────────────
    fun eliminarEvento(eventoId: Long) {
        val snapshot = _uiState.value   // guardar para posible rollback

        // Actualización optimista inmediata
        _uiState.update { current ->
            if (current is EventoListState.Success) {
                EventoListState.Success(current.eventos.filter { it.id != eventoId })
            } else current
        }

        viewModelScope.launch {
            try {
                val response = repository.deleteEvento(eventoId)
                if (response.isSuccessful) {
                    // Éxito: limpiar URL local del mapa
                    _imageUrlMap.update { it - eventoId }
                } else {
                    // Servidor rechazó → rollback
                    _uiState.value = snapshot
                    _uiEvents.emit("❌ No se pudo eliminar el evento (${response.code()})")
                }
            } catch (e: Exception) {
                // Sin conexión → rollback
                _uiState.value = snapshot
                _uiEvents.emit("❌ Sin conexión. El evento no fue eliminado.")
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GESTIÓN DE IMÁGENES
    // ─────────────────────────────────────────────────────────────────────────
    fun setEventoImageUrl(eventoId: Long, url: String) {
        if (url.isNotBlank()) {
            _imageUrlMap.update { it + (eventoId to url) }
        }
    }

    fun getImageUrlForEvento(eventoId: Long): String? = _imageUrlMap.value[eventoId]
}
