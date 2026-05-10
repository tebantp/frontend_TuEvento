package com.tuevento.tueventofinal.data.model

import com.google.gson.annotations.SerializedName

// ─────────────────────────────────────────────────────────────────────────────
// ENUMS
// CRÍTICO: Los @SerializedName deben coincidir EXACTAMENTE con los valores
// que serializa el backend. El backend Java usa el nombre literal del enum.
// ─────────────────────────────────────────────────────────────────────────────

enum class RolUsuario {
    @SerializedName("USUARIO")       USUARIO,
    @SerializedName("ORGANIZADOR")   ORGANIZADOR,
    @SerializedName("STAFF")         STAFF,
    @SerializedName("ADMINISTRADOR") ADMINISTRADOR
}

enum class EstadoEvento {
    @SerializedName("BORRADOR")   BORRADOR,
    @SerializedName("PUBLICADO")  PUBLICADO,
    @SerializedName("ACTIVO")     ACTIVO,
    @SerializedName("FINALIZADO") FINALIZADO,
    @SerializedName("CANCELADO")  CANCELADO
}

enum class EstadoInscripcion {
    @SerializedName("PENDIENTE")  PENDIENTE,
    @SerializedName("CONFIRMADA") CONFIRMADA,
    @SerializedName("CANCELADA")  CANCELADA
}

enum class MetodoRegistro {
    @SerializedName("CHECKBOX") CHECKBOX,
    @SerializedName("QR")       QR
}

// ─────────────────────────────────────────────────────────────────────────────
// USUARIO
// Fix B-1: Se añade el campo 'rol' para permitir registro con roles específicos.
// ─────────────────────────────────────────────────────────────────────────────

data class UsuarioRequest(
    @SerializedName("nombre")   val nombre: String,
    @SerializedName("apellido") val apellido: String,
    @SerializedName("email")    val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("telefono") val telefono: String? = null,
    @SerializedName("rol")      val rol: RolUsuario? = null
)

data class UsuarioResponse(
    @SerializedName("id")            val id: Long,
    @SerializedName("nombre")        val nombre: String,
    @SerializedName("apellido")      val apellido: String,
    @SerializedName("email")         val email: String,
    @SerializedName("telefono")      val telefono: String?,
    @SerializedName("fotoUrl")       val photoUrl: String?,
    @SerializedName("rol")           val rol: RolUsuario,
    @SerializedName("activo")        val activo: Boolean,
    @SerializedName("fechaCreacion") val fechaCreacion: String
)

// ─────────────────────────────────────────────────────────────────────────────
// EVENTO
// ─────────────────────────────────────────────────────────────────────────────

data class EventoRequest(
    @SerializedName("titulo")      val titulo: String,
    @SerializedName("descripcion") val descripcion: String? = null,
    @SerializedName("fechaInicio") val fechaInicio: String,   // ISO: "2025-06-01T10:00:00"
    @SerializedName("fechaFin")    val fechaFin: String,
    @SerializedName("lugar")       val lugar: String,
    @SerializedName("direccion")   val direccion: String? = null,
    @SerializedName("latitud")     val latitud: Double? = null,
    @SerializedName("longitud")    val longitud: Double? = null,
    @SerializedName("cupoMaximo")  val cupoMaximo: Int,
    @SerializedName("estado")      val estado: EstadoEvento? = null
)

data class EventoResponse(
    @SerializedName("id")                val id: Long,
    @SerializedName("titulo")            val titulo: String,
    @SerializedName("descripcion")       val descripcion: String?,
    @SerializedName("fechaInicio")       val fechaInicio: String,
    @SerializedName("fechaFin")          val fechaFin: String,
    @SerializedName("lugar")             val lugar: String,
    @SerializedName("direccion")         val direccion: String?,
    @SerializedName("latitud")           val latitud: Double?,
    @SerializedName("longitud")          val longitud: Double?,
    @SerializedName("cupoMaximo")        val cupoMaximo: Int,
    @SerializedName("cupoDisponible")    val cupoDisponible: Int,
    @SerializedName("estado")            val estado: EstadoEvento,
    @SerializedName("organizadorId")     val organizadorId: Long,
    @SerializedName("organizadorNombre") val organizadorNombre: String,
    @SerializedName("fechaCreacion")     val fechaCreacion: String
)

// ─────────────────────────────────────────────────────────────────────────────
// INSCRIPCION
// ─────────────────────────────────────────────────────────────────────────────

data class InscripcionRequest(
    @SerializedName("eventoId")  val eventoId: Long,
    @SerializedName("usuarioId") val usuarioId: Long
)

data class InscripcionResponse(
    @SerializedName("id")               val id: Long,
    @SerializedName("usuarioId")        val usuarioId: Long,
    @SerializedName("usuarioNombre")    val usuarioNombre: String,
    @SerializedName("eventoId")         val eventoId: Long,
    @SerializedName("eventoTitulo")     val eventoTitulo: String,
    @SerializedName("estado")           val estado: EstadoInscripcion,
    @SerializedName("fechaInscripcion") val fechaInscripcion: String,
    @SerializedName("codigoQR")         val codigoQR: String?,
    @SerializedName("urlQR")            val urlQR: String?,
    @SerializedName("qrExpiracion")     val qrExpiracion: String?
)

// ─────────────────────────────────────────────────────────────────────────────
// ASISTENCIA
// ─────────────────────────────────────────────────────────────────────────────

data class AsistenciaRequest(
    @SerializedName("inscripcionId") val inscripcionId: Long,
    @SerializedName("staffId")       val staffId: Long,
    @SerializedName("metodo")        val metodo: MetodoRegistro
)

