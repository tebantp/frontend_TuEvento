package com.tuevento.tueventofinal.util

object ImageUtils {

    /**
     * Devuelve la URL de imagen para un evento.
     * Prioridad:
     *   1. URL provista por el usuario (override)
     *   2. Imagen temática basada en palabras clave del título
     *   3. Imagen aleatoria de Picsum como fallback universal
     */
    fun getEventoImageUrl(
        titulo: String,
        id: Long,
        userImageUrl: String? = null   // ← NUEVO: URL que el usuario pegó en el formulario
    ): String {
        // 1. URL del usuario — validación mínima: debe empezar con http
        if (!userImageUrl.isNullOrBlank() && userImageUrl.startsWith("http")) {
            return userImageUrl
        }

        // 2. Imágenes temáticas por palabras clave
        val t = titulo.lowercase()
        return when {
            t.contains("hackaton") || t.contains("hackathon") ->
                "https://raw.githubusercontent.com/tebantp/frontend_TuEvento/main/hackaton.jpg"
            t.contains("futbol") || t.contains("fútbol") || t.contains("futsal") ->
                "https://raw.githubusercontent.com/tebantp/frontend_TuEvento/main/futbol.jpg"
            t.contains("basketball") || t.contains("basquet") ->
                "https://raw.githubusercontent.com/tebantp/frontend_TuEvento/main/basketball.jpg"
            t.contains("simposio") || t.contains("sinposio") ->
                "https://raw.githubusercontent.com/tebantp/frontend_TuEvento/main/sinposio.png"
            t.contains("voleybol") || t.contains("voleibol") ->
                "https://raw.githubusercontent.com/tebantp/frontend_TuEvento/main/voleybol.jpg"
            t.contains("x pin") || t.contains("xpin") ->
                "https://raw.githubusercontent.com/tebantp/frontend_TuEvento/main/x%20pin.jpg"
            // 3. Fallback: imagen aleatoria determinista por ID
            else -> "https://picsum.photos/seed/$id/800/600"
        }
    }
}
