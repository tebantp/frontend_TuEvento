package com.tuevento.tueventofinal.ui.eventos

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.tuevento.tueventofinal.data.model.EventoResponse
import com.tuevento.tueventofinal.ui.theme.*
import com.tuevento.tueventofinal.util.ImageUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventoListScreen(
    userRole: String = "USUARIO",
    onEventoClick: (Long) -> Unit,
    onAddEventoClick: () -> Unit,
    onDeleteEvento: (Long) -> Unit,
    onScannerClick: () -> Unit,
    onPerfilClick: () -> Unit,
    viewModel: EventoViewModel
) {
    val uiState      by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val imageUrlMap  by viewModel.imageUrlMap.collectAsState()
    val snackbarHost = remember { SnackbarHostState() }

    val categories = listOf("Todos", "Música", "Tecnología", "Deportes", "Cultura")
    var selectedCategory by remember { mutableStateOf("Todos") }
    var searchQuery      by remember { mutableStateOf("") }

    // Escuchar mensajes de rollback
    LaunchedEffect(Unit) {
        viewModel.uiEvents.collect { message ->
            snackbarHost.showSnackbar(message)
        }
    }

    // FIX CRÍTICO: NO llamamos fetchEventos aquí.
    // El ViewModel ya controla la carga inicial con initialLoadDone.
    // Esto evita que al volver de EventoCrearScreen se relance el GET
    // que sobreescribiría el estado optimista.
    // El pull-to-refresh es la única forma manual de recargar.

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHost) },
        containerColor = DarkGrey,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "DESCUBRIR",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 4.sp,
                            color = Color.White
                        )
                    )
                },
                actions = {
                    if (userRole != "USUARIO") {
                        IconButton(
                            onClick = onScannerClick,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.1f))
                        ) {
                            Icon(Icons.Default.QrCodeScanner, "Scanner", tint = MascotGreen)
                        }
                    }
                    IconButton(
                        onClick = onPerfilClick,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f))
                    ) {
                        AsyncImage(
                            model = "https://raw.githubusercontent.com/tebantp/frontend_TuEvento/main/Logo.png",
                            contentDescription = "Perfil",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkGrey,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            // RBAC: solo ORGANIZADOR y ADMINISTRADOR ven el botón de crear
            if (userRole == "ORGANIZADOR" || userRole == "ADMINISTRADOR") {
                FloatingActionButton(
                    onClick = onAddEventoClick,
                    containerColor = MascotGreen,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Icon(Icons.Default.Add, "Crear Evento", modifier = Modifier.size(32.dp))
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { viewModel.fetchEventos(forceSilent = true, forceInvalidate = true) },
                state = rememberPullToRefreshState(),
                modifier = Modifier.fillMaxSize()
            ) {
                when (val state = uiState) {
                    is EventoListState.Loading -> {
                        // Mascota gato verde en pantalla de carga
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            AsyncImage(
                                model = "https://raw.githubusercontent.com/tebantp/frontend_TuEvento/main/Logo.png",
                                contentDescription = "Cargando...",
                                modifier = Modifier.size(120.dp).alpha(0.85f)
                            )
                            Spacer(Modifier.height(28.dp))
                            CircularProgressIndicator(
                                color = MascotGreen,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "Cargando eventos...",
                                color = Color.White.copy(alpha = 0.5f),
                                style = MaterialTheme.typography.bodySmall,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    is EventoListState.Success -> {
                        val filteredEventos = remember(selectedCategory, searchQuery, state.eventos) {
                            state.eventos.filter { evento ->
                                val matchesCategory = when (selectedCategory) {
                                    "Todos" -> true
                                    "Música" -> listOf("Música","Concierto","Festival","Banda","Rock","Pop","Jazz","DJ","Orquesta","Recital","Cantante","Piano","Guitarra").any {
                                        evento.titulo.contains(it, true) || evento.descripcion?.contains(it, true) == true }
                                    "Tecnología" -> listOf("Tecnología","Tech","Taller","IA","Programación","Software","Computación","Desarrollo","Web","Móvil","Hackathon","Hackaton","Conferencia","Meetup","Coding","Robótica","App").any {
                                        evento.titulo.contains(it, true) || evento.descripcion?.contains(it, true) == true }
                                    "Deportes" -> listOf("Deportes","Fútbol","Carrera","Torneo","Entrenamiento","Basquet","Natación","Voleibol","Atletismo","Gimnasia","Yoga","Fitness","Campeonato").any {
                                        evento.titulo.contains(it, true) || evento.descripcion?.contains(it, true) == true }
                                    "Cultura" -> listOf("Cultura","Arte","Teatro","Exposición","Museo","Cine","Literatura","Poesía","Danza","Baile","Pintura","Historia","Gastronomía","Feria","Fotografía").any {
                                        evento.titulo.contains(it, true) || evento.descripcion?.contains(it, true) == true }
                                    else -> true
                                }
                                val matchesSearch = searchQuery.isEmpty() ||
                                    evento.titulo.contains(searchQuery, true) ||
                                    evento.descripcion?.contains(searchQuery, true) == true ||
                                    evento.lugar.contains(searchQuery, true)
                                matchesCategory && matchesSearch
                            }
                        }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 100.dp),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            // Buscador
                            item {
                                Surface(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                                    shape = RoundedCornerShape(32.dp),
                                    color = BlobWhite
                                ) {
                                    TextField(
                                        value = searchQuery,
                                        onValueChange = { searchQuery = it },
                                        placeholder = { Text("Buscar eventos...", color = DarkGrey.copy(alpha = 0.5f)) },
                                        leadingIcon = { Icon(Icons.Default.Search, null, tint = MascotGreen) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = TextFieldDefaults.colors(
                                            focusedContainerColor   = Color.Transparent,
                                            unfocusedContainerColor = Color.Transparent,
                                            focusedIndicatorColor   = Color.Transparent,
                                            unfocusedIndicatorColor = Color.Transparent,
                                            cursorColor             = MascotGreen,
                                            focusedTextColor        = DarkGrey,
                                            unfocusedTextColor      = DarkGrey
                                        ),
                                        singleLine = true
                                    )
                                }
                            }

                            // Categorías
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    categories.forEach { category ->
                                        val isSelected = selectedCategory == category
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.clickable { selectedCategory = category }
                                        ) {
                                            Surface(
                                                modifier = Modifier.size(72.dp),
                                                shape = CircleShape,
                                                color = if (isSelected) MascotGreen else BlobWhite,
                                                border = if (!isSelected) BorderStroke(1.dp, MascotGreen.copy(alpha = 0.2f)) else null
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        when (category) {
                                                            "Música"     -> Icons.Default.MusicNote
                                                            "Tecnología" -> Icons.Default.Computer
                                                            "Deportes"   -> Icons.Default.SportsBasketball
                                                            "Cultura"    -> Icons.Default.Palette
                                                            else         -> Icons.Default.AllInclusive
                                                        },
                                                        contentDescription = null,
                                                        tint = if (isSelected) Color.White else MascotGreen,
                                                        modifier = Modifier.size(30.dp)
                                                    )
                                                }
                                            }
                                            Spacer(Modifier.height(8.dp))
                                            Text(
                                                category,
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                                color = if (isSelected) MascotGreen else Color.White.copy(alpha = 0.6f)
                                            )
                                        }
                                    }
                                }
                            }

                            if (filteredEventos.isNotEmpty()) {
                                item {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        Text("RECOMENDADOS", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black, letterSpacing = 2.sp, color = Color.White))
                                        Text("Ver todos", color = MascotGreen, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(Modifier.height(16.dp))
                                    HeroEventoCard(
                                        evento   = filteredEventos.first(),
                                        userRole = userRole,
                                        imageUrl = imageUrlMap[filteredEventos.first().id],
                                        onClick  = { onEventoClick(filteredEventos.first().id) },
                                        onDelete = { onDeleteEvento(filteredEventos.first().id) }
                                    )
                                }

                                items(filteredEventos.drop(1), key = { it.id }) { evento ->
                                    PremiumEventoItem(
                                        evento   = evento,
                                        userRole = userRole,
                                        imageUrl = imageUrlMap[evento.id],
                                        onClick  = { onEventoClick(evento.id) },
                                        onDelete = { onDeleteEvento(evento.id) }
                                    )
                                }
                            } else {
                                // Mascota gato verde en pantalla vacía
                                item {
                                    Surface(
                                        modifier = Modifier.fillMaxWidth().padding(24.dp).height(220.dp),
                                        shape = RoundedCornerShape(48.dp),
                                        color = BlobWhite.copy(alpha = 0.07f)
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            AsyncImage(
                                                model = "https://raw.githubusercontent.com/tebantp/frontend_TuEvento/main/Logo.png",
                                                contentDescription = null,
                                                modifier = Modifier.size(100.dp).alpha(0.55f)
                                            )
                                            Spacer(Modifier.height(20.dp))
                                            Text(
                                                "NO SE ENCONTRARON EVENTOS",
                                                color = MascotGreen,
                                                fontWeight = FontWeight.Black,
                                                letterSpacing = 1.sp,
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                            Spacer(Modifier.height(8.dp))
                                            Text(
                                                "Prueba otra categoría o búsqueda",
                                                color = Color.White.copy(alpha = 0.35f),
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    is EventoListState.Error -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            AsyncImage(
                                model = "https://raw.githubusercontent.com/tebantp/frontend_TuEvento/main/Logo.png",
                                contentDescription = null,
                                modifier = Modifier.size(100.dp).alpha(0.45f)
                            )
                            Spacer(Modifier.height(24.dp))
                            Surface(
                                color = Color.Red.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier.padding(horizontal = 32.dp)
                            ) {
                                Text(
                                    state.message,
                                    color = Color.Red.copy(alpha = 0.85f),
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Spacer(Modifier.height(20.dp))
                            Button(
                                onClick = { viewModel.fetchEventos(forceInvalidate = true) },
                                colors = ButtonDefaults.buttonColors(containerColor = MascotGreen),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text("REINTENTAR", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── DIÁLOGO DE CONFIRMACIÓN DE ELIMINACIÓN ────────────────────────────────────
@Composable
fun DeleteConfirmDialog(
    eventoTitulo: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = DarkSurface,
        shape            = RoundedCornerShape(40.dp),
        icon = {
            // Blob decorativo alrededor del ícono
            Surface(
                shape = CircleShape,
                color = Color.Red.copy(alpha = 0.12f),
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.DeleteForever,
                        null,
                        tint = Color.Red.copy(alpha = 0.9f),
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        },
        title = {
            Text(
                "¿Eliminar evento?",
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 0.5.sp
            )
        },
        text = {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White.copy(alpha = 0.05f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Se eliminará permanentemente:\n\"$eventoTitulo\"\n\nEsta acción no se puede deshacer.",
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.85f)),
                shape  = RoundedCornerShape(20.dp)
            ) {
                Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("ELIMINAR", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = MascotGreen, fontWeight = FontWeight.Bold)
            }
        }
    )
}

// ── HERO CARD ─────────────────────────────────────────────────────────────────
@Composable
fun HeroEventoCard(
    evento: EventoResponse,
    userRole: String,
    imageUrl: String? = null,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    if (showDeleteDialog) {
        DeleteConfirmDialog(
            eventoTitulo = evento.titulo,
            onConfirm    = { showDeleteDialog = false; onDelete() },
            onDismiss    = { showDeleteDialog = false }
        )
    }

    val resolvedUrl = remember(evento.titulo, evento.id, imageUrl) {
        ImageUtils.getEventoImageUrl(evento.titulo, evento.id, imageUrl)
    }

    Card(
        modifier  = Modifier.fillMaxWidth().height(240.dp).padding(horizontal = 16.dp).clickable { onClick() },
        shape     = RoundedCornerShape(40.dp),
        elevation = CardDefaults.cardElevation(12.dp)
    ) {
        Box {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(resolvedUrl).crossfade(true).build(),
                contentDescription = null,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier.fillMaxSize().background(Color.DarkGray)
            )
            Box(
                modifier = Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.82f))
                    )
                )
            )

            // Botón eliminar — solo ADMINISTRADOR u ORGANIZADOR (RBAC)
            if (userRole == "ADMINISTRADOR" || userRole == "ORGANIZADOR") {
                IconButton(
                    onClick  = { showDeleteDialog = true },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .background(Color.Red.copy(alpha = 0.88f), CircleShape)
                ) {
                    Icon(Icons.Default.Delete, "Eliminar", tint = Color.White)
                }
            }

            Column(modifier = Modifier.align(Alignment.BottomStart).padding(24.dp)) {
                Surface(color = MascotGreen, shape = RoundedCornerShape(16.dp)) {
                    Text(
                        "DESTACADO",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        style    = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color    = Color.White,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    evento.titulo,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = MascotGreen, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(evento.lugar, color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

// ── PREMIUM ITEM ──────────────────────────────────────────────────────────────
@Composable
fun PremiumEventoItem(
    evento: EventoResponse,
    userRole: String,
    imageUrl: String? = null,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    if (showDeleteDialog) {
        DeleteConfirmDialog(
            eventoTitulo = evento.titulo,
            onConfirm    = { showDeleteDialog = false; onDelete() },
            onDismiss    = { showDeleteDialog = false }
        )
    }

    val resolvedUrl = remember(evento.titulo, evento.id, imageUrl) {
        ImageUtils.getEventoImageUrl(evento.titulo, evento.id, imageUrl)
    }

    Card(
        modifier  = Modifier.fillMaxWidth().height(180.dp).padding(horizontal = 16.dp).clickable { onClick() },
        shape     = RoundedCornerShape(36.dp),
        colors    = CardDefaults.cardColors(containerColor = DarkSurface),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Imagen lateral con esquinas redondeadas orgánicas
            Box(
                modifier = Modifier
                    .width(130.dp)
                    .fillMaxHeight()
                    .background(Color.DarkGray)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(resolvedUrl).crossfade(true).build(),
                    contentDescription = null,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier.fillMaxSize()
                )
                // Badge de cupos disponibles
                if (evento.cupoDisponible < 5) {
                    Surface(
                        modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
                        shape    = RoundedCornerShape(12.dp),
                        color    = Color.Red.copy(alpha = 0.9f)
                    ) {
                        Text(
                            "¡Últimos!",
                            modifier  = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style     = MaterialTheme.typography.labelSmall,
                            color     = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(20.dp).weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        evento.titulo,
                        style    = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold),
                        modifier = Modifier.weight(1f),
                        maxLines = 2
                    )
                    // RBAC: botón eliminar solo para roles con permisos
                    if (userRole == "ADMINISTRADOR" || userRole == "ORGANIZADOR") {
                        IconButton(
                            onClick  = { showDeleteDialog = true },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Delete, "Eliminar", tint = Color.Red.copy(alpha = 0.7f))
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = MascotGreen, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(evento.lugar, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                Spacer(Modifier.weight(1f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text("GRATIS", color = MascotGreen, fontWeight = FontWeight.Black, fontSize = 18.sp)
                        Text(
                            "${evento.cupoDisponible} cupos",
                            color = Color.White.copy(alpha = 0.45f),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    Surface(color = Color.White.copy(alpha = 0.05f), shape = CircleShape) {
                        Icon(Icons.Default.FavoriteBorder, null, tint = Color.White, modifier = Modifier.padding(10.dp).size(20.dp))
                    }
                }
            }
        }
    }
}
