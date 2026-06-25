package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.data.RevelaRepository
import com.example.ui.theme.RevelaCoral
import com.example.ui.theme.RevelaPurple
import com.example.ui.theme.RevelaYellow
import kotlinx.coroutines.launch

/**
 * Tela 9: Postar Foto
 * Permite simular câmera ou galeria, escrever legendas, marcar amigos usando @,
 * ativar/desativar comentários anônimos no post e aplicar 5 filtros de imagem
 * real-time baseados em ColorMatrix de Compose.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostScreen(
    onNavigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    var imagePresetUrl by remember { mutableStateOf("https://images.unsplash.com/photo-1501854140801-50d01698950b?w=600") }
    var caption by remember { mutableStateOf("") }
    var permiteAnonimos by remember { mutableStateOf(true) }
    var selectedFilter by remember { mutableStateOf("Normal") }

    var isLoading by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    // Prepara as matrizes de cor para cada um dos 5 filtros de imagem
    val colorFiltersMap = remember {
        mapOf(
            "Normal" to ColorMatrix(),
            "Vintage" to ColorMatrix(floatArrayOf(
                0.9f, 0.1f, 0.1f, 0f, 0f,
                0.1f, 0.8f, 0.1f, 0f, 0f,
                0.1f, 0.1f, 0.5f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )),
            "P&B" to ColorMatrix().apply { setToSaturation(0f) },
            "Quente" to ColorMatrix(floatArrayOf(
                1.2f, 0.1f, 0f, 0f, 0f,
                0.1f, 0.9f, 0f, 0f, 0f,
                0f, 0f, 0.7f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )),
            "Frio" to ColorMatrix(floatArrayOf(
                0.8f, 0f, 0f, 0f, 0f,
                0f, 1.0f, 0.1f, 0f, 0f,
                0f, 0.1f, 1.3f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            ))
        )
    }

    // Presets rápidos de fotos legais da galeria/câmera
    val presets = listOf(
        "https://images.unsplash.com/photo-1501854140801-50d01698950b?w=600",
        "https://images.unsplash.com/photo-1447752875215-b2761acb3c5d?w=600",
        "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?w=600",
        "https://images.unsplash.com/photo-1472214222541-d510753a4707?w=600"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Novo Post 📸", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            isLoading = true
                            isError = false
                            statusMessage = ""
                            coroutineScope.launch {
                                val result = RevelaRepository.createPost(
                                    imagemUrl = imagePresetUrl,
                                    legenda = caption,
                                    filterName = selectedFilter,
                                    permiteAnonimos = permiteAnonimos
                                )
                                isLoading = false
                                if (result.isSuccess) {
                                    statusMessage = "Post publicado com sucesso! Confira no Feed."
                                    caption = ""
                                    // Volta após sucesso
                                    kotlinx.coroutines.delay(1200)
                                    onNavigateBack()
                                } else {
                                    isError = true
                                    statusMessage = result.exceptionOrNull()?.message ?: "Erro desconhecido."
                                }
                            }
                        },
                        modifier = Modifier.testTag("publish_post_button"),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = RevelaPurple)
                        } else {
                            Icon(Icons.Default.Check, contentDescription = "Publicar", tint = RevelaPurple)
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
            if (statusMessage.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isError) MaterialTheme.colorScheme.errorContainer else Color.Green.copy(alpha = 0.15f)
                    )
                ) {
                    Text(
                        text = statusMessage,
                        color = if (isError) MaterialTheme.colorScheme.onErrorContainer else Color.Green,
                        modifier = Modifier.padding(12.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Preview da Imagem com o filtro selecionado aplicado real-time
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = rememberAsyncImagePainter(imagePresetUrl),
                    contentDescription = "Post Preview",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    colorFilter = ColorFilter.colorMatrix(colorFiltersMap[selectedFilter] ?: ColorMatrix())
                )

                // Selo de visualização rápida
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("Filtro: $selectedFilter", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Botão para simular captura da Câmera / Galeria
            Text("Simular captura de mídia (Câmera/Galeria)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                presets.forEachIndexed { i, presetUrl ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1.2f)
                            .clip(RoundedCornerShape(10.dp))
                            .border(
                                width = if (imagePresetUrl == presetUrl) 3.dp else 1.dp,
                                color = if (imagePresetUrl == presetUrl) RevelaPurple else Color.Gray.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { imagePresetUrl = presetUrl }
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(presetUrl),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Preset ${i + 1}", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            HorizontalDivider(color = Color.Gray.copy(alpha = 0.15f))

            // Seletor de 5 Filtros básicos
            Text("Aplicar Filtros básicos (5 Opções)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                colorFiltersMap.keys.forEach { filterName ->
                    val isSelected = selectedFilter == filterName
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                color = if (isSelected) RevelaPurple else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedFilter = filterName }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = filterName,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            HorizontalDivider(color = Color.Gray.copy(alpha = 0.15f))

            // Campo de Legenda e Marcação
            OutlinedTextField(
                value = caption,
                onValueChange = { caption = it },
                label = { Text("Legenda da foto (Marque amigos usando @)") },
                modifier = Modifier.fillMaxWidth().height(100.dp).testTag("post_caption_input"),
                placeholder = { Text("Ex: Vibe incrível na praia com @MariG! 🌴☀️") },
                shape = RoundedCornerShape(12.dp)
            )

            // Configuração de Privacidade do Post
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Permitir Comentários Anônimos", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            text = "Se ativo, outros usuários poderão comentar usando identidades anônimas (🎭).",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Switch(
                        checked = permiteAnonimos,
                        onCheckedChange = { permiteAnonimos = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = RevelaYellow)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
