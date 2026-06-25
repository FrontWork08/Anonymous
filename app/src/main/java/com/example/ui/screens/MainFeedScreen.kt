package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.data.FeedPost
import com.example.data.PostComment
import com.example.data.RevelaRepository
import com.example.data.UserProfile
import com.example.ui.theme.RevelaCoral
import com.example.ui.theme.RevelaPurple
import com.example.ui.theme.RevelaTurquoise
import com.example.ui.theme.RevelaYellow
import kotlinx.coroutines.launch

/**
 * Tela 4: Feed Principal (Tab 1)
 * Apresenta o topo com logo, stories circulares decorados, lista de postagens
 * em cards estilizados, suporte a curtidas, abertura de comentários (públicos/anônimos)
 * e FAB para criação de posts.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainFeedScreen(
    onNavigateToPost: () -> Unit,
    onNavigateToChatWithUser: (UserProfile) -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val posts by RevelaRepository.posts.collectAsState()
    val users by RevelaRepository.users.collectAsState()
    val commentsByPost by RevelaRepository.comments.collectAsState()
    val currentUser by RevelaRepository.currentUser.collectAsState()

    var activeCommentPost by remember { mutableStateOf<FeedPost?>(null) }
    var isCommentSheetOpen by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(
                                    brush = Brush.linearGradient(listOf(RevelaPurple, RevelaCoral)),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("A", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                        Text(
                            text = "Anonymous",
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 20.sp,
                            letterSpacing = (-0.5).sp
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToProfile,
                        modifier = Modifier.testTag("feed_profile_avatar")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(RevelaPurple),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentUser?.nome?.firstOrNull()?.uppercase() ?: "U",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToPost,
                containerColor = RevelaPurple,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = 8.dp, end = 8.dp)
                    .testTag("fab_create_post")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Postar Foto", modifier = Modifier.size(28.dp))
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Seção 1: Stories dos usuários online
            item {
                Text(
                    text = "Amigos Conectados",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    items(users.values.toList()) { user ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { onNavigateToChatWithUser(user) }
                                .testTag("story_item_${user.apelido}")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .border(
                                        width = 2.5.dp,
                                        brush = Brush.sweepGradient(listOf(RevelaPurple, RevelaCoral)),
                                        shape = CircleShape
                                    )
                                    .padding(4.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                if (user.fotoPerfil.isNotEmpty() && user.fotoPerfil != "google_avatar") {
                                    Image(
                                        painter = rememberAsyncImagePainter(user.fotoPerfil),
                                        contentDescription = user.nome,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Text(
                                        text = user.nome.firstOrNull()?.uppercase() ?: "U",
                                        fontWeight = FontWeight.Bold,
                                        color = RevelaPurple,
                                        fontSize = 20.sp
                                    )
                                }
                            }
                            Text(
                                text = user.apelido,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(top = 6.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                
                // REQUISITO 2: ALGORITMO DE DESCOBERTA POR AFINIDADE
                Text(
                    text = "✨ Descoberta por Afinidade",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = RevelaYellow,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                Text(
                    text = "Pessoas sintonizadas com seus interesses e vibes em comum.",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                )

                val recommendedUsers = remember(users, currentUser) {
                    users.values.filter { it.uid != currentUser?.uid }
                        .map { other ->
                            val score = RevelaRepository.getCompatibilityScore(currentUser, other)
                            other to score
                        }
                        .sortedByDescending { it.second }
                }

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    items(recommendedUsers) { (recUser, score) ->
                        Card(
                            modifier = Modifier
                                .width(170.dp)
                                .clickable { onNavigateToChatWithUser(recUser) }
                                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(RevelaTurquoise.copy(alpha = 0.15f), CircleShape)
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "$score% Afinidade",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = RevelaTurquoise
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = "@${recUser.apelido}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                
                                Spacer(modifier = Modifier.height(6.dp))
                                
                                // Interesses em comum
                                val shared = recUser.interesses.filter { currentUser?.interesses?.contains(it) == true }
                                val displayTag = if (shared.isNotEmpty()) "🤝 ${shared.first()}" else "✨ ${recUser.vibes.first()}"
                                
                                Box(
                                    modifier = Modifier
                                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                        .widthIn(max = 140.dp)
                                ) {
                                    Text(
                                        text = displayTag,
                                        fontSize = 10.sp,
                                        color = Color.LightGray,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = "Revelação Gradual Ativa",
                                    fontSize = 8.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
                
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Seção 2: Feed de Posts
            if (posts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🫙", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Nenhuma postagem ainda.",
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            } else {
                items(posts) { post ->
                    PostCard(
                        post = post,
                        onLikeClick = {
                            coroutineScope.launch {
                                RevelaRepository.toggleLikePost(post.postId)
                            }
                        },
                        onCommentClick = {
                            activeCommentPost = post
                            isCommentSheetOpen = true
                        }
                    )
                }
            }
        }
    }

    // Modal de comentários integrados (Público / Anônimo)
    if (isCommentSheetOpen && activeCommentPost != null) {
        CommentDialog(
            post = activeCommentPost!!,
            comments = commentsByPost[activeCommentPost!!.postId] ?: emptyList(),
            onDismiss = { isCommentSheetOpen = false },
            onSendComment = { text, isAnon ->
                coroutineScope.launch {
                    RevelaRepository.addComment(activeCommentPost!!.postId, text, isAnon)
                }
            }
        )
    }
}

@Composable
fun PostCard(
    post: FeedPost,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit
) {
    val isLiked = post.usuariosCurtiram.contains(RevelaRepository.currentUser.value?.uid)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .testTag("post_card_${post.postId}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f))
    ) {
        Column {
            // Cabeçalho do Post
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(RevelaPurple.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (post.autorFoto.isNotEmpty()) {
                        Image(
                            painter = rememberAsyncImagePainter(post.autorFoto),
                            contentDescription = post.autorNome,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = post.autorNome.firstOrNull()?.uppercase() ?: "A",
                            fontWeight = FontWeight.Bold,
                            color = RevelaPurple
                        )
                    }
                }

                Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                    Text(
                        text = post.autorNome,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "@${post.autorApelido} • Filtro: ${post.filterApplied}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }

                // Botão Seguir / Seguindo (Requisito: Seguidores/Seguindo)
                val currentUser by RevelaRepository.currentUser.collectAsState()
                val allFollows by RevelaRepository.follows.collectAsState()
                val scope = rememberCoroutineScope()
                var showFollowDialog by remember { mutableStateOf(false) }

                if (currentUser != null && post.usuarioId != currentUser?.uid) {
                    val isFollowing = allFollows.any { it.followerId == currentUser?.uid && it.followingId == post.usuarioId }
                    
                    if (isFollowing) {
                        TextButton(
                            onClick = {
                                scope.launch {
                                    RevelaRepository.unfollowUser(post.usuarioId)
                                }
                            }
                        ) {
                            Text("Seguindo ✓", color = Color.Green, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = { showFollowDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = RevelaPurple),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Seguir", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (showFollowDialog) {
                    AlertDialog(
                        onDismissRequest = { showFollowDialog = false },
                        title = { Text("Seguir @${post.autorApelido}", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                        text = {
                            Text("Escolha se deseja seguir anonimamente ou revelar sua identidade desde já.")
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showFollowDialog = false
                                    scope.launch {
                                        RevelaRepository.followUser(post.usuarioId, isAnonymous = true)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = RevelaCoral)
                            ) {
                                Text("🎭 Anonimamente")
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    showFollowDialog = false
                                    scope.launch {
                                        RevelaRepository.followUser(post.usuarioId, isAnonymous = false)
                                    }
                                }
                            ) {
                                Text("👤 Publicamente")
                            }
                        }
                    )
                }
            }

            // Imagem do Post
            Image(
                painter = rememberAsyncImagePainter(post.imagemUrl),
                contentDescription = "Post Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .padding(horizontal = 12.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            // Legenda e tags
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = post.legenda,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp
                )
                
                if (post.tags.isNotEmpty()) {
                    Row(
                        modifier = Modifier.padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        post.tags.forEach { tag ->
                            Text(
                                text = "@$tag",
                                color = RevelaPurple,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Barra de interações
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row {
                    // Likes
                    IconButton(
                        onClick = onLikeClick,
                        modifier = Modifier.testTag("like_button_${post.postId}")
                    ) {
                        Icon(
                            imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Curtir",
                            tint = if (isLiked) RevelaCoral else MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Text(
                        text = "${post.curtidas}",
                        modifier = Modifier.align(Alignment.CenterVertically),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    // Comentários
                    IconButton(
                        onClick = onCommentClick,
                        modifier = Modifier.testTag("comment_button_${post.postId}")
                    ) {
                        Icon(Icons.Default.ChatBubble, contentDescription = "Comentar")
                    }
                    Text(
                        text = "Ver comentários",
                        modifier = Modifier.align(Alignment.CenterVertically),
                        fontSize = 13.sp,
                        color = RevelaPurple,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (post.permiteComentarioAnonimo) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("🎭 Anônimos Ativos ", fontSize = 11.sp, color = RevelaYellow, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentDialog(
    post: FeedPost,
    comments: List<PostComment>,
    onDismiss: () -> Unit,
    onSendComment: (String, Boolean) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var isAnonimo by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Comentários (🎭)", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth().height(350.dp)) {
                // Lista de Comentários
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (comments.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("Ninguém comentou ainda. Seja o primeiro!", fontSize = 13.sp, color = Color.Gray)
                            }
                        }
                    } else {
                        items(comments) { comment ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = if (comment.isAnonimo) RevelaYellow.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (comment.isAnonimo) "🎭" else "👤",
                                    fontSize = 18.sp,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = comment.autorNome,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (comment.isAnonimo) RevelaYellow else RevelaPurple
                                    )
                                    Text(text = comment.texto, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Campo de texto + Toggle Anônimo
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Escreva um comentário...") },
                    modifier = Modifier.fillMaxWidth().testTag("comment_input_dialog"),
                    maxLines = 2
                )

                if (post.permiteComentarioAnonimo) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isAnonimo,
                            onCheckedChange = { isAnonimo = it },
                            colors = CheckboxDefaults.colors(checkedColor = RevelaYellow)
                        )
                        Text("Comentar anonimamente (🎭)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = RevelaYellow)
                    }
                } else {
                    Text(
                        "🔒 Comentários anônimos desativados pelo autor.",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (text.isNotEmpty()) {
                        onSendComment(text, isAnonimo)
                        text = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = RevelaPurple),
                modifier = Modifier.testTag("dialog_send_comment_button")
            ) {
                Text("Comentar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar")
            }
        }
    )
}
