package com.tuevento.tueventofinal.data.remote

import com.tuevento.tueventofinal.data.model.*

class UsuarioRepository(private val api: ApiService = NetworkModule.apiService) {
    suspend fun getUsuarios()                                    = api.getUsuarios()
    suspend fun getUsuarioById(id: Long)                        = api.getUsuarioById(id)
    suspend fun createUsuario(request: UsuarioRequest)          = api.createUsuario(request)
    // NUEVO: login con endpoint dedicado
    suspend fun login(request: LoginRequest)                     = api.login(request)
    suspend fun updateUsuario(id: Long, request: UsuarioRequest)= api.updateUsuario(id, request)
    suspend fun deleteUsuario(id: Long)                         = api.deleteUsuario(id)
}

class EventoRepository(private val api: ApiService = NetworkModule.apiService) {
    suspend fun getEventos()                                             = api.getEventos()
    suspend fun getEventoById(id: Long)                                  = api.getEventoById(id)
    suspend fun getEventosByOrganizador(id: Long)                        = api.getEventosByOrganizador(id)
    suspend fun createEvento(organizadorId: Long, request: EventoRequest)= api.createEvento(organizadorId, request)
    suspend fun updateEvento(id: Long, request: EventoRequest)           = api.updateEvento(id, request)
    suspend fun deleteEvento(id: Long)                                   = api.deleteEvento(id)
    // Corrección: rutas PATCH específicas del backend
    suspend fun publicarEvento(id: Long)  = api.publicarEvento(id)
    suspend fun cancelarEvento(id: Long)  = api.cancelarEvento(id)
}

class InscripcionRepository(private val api: ApiService = NetworkModule.apiService) {
    suspend fun getInscripcionById(id: Long)              = api.getInscripcionById(id)
    suspend fun getInscripcionesByUsuario(id: Long)       = api.getInscripcionesByUsuario(id)
    suspend fun getInscripcionesByEvento(id: Long)        = api.getInscripcionesByEvento(id)
    suspend fun createInscripcion(request: InscripcionRequest) = api.createInscripcion(request)
    // Corrección: era DELETE, ahora PATCH /cancelar para conservar el registro
    suspend fun cancelarInscripcion(id: Long)             = api.cancelarInscripcion(id)
}

class AsistenciaRepository(private val api: ApiService = NetworkModule.apiService) {
    suspend fun getAsistenciasByEvento(id: Long)          = api.getAsistenciasByEvento(id)
    suspend fun countAsistenciasByEvento(id: Long)        = api.countAsistenciasByEvento(id)
    // Corrección: ruta real POST /api/asistencias/marcar
    suspend fun marcarAsistencia(request: AsistenciaRequest) = api.marcarAsistencia(request)
    // Corrección: endpoint único que valida QR y registra asistencia en un paso
    suspend fun validarQR(codigoQR: String, staffId: Long)   = api.validarQR(codigoQR, staffId)
}

class QRRepository(private val api: ApiService = NetworkModule.apiService) {
    // QR se genera automáticamente al inscribirse — solo consulta
    suspend fun getQRByInscripcion(id: Long)              = api.getQRByInscripcion(id)
    suspend fun getQRByCodigo(codigoUnico: String)        = api.getQRByCodigo(codigoUnico)
}

class FeedbackRepository(private val api: ApiService = NetworkModule.apiService) {
    suspend fun getFeedbackByEvento(id: Long)             = api.getFeedbackByEvento(id)
    suspend fun createFeedback(request: FeedbackRequest)  = api.createFeedback(request)
}

class ImagenRepository(private val api: ApiService = NetworkModule.apiService) {
    suspend fun getImagenesByEvento(id: Long)             = api.getImagenesByEvento(id)
    suspend fun addImagenEvento(request: ImagenEventoRequest) = api.addImagenEvento(request)
    suspend fun deleteImagenEvento(id: Long)              = api.deleteImagenEvento(id)
}

class ChatRepository(private val api: ApiService = NetworkModule.apiService) {
    suspend fun getMensajesByEvento(id: Long)             = api.getMensajesByEvento(id)
    suspend fun enviarMensaje(request: MensajeChatRequest)= api.enviarMensaje(request)
    // Corrección: endpoint faltante para editar mensajes
    suspend fun editarMensaje(id: Long, contenido: String)= api.editarMensaje(id, contenido)
    suspend fun eliminarMensaje(id: Long)                 = api.eliminarMensaje(id)
}

class NotificacionRepository(private val api: ApiService = NetworkModule.apiService) {
    suspend fun getNotificacionesByUsuario(id: Long)      = api.getNotificacionesByUsuario(id)
    // Corrección: endpoint faltante para badge de no-leídas
    suspend fun getNotificacionesNoLeidas(id: Long)       = api.getNotificacionesNoLeidas(id)
    // Corrección: era /leida → la ruta real es /leer
    suspend fun marcarLeida(id: Long)                     = api.marcarLeida(id)
    suspend fun deleteNotificacion(id: Long)              = api.deleteNotificacion(id)
    suspend fun createNotificacion(request: NotificacionRequest) = api.createNotificacion(request)
}

// EstadisticasRepository ELIMINADO — GET /api/estadisticas/* no existe en el backend.
// Las estadísticas se calculan en AdminViewModel y OrganizadorViewModel combinando
// las llamadas reales existentes.
