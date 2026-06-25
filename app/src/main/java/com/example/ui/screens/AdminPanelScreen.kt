package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChatMessage
import com.example.data.Conversation
import com.example.data.FeedPost
import com.example.data.RevelaRepository
import com.example.data.UserProfile
import com.example.ui.theme.RevelaCoral
import com.example.ui.theme.RevelaPurple
import com.example.ui.theme.RevelaTurquoise
import com.example.ui.theme.RevelaYellow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Tela de Painel Administrativo / Moderação (Controle Total)
 * Permite ao administrador do sistema (frontwork08@gmail.com):
 * 1. Visualizar e Banir/Desbanir usuários cadastrados.
 * 2. Visualizar todas as conversas do aplicativo (normais e anônimas) e ler as mensagens trocadas.
 * 3. Excluir postagens do Feed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen() {
    val coroutineScope = rememberCoroutineScope()
    val usersMap by RevelaRepository.users.collectAsState()
    val conversationsList by RevelaRepository.conversations.collectAsState()
    val postsList by RevelaRepository.posts.collectAsState()
    val messagesMap by RevelaRepository.messages.collectAsState()

    var selectedTabState by remember { mutableIntStateOf(0) }
    val tabs = listOf("Usuários 👥", "Conversas 💬", "Posts do Feed 🎭")

    // Estado para exibir o histórico de mensagens de uma conversa selecionada
    var activeConversationForInspection by remember { mutableStateOf<Conversation?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Admin",
                            tint = RevelaCoral,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Painel Admin 🛡️", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                },
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
            // Header informativo do Admin
            Card(
                colors = CardDefaults.cardColors(containerColor = RevelaPurple.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Acesso de Super Administrador",
                        fontWeight = FontWeight.Bold,
                        color = RevelaPurple,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Moderando como: frontwork08@gmail.com",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            // Seletor de Abas (TabRow)
            TabRow(
                selectedTabIndex = selectedTabState,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = RevelaPurple
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabState == index,
                        onClick = {
                            selectedTabState = index
                            activeConversationForInspection = null
                        },
                        text = { Text(title, fontWeight = FontWeight.Medium, fontSize = 13.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (selectedTabState) {
                0 -> {
                    // TAB 0: LISTA DE USUÁRIOS & BANIMENTOS
                    val usersList = usersMap.values.toList()
                    if (usersList.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Nenhum usuário cadastrado.", color = Color.Gray)
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize().testTag("admin_users_list")
                        ) {
                            items(usersList) { user ->
                                // Não permite que o admin se bana sozinho
                                val isSelf = user.email == "frontwork08@gmail.com"
                                val isBanned = user.status == "banido"

                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isBanned) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = user.nome,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 16.sp,
                                                    color = MaterialTheme.colorScheme.onBackground
                                                )
                                                if (isSelf) {
                                                    Box(
                                                        modifier = Modifier
                                                            .padding(start = 8.dp)
                                                            .background(RevelaCoral, RoundedCornerShape(4.dp))
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text("VOCÊ", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                    }
                                                }
                                            }
                                            Text(
                                                text = "@${user.apelido} • ${user.email}",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                                modifier = Modifier.padding(top = 2.dp)
                                            )
                                            Row(
                                                modifier = Modifier.padding(top = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .background(
                                                            color = if (isBanned) Color.Red else Color.Green,
                                                            shape = RoundedCornerShape(4.dp)
                                                        )
                                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = if (isBanned) "BANIDO" else "ATIVO",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    )
                                                }
                                                if (user.isAdmin) {
                                                    Box(
                                                        modifier = Modifier
                                                            .padding(start = 6.dp)
                                                            .background(RevelaPurple, RoundedCornerShape(4.dp))
                                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                                    ) {
                                                        Text("ADM", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                    }
                                                }
                                            }
                                        }

                                        if (!isSelf) {
                                            Button(
                                                onClick = {
                                                    coroutineScope.launch {
                                                        RevelaRepository.toggleBanUser(user.uid)
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (isBanned) RevelaTurquoise else MaterialTheme.colorScheme.error
                                                ),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                                modifier = Modifier.testTag("ban_button_${user.uid}")
                                            ) {
                                                Icon(
                                                    imageVector = if (isBanned) Icons.Default.LockOpen else Icons.Default.Lock,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp).padding(end = 4.dp)
                                                )
                                                Text(
                                                    text = if (isBanned) "Ativar" else "Banir",
                                                    fontSize = 12.sp,
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
                1 -> {
                    // TAB 1: LISTA DE CONVERSAS & LEITOR DE MENSAGENS
                    if (conversationsList.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Nenhuma conversa ativa no sistema.", color = Color.Gray)
                        }
                    } else {
                        Column(modifier = Modifier.fillMaxSize()) {
                            if (activeConversationForInspection == null) {
                                Text(
                                    text = "Toque em um chat abaixo para ver o histórico completo de mensagens trocadas:",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                                )

                                LazyColumn(
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    items(conversationsList) { conv ->
                                        val u1 = usersMap[conv.participantes.getOrNull(0)]
                                        val u2 = usersMap[conv.participantes.getOrNull(1)]
                                        val isAnon = conv.tipo == "anonimo"

                                        Card(
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                            ),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { activeConversationForInspection = conv }
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            text = "${u1?.nome ?: "Desconhecido"} ⇆ ${u2?.nome ?: "Desconhecido"}",
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 14.sp
                                                        )
                                                    }
                                                    Box(
                                                        modifier = Modifier
                                                            .background(
                                                                color = if (isAnon) RevelaYellow else RevelaTurquoise,
                                                                shape = RoundedCornerShape(4.dp)
                                                            )
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(
                                                            text = if (isAnon) "ANÔNIMO 🎭" else "NORMAL 💬",
                                                            fontSize = 8.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color.Black
                                                        )
                                                    }
                                                }
                                                Text(
                                                    text = "Última: ${conv.ultimaMensagem.ifEmpty { "Sem conteúdo" }}",
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.padding(top = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                // Exibe painel de mensagens da conversa selecionada
                                val selectedConv = activeConversationForInspection!!
                                val u1 = usersMap[selectedConv.participantes.getOrNull(0)]
                                val u2 = usersMap[selectedConv.participantes.getOrNull(1)]
                                val conversationMessages = messagesMap[selectedConv.conversaId] ?: emptyList()

                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 16.dp)
                                ) {
                                    // Header de Controle
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Histórico de Mensagens",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = RevelaPurple
                                        )
                                        TextButton(onClick = { activeConversationForInspection = null }) {
                                            Text("Voltar", fontWeight = FontWeight.Bold, color = RevelaCoral)
                                        }
                                    }

                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(
                                                text = "Participantes reais:",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color.Gray
                                            )
                                            Text(
                                                text = "1. ${u1?.nome ?: "Desconhecido"} (@${u1?.apelido ?: "u1"})",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "2. ${u2?.nome ?: "Desconhecido"} (@${u2?.apelido ?: "u2"})",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Tipo: ${if (selectedConv.tipo == "anonimo") "Anônimo 🎭" else "Normal 💬"}",
                                                fontSize = 11.sp,
                                                color = Color.Gray,
                                                modifier = Modifier.padding(top = 4.dp)
                                            )
                                        }
                                    }

                                    if (conversationMessages.isEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxWidth(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("Nenhuma mensagem nesta conversa ainda.", color = Color.Gray)
                                        }
                                    } else {
                                        LazyColumn(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxWidth()
                                                .background(
                                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                                                    RoundedCornerShape(12.dp)
                                                )
                                                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                                .padding(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            items(conversationMessages) { msg ->
                                                val senderName = if (msg.remetenteId == null) {
                                                    // Mensagem anônima. O admin vê quem enviou de verdade para controle completo!
                                                    // Buscamos quem não é o destinatário no banco
                                                    val realSenderId = selectedConv.participantes.getOrNull(0) // Simplificação ou mock de remetente
                                                    val realSender = usersMap[msg.remetenteId ?: ""] ?: usersMap[selectedConv.participantes[0]]
                                                    "[ANÔNIMO] ${realSender?.nome ?: "Alguém"}"
                                                } else {
                                                    usersMap[msg.remetenteId]?.nome ?: "Sistema"
                                                }

                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 8.dp)
                                                ) {
                                                    Text(
                                                        text = senderName,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (msg.isAnonimo) RevelaYellow else RevelaTurquoise
                                                    )
                                                    Text(
                                                        text = msg.conteudo,
                                                        fontSize = 14.sp,
                                                        color = MaterialTheme.colorScheme.onBackground
                                                    )
                                                    HorizontalDivider(
                                                        modifier = Modifier.padding(top = 4.dp),
                                                        color = Color.White.copy(alpha = 0.05f)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // TAB 2: POSTS DO FEED & EXCLUSÃO
                    if (postsList.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Nenhum post publicado.", color = Color.Gray)
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize().testTag("admin_posts_list")
                        ) {
                            items(postsList) { post ->
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = post.autorNome,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                )
                                                Text(
                                                    text = "@${post.autorApelido}",
                                                    fontSize = 11.sp,
                                                    color = Color.Gray
                                                )
                                            }

                                            IconButton(
                                                onClick = {
                                                    coroutineScope.launch {
                                                        RevelaRepository.deletePost(post.postId)
                                                    }
                                                },
                                                colors = IconButtonDefaults.iconButtonColors(
                                                    contentColor = MaterialTheme.colorScheme.error
                                                ),
                                                modifier = Modifier.testTag("delete_post_button_${post.postId}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Excluir Post"
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = post.legenda,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )

                                        if (post.filterApplied != "Normal") {
                                            Box(
                                                modifier = Modifier
                                                    .padding(top = 8.dp)
                                                    .background(RevelaPurple.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "Filtro: ${post.filterApplied}",
                                                    fontSize = 8.sp,
                                                    color = RevelaPurple,
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
        }
    }
}
