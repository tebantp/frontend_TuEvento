package com.tuevento.tueventofinal.ui.login

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.tuevento.tueventofinal.data.model.RolUsuario
import com.tuevento.tueventofinal.ui.components.PremiumTextField
import com.tuevento.tueventofinal.ui.theme.DarkGrey
import com.tuevento.tueventofinal.ui.theme.MascotGreen
import com.tuevento.tueventofinal.ui.theme.BlobWhite
import com.tuevento.tueventofinal.util.ValidationUtils

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: RegisterViewModel
) {
    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var selectedRol by remember { mutableStateOf(RolUsuario.USUARIO) }

    var nombreError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var telefonoError by remember { mutableStateOf<String?>(null) }

    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkGrey)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            
            // Mascot integration in header
            Surface(
                modifier = Modifier.size(90.dp),
                shape = RoundedCornerShape(32.dp),
                color = BlobWhite,
                shadowElevation = 0.dp
            ) {
                AsyncImage(
                    model = "https://raw.githubusercontent.com/tebantp/frontend_TuEvento/main/Logo.png",
                    contentDescription = null,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Crea tu cuenta".uppercase(),
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            // Form Content
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 48.dp, topEnd = 48.dp),
                color = BlobWhite
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ELIGE TU ROL",
                        color = MascotGreen,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black, letterSpacing = 2.sp),
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        RolUsuario.entries.filter { it != RolUsuario.STAFF }.forEach { rol ->
                            val isSelected = selectedRol == rol
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedRol = rol },
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) MascotGreen else DarkGrey.copy(alpha = 0.05f),
                            ) {
                                Text(
                                    text = rol.name,
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        color = if (isSelected) Color.White else DarkGrey
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Column {
                        PremiumTextField(
                            value = nombre,
                            onValueChange = { 
                                nombre = it
                                nombreError = if (it.isBlank()) "El nombre es obligatorio" else null
                            },
                            label = "Nombre",
                            icon = Icons.Default.Person
                        )
                        nombreError?.let { Text(it, color = Color(0xFFFF4C4C), style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 16.dp)) }
                    }

                    PremiumTextField(
                        value = apellido,
                        onValueChange = { apellido = it },
                        label = "Apellido",
                        icon = Icons.Default.Badge
                    )

                    Column {
                        PremiumTextField(
                            value = email,
                            onValueChange = { 
                                email = it
                                emailError = when {
                                    !ValidationUtils.isValidEmail(it) -> "Formato de email inválido"
                                    (selectedRol == RolUsuario.ADMINISTRADOR || selectedRol == RolUsuario.ORGANIZADOR) && 
                                    !ValidationUtils.isUdecEmail(it) -> "Se requiere correo institucional"
                                    else -> null
                                }
                            },
                            label = "Correo Electrónico",
                            icon = Icons.Default.Email
                        )
                        emailError?.let { Text(it, color = Color(0xFFFF4C4C), style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 16.dp)) }
                    }

                    Column {
                        PremiumTextField(
                            value = password,
                            onValueChange = { 
                                password = it
                                passwordError = if (!ValidationUtils.isValidPassword(it)) "Mín. 8 caracteres" else null
                            },
                            label = "Contraseña",
                            icon = Icons.Default.Lock,
                            visualTransformation = PasswordVisualTransformation()
                        )
                        passwordError?.let { Text(it, color = Color(0xFFFF4C4C), style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 16.dp)) }
                    }

                    Column {
                        PremiumTextField(
                            value = telefono,
                            onValueChange = { 
                                if (it.length <= 10 && it.all { char -> char.isDigit() }) {
                                    telefono = it
                                    telefonoError = if (it.length < 10) "Mín. 10 dígitos" else null
                                }
                            },
                            label = "Teléfono",
                            icon = Icons.Default.Phone,
                            isNumber = true
                        )
                        telefonoError?.let { Text(it, color = Color(0xFFFF4C4C), style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 16.dp)) }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (uiState is RegisterState.Loading) {
                        CircularProgressIndicator(color = MascotGreen)
                    } else {
                        Button(
                            onClick = { 
                                val isBasicEmailValid = ValidationUtils.isValidEmail(email)
                                val isUdecValid = if (selectedRol == RolUsuario.ADMINISTRADOR || selectedRol == RolUsuario.ORGANIZADOR) {
                                    ValidationUtils.isUdecEmail(email)
                                } else true
                                
                                val isEmailValid = isBasicEmailValid && isUdecValid
                                val isPassValid = ValidationUtils.isValidPassword(password)
                                val isPhoneValid = ValidationUtils.isValidPhone(telefono)
                                val isNombreValid = nombre.isNotBlank()

                                if (isEmailValid && isPassValid && isPhoneValid && isNombreValid) {
                                    viewModel.register(nombre, apellido, email, password, telefono, selectedRol)
                                } else {
                                    nombreError = if (!isNombreValid) "Obligatorio" else null
                                    emailError = if (!isEmailValid) "Verifica email" else null
                                    passwordError = if (!isPassValid) "Contraseña débil" else null
                                    telefonoError = if (!isPhoneValid) "Teléfono inválido" else null
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp),
                            shape = RoundedCornerShape(32.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MascotGreen,
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                "COMENZAR",
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp,
                                fontSize = 16.sp
                            )
                        }
                    }

                    TextButton(onClick = onNavigateToLogin) {
                        Text(
                            "¿Ya eres miembro? Inicia sesión",
                            color = MascotGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (uiState is RegisterState.Error) {
                        Text(
                            text = (uiState as RegisterState.Error).message,
                            color = Color(0xFFFF4C4C),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is RegisterState.Success) {
            onRegisterSuccess()
        }
    }
}
