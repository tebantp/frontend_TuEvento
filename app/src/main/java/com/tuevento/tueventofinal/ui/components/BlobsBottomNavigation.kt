package com.tuevento.tueventofinal.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.res.painterResource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.tuevento.tueventofinal.ui.theme.BlobWhite
import com.tuevento.tueventofinal.ui.theme.DarkGrey
import com.tuevento.tueventofinal.ui.theme.MascotGreen

@Composable
fun BlobsBottomNavigation(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp)
            .height(80.dp),
        color = BlobWhite,
        shape = RoundedCornerShape(40.dp),
        shadowElevation = 12.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavigationItem(
                icon = "https://raw.githubusercontent.com/tebantp/frontend_TuEvento/main/Logo.png",
                label = "EVENTOS",
                selected = currentRoute == "eventos",
                onClick = { onNavigate("eventos") }
            )
            NavigationItem(
                icon = Icons.Default.ChatBubble,
                label = "CHATS",
                selected = currentRoute?.startsWith("chat") == true,
                onClick = { /* In an ideal world, navigate to a chats list, but here we keep consistency */ }
            )
            NavigationItem(
                icon = Icons.Default.Notifications,
                label = "ALERTAS",
                selected = currentRoute == "notificaciones",
                onClick = { onNavigate("notificaciones") }
            )
            NavigationItem(
                icon = Icons.Default.Person,
                label = "PERFIL",
                selected = currentRoute == "perfil",
                onClick = { onNavigate("perfil") }
            )
        }
    }
}

@Composable
private fun NavigationItem(
    icon: Any, // Can be ImageVector, Int (resource), or String (URL)
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.1f else 1.0f,
        animationSpec = tween(300),
        label = "scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(8.dp)
            .scale(scale)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (selected) MascotGreen else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            when (icon) {
                is ImageVector -> {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (selected) Color.White else DarkGrey.copy(alpha = 0.4f),
                        modifier = Modifier.size(24.dp)
                    )
                }
                is Int -> {
                    Icon(
                        painter = painterResource(id = icon),
                        contentDescription = label,
                        tint = if (selected) Color.White else DarkGrey.copy(alpha = 0.4f),
                        modifier = Modifier.size(24.dp)
                    )
                }
                is String -> {
                    AsyncImage(
                        model = icon,
                        contentDescription = label,
                        modifier = Modifier.size(24.dp),
                        colorFilter = if (selected) androidx.compose.ui.graphics.ColorFilter.tint(Color.White) else androidx.compose.ui.graphics.ColorFilter.tint(DarkGrey.copy(alpha = 0.4f))
                    )
                }
            }
        }
        if (selected) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 8.sp,
                    letterSpacing = 1.sp
                ),
                color = MascotGreen,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
