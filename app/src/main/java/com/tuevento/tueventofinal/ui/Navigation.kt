package com.tuevento.tueventofinal.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tuevento.tueventofinal.data.model.EventoResponse
import com.tuevento.tueventofinal.data.model.RolUsuario
import com.tuevento.tueventofinal.ui.admin.AdminDashboardScreen
import com.tuevento.tueventofinal.ui.admin.AdminViewModel
import com.tuevento.tueventofinal.ui.asistencia.AsistenciaViewModel
import com.tuevento.tueventofinal.ui.asistencia.ScannerScreen
import com.tuevento.tueventofinal.ui.chat.ChatScreen
import com.tuevento.tueventofinal.ui.chat.ChatViewModel
import com.tuevento.tueventofinal.ui.components.BlobsBottomNavigation
import com.tuevento.tueventofinal.ui.eventos.*
import com.tuevento.tueventofinal.ui.login.*
import com.tuevento.tueventofinal.ui.notificaciones.NotificacionScreen
import com.tuevento.tueventofinal.ui.notificaciones.NotificacionViewModel
import com.tuevento.tueventofinal.ui.organizador.OrganizadorDashboardScreen
import com.tuevento.tueventofinal.ui.organizador.OrganizadorViewModel
import com.tuevento.tueventofinal.ui.perfil.InscripcionViewModel
import com.tuevento.tueventofinal.ui.perfil.InscripcionesScreen
import com.tuevento.tueventofinal.ui.perfil.PerfilScreen
import com.tuevento.tueventofinal.ui.perfil.PerfilViewModel
import com.tuevento.tueventofinal.ui.qr.QRScreen
import com.tuevento.tueventofinal.ui.qr.QRViewModel
import com.tuevento.tueventofinal.ui.reportes.ReportesScreen
import com.tuevento.tueventofinal.ui.reportes.ReportesViewModel
import com.tuevento.tueventofinal.util.SessionManager

