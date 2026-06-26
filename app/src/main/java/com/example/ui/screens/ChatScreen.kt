package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.data.ChatMessage
import com.example.data.Conversation
import com.example.data.RevelaRepository
import com.example.data.UserProfile
import com.example.data.CryptoUtils
import com.example.ui.theme.RevelaCoral
import com.example.ui.theme.RevelaPurple
import com.example.ui.theme.RevelaTurquoise
import com.example.ui.theme.RevelaYellow
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.Date
import androidx.compose.ui.platform.LocalContext
import java.io.File
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager

/**
 * Tela 7: Chat / Conversa Individual
 * O coração do aplicativo Revela. Apresenta bolhas normais e anônimas,
 * indicador de digitando real-time, envio de áudio simulado, sistema de denúncia/bloqueio,
 * e a mecânica de revelação com animação Match! estilo Tinder após 5 mensagens.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChatScreen(
    conversaId: String,
    onNavigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val conversations by RevelaRepository.conversations.collectAsState()
    val messagesMap by RevelaRepository.messages.collectAsState()
    val users by RevelaRepository.users.collectAsState()
    val currentUser by RevelaRepository.currentUser.collectAsState()
    val typingState by RevelaRepository.typingState.collectAsState()

    val conversa = conversations.find { it.conversaId == conversaId }
    val mensagens = messagesMap[conversaId] ?: emptyList()
    val isTyping = typingState[conversaId] ?: false

    // Busca o outro participante da conversa
    val outroParticipanteId = conversa?.participantes?.find { it != currentUser?.uid } ?: ""
    val outroUser = users[outroParticipanteId]

    var typedText by remember { mutableStateOf("") }
    var isRecordingAudio by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val audioRecorder = remember { AudioRecorder(context) }
    val audioPlayer = remember { AudioPlayer() }
    var currentlyPlayingPath by remember { mutableStateOf<String?>(null) }
    var currentAudioFile by remember { mutableStateOf<File?>(null) }

    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasMicPermission = isGranted
            if (!isGranted) {
                Toast.makeText(context, "Permissão do microfone negada. Não é possível gravar áudio real.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Permissão de microfone concedida! Pressione para gravar.", Toast.LENGTH_SHORT).show()
            }
        }
    )

    // Para de tocar áudio quando sair da tela
    DisposableEffect(Unit) {
        onDispose {
            audioPlayer.stop()
            audioRecorder.stop()
        }
    }

    // Diálogos e Modais
    var showReportDialog by remember { mutableStateOf<ChatMessage?>(null) }
    var showMatchDialog by remember { mutableStateOf(false) }
    var isBlocked by remember { mutableStateOf(false) }

    // Novas Variáveis de Funcionalidades do Chat
    var replyMessage by remember { mutableStateOf<ChatMessage?>(null) }
    var showChatActions by remember { mutableStateOf(false) }
    var showPollCreator by remember { mutableStateOf(false) }
    var showImageShareSheet by remember { mutableStateOf(false) }
    var selectedMessageForMenu by remember { mutableStateOf<ChatMessage?>(null) }
    var showVoiceCallMock by remember { mutableStateOf(false) }
    var showInfoTrustDialog by remember { mutableStateOf(false) }

    var showFlameEntrance by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (conversa != null && conversa.streakCount > 0) {
            showFlameEntrance = true
            kotlinx.coroutines.delay(2200)
            showFlameEntrance = false
        }
    }

    // Rolagem automática para a última mensagem
    LaunchedEffect(mensagens.size, isTyping) {
        if (mensagens.isNotEmpty()) {
            listState.animateScrollToItem(mensagens.size - 1)
        }
    }

    // Monitora se o Match foi liberado na conversa
    LaunchedEffect(conversa?.matchRevelado) {
        if (conversa?.matchRevelado == true) {
            showMatchDialog = true
        }
    }

    if (conversa == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Conversa não encontrada.")
        }
        return
    }

    val isAnonima = conversa.tipo == "anonimo"
    val trustLevel = conversa.trustLevel

    // TikTok-style Fire Streak (Foguinho) Config
    val streak = conversa.streakCount
    val streakColor = when {
        streak >= 15 -> Color(0xFF9D4EDD) // Cosmic Blue/Purple
        streak >= 8 -> RevelaCoral // Active Coral/Red
        streak >= 3 -> Color(0xFFFFB703) // Bright Yellow/Orange
        else -> Color.Gray
    }
    val streakIcon = when {
        streak >= 15 -> "🔮"
        streak >= 8 -> "💥"
        streak >= 3 -> "🔥"
        else -> "💨"
    }

    // Animação infinita da chama (Foguinho pulsante/vibrante - Requisito 3)
    val infiniteTransition = rememberInfiniteTransition(label = "flame_pulse")
    val flameScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // Se o outro usuário foi bloqueado, esconde envio
    val hasBlockedCurrent = currentUser?.bloqueados?.contains(outroParticipanteId) == true
    val showRevealButton = trustLevel >= 5 && isAnonima && !conversa.matchRevelado

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isAnonima) RevelaYellow.copy(alpha = 0.2f) else RevelaTurquoise.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isAnonima) {
                                Text("🎭", fontSize = 20.sp)
                            } else {
                                if (outroUser?.fotoPerfil?.isNotEmpty() == true) {
                                    Image(
                                        painter = rememberAsyncImagePainter(outroUser.fotoPerfil),
                                        contentDescription = outroUser.nome,
                                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Text(
                                        text = outroUser?.nome?.firstOrNull()?.toString()?.uppercase() ?: "👤",
                                        fontWeight = FontWeight.Bold,
                                        color = RevelaTurquoise
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isAnonima) "Alguém Anônimo" else (outroUser?.nome ?: "Carregando..."),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = if (isAnonima) RevelaYellow else MaterialTheme.colorScheme.onBackground
                                )
                                
                                if (streak > 0) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .scale(if (streak >= 3) flameScale else 1.0f)
                                            .background(streakColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(text = "$streakIcon $streak", fontSize = 10.sp, color = streakColor, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            
                            if (isTyping) {
                                Text(
                                    text = "digitando...",
                                    fontSize = 11.sp,
                                    color = RevelaPurple,
                                    fontWeight = FontWeight.Medium
                                )
                            } else {
                                Text(
                                    text = if (isAnonima) "Máscara ativa • AES-128 Ativo 🔒" else "AES-128 Criptografado de ponta a ponta 🔒",
                                    fontSize = 10.sp,
                                    color = if (isAnonima) RevelaYellow else RevelaTurquoise
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    if (isAnonima && !conversa.matchRevelado) {
                        IconButton(onClick = { showInfoTrustDialog = true }) {
                            Icon(Icons.Default.Info, contentDescription = "Níveis de Confiança", tint = RevelaYellow)
                        }
                    }
                    if (conversa.matchRevelado) {
                        IconButton(onClick = { showVoiceCallMock = true }) {
                            Icon(Icons.Default.Phone, contentDescription = "Ligação de Voz", tint = Color.Green)
                        }
                    }
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                RevelaRepository.blockUser(outroParticipanteId)
                                isBlocked = true
                            }
                        }
                    ) {
                        Icon(Icons.Default.Block, contentDescription = "Bloquear", tint = RevelaCoral)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                
                // BARRA DE PROGRESSO DE CONFIANÇA GRADUAL (Requisito 1: Modo Anônimo Diferencial)
                if (isAnonima && !conversa.matchRevelado) {
                    val progressValue = trustLevel / 5f
                    val revealInfoText = when (trustLevel) {
                        1 -> "🔒 Nv. 1: Identidade e perfil completamente ocultos."
                        2 -> "🔓 Nv. 2: Cidade e Idade desbloqueadas! (${outroUser?.cidade ?: "SP"}, ${outroUser?.idade ?: 22} anos)"
                        3 -> "🔓 Nv. 3: Vibes e Interesses revelados! (${outroUser?.interesses?.take(3)?.joinToString(", ") ?: ""})"
                        4 -> "🔓 Nv. 4: Bio & Sobre Mim visíveis! (\"${outroUser?.bio ?: ""}\")"
                        else -> "🔓 Nv. 5: Pedido de Revelação pronto para envio! 🎉"
                    }
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Nível de Confiança: $trustLevel/5",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = RevelaYellow
                            )
                            val compatScore = RevelaRepository.getCompatibilityScore(currentUser, outroUser)
                            Text(
                                text = "Compatibilidade: $compatScore%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = RevelaTurquoise
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { progressValue },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = RevelaYellow,
                            trackColor = Color.Gray.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = revealInfoText,
                            fontSize = 10.sp,
                            color = Color.LightGray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // AVISO DE EXPIRAÇÃO DE STREAK (Requisito 3 & 4: Gamificação e Foguinhos)
                if (conversa.streakExpiring && conversa.streakCount > 0) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = RevelaCoral.copy(alpha = 0.15f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RevelaCoral.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🔥",
                                fontSize = 24.sp,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Column {
                                Text(
                                    text = "Sua sequência de ${conversa.streakCount} dias está prestes a acabar! Envie uma mensagem para mantê-la 🔥",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Falta pouco tempo! Não deixe sua chama apagar.",
                                    fontSize = 10.sp,
                                    color = Color.LightGray
                                )
                            }
                        }
                    }
                }

                // Lista de Mensagens
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(mensagens) { msg ->
                        val isMe = msg.remetenteId == currentUser?.uid
                        val decryptedConteudo = CryptoUtils.decrypt(msg.conteudo)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                        ) {
                            if (!isMe && (msg.isAnonimo || msg.remetenteId == null)) {
                                Text("🎭", fontSize = 20.sp, modifier = Modifier.padding(end = 6.dp, top = 8.dp))
                            }

                            Column(
                                horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                            ) {
                                // Exibe se for resposta a uma mensagem específica
                                if (msg.replyToId != null) {
                                    Box(
                                        modifier = Modifier
                                            .padding(bottom = 2.dp)
                                            .background(Color.Gray.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "↩️ Resposta: ${msg.replyToText?.let { CryptoUtils.decrypt(it) } ?: ""}",
                                            fontSize = 11.sp,
                                            color = Color.Gray,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .widthIn(max = 260.dp)
                                        .background(
                                            color = when {
                                                isMe -> RevelaPurple
                                                msg.isAnonimo -> RevelaYellow.copy(alpha = 0.15f)
                                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                            },
                                            shape = RoundedCornerShape(
                                                topStart = 16.dp,
                                                topEnd = 16.dp,
                                                bottomStart = if (isMe) 16.dp else 0.dp,
                                                bottomEnd = if (isMe) 0.dp else 16.dp
                                            )
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = when {
                                                isMe -> Color.Transparent
                                                msg.isAnonimo -> RevelaYellow.copy(alpha = 0.25f)
                                                else -> Color.White.copy(alpha = 0.05f)
                                            },
                                            shape = RoundedCornerShape(
                                                topStart = 16.dp,
                                                topEnd = 16.dp,
                                                bottomStart = if (isMe) 16.dp else 0.dp,
                                                bottomEnd = if (isMe) 0.dp else 16.dp
                                            )
                                        )
                                        .combinedClickable(
                                            onClick = {
                                                selectedMessageForMenu = msg
                                            },
                                            onLongClick = {
                                                selectedMessageForMenu = msg
                                            }
                                        )
                                        .padding(12.dp)
                                ) {
                                    Column {
                                        when (msg.tipo) {
                                            "audio" -> {
                                                val isPlayingThis = currentlyPlayingPath == msg.audioUrl
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier
                                                        .clickable {
                                                            if (msg.audioUrl.isNotEmpty() && !msg.audioUrl.contains("simulated_audio.mp3")) {
                                                                if (isPlayingThis) {
                                                                    audioPlayer.stop()
                                                                    currentlyPlayingPath = null
                                                                } else {
                                                                    currentlyPlayingPath = msg.audioUrl
                                                                    audioPlayer.play(msg.audioUrl) {
                                                                        currentlyPlayingPath = null
                                                                    }
                                                                }
                                                            } else {
                                                                Toast.makeText(context, "Este é um áudio de simulação.", Toast.LENGTH_SHORT).show()
                                                            }
                                                        }
                                                        .padding(vertical = 4.dp)
                                                ) {
                                                    Icon(
                                                         imageVector = if (isPlayingThis) Icons.Default.Pause else Icons.Default.PlayArrow,
                                                         contentDescription = if (isPlayingThis) "Pause" else "Play",
                                                         tint = if (isMe) Color.White else RevelaPurple
                                                     )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Column {
                                                         Text(
                                                             text = if (isPlayingThis) "▶️ Tocando Áudio..." else "🎤 Mensagem de Voz",
                                                             fontSize = 13.sp,
                                                             fontWeight = FontWeight.Bold,
                                                             color = if (isMe) Color.White else MaterialTheme.colorScheme.onBackground
                                                         )
                                                         Text(
                                                             text = if (msg.audioUrl.contains("simulated_audio.mp3")) "Áudio Simulado" else "Toque para ouvir gravação real",
                                                             fontSize = 10.sp,
                                                             color = if (isMe) Color.White.copy(alpha = 0.7f) else Color.Gray
                                                         )
                                                     }
                                                }
                                            }
                                            "enquete" -> {
                                                Column(modifier = Modifier.padding(4.dp)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(Icons.Default.Poll, contentDescription = "Enquete", tint = if (isMe) Color.White else RevelaYellow)
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(
                                                            text = msg.pollQuestion ?: "Enquete",
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 14.sp,
                                                            color = if (isMe) Color.White else MaterialTheme.colorScheme.onBackground
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    msg.pollOptions.forEachIndexed { idx, opt ->
                                                        val optionVotes = msg.pollVotes.values.count { it == idx }
                                                        val totalVotes = msg.pollVotes.size.coerceAtLeast(1)
                                                        val percentage = (optionVotes.toFloat() / totalVotes * 100).toInt()
                                                        val userVotedThis = msg.pollVotes[currentUser?.uid] == idx

                                                        Card(
                                                            colors = CardDefaults.cardColors(
                                                                containerColor = if (userVotedThis) RevelaPurple.copy(alpha = 0.3f) else Color.Gray.copy(alpha = 0.1f)
                                                            ),
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(vertical = 4.dp)
                                                                .clickable {
                                                                    coroutineScope.launch {
                                                                        RevelaRepository.voteInPoll(conversaId, msg.mensagemId, idx)
                                                                    }
                                                                },
                                                            shape = RoundedCornerShape(8.dp)
                                                        ) {
                                                            Row(
                                                                modifier = Modifier.padding(8.dp),
                                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Text(text = opt, fontSize = 12.sp, modifier = Modifier.weight(1f), color = if (isMe) Color.White else MaterialTheme.colorScheme.onBackground)
                                                                Text(text = "$percentage% ($optionVotes)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RevelaCoral)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            "imagem_preset" -> {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    val cardUrl = msg.audioUrl // repurposing audioUrl to store the preset card type
                                                    val cardName = when (cardUrl) {
                                                        "aesthetic_cafe" -> "☕ Café Estético"
                                                        "cyberpunk_vibes" -> "🌃 Noite Cyberpunk"
                                                        "cozy_sunset" -> "🌅 Pôr do Sol Aconchegante"
                                                        else -> "✨ Momento Mágico"
                                                    }
                                                    Box(
                                                        modifier = Modifier
                                                            .size(160.dp)
                                                            .clip(RoundedCornerShape(12.dp))
                                                            .background(Color.DarkGray),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text("🖼️ $cardName", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                    }
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(text = decryptedConteudo, fontSize = 11.sp, color = if (isMe) Color.White else MaterialTheme.colorScheme.onBackground)
                                                }
                                            }
                                            else -> {
                                                Text(
                                                    text = decryptedConteudo,
                                                    color = if (isMe) Color.White else MaterialTheme.colorScheme.onBackground,
                                                    fontSize = 14.sp
                                                )
                                            }
                                        }
                                    }
                                }

                                // Renderização das Reações das Mensagens
                                if (msg.reactions.isNotEmpty()) {
                                    Row(
                                        modifier = Modifier
                                            .padding(top = 2.dp)
                                            .background(Color.DarkGray.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        msg.reactions.values.distinct().forEach { reactionEmoji ->
                                            Text(text = reactionEmoji, fontSize = 12.sp)
                                        }
                                        if (msg.reactions.size > 1) {
                                            Text(text = "${msg.reactions.size}", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                if (!isMe && msg.isAnonimo) {
                                    Text(
                                        text = "Denunciar Mensagem",
                                        fontSize = 9.sp,
                                        color = RevelaCoral,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .padding(top = 2.dp, start = 4.dp)
                                            .clickable { showReportDialog = msg }
                                    )
                                }
                            }
                        }
                    }

                    // Indicador de "digitando..."
                    if (isTyping) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                                        .padding(10.dp)
                                ) {
                                    Text("... Mariana está digitando ...", fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }

                // REQUISITO 5: MODO SURPRESA - BOTÃO FLUTUANTE REVELAR
                AnimatedVisibility(
                    visible = showRevealButton,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    val euJáRevelei = if (conversa.participantes[0] == currentUser?.uid) conversa.revelouUid1 else conversa.revelouUid2
                    val outroJáRevelou = if (conversa.participantes[0] == currentUser?.uid) conversa.revelouUid2 else conversa.revelouUid1

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                            .testTag("reveal_identity_banner"),
                        colors = CardDefaults.cardColors(containerColor = RevelaYellow),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "✨ MODO SURPRESA ATIVADO! ✨",
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (outroJáRevelou) "O outro participante QUER REVELAR a identidade!" else "Vocês já acumularam confiança! Deseja revelar sua identidade?",
                                fontSize = 12.sp,
                                color = Color.Black.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                            )

                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        RevelaRepository.requestIdentityReveal(conversaId)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = RevelaPurple),
                                shape = RoundedCornerShape(8.dp),
                                enabled = !euJáRevelei
                            ) {
                                Text(
                                    text = if (euJáRevelei) "Aguardando o outro aceitar..." else "Revelar quem sou! 🎭",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Caixa Informativa de Resposta a Mensagem Específica
                if (replyMessage != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.DarkGray.copy(alpha = 0.3f))
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Reply, contentDescription = "Respondendo", tint = RevelaYellow, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Respondendo a: \"${CryptoUtils.decrypt(replyMessage!!.conteudo)}\"",
                                fontSize = 11.sp,
                                color = Color.LightGray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(
                            onClick = { replyMessage = null },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Cancelar", tint = Color.Gray, modifier = Modifier.size(14.dp))
                        }
                    }
                }

                // Caixa de Entrada de Mensagens com Gaveta de Ações ➕
                if (hasBlockedCurrent || isBlocked) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Você ou este usuário estão bloqueados.", color = Color.Gray, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Gaveta de Ações (Enquete, Compartilhamento de Imagens, etc)
                        IconButton(
                            onClick = { showChatActions = !showChatActions }
                        ) {
                            Icon(
                                imageVector = if (showChatActions) Icons.Default.Close else Icons.Default.AddCircle,
                                contentDescription = "Mais Ações",
                                tint = RevelaYellow
                            )
                        }

                        // Gravador de áudio real com pedido de permissão (Requisito Microfone)
                        IconButton(
                            onClick = {
                                val hasPerm = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                                if (!hasPerm) {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                } else {
                                    if (!isRecordingAudio) {
                                        try {
                                            val cacheFile = File(context.cacheDir, "audio_${System.currentTimeMillis()}.mp4")
                                            currentAudioFile = cacheFile
                                            audioRecorder.start(cacheFile)
                                            isRecordingAudio = true
                                            Toast.makeText(context, "🔴 Gravando... Toque novamente para enviar!", Toast.LENGTH_SHORT).show()
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Erro ao iniciar gravador: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        audioRecorder.stop()
                                        isRecordingAudio = false
                                        val file = currentAudioFile
                                        if (file != null && file.exists() && file.length() > 0) {
                                            coroutineScope.launch {
                                                RevelaRepository.sendMessage(
                                                    conversaId = conversaId,
                                                    texto = "",
                                                    isAudio = true,
                                                    audioLocalPath = file.absolutePath,
                                                    tipo = "audio"
                                                )
                                            }
                                            Toast.makeText(context, "✅ Áudio gravado e enviado!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Gravação cancelada ou vazia.", Toast.LENGTH_SHORT).show()
                                        }
                                        currentAudioFile = null
                                    }
                                }
                            },
                            modifier = Modifier.testTag("mic_button")
                        ) {
                            Icon(
                                imageVector = if (isRecordingAudio) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = "Gravar Áudio",
                                tint = if (isRecordingAudio) RevelaCoral else RevelaPurple
                            )
                        }

                        // Input Texto
                        OutlinedTextField(
                            value = if (isRecordingAudio) "Gravando áudio..." else typedText,
                            onValueChange = { if (!isRecordingAudio) typedText = it },
                            placeholder = { Text("Mande um segredo...") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chat_input_text"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                                focusedBorderColor = RevelaPurple.copy(alpha = 0.5f),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(24.dp),
                            maxLines = 3,
                            enabled = !isRecordingAudio
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Enviar
                        IconButton(
                            onClick = {
                                if (typedText.isNotEmpty()) {
                                    coroutineScope.launch {
                                        RevelaRepository.sendMessage(
                                            conversaId = conversaId,
                                            texto = typedText,
                                            replyToId = replyMessage?.mensagemId,
                                            replyToText = replyMessage?.conteudo
                                        )
                                        typedText = ""
                                        replyMessage = null
                                    }
                                }
                            },
                            modifier = Modifier.testTag("send_message_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar", tint = RevelaPurple)
                        }
                    }

                    // Painel de Ações Estendido (Requisito 4: Melhorar o Chat)
                    AnimatedVisibility(
                        visible = showChatActions,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // Criar Enquete
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable {
                                        showChatActions = false
                                        showPollCreator = true
                                    }
                                    .padding(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(RevelaPurple),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Poll, contentDescription = "Enquete", tint = Color.White)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Criar Enquete", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            // Compartilhar Cartão Estético
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable {
                                        showChatActions = false
                                        showImageShareSheet = true
                                    }
                                    .padding(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(RevelaTurquoise),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Image, contentDescription = "Imagem", tint = Color.White)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Enviar Cartão", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            // Chamada de voz simulada
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable {
                                        if (conversa.matchRevelado) {
                                            showChatActions = false
                                            showVoiceCallMock = true
                                        }
                                    }
                                    .padding(8.dp)
                            ) {
                                val callAllowed = conversa.matchRevelado
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(if (callAllowed) Color.Green else Color.Gray),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Phone, contentDescription = "Chamada", tint = Color.White)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (callAllowed) "Chamada" else "Bloqueada",
                                    fontSize = 11.sp,
                                    color = if (callAllowed) Color.White else Color.Gray,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Animação da chama ao abrir o chat (Requisito 3 & Efeitos Visuais)
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = showFlameEntrance,
                        enter = fadeIn() + scaleIn(initialScale = 0.3f),
                        exit = fadeOut() + scaleOut(targetScale = 2.0f)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.85f), RoundedCornerShape(24.dp))
                                .padding(32.dp)
                        ) {
                            Text(text = streakIcon, fontSize = 72.sp, modifier = Modifier.scale(flameScale))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Sequência Ativa! ⚡",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                            Text(
                                text = "$streak Dias Consecutivos",
                                color = streakColor,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp
                            )

                            // Mostra mensagens especiais baseadas em marcos (Requisito 3)
                            val milestoneText = when {
                                streak >= 365 -> "🏆 Recorde de 1 Ano Conquistado! Lenda absoluta."
                                streak >= 100 -> "👑 Nível Lendário de 100 Dias! Conexão de alma."
                                streak >= 30 -> "🔮 Badge Especial Sintonia de Ouro Ativado!"
                                streak >= 7 -> "⚡ Uma semana de conversa! Conexão forte."
                                else -> "Mantenha o ritmo diário! 🔥"
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = milestoneText,
                                color = Color.LightGray,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal de Reações & Ações Rápidas de Mensagem (Requisito 4: Melhorar o Chat)
    if (selectedMessageForMenu != null) {
        val msg = selectedMessageForMenu!!
        AlertDialog(
            onDismissRequest = { selectedMessageForMenu = null },
            title = { Text("Ações para esta mensagem", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "\"${CryptoUtils.decrypt(msg.conteudo)}\"", fontSize = 13.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 16.dp))
                    
                    Text("Reagir com Emojis:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val emojis = listOf("❤️", "😂", "😮", "🔥", "👍", "😢")
                        emojis.forEach { emoji ->
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.05f))
                                    .clickable {
                                        coroutineScope.launch {
                                            RevelaRepository.reactToMessage(conversaId, msg.mensagemId, emoji)
                                            selectedMessageForMenu = null
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(emoji, fontSize = 18.sp)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = Color.Gray.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                replyMessage = msg
                                selectedMessageForMenu = null
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Reply, contentDescription = "Responder", tint = RevelaYellow)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Responder esta mensagem", fontSize = 14.sp)
                    }

                    if (msg.remetenteId != currentUser?.uid && msg.isAnonimo) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showReportDialog = msg
                                    selectedMessageForMenu = null
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = "Denunciar", tint = RevelaCoral)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Denunciar ao Moderador IA", fontSize = 14.sp, color = RevelaCoral)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedMessageForMenu = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Modal para Criar Enquete
    if (showPollCreator) {
        var question by remember { mutableStateOf("") }
        var opt1 by remember { mutableStateOf("") }
        var opt2 by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showPollCreator = false },
            title = { Text("Criar Enquete de Segredos 📊", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = question,
                        onValueChange = { question = it },
                        label = { Text("Pergunta da Enquete") },
                        placeholder = { Text("Ex: Você perdoaria uma mentira?") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = opt1,
                        onValueChange = { opt1 = it },
                        label = { Text("Opção A") },
                        placeholder = { Text("Ex: Sim, com certeza") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = opt2,
                        onValueChange = { opt2 = it },
                        label = { Text("Opção B") },
                        placeholder = { Text("Ex: Jamais, acabou ali") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (question.isNotEmpty() && opt1.isNotEmpty() && opt2.isNotEmpty()) {
                            coroutineScope.launch {
                                RevelaRepository.sendMessage(
                                    conversaId = conversaId,
                                    texto = "Enquete criada",
                                    tipo = "enquete",
                                    pollQuestion = question,
                                    pollOptions = listOf(opt1, opt2)
                                )
                                showPollCreator = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RevelaPurple)
                ) {
                    Text("Lançar Enquete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPollCreator = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Modal para Compartilhar Imagem Estética Preset (Requisito 4: Compartilhamento de Imagens)
    if (showImageShareSheet) {
        AlertDialog(
            onDismissRequest = { showImageShareSheet = false },
            title = { Text("Escolha um Cartão de Momento Estético ✨", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val presets = listOf(
                        Triple("aesthetic_cafe", "☕ Café Estético", "Enviou um momento relaxante no café."),
                        Triple("cyberpunk_vibes", "🌃 Noite Cyberpunk", "Compartilhou a vibração das luzes da noite."),
                        Triple("cozy_sunset", "🌅 Pôr do Sol Aconchegante", "Mandou um sol radiante se pondo na praia.")
                    )
                    
                    presets.forEach { (presetKey, title, desc) ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    coroutineScope.launch {
                                        RevelaRepository.sendMessage(
                                            conversaId = conversaId,
                                            texto = desc,
                                            tipo = "imagem_preset",
                                            audioLocalPath = presetKey // store the key in audioUrl
                                        )
                                        showImageShareSheet = false
                                    }
                                },
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = RevelaYellow)
                                Text(text = desc, fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showImageShareSheet = false }) {
                    Text("Fechar")
                }
            }
        )
    }

    // Diálogo Explicativo dos Níveis de Confiança
    if (showInfoTrustDialog) {
        AlertDialog(
            onDismissRequest = { showInfoTrustDialog = false },
            title = { Text("Mecânica de Confiança Anônima 🔒", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = RevelaYellow) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("No Revela, o anonimato é protegido, mas a conexão é real! Troque mensagens para acumular confiança e desbloquear dados do perfil do outro usuário de forma gradual:", fontSize = 12.sp)
                    Text("• Nível 1: Identidade oculta, bios e dados ocultos.", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Text("• Nível 2 (6 msgs): Desbloqueia Idade e Cidade.", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Text("• Nível 3 (12 msgs): Desbloqueia Vibes e Interesses.", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Text("• Nível 4 (24 msgs): Desbloqueia Biografia e histórias pessoais.", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Text("• Nível 5 (40 msgs): Libera o botão de Enviar Proposta de Revelação Mútua de Identidade! 🎉", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            },
            confirmButton = {
                Button(onClick = { showInfoTrustDialog = false }) {
                    Text("Ok, entendi!")
                }
            }
        )
    }

    // Modal de Chamada de Voz Simulada (Visual Interactive Feature)
    if (showVoiceCallMock) {
        var isCallConnected by remember { mutableStateOf(false) }
        var timerCount by remember { mutableStateOf(0) }

        LaunchedEffect(isCallConnected) {
            if (isCallConnected) {
                while (true) {
                    kotlinx.coroutines.delay(1000)
                    timerCount++
                }
            }
        }

        AlertDialog(
            onDismissRequest = { showVoiceCallMock = false },
            title = null,
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("📞 Chamada de Voz Criptografada", fontSize = 14.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(RevelaTurquoise.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🗣️", fontSize = 48.sp)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (isCallConnected) "Conectado • ${timerCount}s" else "Ligando para @${outroUser?.apelido}...",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Voz modificada por IA desativada (Identidades já reveladas)",
                        fontSize = 11.sp,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    if (!isCallConnected) {
                        Button(
                            onClick = { isCallConnected = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
                        ) {
                            Text("Atender Ligação")
                        }
                    } else {
                        Button(
                            onClick = { showVoiceCallMock = false },
                            colors = ButtonDefaults.buttonColors(containerColor = RevelaCoral)
                        ) {
                            Text("Desligar Chamada")
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    // Modal de Denúncia (Requisito 6)
    if (showReportDialog != null) {
        var reason by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showReportDialog = null },
            title = { Text("Denunciar Mensagem Ofensiva") },
            text = {
                Column {
                    Text("Motivo da denúncia:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        placeholder = { Text("Ex: Ofensa, Assédio, Palavrões...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "🔒 Nota de segurança: O Anonymous registrará o IP do remetente junto com a denúncia para moderação e banimento completo do dispositivo se for reincidente.",
                        fontSize = 10.sp,
                        color = RevelaCoral,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            RevelaRepository.reportMessage(showReportDialog!!.mensagemId, reason)
                            showReportDialog = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RevelaCoral)
                ) {
                    Text("Denunciar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo MATCH / REVELADO (Tinder Style)
    if (showMatchDialog) {
        AlertDialog(
            onDismissRequest = { showMatchDialog = false },
            title = null,
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🎉 MATCH! 🎉", fontSize = 34.sp, fontWeight = FontWeight.Bold, color = RevelaCoral)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Identidades Reveladas!", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = RevelaPurple)
                    
                    Spacer(modifier = Modifier.height(24.dp))

                    // Fotos de perfil unificadas
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Eu
                        Box(
                            modifier = Modifier
                                .size(74.dp)
                                .clip(CircleShape)
                                .background(RevelaPurple)
                        ) {
                            Text(
                                text = currentUser?.nome?.firstOrNull()?.toString()?.uppercase() ?: "U",
                                modifier = Modifier.align(Alignment.Center),
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 24.sp
                            )
                        }

                        Text(" ❤️ ", fontSize = 24.sp, modifier = Modifier.padding(horizontal = 12.dp))

                        // Ele
                        Box(
                            modifier = Modifier
                                .size(74.dp)
                                .clip(CircleShape)
                                .background(RevelaTurquoise)
                        ) {
                            if (outroUser?.fotoPerfil?.isNotEmpty() == true) {
                                Image(
                                    painter = rememberAsyncImagePainter(outroUser.fotoPerfil),
                                    contentDescription = outroUser.nome,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    text = outroUser?.nome?.firstOrNull()?.toString()?.uppercase() ?: "O",
                                    modifier = Modifier.align(Alignment.Center),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 24.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Você e @${outroUser?.apelido} agora são amigos normais! Vocês podem ver as fotos de perfil e todos os posts um do outro.",
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showMatchDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = RevelaPurple),
                    modifier = Modifier.fillMaxWidth().testTag("match_dialog_dismiss")
                ) {
                    Text("Continuar Conversando")
                }
            }
        )
    }
}
