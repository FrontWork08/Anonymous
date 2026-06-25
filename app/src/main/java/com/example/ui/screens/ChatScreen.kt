package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.data.ChatMessage
import com.example.data.Conversation
import com.example.data.RevelaRepository
import com.example.data.UserProfile
import com.example.ui.theme.RevelaCoral
import com.example.ui.theme.RevelaPurple
import com.example.ui.theme.RevelaTurquoise
import com.example.ui.theme.RevelaYellow
import kotlinx.coroutines.launch

/**
 * Tela 7: Chat / Conversa Individual
 * O coração do aplicativo Revela. Apresenta bolhas normais e anônimas,
 * indicador de digitando real-time, envio de áudio simulado, sistema de denúncia/bloqueio,
 * e a mecânica de revelação com animação Match! estilo Tinder após 5 mensagens.
 */
@OptIn(ExperimentalMaterial3Api::class)
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

    // Diálogos e Modais
    var showReportDialog by remember { mutableStateOf<ChatMessage?>(null) }
    var showMatchDialog by remember { mutableStateOf(false) }
    var isBlocked by remember { mutableStateOf(false) }

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
    val showRevealButton = mensagens.size >= 5 && isAnonima && !conversa.matchRevelado

    // Se o outro usuário foi bloqueado, esconde envio
    val hasBlockedCurrent = currentUser?.bloqueados?.contains(outroParticipanteId) == true

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
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

                        Column(modifier = Modifier.padding(start = 12.dp)) {
                            Text(
                                text = if (isAnonima) "Alguém Anônimo" else (outroUser?.nome ?: "Carregando..."),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isAnonima) RevelaYellow else MaterialTheme.colorScheme.onBackground
                            )
                            if (isTyping) {
                                Text(
                                    text = "digitando...",
                                    fontSize = 11.sp,
                                    color = RevelaPurple,
                                    fontWeight = FontWeight.Medium
                                )
                            } else {
                                Text(
                                    text = if (isAnonima) "Máscara ativa 🎭" else "Conectado",
                                    fontSize = 11.sp,
                                    color = Color.Gray
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
                    // Opções de Denúncia e Bloqueio
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
                // Mensagens de aviso e botão de revelação na parte superior
                if (isAnonima && !conversa.matchRevelado) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = RevelaPurple.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(0.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("🔒 Chat Criptografado. Suas identidades estão seguras.", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(mensagens) { msg ->
                        val isMe = msg.remetenteId == currentUser?.uid

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
                                        .padding(12.dp)
                                        .clickable {
                                            if (!isMe && msg.isAnonimo) {
                                                showReportDialog = msg
                                            }
                                        }
                                ) {
                                    Column {
                                        if (msg.tipo == "audio") {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = if (isMe) Color.White else RevelaPurple)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("🔊 [Áudio de Voz Simulada]", fontSize = 13.sp, color = if (isMe) Color.White else MaterialTheme.colorScheme.onBackground)
                                            }
                                        } else {
                                            Text(
                                                text = msg.conteudo,
                                                color = if (isMe) Color.White else MaterialTheme.colorScheme.onBackground,
                                                fontSize = 14.sp
                                            )
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
                                    Text("... digitando ...", fontSize = 12.sp, color = Color.Gray)
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
                                text = if (outroJáRevelou) "O outro participante QUER REVELAR a identidade!" else "Vocês já trocaram 5 mensagens! Quer revelar sua identidade?",
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
                                    text = if (euJáRevelei) "Aguardando o outro clicar..." else "Revelar quem sou! 🎭",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Caixa de Entrada de Mensagens
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
                        // Simula mensagem de Voz
                        IconButton(
                            onClick = {
                                isRecordingAudio = !isRecordingAudio
                                if (!isRecordingAudio) {
                                    // Mandou áudio
                                    coroutineScope.launch {
                                        RevelaRepository.sendMessage(conversaId, "", isAudio = true, audioLocalPath = "simulated_audio.mp3")
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
                                        RevelaRepository.sendMessage(conversaId, typedText)
                                        typedText = ""
                                    }
                                }
                            },
                            modifier = Modifier.testTag("send_message_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar", tint = RevelaPurple)
                        }
                    }
                }
            }
        }
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
