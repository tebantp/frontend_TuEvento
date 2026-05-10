package com.tuevento.tueventofinal.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
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
import com.tuevento.tueventofinal.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    eventoId: Long,
    usuarioId: Long,
    onBack: () -> Unit,
    viewModel: ChatViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var nuevoMensaje by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(eventoId) {
        viewModel.fetchMensajes(eventoId)
    }

    LaunchedEffect(uiState) {
        if (uiState is ChatState.Success) {
            val messages = (uiState as ChatState.Success).mensajes
            if (messages.isNotEmpty()) {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    Scaffold(
        containerColor = DarkGrey,
        topBar = {
            Surface(
                color = MascotGreen,
                shape = RoundedCornerShape(bottomStart = 48.dp, bottomEnd = 48.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
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
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "SALA DE CHAT",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp,
                                    color = Color.White
                                )
                            )
                            Text(
                                "COMUNIDAD DEL EVENTO #$eventoId",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        AsyncImage(
                            model = "https://raw.githubusercontent.com/tebantp/frontend_TuEvento/main/Logo.png",
                            contentDescription = null,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                color = BlobWhite,
                shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                        .navigationBarsPadding()
                        .imePadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = nuevoMensaje,
                        onValueChange = { nuevoMensaje = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Mensaje...", color = DarkGrey.copy(alpha = 0.4f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MascotGreen,
                            unfocusedBorderColor = DarkGrey.copy(alpha = 0.1f),
                            focusedTextColor = DarkGrey,
                            unfocusedTextColor = DarkGrey,
                            cursorColor = MascotGreen,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 4
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    IconButton(
                        onClick = {
                            if (nuevoMensaje.isNotBlank()) {
                                viewModel.enviarMensaje(usuarioId, eventoId, nuevoMensaje)
                                nuevoMensaje = ""
                            }
                        },
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(MascotGreen)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar", tint = Color.White)
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(top = padding.calculateTopPadding())
                .padding(bottom = padding.calculateBottomPadding())
                .fillMaxSize()
                .background(DarkGrey)
        ) {
            when (val state = uiState) {
                is ChatState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MascotGreen
                    )
                }
                is ChatState.Success -> {
                    if (state.mensajes.isEmpty()) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AsyncImage(
                                model = "https://raw.githubusercontent.com/tebantp/frontend_TuEvento/main/Logo.png",
                                contentDescription = null,
                                modifier = Modifier.size(100.dp).alpha(0.5f)
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                "¡SÉ EL PRIMERO EN SALUDAR!",
                                color = MascotGreen.copy(alpha = 0.4f),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(state.mensajes) { mensaje ->
                                ChatBubblePremium(
                                    mensaje = mensaje.contenido,
                                    remitente = mensaje.remitenteNombre,
                                    esPropio = mensaje.remitenteId == usuarioId
                                )
                            }
                        }
                    }
                }
                is ChatState.Error -> {
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
fun ChatBubblePremium(mensaje: String, remitente: String, esPropio: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (esPropio) Alignment.End else Alignment.Start
    ) {
        if (!esPropio) {
            Text(
                text = remitente.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 10.sp
                ),
                color = MascotGreen,
                modifier = Modifier.padding(start = 12.dp, bottom = 6.dp),
                letterSpacing = 1.sp
            )
        }
        Surface(
            color = if (esPropio) MascotGreen else BlobWhite,
            shape = RoundedCornerShape(
                topStart = 32.dp,
                topEnd = 32.dp,
                bottomStart = if (esPropio) 32.dp else 4.dp,
                bottomEnd = if (esPropio) 4.dp else 32.dp
            ),
            shadowElevation = 0.dp
        ) {
            Text(
                text = mensaje,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp),
                color = if (esPropio) Color.White else DarkGrey,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    lineHeight = 22.sp
                )
            )
        }
    }
}
