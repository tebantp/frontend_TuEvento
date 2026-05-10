package com.tuevento.tueventofinal.ui.eventos

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.tuevento.tueventofinal.data.model.FeedbackRequest
import com.tuevento.tueventofinal.data.model.InscripcionRequest
import com.tuevento.tueventofinal.data.remote.FeedbackRepository
import com.tuevento.tueventofinal.data.remote.InscripcionRepository
import com.tuevento.tueventofinal.ui.theme.*
import com.tuevento.tueventofinal.util.ImageUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventoDetalleScreen(
    eventoId: Long,
    usuarioId: Long,
    onBack: () -> Unit,
    viewModel: EventoDetalleViewModel,
    sharedEventoVm: EventoViewModel,
    onNavigateToChat: (Long) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val inscripcionRepo = remember { InscripcionRepository() }

    LaunchedEffect(eventoId, usuarioId) {
        viewModel.fetchEvento(eventoId, usuarioId)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (uiState is EventoDetalleState.Success) {
                val state = uiState as EventoDetalleState.Success
                val yaInscrito = state.userInscripcion != null

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = DarkGrey,
                    shadowElevation = 16.dp
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            onClick = { onNavigateToChat(eventoId) },
                            modifier = Modifier.height(56.dp).weight(0.3f),
                            shape = RoundedCornerShape(28.dp),
                            color = MascotGreen.copy(alpha = 0.1f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Chat, contentDescription = "Chat", tint = MascotGreen)
                            }
                        }

                        Button(
                            onClick = {
                                if (!yaInscrito) {
                                    scope.launch {
                                        try {
                                            val response = inscripcionRepo.createInscripcion(
                                                InscripcionRequest(eventoId, usuarioId)
                                            )
                                            if (response.isSuccessful) {
                                                snackbarHostState.showSnackbar("¡Inscripción exitosa!")
                                                viewModel.fetchEvento(eventoId, usuarioId)
                                            } else {
                                                snackbarHostState.showSnackbar("Error al inscribirse")
                                            }
                                        } catch (e: Exception) {
                                            snackbarHostState.showSnackbar("Error de conexión")
                                        }
                                    }
                                }
                            },
                            enabled = !yaInscrito,
                            modifier = Modifier
                                .weight(0.7f)
                                .height(56.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MascotGreen,
                                contentColor = Color.White,
                                disabledContainerColor = Color.White.copy(alpha = 0.1f),
                                disabledContentColor = Color.White.copy(alpha = 0.3f)
                            )
                        ) {
                            Text(
                                if (yaInscrito) "INSCRITO" else "RESERVAR LUGAR",
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(DarkGrey)
        ) {
            when (val state = uiState) {
                is EventoDetalleState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = MascotGreen)
                }
                is EventoDetalleState.Success -> {
                    val evento = state.evento
                    val imageUrlFromMap = sharedEventoVm.getImageUrlForEvento(evento.id)
                    val resolvedImageUrl = remember(evento.titulo, evento.id, state.imagenes, imageUrlFromMap) {
                        // Prioridad: 1. Mapa local (recién creado/editado), 2. Backend, 3. Temática/Picsum
                        val backendUrl = state.imagenes.firstOrNull()?.url
                        ImageUtils.getEventoImageUrl(evento.titulo, evento.id, imageUrlFromMap ?: backendUrl)
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Header Image
                        Box(modifier = Modifier.height(380.dp).fillMaxWidth()) {
                            AsyncImage(
                                model = resolvedImageUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(bottomStart = 64.dp, bottomEnd = 64.dp))
                            )
                            
                            // Top controls overlay
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Surface(
                                    onClick = onBack,
                                    color = Color.White.copy(alpha = 0.2f),
                                    shape = CircleShape,
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                                    }
                                }
                                Surface(
                                    onClick = { /* Share */ },
                                    color = Color.White.copy(alpha = 0.2f),
                                    shape = CircleShape,
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Share, contentDescription = "Compartir", tint = Color.White)
                                    }
                                }
                            }

                            // Mascot floating over image
                            AsyncImage(
                                model = "https://raw.githubusercontent.com/tebantp/frontend_TuEvento/main/Logo.png",
                                contentDescription = null,
                                modifier = Modifier
                                    .size(80.dp)
                                    .align(Alignment.BottomEnd)
                                    .padding(bottom = 16.dp, end = 24.dp)
                            )
                        }

                        // Content
                        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 32.dp)) {
                            Surface(
                                color = MascotGreen,
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Text(
                                    text = evento.estado.name.uppercase(),
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 1.sp
                                    ),
                                    color = Color.White
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = evento.titulo,
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    lineHeight = 40.sp
                                )
                            )
                            
                            Spacer(modifier = Modifier.height(32.dp))

                            // Info Block: Date and Location in a unified organic container
                            Surface(
                                color = BlobWhite,
                                shape = RoundedCornerShape(48.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(24.dp)) {
                                    // Info Row: Date
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            color = MascotGreen.copy(alpha = 0.1f),
                                            shape = CircleShape,
                                            modifier = Modifier.size(52.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.CalendarMonth,
                                                contentDescription = null,
                                                modifier = Modifier.padding(14.dp),
                                                tint = MascotGreen
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column {
                                            Text(text = "CUÁNDO", style = MaterialTheme.typography.labelSmall, color = MascotGreen.copy(alpha = 0.6f), fontWeight = FontWeight.ExtraBold)
                                            Text(text = evento.fechaInicio, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = DarkGrey)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(20.dp))

                                    // Info Row: Location
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            color = MascotGreen.copy(alpha = 0.1f),
                                            shape = CircleShape,
                                            modifier = Modifier.size(52.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.LocationOn,
                                                contentDescription = null,
                                                modifier = Modifier.padding(14.dp),
                                                tint = MascotGreen
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column {
                                            Text(text = "DÓNDE", style = MaterialTheme.typography.labelSmall, color = MascotGreen.copy(alpha = 0.6f), fontWeight = FontWeight.ExtraBold)
                                            Text(text = evento.lugar, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = DarkGrey)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(40.dp))

                            Text(
                                text = "ACERCA DEL EVENTO",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MascotGreen,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = evento.descripcion ?: "Este evento es una experiencia orgánica y única diseñada para la comunidad.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White.copy(alpha = 0.8f),
                                lineHeight = 28.sp
                            )
                            
                            Spacer(modifier = Modifier.height(40.dp))
                            
                            // Organizer info
                            Text(
                                text = "HOST DEL EVENTO",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MascotGreen,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(BlobWhite, RoundedCornerShape(32.dp))
                                    .padding(16.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.size(56.dp),
                                    shape = CircleShape,
                                    color = MascotGreen
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = evento.organizadorNombre.take(1).uppercase(),
                                            color = Color.White,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 24.sp
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(text = evento.organizadorNombre, fontWeight = FontWeight.ExtraBold, color = DarkGrey, fontSize = 18.sp)
                                    Text(text = "ORGANIZADOR VERIFICADO", style = MaterialTheme.typography.labelSmall, color = MascotGreen, fontWeight = FontWeight.ExtraBold)
                                }
                            }

                            Spacer(modifier = Modifier.height(40.dp))

                            // --- SECCIÓN DE FEEDBACK ---
                            Text(
                                text = "RESEÑAS",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MascotGreen,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            if (state.feedback.isEmpty()) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(32.dp))
                                        .padding(32.dp)
                                ) {
                                    AsyncImage(
                                        model = "https://raw.githubusercontent.com/tebantp/frontend_TuEvento/main/Logo.png",
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        alpha = 0.5f
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("¡SÉ EL PRIMERO EN OPINAR!", color = Color.White.copy(alpha = 0.4f), fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                                }
                            } else {
                                state.feedback.forEach { feedback ->
                                    FeedbackItemPremium(feedback)
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }

                            if (state.userInscripcion != null) {
                                Spacer(modifier = Modifier.height(24.dp))
                                var showFeedbackDialog by remember { mutableStateOf(false) }
                                Button(
                                    onClick = { showFeedbackDialog = true },
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = BlobWhite),
                                    shape = RoundedCornerShape(28.dp)
                                ) {
                                    Text("ESCRIBIR UNA RESEÑA", color = MascotGreen, fontWeight = FontWeight.Bold)
                                }

                                if (showFeedbackDialog) {
                                    FeedbackDialogPremium(
                                        onDismiss = { showFeedbackDialog = false },
                                        onSubmit = { calificacion, comentario ->
                                            scope.launch {
                                                try {
                                                    val feedbackRepo = FeedbackRepository()
                                                    val res = feedbackRepo.createFeedback(
                                                        FeedbackRequest(state.userInscripcion.id, calificacion, comentario)
                                                    )
                                                    if (res.isSuccessful) {
                                                        viewModel.fetchEvento(eventoId, usuarioId)
                                                        showFeedbackDialog = false
                                                    }
                                                } catch (e: Exception) {
                                                    // Handle error
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(100.dp))
                        }
                    }
                }
                is EventoDetalleState.Error -> {
                    Text(text = state.message, color = MascotGreen, modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}

@Composable
fun FeedbackItemPremium(feedback: com.tuevento.tueventofinal.data.model.FeedbackResponse) {
    Surface(
        color = BlobWhite,
        shape = RoundedCornerShape(32.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(feedback.usuarioNombre.uppercase(), fontWeight = FontWeight.ExtraBold, color = DarkGrey, fontSize = 14.sp)
                Row {
                    repeat(5) { index ->
                        Icon(
                            imageVector = if (index < feedback.calificacion) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = null,
                            tint = if (index < feedback.calificacion) MascotGreen else DarkGrey.copy(alpha = 0.1f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                feedback.comentario ?: "Sin comentario.",
                color = DarkGrey.copy(alpha = 0.8f),
                fontSize = 15.sp,
                lineHeight = 22.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                feedback.fechaEnvio.take(10),
                color = MascotGreen,
                fontSize = 10.sp,
                modifier = Modifier.align(Alignment.End),
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
fun FeedbackDialogPremium(
    onDismiss: () -> Unit,
    onSubmit: (Int, String) -> Unit
) {
    var rating by remember { mutableIntStateOf(5) }
    var comment by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BlobWhite,
        shape = RoundedCornerShape(32.dp),
        title = { Text("Tu Reseña", color = DarkGrey, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("¿Cómo calificarías este evento?", color = DarkGrey.copy(alpha = 0.6f), fontSize = 14.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    repeat(5) { index ->
                        IconButton(onClick = { rating = index + 1 }) {
                            Icon(
                                imageVector = if (index < rating) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = null,
                                tint = if (index < rating) MascotGreen else DarkGrey.copy(alpha = 0.1f),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Escribe tu comentario...", color = DarkGrey.copy(alpha = 0.4f)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = DarkGrey,
                        unfocusedTextColor = DarkGrey,
                        focusedBorderColor = MascotGreen,
                        unfocusedBorderColor = DarkGrey.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(rating, comment) },
                colors = ButtonDefaults.buttonColors(containerColor = MascotGreen, contentColor = Color.White),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("ENVIAR", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCELAR", color = DarkGrey.copy(alpha = 0.5f))
            }
        }
    )
}
