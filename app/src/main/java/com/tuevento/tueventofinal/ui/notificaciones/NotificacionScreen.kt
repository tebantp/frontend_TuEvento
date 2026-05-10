package com.tuevento.tueventofinal.ui.notificaciones

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.tuevento.tueventofinal.data.model.NotificacionResponse
import com.tuevento.tueventofinal.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificacionScreen(
    usuarioId: Long,
    onBack: () -> Unit,
    onNavigateToEvento: (Long) -> Unit,
    viewModel: NotificacionViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(usuarioId) {
        viewModel.fetchNotificaciones(usuarioId)
    }

    Scaffold(
        containerColor = DarkGrey,
        topBar = {
            Surface(
                color = MascotGreen,
                shape = RoundedCornerShape(bottomStart = 48.dp, bottomEnd = 48.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        "NOTIFICACIONES",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = Color.White
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(top = padding.calculateTopPadding())
                .fillMaxSize()
        ) {
            // Decorative Blobs
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 100.dp, y = 100.dp)
                    .clip(CircleShape)
                    .background(MascotGreen.copy(alpha = 0.05f))
            )

            when (val state = uiState) {
                is NotificacionState.Loading -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AsyncImage(
                            model = "https://raw.githubusercontent.com/tebantp/frontend_TuEvento/main/Logo.png",
                            contentDescription = null,
                            modifier = Modifier.size(100.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        CircularProgressIndicator(color = MascotGreen)
                    }
                }
                is NotificacionState.Success -> {
                    if (state.notificaciones.isEmpty()) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AsyncImage(
                                model = "https://raw.githubusercontent.com/tebantp/frontend_TuEvento/main/Logo.png",
                                contentDescription = null,
                                modifier = Modifier.size(150.dp).alpha(0.2f)
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                "TODO EN ORDEN POR AQUÍ",
                                color = Color.White.copy(alpha = 0.3f),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(state.notificaciones) { notificacion ->
                                NotificacionItemPremium(
                                    notificacion = notificacion,
                                    onClick = {
                                        viewModel.marcarComoLeida(notificacion.id, usuarioId)
                                        notificacion.eventoId?.let { onNavigateToEvento(it) }
                                    }
                                )
                            }
                            item { Spacer(modifier = Modifier.height(80.dp)) }
                        }
                    }
                }
                is NotificacionState.Error -> {
                    Text(
                        text = state.message,
                        color = Color(0xFFFF4C4C),
                        modifier = Modifier.align(Alignment.Center),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun NotificacionItemPremium(notificacion: NotificacionResponse, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        color = if (notificacion.leida) BlobWhite.copy(alpha = 0.8f) else BlobWhite,
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(if (notificacion.leida) DarkGrey.copy(alpha = 0.05f) else MascotGreen.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = null,
                    tint = if (notificacion.leida) DarkGrey.copy(alpha = 0.3f) else MascotGreen,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notificacion.titulo.uppercase(),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    ),
                    color = if (notificacion.leida) DarkGrey.copy(alpha = 0.5f) else DarkGrey
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notificacion.mensaje,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (notificacion.leida) DarkGrey.copy(alpha = 0.4f) else DarkGrey.copy(alpha = 0.7f),
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = notificacion.fechaCreacion.take(10),
                    style = MaterialTheme.typography.labelSmall,
                    color = MascotGreen,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            if (!notificacion.leida) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(MascotGreen)
                )
            }
        }
    }
}
