package com.tuevento.tueventofinal.ui.organizador

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuevento.tueventofinal.data.model.EventoResponse
import com.tuevento.tueventofinal.data.remote.AsistenciaRepository
import com.tuevento.tueventofinal.data.remote.EventoRepository
import com.tuevento.tueventofinal.data.remote.InscripcionRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class OrganizadorUiState {
    object Loading : OrganizadorUiState()
    data class Success(
        val eventos: List<EventoResponse>,
        val stats: Map<String, Any>? = null
    ) : OrganizadorUiState()
    data class Error(val message: String) : OrganizadorUiState()
}

class OrganizadorViewModel(
    private val eventoRepo:      EventoRepository      = EventoRepository(),
    private val inscripcionRepo: InscripcionRepository = InscripcionRepository(),
    private val asistenciaRepo:  AsistenciaRepository  = AsistenciaRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<OrganizadorUiState>(OrganizadorUiState.Loading)
    val uiState: StateFlow<OrganizadorUiState> = _uiState

    // Eventos UI (snackbars de rollback)
    private val _uiEvents = MutableSharedFlow<String>()
    val uiEvents: SharedFlow<String> = _uiEvents

    fun loadDashboard(organizadorId: Long) {
        viewModelScope.launch {
            _uiState.value = OrganizadorUiState.Loading
            try {
                val eventos = eventoRepo.getEventosByOrganizador(organizadorId)

                // Calcular inscritos y asistentes en paralelo
                var totalInscritos  = 0
                var totalAsistentes = 0

                val jobs = eventos.map { evento ->
                    async {
                        val ins  = runCatching {
                            inscripcionRepo.getInscripcionesByEvento(evento.id).size
                        }.getOrDefault(0)
                        val asis = runCatching {
                            asistenciaRepo.getAsistenciasByEvento(evento.id).count { it.presente }
                        }.getOrDefault(0)
                        Pair(ins, asis)
                    }
                }
                jobs.forEach { d ->
                    val (i, a) = d.await()
                    totalInscritos  += i
                    totalAsistentes += a
                }

                val porcentaje = if (totalInscritos > 0)
                    (totalAsistentes * 100 / totalInscritos) else 0

                // FIX B-5: Claves coinciden con las que usa OrganizadorDashboardScreen
                val stats = mapOf<String, Any>(
                    "porcentajeAsistencia" to porcentaje,   // Usado en StatCardPremium
                    "totalVentas"          to totalInscritos, // Reutilizar para "Inscritos"
                    "total_eventos"        to eventos.size,
                    "eventos_activos"      to eventos.count { it.estado.name == "ACTIVO" }
                )

                _uiState.value = OrganizadorUiState.Success(eventos, stats)
            } catch (e: Exception) {
                _uiState.value = OrganizadorUiState.Error("Error al cargar dashboard: ${e.message}")
            }
        }
    }

    fun publicarEvento(eventoId: Long, organizadorId: Long) {
        viewModelScope.launch {
            runCatching { eventoRepo.publicarEvento(eventoId) }
            loadDashboard(organizadorId)
        }
    }

    fun cancelarEvento(eventoId: Long, organizadorId: Long) {
        viewModelScope.launch {
            runCatching { eventoRepo.cancelarEvento(eventoId) }
            loadDashboard(organizadorId)
        }
    }

    fun eliminarEvento(eventoId: Long) {
        val snapshot = _uiState.value
        if (snapshot is OrganizadorUiState.Success) {
            _uiState.value = snapshot.copy(
                eventos = snapshot.eventos.filter { it.id != eventoId }
            )
        }

        viewModelScope.launch {
            try {
                val response = eventoRepo.deleteEvento(eventoId)
                if (!response.isSuccessful) {
                    _uiState.value = snapshot
                    _uiEvents.emit("❌ No se pudo eliminar el evento (${response.code()})")
                }
            } catch (e: Exception) {
                _uiState.value = snapshot
                _uiEvents.emit("❌ Sin conexión. El evento no fue eliminado.")
            }
        }
    }
}