// Roles que tienen acceso al escáner de QR
private val ROLES_SCANNER = setOf(
    RolUsuario.STAFF,
    RolUsuario.ORGANIZADOR,
    RolUsuario.ADMINISTRADOR
)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context       = LocalContext.current
    val session       = SessionManager(context)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // ViewModel compartido para que todas las pantallas vean los mismos eventos
    val sharedEventoVm: EventoViewModel = viewModel()

    val showBottomBar = currentRoute in listOf("eventos", "notificaciones", "perfil")

    // Helper para forzar rol en la UI por email si el servidor falla
    fun getEffectiveUserRole(): String {
        val user = session.getUser() ?: return "USUARIO"
        val email = user.email.lowercase()
        return when {
            email.contains("admin") -> "ADMINISTRADOR"
            email.contains("org")   -> "ORGANIZADOR"
            else -> user.rol.name
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
                    BlobsBottomNavigation(
                        currentRoute = currentRoute,
                        onNavigate = { route ->
                            navController.navigate(route) {
                                popUpTo("eventos") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController, 
            startDestination = "splash",
            modifier = Modifier.padding(if (showBottomBar) innerPadding else PaddingValues(0.dp))
        ) {

        // ── SPLASH ────────────────────────────────────────────────────────────
        composable("splash") {
            SplashScreen(
                onNavigate = {
                    if (session.isLoggedIn()) {
                        // SEGREGACIÓN DE ROLES: when exhaustivo — nunca cae en un camino
                        // ambiguo. El rol viene ya verificado por @SerializedName corregido.
                        val destination = when (session.getUser()?.rol) {
                            RolUsuario.ADMINISTRADOR -> "admin_dashboard"
                            RolUsuario.ORGANIZADOR   -> "organizador_dashboard"
                            RolUsuario.STAFF         -> "scanner"
                            else                     -> "eventos"      // USUARIO o null
                        }
                        navController.navigate(destination) {
                            popUpTo("splash") { inclusive = true }
                        }
                    } else {
                        navController.navigate("login") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                }
            )
        }

        // ── LOGIN ─────────────────────────────────────────────────────────────
        composable("login") {
            val vm: LoginViewModel = viewModel(
                factory = GenericViewModelFactory { LoginViewModel(sessionManager = session) }
            )
            LoginScreen(
                onLoginSuccess = { user ->
                    // SEGREGACIÓN DE ROLES post-login: bloque when forzoso
                    val destination = when (user.rol) {
                        RolUsuario.ADMINISTRADOR -> "admin_dashboard"
                        RolUsuario.ORGANIZADOR   -> "organizador_dashboard"
                        RolUsuario.STAFF         -> "scanner"
                        else                     -> "eventos"
                    }
                    navController.navigate(destination) {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate("register") },
                viewModel = vm
            )
        }

        // ── REGISTER ──────────────────────────────────────────────────────────
        composable("register") {
            val vm: RegisterViewModel = viewModel()
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() },
                viewModel = vm
            )
        }

        // ── ADMIN (GUARD: solo ADMINISTRADOR) ─────────────────────────────────
        composable("admin_dashboard") {
            val user = session.getUser()
            // Guard de rol: si no es ADMIN, redirigir inmediatamente
            if (user?.rol != RolUsuario.ADMINISTRADOR) {
                navController.navigate("eventos") {
                    popUpTo("admin_dashboard") { inclusive = true }
                }
                return@composable
            }
            val vm: AdminViewModel = viewModel()
            AdminDashboardScreen(
                viewModel = vm,
                onNavigateToCrear = { navController.navigate("crear_evento") },
                onLogout = {
                    session.logout()
                    navController.navigate("login") { popUpTo(0) { inclusive = true } }
                }
            )
        }

        // ── ORGANIZADOR (GUARD: solo ORGANIZADOR o ADMIN) ─────────────────────
        composable("organizador_dashboard") {
            val user = session.getUser()
            if (user == null ||
                (user.rol != RolUsuario.ORGANIZADOR && user.rol != RolUsuario.ADMINISTRADOR)
            ) {
                navController.navigate("eventos") {
                    popUpTo("organizador_dashboard") { inclusive = true }
                }
                return@composable
            }
            val vm: OrganizadorViewModel = viewModel()
            OrganizadorDashboardScreen(
                organizadorId         = user.id,
                viewModel             = vm,
                onNavigateToCrear     = { navController.navigate("crear_evento") },
                onNavigateToScanner   = { navController.navigate("scanner") },
                onNavigateToDetalle   = { id -> navController.navigate("evento_detalle/$id") },
                onNavigateToEditar    = { evento -> 
                    navController.currentBackStackEntry?.savedStateHandle?.set("evento_editar", evento)
                    navController.navigate("editar_evento")
                },
                onNavigateToReportes  = { navController.navigate("reportes") }
            )
        }

        composable("reportes") {
            val user = session.getUser() ?: run {
                navController.navigate("login") { popUpTo(0) }
                return@composable
            }
            val vm: ReportesViewModel = viewModel()
            ReportesScreen(
                organizadorId = user.id,
                onBack        = { navController.popBackStack() },
                viewModel     = vm
            )
        }

        // ── SCANNER (GUARD: solo STAFF / ORGANIZADOR / ADMIN) ─────────────────
        composable("scanner") {
            val user = session.getUser()
            if (user == null || user.rol !in ROLES_SCANNER) {
                navController.navigate("eventos") {
                    popUpTo("scanner") { inclusive = true }
                }
                return@composable
            }
            val vm: AsistenciaViewModel = viewModel()
            ScannerScreen(
                staffId   = user.id,
                onBack    = { navController.popBackStack() },
                viewModel = vm
            )
        }

        // ── EVENTOS (acceso libre para usuarios autenticados) ─────────────────
        composable("eventos") {
            val user = session.getUser()
            EventoListScreen(
                userRole       = getEffectiveUserRole(),
                onEventoClick  = { id -> navController.navigate("evento_detalle/$id") },
                onAddEventoClick = { navController.navigate("crear_evento") },
                onDeleteEvento = { id -> 
                    if (user?.rol == RolUsuario.ADMINISTRADOR || user?.rol == RolUsuario.ORGANIZADOR) {
                        sharedEventoVm.eliminarEvento(id)
                    }
                },
                onScannerClick = { navController.navigate("scanner") },
                onPerfilClick  = { navController.navigate("perfil") },
                viewModel      = sharedEventoVm
            )
        }

        composable("perfil") {
            val vm: PerfilViewModel = viewModel(
                factory = GenericViewModelFactory { PerfilViewModel(sessionManager = session) }
            )
            PerfilScreen(
                onBack    = { navController.popBackStack() },
                onLogout  = {
                    navController.navigate("login") { popUpTo(0) { inclusive = true } }
                },
                onNavigateToInscripciones = { uid -> navController.navigate("inscripciones/$uid") },
                viewModel = vm
            )
        }

        composable("crear_evento") {
            val user = session.getUser() ?: run {
                navController.navigate("login") { popUpTo(0) }
                return@composable
            }
            val vm: EventoCrearViewModel = viewModel()
            EventoCrearScreen(
                organizadorId = user.id,
                onSuccess = { evento, url ->
                    // Sincronizar con el ViewModel compartido
                    sharedEventoVm.agregarEventoLocal(evento)
                    sharedEventoVm.setEventoImageUrl(evento.id, url)
                    navController.popBackStack()
                },
                onBack    = { navController.popBackStack() },
                viewModel = vm
            )
        }

        composable("editar_evento") {
            val evento = navController.previousBackStackEntry?.savedStateHandle?.get<EventoResponse>("evento_editar")
            if (evento == null) {
                navController.popBackStack()
                return@composable
            }
            val vm: EventoEditarViewModel = viewModel()
            EventoEditarScreen(
                evento = evento,
                currentImageUrl = sharedEventoVm.getImageUrlForEvento(evento.id) ?: "",
                onSuccess = { eventoEditado, url ->
                    // Sincronizar cambios localmente
                    sharedEventoVm.actualizarEventoLocal(eventoEditado)
                    sharedEventoVm.setEventoImageUrl(eventoEditado.id, url)
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() },
                viewModel = vm
            )
        }

        composable(
            "evento_detalle/{eventoId}",
            arguments = listOf(navArgument("eventoId") { type = NavType.LongType })
        ) { back ->
            val id   = back.arguments?.getLong("eventoId") ?: 0L
            val user = session.getUser() ?: run {
                navController.navigate("login") { popUpTo(0) }
                return@composable
            }
            val vm: EventoDetalleViewModel = viewModel()
            EventoDetalleScreen(
                eventoId          = id,
                usuarioId         = user.id,
                onBack            = { navController.popBackStack() },
                viewModel         = vm,
                sharedEventoVm    = sharedEventoVm,
                onNavigateToChat  = { eid -> navController.navigate("chat/$eid") }
            )
        }

        composable(
            "chat/{eventoId}",
            arguments = listOf(navArgument("eventoId") { type = NavType.LongType })
        ) { back ->
            val id   = back.arguments?.getLong("eventoId") ?: 0L
            val user = session.getUser() ?: run {
                navController.navigate("login") { popUpTo(0) }
                return@composable
            }
            val vm: ChatViewModel = viewModel()
            ChatScreen(
                eventoId  = id,
                usuarioId = user.id,
                onBack    = { navController.popBackStack() },
                viewModel = vm
            )
        }

        composable("notificaciones") {
            val user = session.getUser() ?: run {
                navController.navigate("login") { popUpTo(0) }
                return@composable
            }
            val vm: NotificacionViewModel = viewModel()
            NotificacionScreen(
                usuarioId           = user.id,
                onBack              = { navController.popBackStack() },
                onNavigateToEvento  = { eid -> navController.navigate("evento_detalle/$eid") },
                viewModel           = vm
            )
        }

        composable(
            "qr/{inscripcionId}",
            arguments = listOf(navArgument("inscripcionId") { type = NavType.LongType })
        ) { back ->
            val inscripcionId = back.arguments?.getLong("inscripcionId") ?: 0L
            val vm: QRViewModel = viewModel()
            QRScreen(
                inscripcionId = inscripcionId,
                onBack        = { navController.popBackStack() },
                viewModel     = vm
            )
        }

        composable(
            "inscripciones/{usuarioId}",
            arguments = listOf(navArgument("usuarioId") { type = NavType.LongType })
        ) { back ->
            val usuarioId = back.arguments?.getLong("usuarioId") ?: 0L
            val vm: InscripcionViewModel = viewModel()
            InscripcionesScreen(
                usuarioId = usuarioId,
                onInscripcionClick = { inscripcionId -> navController.navigate("qr/$inscripcionId") },
                onBack    = { navController.popBackStack() },
                viewModel = vm
            )
        }
        }
    }
}

class GenericViewModelFactory<T : androidx.lifecycle.ViewModel>(
    private val creator: () -> T
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return creator() as T
    }
}
