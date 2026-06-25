package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
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
import com.example.data.RevelaRepository
import com.example.ui.theme.RevelaCoral
import com.example.ui.theme.RevelaPurple
import com.example.ui.theme.RevelaYellow

/**
 * Tela 5: Perfil do Usuário (Tab 2) - Estilo Instagram
 * Apresenta as estatísticas de Seguidores/Seguindo, dados completos do perfil,
 * badges conquistadas (Gamificação), Missões Diárias e grade de fotos publicadas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToEditProfile: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val currentUser by RevelaRepository.currentUser.collectAsState()
    val allPosts by RevelaRepository.posts.collectAsState()
    val badges by RevelaRepository.badges.collectAsState()
    val missions by RevelaRepository.missions.collectAsState()

    val allFollows by RevelaRepository.follows.collectAsState()
    val allUsers by RevelaRepository.users.collectAsState()

    var showFollowersDialog by remember { mutableStateOf(false) }
    var showFollowingDialog by remember { mutableStateOf(false) }

    val myFollowers = allFollows.filter { it.followingId == currentUser?.uid }
    val myFollowing = allFollows.filter { it.followerId == currentUser?.uid }

    val myPosts = allPosts.filter { it.usuarioId == currentUser?.uid }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Meu Perfil", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("settings_button")
                    ) {
                        Text("⚙️", fontSize = 22.sp)
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Foto de perfil com iniciais ou foto real
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .border(3.dp, RevelaPurple, CircleShape)
                    .padding(4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (currentUser?.fotoPerfil?.isNotEmpty() == true && currentUser?.fotoPerfil != "google_avatar") {
                    Image(
                        painter = rememberAsyncImagePainter(currentUser?.fotoPerfil),
                        contentDescription = "Foto de Perfil",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = currentUser?.nome?.firstOrNull()?.toString()?.uppercase() ?: "R",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = RevelaPurple
                    )
                }
            }

            Text(
                text = currentUser?.nome ?: "Usuário",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 12.dp)
            )

            Text(
                text = "@${currentUser?.apelido ?: "apelido"}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = RevelaCoral,
                modifier = Modifier.padding(top = 2.dp)
            )

            // Localidade
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 6.dp)
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                Text(
                    text = "${currentUser?.cidade ?: "Cidade"}, ${currentUser?.estado ?: "Estado"}",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Estatísticas (Seguidores / Seguindo)
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { showFollowersDialog = true }
                        .padding(8.dp)
                ) {
                    val followersCount = if (myFollowers.isNotEmpty()) myFollowers.size else (currentUser?.seguidores ?: 0)
                    Text(text = "$followersCount", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(text = "Seguidores", fontSize = 12.sp, color = Color.Gray)
                }
                VerticalDivider(modifier = Modifier.height(30.dp), color = Color.Gray.copy(alpha = 0.3f))
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { showFollowingDialog = true }
                        .padding(8.dp)
                ) {
                    val followingCount = if (myFollowing.isNotEmpty()) myFollowing.size else (currentUser?.seguindo ?: 0)
                    Text(text = "$followingCount", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(text = "Seguindo", fontSize = 12.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botão Editar Perfil
            Button(
                onClick = onNavigateToEditProfile,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("edit_profile_button"),
                colors = ButtonDefaults.buttonColors(containerColor = RevelaPurple),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Editar Perfil", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Bio
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Bio", fontWeight = FontWeight.Bold, color = RevelaPurple, fontSize = 12.sp)
                    Text(
                        text = currentUser?.bio ?: "Nenhuma biografia adicionada.",
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Vibes Selecionadas
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Vibes: ", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                currentUser?.vibes?.forEach { vibe ->
                    Box(
                        modifier = Modifier
                            .background(RevelaPurple.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(text = vibe, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RevelaPurple)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // REQUISITO 7: SISTEMA DE BADGES (GAMIFICAÇÃO)
            Text(
                text = "Medalhas de Conexão 🏅",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                badges.forEach { badge ->
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = if (badge.earned) RevelaYellow.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = if (badge.earned) BorderStroke(1.5.dp, RevelaYellow) else null
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = badge.icon, fontSize = 28.sp)
                            Text(text = badge.title, fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.Center)
                            Text(
                                text = if (badge.earned) "Conquistado" else "Bloqueado",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (badge.earned) RevelaYellow else Color.Gray,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // REQUISITO 7: MISSÕES DIÁRIAS (GAMIFICAÇÃO)
            Text(
                text = "Missões Diárias 🗺️",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                missions.forEach { mission ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = if (mission.completed) Color.Green.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = if (mission.completed) Color.Green.copy(alpha = 0.3f) else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = if (mission.completed) "✅" else "⏳", fontSize = 18.sp, modifier = Modifier.padding(end = 12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = mission.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = "Progresso: ${mission.progress}", fontSize = 11.sp, color = Color.Gray)
                        }
                        if (mission.completed) {
                            Text("+50 XP", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Green)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Grid de Fotos Publicadas
            Text(
                text = "Minhas Postagens (${myPosts.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            )

            if (myPosts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Nenhuma foto publicada ainda.", color = Color.Gray, fontSize = 13.sp)
                }
            } else {
                // Grade responsiva de 3 colunas
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val chunks = myPosts.chunked(3)
                    chunks.forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            for (post in rowItems) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                ) {
                                    Image(
                                        painter = rememberAsyncImagePainter(post.imagemUrl),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                            // Preenchimento se houver menos de 3 itens na última linha
                            val fillCount = 3 - rowItems.size
                            for (i in 0 until fillCount) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }

    if (showFollowersDialog) {
        AlertDialog(
            onDismissRequest = { showFollowersDialog = false },
            title = {
                Text(
                    text = "Seguidores 👥",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                if (myFollowers.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Você ainda não tem seguidores.", color = Color.Gray, fontSize = 14.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(myFollowers) { rel ->
                            val followerUser = allUsers[rel.followerId]
                            val isAnon = rel.isAnonymous && !rel.isRevealed

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isAnon) Color.DarkGray else RevelaPurple.copy(alpha = 0.15f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isAnon) {
                                        Text("🎭", fontSize = 20.sp)
                                    } else {
                                        val foto = followerUser?.fotoPerfil ?: ""
                                        if (foto.isNotEmpty() && foto != "google_avatar") {
                                            Image(
                                                painter = rememberAsyncImagePainter(foto),
                                                contentDescription = null,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Text(
                                                text = followerUser?.nome?.firstOrNull()?.uppercase() ?: "?",
                                                fontWeight = FontWeight.Bold,
                                                color = RevelaPurple
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    if (isAnon) {
                                        Text(
                                            text = "Seguidor Anônimo",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.Lock,
                                                contentDescription = null,
                                                tint = Color.Gray,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Identidade protegida",
                                                fontSize = 11.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = followerUser?.nome ?: "Usuário",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "@${followerUser?.apelido ?: "anonimo"}",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                        if (rel.isAnonymous && rel.isRevealed) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = RevelaCoral,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "Revelado voluntariamente!",
                                                    fontSize = 10.sp,
                                                    color = RevelaCoral,
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
            },
            confirmButton = {
                Button(onClick = { showFollowersDialog = false }) {
                    Text("Fechar")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (showFollowingDialog) {
        val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
        AlertDialog(
            onDismissRequest = { showFollowingDialog = false },
            title = {
                Text(
                    text = "Seguindo 👥",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                if (myFollowing.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Você não segue ninguém ainda.", color = Color.Gray, fontSize = 14.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(myFollowing) { rel ->
                            val followedUser = allUsers[rel.followingId]
                            var isRevealingInProgress by remember { mutableStateOf(false) }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(RevelaPurple.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val foto = followedUser?.fotoPerfil ?: ""
                                    if (foto.isNotEmpty() && foto != "google_avatar") {
                                        Image(
                                            painter = rememberAsyncImagePainter(foto),
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Text(
                                            text = followedUser?.nome?.firstOrNull()?.uppercase() ?: "?",
                                            fontWeight = FontWeight.Bold,
                                            color = RevelaPurple
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = followedUser?.nome ?: "Usuário",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "@${followedUser?.apelido ?: "anonimo"}",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )

                                    if (rel.isAnonymous) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        if (rel.isRevealed) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = Color.Green,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "Sua identidade foi revelada!",
                                                    fontSize = 10.sp,
                                                    color = Color.Green,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        } else {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.Visibility,
                                                    contentDescription = null,
                                                    tint = RevelaCoral,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "Seguindo de forma anônima",
                                                    fontSize = 10.sp,
                                                    color = RevelaCoral
                                                )
                                            }
                                        }
                                    }
                                }

                                if (rel.isAnonymous && !rel.isRevealed) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    if (isRevealingInProgress) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                    } else {
                                        Button(
                                            onClick = {
                                                isRevealingInProgress = true
                                                coroutineScope.launch {
                                                    val result = RevelaRepository.revealFollowIdentity(rel.followId)
                                                    isRevealingInProgress = false
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = RevelaCoral),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Text("Revelar 🔓", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showFollowingDialog = false }) {
                    Text("Fechar")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}
