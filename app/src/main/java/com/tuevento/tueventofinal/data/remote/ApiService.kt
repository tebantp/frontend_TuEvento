package com.tuevento.tueventofinal.data.remote

import com.tuevento.tueventofinal.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ─── USUARIOS (5 endpoints verificados) ──────────────────────────────────
    @GET("api/usuarios")
    suspend fun getUsuarios(): List<UsuarioResponse>

    @GET("api/usuarios/{id}")
    suspend fun getUsuarioById(@Path("id") id: Long): Response<UsuarioResponse>

    @POST("api/usuarios")
    suspend fun createUsuario(@Body request: UsuarioRequest): Response<UsuarioResponse>

    // NUEVO: Endpoint de autenticación — devuelve el usuario con su rol real
    @POST("api/usuarios/login")
    suspend fun login(@Body request: LoginRequest): Response<UsuarioResponse>

    @PUT("api/usuarios/{id}")
    suspend fun updateUsuario(
        @Path("id") id: Long,
        @Body request: UsuarioRequest
    ): Response<UsuarioResponse>

    @DELETE("api/usuarios/{id}")
    suspend fun deleteUsuario(@Path("id") id: Long): Response<Void>

    // ELIMINADOS (no existen en el backend):
    //   POST /api/usuarios/{id}/foto  — uploadFotoPerfil()
    //   PATCH /api/usuarios/{id}/rol
    //   PATCH /api/usuarios/{id}/estado

    // ─── EVENTOS (7 endpoints verificados) ───────────────────────────────────
    @GET("api/eventos")
    suspend fun getEventos(): List<EventoResponse>

    @GET("api/eventos/{id}")
    suspend fun getEventoById(@Path("id") id: Long): Response<EventoResponse>

    @GET("api/eventos/organizador/{id}")
    suspend fun getEventosByOrganizador(@Path("id") id: Long): List<EventoResponse>

    @POST("api/eventos")
    suspend fun createEvento(
        @Query("organizadorId") organizadorId: Long,
        @Body request: EventoRequest
    ): Response<EventoResponse>

    @PUT("api/eventos/{id}")
    suspend fun updateEvento(
        @Path("id") id: Long,
        @Body request: EventoRequest
    ): Response<EventoResponse>

    // CORRECCIÓN: Las rutas específicas PATCH — NO existe PATCH /eventos/{id}/estado
    @PATCH("api/eventos/{id}/publicar")
    suspend fun publicarEvento(@Path("id") id: Long): Response<EventoResponse>

    @PATCH("api/eventos/{id}/cancelar")
    suspend fun cancelarEvento(@Path("id") id: Long): Response<EventoResponse>

    @DELETE("api/eventos/{id}")
    suspend fun deleteEvento(@Path("id") id: Long): Response<Void>

    // ─── INSCRIPCIONES (5 endpoints verificados) ─────────────────────────────
    // ELIMINADO: GET /api/inscripciones (lista global no existe)
    @GET("api/inscripciones/{id}")
    suspend fun getInscripcionById(@Path("id") id: Long): Response<InscripcionResponse>

    @GET("api/inscripciones/usuario/{id}")
    suspend fun getInscripcionesByUsuario(@Path("id") id: Long): List<InscripcionResponse>

    @GET("api/inscripciones/evento/{id}")
    suspend fun getInscripcionesByEvento(@Path("id") id: Long): List<InscripcionResponse>

    @POST("api/inscripciones")
    suspend fun createInscripcion(@Body request: InscripcionRequest): Response<InscripcionResponse>

    // CORRECCIÓN: Era @DELETE que borraba el registro permanentemente.
    // El backend espera PATCH /cancelar para cambiar estado a CANCELADA.
    @PATCH("api/inscripciones/{id}/cancelar")
    suspend fun cancelarInscripcion(@Path("id") id: Long): Response<Void>

    // ─── ASISTENCIAS (4 endpoints verificados) ───────────────────────────────
    // CORRECCIÓN: POST /api/asistencias no existe. La ruta real es /marcar
    @POST("api/asistencias/marcar")
    suspend fun marcarAsistencia(@Body request: AsistenciaRequest): Response<AsistenciaResponse>

    // CORRECCIÓN: Endpoint único que valida QR + registra asistencia en un paso
    @POST("api/asistencias/validar-qr")
    suspend fun validarQR(
        @Query("codigoQR") codigoQR: String,
        @Query("staffId")  staffId: Long
    ): Response<AsistenciaResponse>

    @GET("api/asistencias/evento/{id}")
    suspend fun getAsistenciasByEvento(@Path("id") id: Long): List<AsistenciaResponse>

    @GET("api/asistencias/evento/{id}/count")
    suspend fun countAsistenciasByEvento(@Path("id") id: Long): Response<Map<String, Long>>

    // ─── CODIGOS QR (2 endpoints verificados) ────────────────────────────────
    // ELIMINADOS: POST /api/qr, GET /api/qr, GET /api/qr/codigo/{codigo}
    // CORRECCIÓN: La ruta real es /api/qr/{codigoUnico} (no /codigo/{codigo})
    @GET("api/qr/{codigoUnico}")
    suspend fun getQRByCodigo(@Path("codigoUnico") codigoUnico: String): Response<CodigoQRResponse>

    @GET("api/qr/inscripcion/{id}")
    suspend fun getQRByInscripcion(@Path("id") id: Long): Response<CodigoQRResponse>

    // ─── FEEDBACK (2 endpoints verificados) ──────────────────────────────────
    @GET("api/feedback/evento/{id}")
    suspend fun getFeedbackByEvento(@Path("id") id: Long): List<FeedbackResponse>

    @POST("api/feedback")
    suspend fun createFeedback(@Body request: FeedbackRequest): Response<FeedbackResponse>

    // ─── IMÁGENES (3 endpoints verificados) ──────────────────────────────────
    @GET("api/imagenes/evento/{id}")
    suspend fun getImagenesByEvento(@Path("id") id: Long): List<ImagenEventoResponse>

    @POST("api/imagenes")
    suspend fun addImagenEvento(@Body request: ImagenEventoRequest): Response<ImagenEventoResponse>

    @DELETE("api/imagenes/{id}")
    suspend fun deleteImagenEvento(@Path("id") id: Long): Response<Void>

    // ─── CHAT (4 endpoints verificados) ──────────────────────────────────────
    @GET("api/chat/evento/{id}")
    suspend fun getMensajesByEvento(@Path("id") id: Long): List<MensajeChatResponse>

    @POST("api/chat")
    suspend fun enviarMensaje(@Body request: MensajeChatRequest): Response<MensajeChatResponse>

    // CORRECCIÓN: Endpoint faltante para editar mensajes
    @PATCH("api/chat/{id}")
    suspend fun editarMensaje(
        @Path("id")             id: Long,
        @Query("contenido") contenido: String
    ): Response<MensajeChatResponse>

    @DELETE("api/chat/{id}")
    suspend fun eliminarMensaje(@Path("id") id: Long): Response<Void>

    // ─── NOTIFICACIONES (5 endpoints verificados) ────────────────────────────
    @POST("api/notificaciones")
    suspend fun createNotificacion(@Body request: NotificacionRequest): Response<NotificacionResponse>

    @GET("api/notificaciones/usuario/{id}")
    suspend fun getNotificacionesByUsuario(@Path("id") id: Long): List<NotificacionResponse>

    // CORRECCIÓN: Endpoint faltante para badge de no-leídas
    @GET("api/notificaciones/usuario/{id}/no-leidas")
    suspend fun getNotificacionesNoLeidas(@Path("id") id: Long): List<NotificacionResponse>

    // CORRECCIÓN: Era /leida → la ruta real del backend es /leer
    @PATCH("api/notificaciones/{id}/leer")
    suspend fun marcarLeida(@Path("id") id: Long): Response<NotificacionResponse>

    @DELETE("api/notificaciones/{id}")
    suspend fun deleteNotificacion(@Path("id") id: Long): Response<Void>

    // ELIMINADO: EstadisticasRepository completo — endpoints /estadisticas/* no existen
}
