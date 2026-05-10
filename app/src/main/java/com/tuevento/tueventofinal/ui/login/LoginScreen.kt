package com.tuevento.tueventofinal.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.tuevento.tueventofinal.data.model.UsuarioResponse
import com.tuevento.tueventofinal.ui.components.PremiumTextField
import com.tuevento.tueventofinal.ui.theme.DarkGrey
import com.tuevento.tueventofinal.ui.theme.MascotGreen
import com.tuevento.tueventofinal.ui.theme.BlobWhite

@Composable
fun LoginScreen(
    onLoginSuccess: (UsuarioResponse) -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: LoginViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkGrey)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            
            // Logo Container
            Surface(
                modifier = Modifier.size(110.dp),
                shape = RoundedCornerShape(40.dp),
                color = BlobWhite,
                shadowElevation = 0.dp
            ) {
                AsyncImage(
                    model = "https://raw.githubusercontent.com/tebantp/frontend_TuEvento/main/Logo.png",
                    contentDescription = null,
                    modifier = Modifier.padding(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "TUEVENTO",
                style = MaterialTheme.typography.displaySmall.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 6.sp
                )
            )

            Text(
                text = "Inicia sesión para continuar",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp
                )
            )
            
            Spacer(modifier = Modifier.height(48.dp))

            // Login Card with Organic Corners
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                color = BlobWhite
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text(
                        text = "BIENVENIDO",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = DarkGrey,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                    )
                    
                    PremiumTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Email",
                        icon = Icons.Default.Email
                    )

                    PremiumTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Contraseña",
                        icon = Icons.Default.Lock,
                        visualTransformation = PasswordVisualTransformation()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (uiState is LoginState.Loading) {
                        CircularProgressIndicator(color = MascotGreen)
                    } else {
                        Button(
                            onClick = { viewModel.login(email, password) },
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
                                "ENTRAR",
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp,
                                fontSize = 16.sp
                            )
                        }
                    }

                    // BOTÓN DE REGISTRO VISIBLE (FIX PROBLEMA 1)
                    TextButton(
                        onClick = onNavigateToRegister,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(
                            "¿No tienes cuenta? Regístrate",
                            color = MascotGreen,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }

                    if (uiState is LoginState.Error) {
                        Text(
                            text = (uiState as LoginState.Error).message,
                            color = Color(0xFFFF4C4C),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is LoginState.Success) {
            onLoginSuccess((uiState as LoginState.Success).user)
        }
    }
}
