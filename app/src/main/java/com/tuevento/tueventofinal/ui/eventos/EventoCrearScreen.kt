package com.tuevento.tueventofinal.ui.eventos

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.tuevento.tueventofinal.data.model.EventoRequest
import com.tuevento.tueventofinal.data.model.EventoResponse
import com.tuevento.tueventofinal.ui.components.PremiumTextField
import com.tuevento.tueventofinal.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventoCrearScreen(
    organizadorId: Long,
    onSuccess: (EventoResponse, String) -> Unit,
    onBack: () -> Unit,
    viewModel: EventoCrearViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var titulo      by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var lugar       by remember { mutableStateOf("") }
    var direccion   by remember { mutableStateOf("") }
    var cupoMaximo  by remember { mutableStateOf("") }
    var fechaInicio by remember { mutableStateOf("2025-06-01T10:00:00") }
    var fechaFin    by remember { mutableStateOf("2025-06-01T20:00:00") }
    var imageUrl    by remember { mutableStateOf("") }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is EventoCrearState.Success -> {
                snackbarHostState.showSnackbar("✅ Evento publicado exitosamente")
                kotlinx.coroutines.delay(600)
                onSuccess(state.evento, state.imageUrl)
                viewModel.resetState()
            }
            is EventoCrearState.Error -> {
                snackbarHostState.showSnackbar("❌ ${state.message}")
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DarkGrey,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "NUEVO EVENTO",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            color = MascotGreen
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = MascotGreen)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Blobs decorativos de fondo
            Box(
                modifier = Modifier.size(320.dp).offset(x = (-110).dp, y = (-110).dp)
                    .clip(CircleShape).background(MascotGreen.copy(alpha = 0.06f))
            )
            Box(
                modifier = Modifier.size(200.dp).align(Alignment.BottomEnd).offset(x = 70.dp, y = 70.dp)
                    .clip(CircleShape).background(MascotGreen.copy(alpha = 0.04f))
            )

            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Mascota gato verde
                AsyncImage(
                    model = "https://raw.githubusercontent.com/tebantp/frontend_TuEvento/main/Logo.png",
                    contentDescription = "Mascota TuEvento",
                    modifier = Modifier.size(72.dp)
                )

                // ── Card: Información básica ───────────────────────────────
                Card(
                    modifier  = Modifier.fillMaxWidth(),
                    shape     = RoundedCornerShape(36.dp),
                    colors    = CardDefaults.cardColors(containerColor = BlobWhite),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        SectionLabel("INFORMACIÓN DEL EVENTO")
                        PremiumTextField(titulo, { titulo = it }, "Título del Evento", Icons.Default.Title, onDarkBackground = false)
                        PremiumTextField(descripcion, { descripcion = it }, "Descripción", Icons.Default.Description, singleLine = false, onDarkBackground = false)
                        PremiumTextField(lugar, { lugar = it }, "Lugar", Icons.Default.LocationOn, onDarkBackground = false)
                        PremiumTextField(direccion, { direccion = it }, "Dirección Exacta", Icons.Default.Map, onDarkBackground = false)
                        PremiumTextField(cupoMaximo, { cupoMaximo = it }, "Cupo Máximo", Icons.Default.Group, isNumber = true, onDarkBackground = false)
                    }
                }

                // ── Card: Fechas ──────────────────────────────────────────
                Card(
                    modifier  = Modifier.fillMaxWidth(),
                    shape     = RoundedCornerShape(36.dp),
                    colors    = CardDefaults.cardColors(containerColor = BlobWhite),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        SectionLabel("FECHAS (formato: YYYY-MM-DDThh:mm:ss)")
                        PremiumTextField(fechaInicio, { fechaInicio = it }, "Fecha de inicio", Icons.Default.CalendarMonth, onDarkBackground = false)
                        PremiumTextField(fechaFin, { fechaFin = it }, "Fecha de fin", Icons.Default.CalendarToday, onDarkBackground = false)
                    }
                }

                // ── Card: Imagen ──────────────────────────────────────────
                Card(
                    modifier  = Modifier.fillMaxWidth(),
                    shape     = RoundedCornerShape(36.dp),
                    colors    = CardDefaults.cardColors(containerColor = BlobWhite),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        SectionLabel("IMAGEN DEL EVENTO")

                        PremiumTextField(
                            value         = imageUrl,
                            onValueChange = { imageUrl = it },
                            label         = "URL de imagen (opcional)",
                            icon          = Icons.Default.Image,
                            onDarkBackground = false
                        )

                        // Preview en tiempo real
                        AnimatedVisibility(
                            visible = imageUrl.isNotBlank() && imageUrl.startsWith("http"),
                            enter   = fadeIn() + expandVertically(),
                            exit    = fadeOut() + shrinkVertically()
                        ) {
                            ImagePreviewBlob(url = imageUrl)
                        }

                        // Hint cuando no hay URL
                        AnimatedVisibility(visible = imageUrl.isBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MascotGreen.copy(alpha = 0.5f),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    "Sin URL: se asigna una imagen automática según el título del evento.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DarkGrey.copy(alpha = 0.45f)
                                )
                            }
                        }
                    }
                }

                // ── Botón publicar ─────────────────────────────────────────
                Button(
                    onClick = {
                        val trimTitulo = titulo.trim()
                        val trimLugar  = lugar.trim()
                        val cupo       = cupoMaximo.toIntOrNull()
                        when {
                            trimTitulo.isEmpty() ->
                                scope.launch { snackbarHostState.showSnackbar("El título es obligatorio") }
                            trimLugar.isEmpty() ->
                                scope.launch { snackbarHostState.showSnackbar("El lugar es obligatorio") }
                            cupo == null || cupo <= 0 ->
                                scope.launch { snackbarHostState.showSnackbar("El cupo debe ser mayor a 0") }
                            else -> viewModel.crearEvento(
                                organizadorId = organizadorId,
                                request = EventoRequest(
                                    titulo      = trimTitulo,
                                    descripcion = descripcion.trim().ifBlank { null },
                                    fechaInicio = fechaInicio,
                                    fechaFin    = fechaFin,
                                    lugar       = trimLugar,
                                    direccion   = direccion.trim().ifBlank { null },
                                    cupoMaximo  = cupo
                                ),
                                imageUrl = imageUrl.trim()
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape    = RoundedCornerShape(30.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = MascotGreen),
                    enabled  = uiState !is EventoCrearState.Loading
                ) {
                    if (uiState is EventoCrearState.Loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Publish, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("PUBLICAR EVENTO", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

// ── Etiqueta de sección ───────────────────────────────────────────────────────
@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge.copy(
            fontWeight    = FontWeight.Black,
            letterSpacing = 1.sp,
            color         = DarkGrey.copy(alpha = 0.45f)
        )
    )
}

// ── Preview de imagen (blob) ──────────────────────────────────────────────────
@Composable
fun ImagePreviewBlob(url: String, modifier: Modifier = Modifier) {
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(url).crossfade(true).build()
    )
    val state = painter.state

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(170.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(DarkSurface),
        contentAlignment = Alignment.Center
    ) {
        when (state) {
            is AsyncImagePainter.State.Loading -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    CircularProgressIndicator(color = MascotGreen, modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
                    Text("Cargando preview...", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.45f))
                }
            }
            is AsyncImagePainter.State.Error -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(shape = CircleShape, color = Color.Red.copy(alpha = 0.1f), modifier = Modifier.size(56.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.BrokenImage, null, tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(28.dp))
                        }
                    }
                    Text("URL inválida o sin acceso", style = MaterialTheme.typography.bodySmall, color = Color.Red.copy(alpha = 0.7f))
                }
            }
            else -> {
                androidx.compose.foundation.Image(
                    painter      = painter,
                    contentDescription = "Preview imagen evento",
                    modifier     = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Badge verde orgánico
                Surface(
                    modifier = Modifier.align(Alignment.TopStart).padding(12.dp),
                    shape    = RoundedCornerShape(16.dp),
                    color    = MascotGreen.copy(alpha = 0.92f)
                ) {
                    Text(
                        "VISTA PREVIA",
                        modifier  = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style     = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color     = Color.White,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}
