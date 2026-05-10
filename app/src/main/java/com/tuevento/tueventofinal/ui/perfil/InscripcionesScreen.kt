package com.tuevento.tueventofinal.ui.perfil

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ConfirmationNumber
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
import com.tuevento.tueventofinal.data.model.EstadoInscripcion
import com.tuevento.tueventofinal.data.model.InscripcionResponse
import com.tuevento.tueventofinal.ui.theme.BlobWhite
import com.tuevento.tueventofinal.ui.theme.DarkGrey
import com.tuevento.tueventofinal.ui.theme.MascotGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InscripcionesScreen(
    usuarioId: Long,
    onInscripcionClick: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: InscripcionViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(usuarioId) {
        viewModel.fetchInscripciones(usuarioId)
    }

    Scaffold(
        containerColor = DarkGrey,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "MIS ENTRADAS",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            color = MascotGreen
                        )
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .padding(8.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.3f))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        val scrollState = rememberScrollState()
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(DarkGrey)
        ) {
            // High contrast organic top section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .offset(y = (-100).dp)
                    .clip(RoundedCornerShape(bottomStart = 100.dp, bottomEnd = 100.dp))
                    .background(MascotGreen)
            )

            // Decorative Blobs
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .offset(x = (-100).dp, y = 100.dp)
                    .clip(CircleShape)
                    .background(MascotGreen.copy(alpha = 0.05f))
            )

            when (val state = uiState) {
                is InscripcionListState.Loading -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AsyncImage(
                            model = "https://raw.githubusercontent.com/tebantp/frontend_TuEvento/main/Logo.png",
                            contentDescription = null,
                            modifier = Modifier.size(80.dp).alpha(0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        CircularProgressIndicator(color = MascotGreen)
                    }
                }
                is InscripcionListState.Success -> {
                    if (state.inscripciones.isEmpty()) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AsyncImage(
                                model = "https://raw.githubusercontent.com/tebantp/frontend_TuEvento/main/Logo.png",
                                contentDescription = null,
                                modifier = Modifier.size(120.dp).alpha(0.3f)
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "Aún no tienes entradas",
                                style = MaterialTheme.typography.titleMedium,
                                color = MascotGreen.copy(alpha = 0.6f)
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(state.inscripciones) { inscripcion ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onInscripcionClick(inscripcion.id) },
                                    shape = RoundedCornerShape(28.dp),
                                    colors = CardDefaults.cardColors(containerColor = BlobWhite),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(20.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            modifier = Modifier.size(56.dp),
                                            shape = RoundedCornerShape(16.dp),
                                            color = MascotGreen.copy(alpha = 0.1f)
                                        ) {
                                            Icon(
                                                Icons.Default.ConfirmationNumber,
                                                contentDescription = null,
                                                tint = MascotGreen,
                                                modifier = Modifier.padding(14.dp)
                                            )
                                        }
                                        
                                        Spacer(modifier = Modifier.width(16.dp))
                                        
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = inscripcion.eventoTitulo,
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = DarkGrey
                                                )
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.CalendarMonth,
                                                    contentDescription = null,
                                                    tint = Color.Gray,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = inscripcion.fechaInscripcion,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color.Gray
                                                )
                                            }
                                        }
                                        
                                        Surface(
                                            color = if (inscripcion.estado == EstadoInscripcion.CONFIRMADA) MascotGreen else Color.Gray.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text(
                                                text = inscripcion.estado.name,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (inscripcion.estado == EstadoInscripcion.CONFIRMADA) Color.White else Color.Gray
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                is InscripcionListState.Error -> {
                    Text(
                        text = state.message,
                        color = Color(0xFFFF4C4C),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}
