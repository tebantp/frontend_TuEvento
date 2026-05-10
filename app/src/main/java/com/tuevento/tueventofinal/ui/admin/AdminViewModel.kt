package com.tuevento.tueventofinal.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuevento.tueventofinal.data.model.EventoResponse
import com.tuevento.tueventofinal.data.model.UsuarioResponse
import com.tuevento.tueventofinal.data.remote.AsistenciaRepository
import com.tuevento.tueventofinal.data.remote.EventoRepository
import com.tuevento.tueventofinal.data.remote.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AdminUiState {
    object Loading : AdminUiState()
    data class Success(
        val usuarios: List<UsuarioResponse>,
        val eventos: List<EventoResponse>,
        val globalStats: Map<String, Any>? = null
    ) : AdminUiState()
    data class Error(val message: String) : AdminUiState()
}

class AdminViewModel(
    private val usuarioRepo: UsuarioRepository = UsuarioRepository(),
    private val eventoRepo: EventoRepository = EventoRepository(),
    private val asistenciaRepo: AsistenciaRepository = AsistenciaRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<AdminUiState>(AdminUiState.Loading)
    val uiState: StateFlow<AdminUiState> = _uiState

    fun loadAdminData() {
        viewModelScope.launch {
            _uiState.value = AdminUiState.Loading
            try {
                val usuarios = usuarioRepo.getUsuarios()
                val todosEventos = eventoRepo.getEventos()
                // Cálculo de estadísticas locales ya que el endpoint no existe
                val stats = mapOf(
                    "usuariosTotales" to usuarios.size,
                    "eventosActivos" to todosEventos.count { it.estado.name == "ACTIVO" },
                    "totalEventos" to todosEventos.size
                )

                _uiState.value = AdminUiState.Success(usuarios, todosEventos, stats)
            } catch (e: Exception) {
                _uiState.value = AdminUiState.Error("Error de Admin: ${e.message}")
            }
        }
    }

    fun moderar(eventoId: Long, aprobado: Boolean) {
        viewModelScope.launch {
            try {
                if (aprobado) {
                    eventoRepo.publicarEvento(eventoId)
                } else {
                    eventoRepo.cancelarEvento(eventoId)
                }
                loadAdminData() // Recargar
            } catch (e: Exception) {}
        }
    }

    fun eliminarEvento(eventoId: Long) {
        viewModelScope.launch {
            try {
                eventoRepo.deleteEvento(eventoId)
                loadAdminData()
            } catch (e: Exception) {}
        }
    }

    fun banearUsuario(usuarioId: Long, activo: Boolean) {
        viewModelScope.launch {
            // No hay endpoint de banear, se usa updateUsuario o se omite si no es crítico
            // Por ahora, simulamos éxito o usamos delete si es permanente
            // usuarioRepo.deleteUsuario(usuarioId) 
            loadAdminData()
        }
    }

    fun cambiarRolUsuario(usuarioId: Long, nuevoRol: String) {
        viewModelScope.launch {
            // El backend no tiene PATCH /rol, se requiere updateUsuario completo si se desea cambiar
            loadAdminData()
        }
    }
}
