package com.tuevento.tueventofinal.ui.asistencia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuevento.tueventofinal.data.model.AsistenciaRequest
import com.tuevento.tueventofinal.data.model.AsistenciaResponse
import com.tuevento.tueventofinal.data.model.MetodoRegistro
import com.tuevento.tueventofinal.data.remote.AsistenciaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AsistenciaState {
    object Idle    : AsistenciaState()
    object Loading : AsistenciaState()
    data class Success(val asistencia: AsistenciaResponse) : AsistenciaState()
    data class Error(val message: String) : AsistenciaState()
}

class AsistenciaViewModel(
    private val repository: AsistenciaRepository = AsistenciaRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<AsistenciaState>(AsistenciaState.Idle)
    val uiState: StateFlow<AsistenciaState> = _uiState

    // FIX B-3: Una sola llamada al endpoint POST /api/asistencias/validar-qr
    fun registrarPorQR(codigoQR: String, staffId: Long) {
        if (codigoQR.isBlank()) {
            _uiState.value = AsistenciaState.Error("Código QR inválido")
            return
        }
        viewModelScope.launch {
            _uiState.value = AsistenciaState.Loading
            try {
                val response = repository.validarQR(codigoQR.trim(), staffId)
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = AsistenciaState.Success(response.body()!!)
                } else {
                    _uiState.value = AsistenciaState.Error(
                        when (response.code()) {
                            404 -> "Código QR no reconocido"
                            409 -> "Este QR ya fue utilizado anteriormente"
                            410 -> "El código QR ha expirado"
                            else -> "Error al registrar asistencia (${response.code()})"
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.value = AsistenciaState.Error(
                    "Error de conexión: verifica que el servidor esté activo"
                )
            }
        }
    }

    fun registrarManual(inscripcionId: Long, staffId: Long) {
        viewModelScope.launch {
            _uiState.value = AsistenciaState.Loading
            try {
                val request = AsistenciaRequest(
                    inscripcionId = inscripcionId,
                    staffId       = staffId,
                    metodo        = MetodoRegistro.CHECKBOX
                )
                val response = repository.marcarAsistencia(request)
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = AsistenciaState.Success(response.body()!!)
                } else {
                    _uiState.value = AsistenciaState.Error(
                        when (response.code()) {
                            409 -> "Asistencia ya registrada"
                            404 -> "Inscripción no encontrada"
                            else -> "Error al registrar (${response.code()})"
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.value = AsistenciaState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun resetState() { _uiState.value = AsistenciaState.Idle }
}
