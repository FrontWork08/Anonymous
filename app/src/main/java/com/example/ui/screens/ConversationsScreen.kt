package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.data.Conversation
import com.example.data.RevelaRepository
import com.example.ui.theme.ImmersiveLavender
import com.example.ui.theme.RevelaPurple
import com.example.ui.theme.RevelaTurquoise
import com.example.ui.theme.RevelaYellow
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Tela 6: Conversas (Tab 3) - Caixa de Entrada
 * Lista de chats com ícones obrigatórios em cores para diferenciação:
 * - 💬 Azul (Verde Água - RevelaTurquoise) = Conversa Normal
 * - 🎭 Amarelo/Roxo (RevelaYellow/RevelaPurple) = Conversa Anônima
 * Inclui badge de não lidas e visualização dinâmica.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationsScreen(
    onNavigateToChat: (String) -> Unit
) {
    val allConversations by RevelaRepository.conversations.collectAsState()
    val users by RevelaRepository.users.collectAsState()
    val currentUser by RevelaRepository.currentUser.collectAsState()

    val conversations = remember(allConversations, currentUser) {
        if (currentUser?.isAdmin == true) {
            allConversations
        } else {
            allConversations.filter { it.participantes.contains(currentUser?.uid) }
        }
    }

    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mensagens 📬", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (conversations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("💬", fontSize = 54.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Nenhuma conversa ativa ainda.",
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Vá ao Feed e interaja com os Stories de amigos para iniciar um bate-papo!",
                            fontSize = 12.sp,
                            color = Color.Gray.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp, start = 24.dp, end = 24.dp)
                        )
                    }
                }
            } else {
                val messagesMap by RevelaRepository.messages.collectAsState()

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(conversations) { conversa ->
                        // Busca o outro participante da conversa
                        val outroParticipanteId = conversa.participantes.find { it != currentUser?.uid } ?: ""
                        val outroUser = users[outroParticipanteId]

                        val isAnonima = conversa.tipo == "anonimo"
                        val chatMessages = messagesMap[conversa.conversaId] ?: emptyList()
                        val messageCount = chatMessages.size

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = if (isAnonima) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isAnonima) Color.White.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.03f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable { onNavigateToChat(conversa.conversaId) }
                                .padding(12.dp)
                                .testTag("conversation_item_${conversa.conversaId}"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // --- ÍCONES OBRIGATÓRIOS PARA DIFERENCIAR ---
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(
                                        brush = if (isAnonima) {
                                            Brush.linearGradient(listOf(RevelaYellow, Color(0xFFFF9F1C)))
                                        } else {
                                            Brush.linearGradient(listOf(RevelaTurquoise.copy(alpha = 0.2f), RevelaTurquoise.copy(alpha = 0.05f)))
                                        },
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isAnonima) {
                                    // 🎭 Roxo/Amarelo para Anônimo
                                    Text("🎭", fontSize = 28.sp)
                                } else {
                                    // 💬 Azul/Verde água para Normal (mostra foto ou inicial)
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
                                            color = RevelaTurquoise,
                                            fontSize = 20.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Conteúdo de texto
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val displayName = if (currentUser?.isAdmin == true) {
                                        val p1 = users[conversa.participantes.getOrNull(0)]?.nome ?: "Desconhecido"
                                        val p2 = users[conversa.participantes.getOrNull(1)]?.nome ?: "Desconhecido"
                                        "$p1 ⇆ $p2"
                                    } else {
                                        if (isAnonima) "Alguém Anônimo" else (outroUser?.nome ?: "Usuário")
                                    }
                                    Text(
                                        text = displayName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = if (isAnonima && currentUser?.isAdmin != true) RevelaYellow else MaterialTheme.colorScheme.onBackground
                                    )
                                    
                                    Text(
                                        text = timeFormatter.format(conversa.ultimaMensagemData),
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }

                                Text(
                                    text = conversa.ultimaMensagem.ifEmpty { "Sem mensagens" },
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(top = 4.dp)
                                )

                                // Indicadores de chat
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (isAnonima) {
                                            Box(
                                                modifier = Modifier
                                                    .background(RevelaYellow, RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text("🎭 ANÔNIMO", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                            }
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .background(RevelaTurquoise, RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text("💬 NORMAL", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                            }
                                        }

                                        if (conversa.matchRevelado) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .background(RevelaPurple, RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text("🎉 REVELADO", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            }
                                        }
                                    }

                                    // Badge de Progresso (Modo Surpresa)
                                    if (isAnonima && !conversa.matchRevelado) {
                                        Box(
                                            modifier = Modifier
                                                .background(RevelaPurple.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "Progresso: ${minOf(messageCount, 5)}/5 🎭",
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = ImmersiveLavender
                                            )
                                        }
                                    }
                                }
                            }

                            // Badge de não lidas
                            if (conversa.unreadCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .padding(start = 12.dp)
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(RevelaPurple),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${conversa.unreadCount}",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
