package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.RevelaRepository
import com.example.ui.theme.RevelaCoral
import com.example.ui.theme.RevelaPurple
import kotlinx.coroutines.launch

/**
 * Tela 8: Editar Perfil
 * Permite customizar Nome, Apelido único, Bio (limite 500 carac),
 * Gênero, Cidade/Estado, Sobre Mim longo e escolher até 3 vibes.
 * Inclui preview de fotos (Firebase Storage) e cálculo automático de idade.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val currentUser by RevelaRepository.currentUser.collectAsState()

    var name by remember { mutableStateOf(currentUser?.nome ?: "") }
    var username by remember { mutableStateOf(currentUser?.apelido ?: "") }
    var bio by remember { mutableStateOf(currentUser?.bio ?: "") }
    var gender by remember { mutableStateOf(currentUser?.genero ?: "") }
    var birthDate by remember { mutableStateOf(currentUser?.dataNascimento ?: "2000-01-01") }
    var city by remember { mutableStateOf(currentUser?.cidade ?: "") }
    var state by remember { mutableStateOf(currentUser?.estado ?: "") }
    var aboutMe by remember { mutableStateOf(currentUser?.sobreMim ?: "") }
    var selectedVibes by remember { mutableStateOf(currentUser?.vibes ?: emptyList()) }
    var photoUrl by remember { mutableStateOf(currentUser?.fotoPerfil ?: "") }

    var isLoading by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    val allVibes = listOf("🎵 Música", "🎮 Games", "📚 Leitura", "🏃 Esportes", "🎨 Arte", "✈️ Viagens", "🍕 Pizza", "🌱 Plantas", "☕ Café")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Perfil", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            isLoading = true
                            successMessage = ""
                            errorMessage = ""
                            coroutineScope.launch {
                                // Calcula idade automática simples baseado em ano de nascimento
                                val year = birthDate.split("-").firstOrNull()?.toIntOrNull() ?: 2000
                                val age = 2026 - year // Ano local definido no ADDITIONAL_METADATA é 2026

                                val result = RevelaRepository.updateProfile(
                                    nome = name,
                                    apelido = username,
                                    bio = bio,
                                    genero = gender,
                                    dataNascimento = birthDate,
                                    idade = age,
                                    cidade = city,
                                    estado = state,
                                    sobreMim = aboutMe,
                                    vibes = selectedVibes,
                                    fotoLocalPath = photoUrl
                                )
                                isLoading = false
                                if (result.isSuccess) {
                                    successMessage = "Perfil atualizado no Firebase com sucesso! ✨"
                                } else {
                                    errorMessage = result.exceptionOrNull()?.message ?: "Falha ao salvar."
                                }
                            }
                        },
                        modifier = Modifier.testTag("save_profile_button"),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = RevelaPurple)
                        } else {
                            Icon(Icons.Default.Save, contentDescription = "Salvar", tint = RevelaPurple)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (successMessage.isNotEmpty()) {
                Card(colors = CardDefaults.cardColors(containerColor = Color.Green.copy(alpha = 0.15f))) {
                    Text(text = successMessage, color = Color.Green, modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
                }
            }

            if (errorMessage.isNotEmpty()) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text(text = errorMessage, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.padding(12.dp))
                }
            }

            // Simulação de alteração de avatar
            Text("Foto de Perfil", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            OutlinedTextField(
                value = photoUrl,
                onValueChange = { photoUrl = it },
                label = { Text("URL da foto de perfil (Armazenada no Storage)") },
                modifier = Modifier.fillMaxWidth().testTag("avatar_url_input"),
                shape = RoundedCornerShape(12.dp)
            )

            // Preview rápido
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Text(
                    text = "Foto Atual:  ",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
                Button(
                    onClick = {
                        // Escolhe um avatar mockado rápido
                        photoUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=300"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RevelaCoral),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Usar Mock Avatar 👩", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nome Completo") },
                modifier = Modifier.fillMaxWidth().testTag("edit_name_input"),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Apelido / Username (Exclusivo)") },
                leadingIcon = { Text(" @ ", fontWeight = FontWeight.Bold) },
                modifier = Modifier.fillMaxWidth().testTag("edit_username_input"),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = bio,
                onValueChange = { if (it.length <= 500) bio = it },
                label = { Text("Biografia curta (Até 500 caracteres)") },
                modifier = Modifier.fillMaxWidth().height(100.dp).testTag("edit_bio_input"),
                shape = RoundedCornerShape(12.dp),
                maxLines = 4
            )
            Text(
                text = "${bio.length}/500 caracteres",
                fontSize = 11.sp,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.End)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = gender,
                    onValueChange = { gender = it },
                    label = { Text("Gênero") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = birthDate,
                    onValueChange = { birthDate = it },
                    label = { Text("Nascimento (AAAA-MM-DD)") },
                    modifier = Modifier.weight(1.2f),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("Cidade") },
                    modifier = Modifier.weight(1.2f),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = state,
                    onValueChange = { state = it },
                    label = { Text("UF") },
                    modifier = Modifier.weight(0.6f),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            OutlinedTextField(
                value = aboutMe,
                onValueChange = { aboutMe = it },
                label = { Text("Sobre Mim (Texto longo)") },
                modifier = Modifier.fillMaxWidth().height(120.dp).testTag("edit_about_input"),
                shape = RoundedCornerShape(12.dp),
                maxLines = 6
            )

            // Seletor de Vibes (Até 3 escolhas)
            Text("Minhas Vibes (Escolha até 3)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                allVibes.forEach { vibe ->
                    val isSelected = selectedVibes.contains(vibe)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (isSelected) {
                                selectedVibes = selectedVibes - vibe
                            } else {
                                if (selectedVibes.size < 3) {
                                    selectedVibes = selectedVibes + vibe
                                }
                            }
                        },
                        label = { Text(vibe, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RevelaPurple,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
