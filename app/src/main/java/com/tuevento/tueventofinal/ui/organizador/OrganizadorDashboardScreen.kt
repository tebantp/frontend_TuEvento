package com.tuevento.tueventofinal.ui.organizador

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.tuevento.tueventofinal.data.model.EventoResponse
import com.tuevento.tueventofinal.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizadorDashboardScreen(
    organizadorId: Long,
    viewModel: OrganizadorViewModel,
    onNavigateToCrear: () -> Unit,
    onNavigateToScanner: () -> Unit,
    onNavigateToDetalle: (Long) -> Unit,
    onNavigateToEditar: (EventoResponse) -> Unit,
    onNavigateToReportes: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.loadDashboard(organizadorId)
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvents.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        containerColor = DarkGrey,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text("GESTIÓN".uppercase(), color = Color.White, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, letterSpacing = 3.sp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                actions = {
                    IconButton(
                        onClick = onNavigateToReportes,
                        modifier = Modifier.padding(horizontal = 4.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.1f))
                    ) {
                        Icon(Icons.Default.Assessment, contentDescription = "Reportes", tint = MascotGreen)
                    }
                    IconButton(
                        onClick = onNavigateToScanner,
                        modifier = Modifier.padding(horizontal = 4.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.1f))
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Scanner", tint = MascotGreen)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCrear,
                containerColor = MascotGreen,
                contentColor = Color.White,
                shape = RoundedCornerShape(24.dp),
                elevation = FloatingActionButtonDefaults.elevation(0.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Crear Evento", modifier = Modifier.size(32.dp))
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // High contrast header blob
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .offset(y = (-40).dp)
                    .clip(RoundedCornerShape(bottomStart = 80.dp, bottomEnd = 80.dp))
                    .background(MascotGreen)
            )

            when (val state = uiState) {
                is OrganizadorUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = MascotGreen)
                is OrganizadorUiState.Error -> Text(state.message, color = MascotGreen, modifier = Modifier.align(Alignment.Center))
                is OrganizadorUiState.Success -> {
                    var eventoAEliminar by remember { mutableStateOf<Long?>(null) }

                    DashboardContentPremium(
                        eventos = state.eventos,
                        stats = state.stats,
                        onEventoClick = onNavigateToDetalle,
                        onEditarEvento = onNavigateToEditar,
                        onDeleteEvento = { id -> eventoAEliminar = id }
                    )

                    if (eventoAEliminar != null) {
                        AlertDialog(
                            onDismissRequest = { eventoAEliminar = null },
                            title = { Text("Eliminar Evento", fontWeight = FontWeight.Bold) },
                            text = { Text("¿Estás seguro de que deseas eliminar este evento? Esta acción no se puede deshacer.") },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        eventoAEliminar?.let { viewModel.eliminarEvento(it) }
                                        eventoAEliminar = null
                                    }
                                ) {
                                    Text("ELIMINAR", color = Color.Red, fontWeight = FontWeight.Bold)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { eventoAEliminar = null }) {
                                    Text("CANCELAR")
                                }
                            },
                            containerColor = BlobWhite,
                            shape = RoundedCornerShape(28.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardContentPremium(
    eventos: List<EventoResponse>,
    stats: Map<String, Any>?,
    onEventoClick: (Long) -> Unit,
    onEditarEvento: (EventoResponse) -> Unit,
    onDeleteEvento: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Column {
                Text(
                    "RESUMEN GENERAL",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black, color = Color.White, letterSpacing = 2.sp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    StatCardPremium(
                        title = "Asistencia",
                        value = "${stats?.get("porcentajeAsistencia") ?: "85"}%",
                        icon = Icons.Default.TrendingUp,
                        modifier = Modifier.weight(1f)
                    )
                    StatCardPremium(
                        title = "Ingresos",
                        value = stats?.get("totalVentas")?.toString() ?: "$1.2k",
                        icon = Icons.Default.Payments,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "EVENTOS ACTIVOS",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black, color = Color.White, letterSpacing = 2.sp)
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MascotGreen.copy(alpha = 0.1f)
                ) {
                    Text(
                        "${eventos.size} TOTAL",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MascotGreen)
                    )
                }
            }
        }

        if (eventos.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    shape = RoundedCornerShape(40.dp),
                    color = BlobWhite
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        AsyncImage(
                            model = "https://raw.githubusercontent.com/tebantp/frontend_TuEvento/main/Logo.png",
                            contentDescription = null,
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("LISTO PARA COMENZAR", fontWeight = FontWeight.Black, color = MascotGreen)
                    }
                }
            }
        } else {
            items(eventos) { evento ->
                EventoGestionCardPremium(
                    evento = evento, 
                    onClick = { onEventoClick(evento.id) },
                    onEdit = { onEditarEvento(evento) },
                    onDelete = { onDeleteEvento(evento.id) }
                )
            }
        }
        
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
fun StatCardPremium(title: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = BlobWhite),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Surface(
                shape = CircleShape,
                color = MascotGreen.copy(alpha = 0.1f),
                modifier = Modifier.size(44.dp)
            ) {
                Icon(icon, contentDescription = null, tint = MascotGreen, modifier = Modifier.padding(10.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.Black, color = DarkGrey)
            Text(title.uppercase(), fontSize = 10.sp, color = MascotGreen, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }
    }
}

@Composable
fun EventoGestionCardPremium(
    evento: EventoResponse, 
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = BlobWhite),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    evento.titulo, 
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = DarkGrey)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.People, contentDescription = null, tint = MascotGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "${evento.cupoDisponible} / ${evento.cupoMaximo} cupos", 
                        style = MaterialTheme.typography.bodySmall, 
                        color = DarkGrey.copy(alpha = 0.6f)
                    )
                }
            }
            
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MascotGreen.copy(alpha = 0.8f))
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red.copy(alpha = 0.6f))
            }

            Surface(
                color = if (evento.estado.name == "ACTIVO") MascotGreen.copy(alpha = 0.15f) else DarkGrey.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    evento.estado.name,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (evento.estado.name == "ACTIVO") MascotGreen else DarkGrey.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
