package com.tuevento.tueventofinal.ui.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuevento.tueventofinal.data.model.InscripcionResponse
import com.tuevento.tueventofinal.data.remote.InscripcionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class InscripcionListState {
    object Loading : InscripcionListState()
    data class Success(val inscripciones: List<InscripcionResponse>) : InscripcionListState()
    data class Error(val message: String) : InscripcionListState()
}

class InscripcionViewModel(
    private val repository: InscripcionRepository = InscripcionRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<InscripcionListState>(InscripcionListState.Loading)
    val uiState: StateFlow<InscripcionListState> = _uiState

    fun fetchInscripciones(usuarioId: Long) {
        viewModelScope.launch {
            _uiState.value = InscripcionListState.Loading
            try {
                val list = repository.getInscripcionesByUsuario(usuarioId)
                _uiState.value = InscripcionListState.Success(list)
            } catch (e: Exception) {
                _uiState.value = InscripcionListState.Error(e.message ?: "Error al cargar inscripciones")
            }
        }
    }
}
