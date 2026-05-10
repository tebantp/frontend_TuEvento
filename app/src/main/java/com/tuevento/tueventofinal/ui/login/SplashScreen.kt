package com.tuevento.tueventofinal.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import com.tuevento.tueventofinal.ui.theme.DarkGrey
import com.tuevento.tueventofinal.ui.theme.MascotGreen
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onNavigate: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkGrey),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AsyncImage(
                model = "https://raw.githubusercontent.com/tebantp/frontend_TuEvento/main/Logo.png",
                contentDescription = "Mascot",
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f))
                    .padding(16.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "TUEVENTO",
                style = MaterialTheme.typography.displayMedium.copy(
                    color = MascotGreen,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 8.sp
                )
            )
            Spacer(modifier = Modifier.height(32.dp))
            LinearProgressIndicator(
                color = MascotGreen,
                trackColor = MascotGreen.copy(alpha = 0.2f),
                modifier = Modifier
                    .width(100.dp)
                    .clip(CircleShape)
            )
        }
    }

    LaunchedEffect(Unit) {
        delay(1500) // Simulación de carga
        onNavigate()
    }
}
