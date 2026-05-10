package com.tuevento.tueventofinal.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.tuevento.tueventofinal.ui.theme.DarkGrey
import com.tuevento.tueventofinal.ui.theme.MascotGreen

@Composable
fun PremiumTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    isNumber: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    onDarkBackground: Boolean = false
) {
    val textColor = if (onDarkBackground) Color.White else DarkGrey
    val labelColor = if (onDarkBackground) Color.White.copy(alpha = 0.5f) else DarkGrey.copy(alpha = 0.6f)
    val containerColor = if (onDarkBackground) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.03f)
    val borderColor = if (onDarkBackground) Color.White.copy(alpha = 0.2f) else DarkGrey.copy(alpha = 0.1f)

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, style = MaterialTheme.typography.bodyMedium) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = MascotGreen) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MascotGreen,
            unfocusedBorderColor = borderColor,
            focusedLabelColor = MascotGreen,
            unfocusedLabelColor = labelColor,
            focusedTextColor = textColor,
            unfocusedTextColor = textColor,
            cursorColor = MascotGreen,
            focusedContainerColor = containerColor,
            unfocusedContainerColor = containerColor
        ),
        singleLine = singleLine,
        visualTransformation = visualTransformation,
        keyboardOptions = if (isNumber) KeyboardOptions(keyboardType = KeyboardType.Number) else KeyboardOptions.Default
    )
}
