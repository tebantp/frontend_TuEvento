package com.tuevento.tueventofinal.data.remote

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {

    // ⚠️ CAMBIA ESTA URL SEGÚN TU ENTORNO:
    //   Emulador Android → apunta a localhost del host:  "http://10.0.2.2:8080/"
    //   Dispositivo físico en la misma red WiFi:         "http://192.168.X.X:8080/"
    //   Servidor en producción:                          "https://tu-dominio.com/"
    private const val BASE_URL = "http://10.0.2.2:8080/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // ── AuthInterceptor ────────────────────────────────────────────────────
    // El backend actual NO usa JWT. Este interceptor está preparado para cuando
    // se añada autenticación. Si no hay token guardado, la petición sigue sin header.
    // Cuando el backend emita tokens, simplemente guárdalos en SessionManager
    // y se inyectarán automáticamente en cada petición.
    private var authToken: String? = null

    fun setAuthToken(token: String?) {
        authToken = token
    }

    private val authInterceptor = okhttp3.Interceptor { chain ->
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "application/json")

        // Inyectar token JWT si existe
        authToken?.let { token ->
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        chain.proceed(requestBuilder.build())
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}
