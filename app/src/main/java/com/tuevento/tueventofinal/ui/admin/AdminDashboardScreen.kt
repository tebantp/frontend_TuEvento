package com.tuevento.tueventofinal.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.tuevento.tueventofinal.data.model.RolUsuario
import com.tuevento.tueventofinal.data.model.UsuarioResponse
import com.tuevento.tueventofinal.util.ValidationUtils
import com.tuevento.tueventofinal.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: AdminViewModel,
    onNavigateToCrear: () -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        viewModel.loadAdminData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "ADMINISTRADOR", 
                        color = MascotGreen, 
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold, 
                            letterSpacing = 2.sp
                        )
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkGrey),
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = "Cerrar Sesión", tint = MascotGreen)
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = onNavigateToCrear,
                    containerColor = MascotGreen,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Crear Evento", modifier = Modifier.size(32.dp))
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(DarkGrey)
        ) {
            TabRow(
                selectedTabIndex = selectedTab, 
                containerColor = Color.Transparent, 
                contentColor = MascotGreen,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = MascotGreen
                    )
                },
                divider = {}
            ) {
                AdminTab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, icon = Icons.AutoMirrored.Filled.EventNote, label = "Eventos")
                AdminTab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, icon = Icons.Default.People, label = "Usuarios")
                AdminTab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, icon = Icons.Default.Insights, label = "Global")
            }

            Box(modifier = Modifier.fillMaxSize()) {
                // Decorative Blob
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = 60.dp, y = 60.dp)
                        .clip(RoundedCornerShape(100.dp))
                        .background(MascotGreen.copy(alpha = 0.05f))
                )

                when (val state = uiState) {
                    is AdminUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = MascotGreen)
                    is AdminUiState.Error -> Text(state.message, color = MascotGreen, modifier = Modifier.align(Alignment.Center))
                    is AdminUiState.Success -> {
                    var eventoAEliminar by remember { mutableStateOf<Long?>(null) }

                    when (selectedTab) {
                        0 -> ModeracionListPremium(
                            state.eventos, 
                            onModerar = viewModel::moderar,
                            onDelete = { id -> eventoAEliminar = id }
                        )
                        1 -> UsuariosListPremium(
                            state.usuarios, 
                            onBan = viewModel::banearUsuario,
                            onChangeRol = viewModel::cambiarRolUsuario
                        )
                        2 -> AnaliticaGlobalPremium(state.globalStats)
                    }

                    if (eventoAEliminar != null) {
                        AlertDialog(
                            onDismissRequest = { eventoAEliminar = null },
                            title = { Text("Eliminar Evento (Admin)", fontWeight = FontWeight.Bold) },
                            text = { Text("¿Confirmas la eliminación permanente de este evento?") },
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
}

@Composable
fun AdminTab(selected: Boolean, onClick: () -> Unit, icon: ImageVector, label: String) {
    Tab(
        selected = selected,
        onClick = onClick,
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(icon, contentDescription = null, tint = if (selected) MascotGreen else MascotGreen.copy(alpha = 0.4f))
                Text(label, style = MaterialTheme.typography.labelSmall, color = if (selected) Color.White else Color.White.copy(alpha = 0.5f))
            }
        }
    )
}

