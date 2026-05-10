package com.tuevento.tueventofinal.ui.reportes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuevento.tueventofinal.data.remote.AsistenciaRepository
import com.tuevento.tueventofinal.data.remote.EventoRepository
import com.tuevento.tueventofinal.data.remote.InscripcionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ReportesState {
    object Idle : ReportesState()
    object Loading : ReportesState()
    data class Success(val stats: Map<String, Any>) : ReportesState()
    data class Error(val message: String) : ReportesState()
}

class ReportesViewModel(
    private val eventoRepo: EventoRepository = EventoRepository(),
    private val inscripcionRepo: InscripcionRepository = InscripcionRepository(),
    private val asistenciaRepo: AsistenciaRepository = AsistenciaRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReportesState>(ReportesState.Idle)
    val uiState: StateFlow<ReportesState> = _uiState

    fun loadEstadisticas(organizadorId: Long) {
        viewModelScope.launch {
            _uiState.value = ReportesState.Loading
            try {
                val eventos = eventoRepo.getEventosByOrganizador(organizadorId)
                val totalInscripciones = eventos.sumOf { inscripcionRepo.getInscripcionesByEvento(it.id).size }
                
                val stats = mapOf(
                    "Total Eventos" to eventos.size,
                    "Total Inscritos" to totalInscripciones,
                    "Eventos Activos" to eventos.count { it.estado.name == "ACTIVO" }
                )
                _uiState.value = ReportesState.Success(stats)
            } catch (e: Exception) {
                _uiState.value = ReportesState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    // Mock para simular generación de reportes por ahora
    fun generarCSV(stats: Map<String, Any>): String {
        val builder = StringBuilder()
        builder.append("Metrica,Valor\n")
        stats.forEach { (key, value) ->
            builder.append("$key,$value\n")
        }
        return builder.toString()
    }
}
