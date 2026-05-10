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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.tuevento.tueventofinal.data.model.EventoRequest
import com.tuevento.tueventofinal.data.model.EventoResponse
import com.tuevento.tueventofinal.ui.components.PremiumTextField
import com.tuevento.tueventofinal.ui.theme.BlobWhite
import com.tuevento.tueventofinal.ui.theme.DarkGrey
import com.tuevento.tueventofinal.ui.theme.MascotGreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventoEditarScreen(
    evento: EventoResponse,
    currentImageUrl: String = "",           // ← URL actual del evento (del mapa local)
    onSuccess: (EventoResponse, String) -> Unit,
    onBack: () -> Unit,
    viewModel: EventoEditarViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var titulo      by remember { mutableStateOf(evento.titulo) }
    var descripcion by remember { mutableStateOf(evento.descripcion ?: "") }
    var lugar       by remember { mutableStateOf(evento.lugar) }
    var direccion   by remember { mutableStateOf(evento.direccion ?: "") }
    var cupoMaximo  by remember { mutableStateOf(evento.cupoMaximo.toString()) }
    var fechaInicio by remember { mutableStateOf(evento.fechaInicio) }
    var fechaFin    by remember { mutableStateOf(evento.fechaFin) }
    var estado      by remember { mutableStateOf(evento.estado) }
    // Pre-llenado con la URL actual si existe
    var imageUrl    by remember { mutableStateOf(currentImageUrl) }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is EventoEditarState.Success -> {
                scope.launch {
                    snackbarHostState.showSnackbar("✅ Evento actualizado")
                    kotlinx.coroutines.delay(800)
                    onSuccess(state.evento, state.imageUrl)
                    viewModel.resetState()
                }
            }
            is EventoEditarState.Error -> {
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
                        "EDITAR EVENTO",
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
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .offset(x = (-100).dp, y = (-100).dp)
                    .clip(CircleShape)
                    .background(MascotGreen.copy(alpha = 0.07f))
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    model = "https://raw.githubusercontent.com/tebantp/frontend_TuEvento/main/Logo.png",
                    contentDescription = null,
                    modifier = Modifier.size(60.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = BlobWhite),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "INFORMACIÓN DEL EVENTO",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                color = DarkGrey.copy(alpha = 0.5f)
                            )
                        )
                        PremiumTextField(titulo, { titulo = it }, "Título", Icons.Default.Title, onDarkBackground = false)
                        PremiumTextField(descripcion, { descripcion = it }, "Descripción", Icons.Default.Description, singleLine = false, onDarkBackground = false)
                        PremiumTextField(lugar, { lugar = it }, "Lugar", Icons.Default.LocationOn, onDarkBackground = false)
                        PremiumTextField(direccion, { direccion = it }, "Dirección", Icons.Default.Map, onDarkBackground = false)
                        PremiumTextField(cupoMaximo, { cupoMaximo = it }, "Cupo Máximo", Icons.Default.Group, isNumber = true, onDarkBackground = false)
                    }
                }

                // ── Card de imagen ──────────────────────────────────────────
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = BlobWhite),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "IMAGEN DEL EVENTO",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                color = DarkGrey.copy(alpha = 0.5f)
                            )
                        )
                        PremiumTextField(
                            value = imageUrl, onValueChange = { imageUrl = it },
                            label = "URL de imagen", icon = Icons.Default.Image,
                            onDarkBackground = false
                        )
                        AnimatedVisibility(
                            visible = imageUrl.isNotBlank() && imageUrl.startsWith("http"),
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            ImagePreviewBlob(url = imageUrl)
                        }
                    }
                }

                Button(
                    onClick = {
                        val cupo = cupoMaximo.toIntOrNull()
                        when {
                            titulo.trim().isEmpty() ->
                                scope.launch { snackbarHostState.showSnackbar("El título es obligatorio") }
                            lugar.trim().isEmpty() ->
                                scope.launch { snackbarHostState.showSnackbar("El lugar es obligatorio") }
                            cupo == null || cupo <= 0 ->
                                scope.launch { snackbarHostState.showSnackbar("El cupo debe ser mayor a 0") }
                            else -> {
                                viewModel.editarEvento(
                                    evento.id,
                                    EventoRequest(
                                        titulo      = titulo.trim(),
                                        descripcion = descripcion.trim().ifBlank { null },
                                        fechaInicio = fechaInicio,
                                        fechaFin    = fechaFin,
                                        lugar       = lugar.trim(),
                                        direccion   = direccion.trim().ifBlank { null },
                                        cupoMaximo  = cupo,
                                        estado      = estado
                                    ),
                                    imageUrl.trim()
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MascotGreen),
                    enabled = uiState !is EventoEditarState.Loading
                ) {
                    if (uiState is EventoEditarState.Loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Save, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("GUARDAR CAMBIOS", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}
