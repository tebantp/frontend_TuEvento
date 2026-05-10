package com.tuevento.tueventofinal.ui.perfil

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.tuevento.tueventofinal.data.model.UsuarioRequest
import com.tuevento.tueventofinal.ui.components.PremiumTextField
import com.tuevento.tueventofinal.ui.theme.*
import com.tuevento.tueventofinal.util.ValidationUtils
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToInscripciones: (Long) -> Unit,
    viewModel: PerfilViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val usuario by viewModel.usuario.collectAsState()
    val context = LocalContext.current

    var nombre by remember(usuario) { mutableStateOf(usuario?.nombre ?: "") }
    var apellido by remember(usuario) { mutableStateOf(usuario?.apellido ?: "") }
    var email by remember(usuario) { mutableStateOf(usuario?.email ?: "") }
    var telefono by remember(usuario) { mutableStateOf(usuario?.telefono ?: "") }
    var password by remember { mutableStateOf("") }

    var emailError by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let {
                val file = uriToFile(it, context)
                if (file != null && usuario != null) {
                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                    val body = MultipartBody.Part.createFormData("foto", file.name, requestFile)
                    viewModel.subirFoto(usuario!!.id, body)
                }
            }
        }
    )

    LaunchedEffect(uiState) {
        if (uiState is PerfilState.Success) {
            viewModel.resetState()
        }
    }

    Scaffold(
        containerColor = DarkGrey,
        topBar = {
            TopAppBar(
                title = {},
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
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.logout()
                            onLogout()
                        },
                        modifier = Modifier
                            .padding(8.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.3f))
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Cerrar Sesión",
                            tint = Color(0xFFFF6B6B)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        val scrollState = rememberScrollState()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding())
        ) {
            // Organic Background Blobs
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
                    .offset(y = (-80).dp - (scrollState.value / 4).dp)
                    .clip(RoundedCornerShape(bottomStart = 120.dp, bottomEnd = 120.dp))
                    .background(MascotGreen)
            )

            Box(
                modifier = Modifier
                    .size(300.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 100.dp, y = 100.dp + (scrollState.value / 6).dp)
                    .clip(CircleShape)
                    .background(MascotGreen.copy(alpha = 0.05f))
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(40.dp))

                // Profile Photo Section with Organic Frame
                Box(
                    modifier = Modifier.size(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Blob background for photo
                    Surface(
                        modifier = Modifier.size(180.dp),
                        shape = RoundedCornerShape(60.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {}

                    Surface(
                        modifier = Modifier.size(160.dp),
                        shape = RoundedCornerShape(54.dp),
                        color = BlobWhite,
                        shadowElevation = 0.dp
                    ) {
                        AsyncImage(
                            model = if (usuario?.photoUrl.isNullOrEmpty())
                                "https://ui-avatars.com/api/?name=${usuario?.nombre}+${usuario?.apellido}&background=4CAF50&color=FFFFFF"
                            else usuario?.photoUrl,
                            contentDescription = "Foto de perfil",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Mascot Cat Peeking
                    AsyncImage(
                        model = "https://raw.githubusercontent.com/tebantp/frontend_TuEvento/main/Logo.png",
                        contentDescription = null,
                        modifier = Modifier
                            .size(70.dp)
                            .align(Alignment.BottomEnd)
                            .offset(x = 10.dp, y = 10.dp)
                            .clip(CircleShape)
                            .background(BlobWhite)
                            .padding(8.dp)
                    )

                    // Edit Button
                    IconButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = (-10).dp, y = 10.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Cambiar foto", tint = MascotGreen, modifier = Modifier.size(20.dp))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${usuario?.nombre} ${usuario?.apellido}".uppercase(),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                    )
                    Surface(
                        color = Color.Black.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        val roleText = when (usuario?.rol) {
                            com.tuevento.tueventofinal.data.model.RolUsuario.ADMINISTRADOR -> "ADMINISTRADOR"
                            com.tuevento.tueventofinal.data.model.RolUsuario.ORGANIZADOR -> "ORGANIZADOR"
                            com.tuevento.tueventofinal.data.model.RolUsuario.STAFF -> "STAFF"
                            else -> "USUARIO"
                        }
                        Text(
                            text = roleText,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Action Buttons Row (Like in the image's dashboard)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    usuario?.let { u ->
                        ProfileQuickAction(
                            label = "Entradas",
                            icon = Icons.Default.ConfirmationNumber,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateToInscripciones(u.id) }
                        )
                    }
                    ProfileQuickAction(
                        label = "Eventos",
                        icon = Icons.Default.Event,
                        modifier = Modifier.weight(1f),
                        onClick = { /* Navigate to history */ }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Form Container
                Surface(
                    color = BlobWhite,
                    shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Text(
                            "INFORMACIÓN PERSONAL",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = MascotGreen,
                                letterSpacing = 2.sp
                            )
                        )

                        PremiumTextField(value = nombre, onValueChange = { nombre = it }, label = "Nombre", icon = Icons.Default.Person, onDarkBackground = false)
                        PremiumTextField(value = apellido, onValueChange = { apellido = it }, label = "Apellido", icon = Icons.Default.Badge, onDarkBackground = false)
                        
                        Column {
                            PremiumTextField(
                                value = email,
                                onValueChange = {
                                    email = it
                                    emailError = when {
                                        !ValidationUtils.isValidEmail(it) -> "Formato de email inválido"
                                        (usuario?.rol == com.tuevento.tueventofinal.data.model.RolUsuario.ADMINISTRADOR ||
                                                usuario?.rol == com.tuevento.tueventofinal.data.model.RolUsuario.ORGANIZADOR) &&
                                                !ValidationUtils.isUdecEmail(it) -> "Se requiere correo institucional"
                                        else -> null
                                    }
                                },
                                label = "Email",
                                icon = Icons.Default.Email,
                                onDarkBackground = false
                            )
                            emailError?.let { Text(it, color = Color.Red, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 16.dp)) }
                        }
                        
                        PremiumTextField(value = telefono, onValueChange = { telefono = it }, label = "Teléfono", icon = Icons.Default.Phone, onDarkBackground = false)
                        PremiumTextField(
                            value = password, 
                            onValueChange = { password = it }, 
                            label = "Nueva Contraseña (Opcional)", 
                            icon = Icons.Default.Lock,
                            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                            onDarkBackground = false
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                val isBasicEmailValid = ValidationUtils.isValidEmail(email)
                                val isUdecValid = if (usuario?.rol == com.tuevento.tueventofinal.data.model.RolUsuario.ADMINISTRADOR ||
                                    usuario?.rol == com.tuevento.tueventofinal.data.model.RolUsuario.ORGANIZADOR
                                ) {
                                    ValidationUtils.isUdecEmail(email)
                                } else true

                                if (isBasicEmailValid && isUdecValid) {
                                    val request = UsuarioRequest(
                                        nombre = nombre,
                                        apellido = apellido,
                                        email = email,
                                        password = password.ifEmpty { "no_change" },
                                        telefono = telefono
                                    )
                                    usuario?.let { viewModel.actualizarPerfil(it.id, request) }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            shape = RoundedCornerShape(30.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MascotGreen,
                                contentColor = Color.White
                            ),
                            enabled = uiState !is PerfilState.Loading
                        ) {
                            if (uiState is PerfilState.Loading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Text("GUARDAR PERFIL", fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                            }
                        }

                        if (uiState is PerfilState.Error) {
                            Text(
                                text = (uiState as PerfilState.Error).message,
                                color = Color(0xFFFF4C4C),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 8.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (uiState is PerfilState.Success) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MascotGreen.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MascotGreen)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "¡Perfil actualizado!",
                                    color = MascotGreen,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileQuickAction(label: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        color = Color.White.copy(alpha = 0.1f),
        shape = RoundedCornerShape(32.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, color = Color.White, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
    }
}

private fun uriToFile(uri: Uri, context: android.content.Context): File? {
    val contentResolver = context.contentResolver ?: return null
    val filePath = context.applicationInfo.dataDir + File.separator + "temp_image_${System.currentTimeMillis()}.jpg"
    val file = File(filePath)
    try {
        val inputStream = contentResolver.openInputStream(uri) ?: return null
        val outputStream = FileOutputStream(file)
        val buffer = ByteArray(1024)
        var len: Int
        while (inputStream.read(buffer).also { len = it } > 0) {
            outputStream.write(buffer, 0, len)
        }
        outputStream.close()
        inputStream.close()
        return file
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}