@Composable
fun ModeracionListPremium(
    eventos: List<EventoResponse>, 
    onModerar: (Long, Boolean) -> Unit,
    onDelete: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        if (eventos.isEmpty()) {
            item {
                Column(
                    modifier = Modifier.fillParentMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    AsyncImage(
                        model = "https://raw.githubusercontent.com/tebantp/frontend_TuEvento/main/Logo.png",
                        contentDescription = null,
                        modifier = Modifier.size(100.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No hay eventos registrados.", color = MascotGreen.copy(alpha = 0.6f))
                }
            }
        } else {
            items(eventos) { evento ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = BlobWhite),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Surface(
                                    shape = CircleShape,
                                    color = MascotGreen.copy(alpha = 0.1f),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(Icons.Default.Event, contentDescription = null, tint = MascotGreen, modifier = Modifier.padding(8.dp))
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(evento.titulo, fontWeight = FontWeight.ExtraBold, color = DarkGrey, fontSize = 18.sp, maxLines = 1)
                                    Text("Org: ${evento.organizadorNombre}", color = MascotGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            
                            IconButton(onClick = { onDelete(evento.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red.copy(alpha = 0.6f))
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(evento.descripcion ?: "Sin descripción", color = DarkGrey.copy(alpha = 0.7f), maxLines = 2, fontSize = 13.sp)
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (evento.estado.name != "ACTIVO") {
                                Button(
                                    onClick = { onModerar(evento.id, true) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MascotGreen),
                                    modifier = Modifier.weight(1f).height(40.dp),
                                    shape = RoundedCornerShape(20.dp)
                                ) { Text("APROBAR", fontWeight = FontWeight.Black, fontSize = 12.sp) }
                            }
                            
                            if (evento.estado.name != "CANCELADO") {
                                TextButton(
                                    onClick = { onModerar(evento.id, false) },
                                    modifier = Modifier.weight(1f).height(40.dp)
                                ) { Text("RECHAZAR", color = Color.Red.copy(alpha = 0.7f), fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UsuariosListPremium(
    usuarios: List<UsuarioResponse>, 
    onBan: (Long, Boolean) -> Unit,
    onChangeRol: (Long, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(usuarios) { usuario ->
            var showRoleDialog by remember { mutableStateOf(false) }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = BlobWhite),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (usuario.activo) MascotGreen.copy(alpha = 0.15f) else Color.Red.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (usuario.activo) Icons.Default.Person else Icons.Default.PersonOff,
                            contentDescription = null,
                            tint = if (usuario.activo) MascotGreen else Color.Red.copy(alpha = 0.7f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "${usuario.nombre} ${usuario.apellido}", 
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = DarkGrey)
                        )
                        Text(usuario.email, color = DarkGrey.copy(alpha = 0.5f), fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            onClick = { showRoleDialog = true },
                            color = MascotGreen.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                usuario.rol.name, 
                                color = MascotGreen, 
                                fontSize = 10.sp, 
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Switch(
                            checked = usuario.activo,
                            onCheckedChange = { onBan(usuario.id, it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MascotGreen,
                                checkedTrackColor = MascotGreen.copy(alpha = 0.3f)
                            )
                        )
                    }
                }
            }

            if (showRoleDialog) {
                AlertDialog(
                    onDismissRequest = { showRoleDialog = false },
                    title = { Text("Asignar Rol", color = DarkGrey, fontWeight = FontWeight.Bold) },
                    containerColor = BlobWhite,
                    shape = RoundedCornerShape(32.dp),
                    text = {
                        Column {
                            RolUsuario.entries.forEach { rol ->
                                val isUdec = ValidationUtils.isUdecEmail(usuario.email)
                                val isPrivileged = rol == RolUsuario.ADMINISTRADOR || rol == RolUsuario.ORGANIZADOR
                                val enabled = !isPrivileged || isUdec

                                TextButton(
                                    onClick = { 
                                        onChangeRol(usuario.id, rol.name)
                                        showRoleDialog = false 
                                    },
                                    enabled = enabled,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = if (isPrivileged && !isUdec) "${rol.name} (Solo Udec)" else rol.name,
                                        color = when {
                                            usuario.rol == rol -> MascotGreen
                                            !enabled -> DarkGrey.copy(alpha = 0.3f)
                                            else -> DarkGrey
                                        },
                                        fontWeight = if (usuario.rol == rol) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showRoleDialog = false }) { 
                            Text("CERRAR", color = MascotGreen, fontWeight = FontWeight.Bold) 
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun AnaliticaGlobalPremium(stats: Map<String, Any>?) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Surface(
                modifier = Modifier.size(100.dp),
                shape = CircleShape,
                color = MascotGreen.copy(alpha = 0.05f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(48.dp), tint = MascotGreen)
                }
            }
        }
        
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "ECOSISTEMA", 
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, letterSpacing = 4.sp), 
                    color = Color.White
                )
                Text(
                    "Métricas de rendimiento orgánico", 
                    color = MascotGreen.copy(alpha = 0.7f), 
                    fontSize = 12.sp, 
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                AdminStatCard(
                    label = "Usuarios",
                    value = stats?.get("usuariosTotales")?.toString() ?: "0",
                    icon = Icons.Default.Groups,
                    modifier = Modifier.weight(1f)
                )
                AdminStatCard(
                    label = "Eventos",
                    value = stats?.get("eventosActivos")?.toString() ?: "0",
                    icon = Icons.Default.AutoGraph,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = MascotGreen)
            ) {
                Row(
                    modifier = Modifier.padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Crecimiento Saludable", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("+12% actividad este mes", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    }
                    AsyncImage(
                        model = "https://raw.githubusercontent.com/tebantp/frontend_TuEvento/main/Logo.png",
                        contentDescription = null,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AdminStatCard(label: String, value: String, icon: ImageVector, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = BlobWhite),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Icon(icon, contentDescription = null, tint = MascotGreen, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.Black, color = DarkGrey)
            Text(label.uppercase(), color = MascotGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }
    }
}
