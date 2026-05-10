package com.tuevento.tueventofinal.ui.qr

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.tuevento.tueventofinal.ui.theme.BlobWhite
import com.tuevento.tueventofinal.ui.theme.DarkGrey
import com.tuevento.tueventofinal.ui.theme.MascotGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRScreen(
    inscripcionId: Long,
    onBack: () -> Unit,
    viewModel: QRViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(inscripcionId) {
        viewModel.fetchQR(inscripcionId)
    }

    Scaffold(
        containerColor = DarkGrey,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "MI ENTRADA",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            color = MascotGreen
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = MascotGreen)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Background Blobs
            Box(
                modifier = Modifier
                    .size(250.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 100.dp, y = (-50).dp)
                    .clip(CircleShape)
                    .background(MascotGreen.copy(alpha = 0.08f))
            )

            when (val state = uiState) {
                is QRState.Loading -> {
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
                is QRState.Success -> {
                    val qrCode = state.qr.codigoUnico
                    val bitmap = remember(qrCode) {
                        try {
                            val barcodeEncoder = BarcodeEncoder()
                            barcodeEncoder.encodeBitmap(qrCode, BarcodeFormat.QR_CODE, 800, 800)
                        } catch (e: Exception) {
                            null
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(32.dp),
                            colors = CardDefaults.cardColors(containerColor = BlobWhite),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = state.qr.eventoTitulo,
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        color = DarkGrey
                                    ),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                if (bitmap != null) {
                                    Surface(
                                        modifier = Modifier.size(240.dp),
                                        shape = RoundedCornerShape(24.dp),
                                        color = Color.White,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                                    ) {
                                        Image(
                                            bitmap = bitmap.asImageBitmap(),
                                            contentDescription = "QR Code",
                                            modifier = Modifier.padding(16.dp)
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = state.qr.usuarioNombre,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = DarkGrey
                                        )
                                    )
                                    Text(
                                        text = "Válido hasta: ${state.qr.fechaExpiracion}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                AsyncImage(
                                    model = "https://raw.githubusercontent.com/tebantp/frontend_TuEvento/main/Logo.png",
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp).alpha(0.5f)
                                )
                            }
                        }
                    }
                }
                is QRState.Error -> {
                    Text(text = state.message, color = Color(0xFFFF4C4C), modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}