data class AsistenciaResponse(
    @SerializedName("id")             val id: Long,
    @SerializedName("inscripcionId")  val inscripcionId: Long,
    @SerializedName("usuarioNombre")  val usuarioNombre: String,
    @SerializedName("presente")       val presente: Boolean,
    @SerializedName("horaEntrada")    val horaEntrada: String?,
    @SerializedName("metodoRegistro") val metodoRegistro: MetodoRegistro
)

// ─────────────────────────────────────────────────────────────────────────────
// CODIGO QR
// Corrección: se elimina CodigoQRRequest porque POST /api/qr no existe.
// El QR se genera automáticamente al crear la inscripción.
// ─────────────────────────────────────────────────────────────────────────────

data class CodigoQRResponse(
    @SerializedName("id")              val id: Long,
    @SerializedName("inscripcionId")   val inscripcionId: Long,
    @SerializedName("usuarioNombre")   val usuarioNombre: String,
    @SerializedName("eventoTitulo")    val eventoTitulo: String,
    @SerializedName("codigoUnico")     val codigoUnico: String,
    @SerializedName("urlQR")           val urlQR: String,
    @SerializedName("fechaGeneracion") val fechaGeneracion: String,
    @SerializedName("fechaExpiracion") val fechaExpiracion: String,
    @SerializedName("utilizado")       val utilizado: Boolean
)

// ─────────────────────────────────────────────────────────────────────────────
// FEEDBACK
// ─────────────────────────────────────────────────────────────────────────────

data class FeedbackRequest(
    @SerializedName("inscripcionId") val inscripcionId: Long,
    @SerializedName("calificacion")  val calificacion: Int,   // 1..5
    @SerializedName("comentario")    val comentario: String? = null
)

data class FeedbackResponse(
    @SerializedName("id")            val id: Long,
    @SerializedName("inscripcionId") val inscripcionId: Long,
    @SerializedName("usuarioNombre") val usuarioNombre: String,
    @SerializedName("eventoTitulo")  val eventoTitulo: String,
    @SerializedName("calificacion")  val calificacion: Int,
    @SerializedName("comentario")    val comentario: String?,
    @SerializedName("fechaEnvio")    val fechaEnvio: String
)

// ─────────────────────────────────────────────────────────────────────────────
// IMAGEN EVENTO
// ─────────────────────────────────────────────────────────────────────────────

data class ImagenEventoRequest(
    @SerializedName("eventoId")    val eventoId: Long,
    @SerializedName("url")         val url: String,
    @SerializedName("descripcion") val descripcion: String? = null,
    @SerializedName("orden")       val orden: Int
)

data class ImagenEventoResponse(
    @SerializedName("id")          val id: Long,
    @SerializedName("eventoId")    val eventoId: Long,
    @SerializedName("url")         val url: String,
    @SerializedName("descripcion") val descripcion: String?,
    @SerializedName("orden")       val orden: Int,
    @SerializedName("fechaSubida") val fechaSubida: String
)

// ─────────────────────────────────────────────────────────────────────────────
// MENSAJE CHAT
// ─────────────────────────────────────────────────────────────────────────────

data class MensajeChatRequest(
    @SerializedName("remitenteId") val remitenteId: Long,
    @SerializedName("eventoId")    val eventoId: Long,
    @SerializedName("contenido")   val contenido: String
)

data class MensajeChatResponse(
    @SerializedName("id")              val id: Long,
    @SerializedName("remitenteId")     val remitenteId: Long,
    @SerializedName("remitenteNombre") val remitenteNombre: String,
    @SerializedName("eventoId")        val eventoId: Long,
    @SerializedName("eventoTitulo")    val eventoTitulo: String,
    @SerializedName("contenido")       val contenido: String,
    @SerializedName("fechaEnvio")      val fechaEnvio: String,
    @SerializedName("editado")         val editado: Boolean
)

// ─────────────────────────────────────────────────────────────────────────────
// NOTIFICACION
// ─────────────────────────────────────────────────────────────────────────────

data class NotificacionRequest(
    @SerializedName("usuarioId") val usuarioId: Long,
    @SerializedName("eventoId")  val eventoId: Long? = null,
    @SerializedName("titulo")    val titulo: String,
    @SerializedName("mensaje")   val mensaje: String,
    @SerializedName("tipo")      val tipo: String
)

data class NotificacionResponse(
    @SerializedName("id")            val id: Long,
    @SerializedName("usuarioId")     val usuarioId: Long,
    @SerializedName("usuarioNombre") val usuarioNombre: String,
    @SerializedName("eventoId")      val eventoId: Long?,
    @SerializedName("eventoTitulo")  val eventoTitulo: String?,
    @SerializedName("titulo")        val titulo: String,
    @SerializedName("mensaje")       val mensaje: String,
    @SerializedName("tipo")          val tipo: String,
    @SerializedName("leida")         val leida: Boolean,
    @SerializedName("fechaCreacion") val fechaCreacion: String
)

// ─────────────────────────────────────────────────────────────────────────────
// ESTADÍSTICAS — Calculadas en el CLIENTE (no existen endpoints en el backend)
// ─────────────────────────────────────────────────────────────────────────────

data class EstadisticasOrganizador(
    val totalEventos: Int,
    val totalInscritos: Int,
    val totalAsistentes: Int,
    val tasaAsistencia: Float,
    val eventosPorEstado: Map<String, Int>
)

data class EstadisticasAdmin(
    val totalUsuarios: Int,
    val totalEventos: Int,
    val eventosPorEstado: Map<String, Int>,
    val usuariosPorRol: Map<String, Int>
)

// ─────────────────────────────────────────────────────────────────────────────
// AUTH
// ─────────────────────────────────────────────────────────────────────────────

data class LoginRequest(
    @SerializedName("email")    val email: String,
    @SerializedName("password") val password: String
)
