package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AdminPermissions
import com.example.data.Conversation
import com.example.data.RevelaRepository
import com.example.data.UserProfile
import com.example.data.UserReport
import com.example.ui.theme.RevelaCoral
import com.example.ui.theme.RevelaPurple
import com.example.ui.theme.RevelaTurquoise
import com.example.ui.theme.RevelaYellow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Gavel
import kotlinx.coroutines.launch

/**
 * Tela de Painel Administrativo / Moderação com Controle de Níveis de Administração (Autoridades Granulares)
 * Permite promover outros usuários e escolher cada autoridade de forma independente:
 * 1. Banir/Ativar Usuários (canBanUsers)
 * 2. Inspecionar Chats Privados (canViewChats)
 * 3. Excluir Posts do Feed (canDeletePosts)
 * 4. Gerenciar Outros ADMs (canManageAdmins)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen() {
    val coroutineScope = rememberCoroutineScope()
    val usersMap by RevelaRepository.users.collectAsState()
    val conversationsList by RevelaRepository.conversations.collectAsState()
    val postsList by RevelaRepository.posts.collectAsState()
    val messagesMap by RevelaRepository.messages.collectAsState()
    val reportsList by RevelaRepository.reports.collectAsState()
    val currentUser by RevelaRepository.currentUser.collectAsState()

    // Controle de abas
    var selectedTabState by remember { mutableIntStateOf(0) }
    val tabs = listOf("Usuários 👥", "Denúncias 🚨", "Conversas 💬", "Posts do Feed 🎭", "Atualizações 🚀")

    // Estado para saber qual usuário está com o painel de permissões expandido inline
    var expandedUserUidForPermissions by remember { mutableStateOf<String?>(null) }

    // Estado para exibir o histórico de mensagens de uma conversa selecionada
    var activeConversationForInspection by remember { mutableStateOf<Conversation?>(null) }

    // Mapeamento e fallback para garantir controle total ao Super Admin (frontwork08@gmail.com)
    val isSuperAdmin = currentUser?.email == "frontwork08@gmail.com"
    val canBan = currentUser?.adminPermissions?.canBanUsers == true || isSuperAdmin
    val canViewChats = currentUser?.adminPermissions?.canViewChats == true || isSuperAdmin
    val canDeletePosts = currentUser?.adminPermissions?.canDeletePosts == true || isSuperAdmin
    val canManageAdmins = currentUser?.adminPermissions?.canManageAdmins == true || isSuperAdmin

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
                        Text("Painel de Moderação 🛡️", fontWeight = FontWeight.Bold, fontSize = 20.sp)
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
            // Header informativo de Identidade e Direitos
            Card(
                colors = CardDefaults.cardColors(containerColor = RevelaPurple.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = if (isSuperAdmin) "Super Administrador Geral" else "Painel Administrativo",
                        fontWeight = FontWeight.Bold,
                        color = RevelaPurple,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Sessão: ${currentUser?.email ?: "Desconhecido"}",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    
                    // Resumo das próprias autoridades ativas
                    Row(
                        modifier = Modifier.padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (canBan) Box(modifier = Modifier.background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                            Text("⚡ Banir", fontSize = 9.sp, color = RevelaTurquoise, fontWeight = FontWeight.Bold)
                        }
                        if (canViewChats) Box(modifier = Modifier.background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                            Text("💬 Chats", fontSize = 9.sp, color = RevelaYellow, fontWeight = FontWeight.Bold)
                        }
                        if (canDeletePosts) Box(modifier = Modifier.background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                            Text("🎭 Posts", fontSize = 9.sp, color = RevelaCoral, fontWeight = FontWeight.Bold)
                        }
                        if (canManageAdmins) Box(modifier = Modifier.background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                            Text("🛡️ ADMs", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
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
                    // TAB 0: LISTA DE USUÁRIOS, NÍVEIS DE ADM & BANIMENTOS
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
                                val isSelf = user.uid == currentUser?.uid
                                val isBanned = user.status == "banido"
                                val isEditingThisUser = (expandedUserUidForPermissions == user.uid)

                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isBanned) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
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
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
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
                                                                .background(RevelaPurple, RoundedCornerShape(4.dp))
                                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                                        ) {
                                                            Text("ADM", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                        }
                                                    }
                                                }

                                                // Se for ADM, exibe o resumo de autoridades ativas para controle imediato
                                                if (user.isAdmin) {
                                                    val auths = mutableListOf<String>()
                                                    // Super admin sempre tem tudo
                                                    val isUserSuperAdmin = user.email == "frontwork08@gmail.com"
                                                    if (user.adminPermissions.canBanUsers || isUserSuperAdmin) auths.add("Banir")
                                                    if (user.adminPermissions.canViewChats || isUserSuperAdmin) auths.add("Chats")
                                                    if (user.adminPermissions.canDeletePosts || isUserSuperAdmin) auths.add("Posts")
                                                    if (user.adminPermissions.canManageAdmins || isUserSuperAdmin) auths.add("Gerenciar ADMs")

                                                    Text(
                                                        text = "Autoridades: ${if (auths.isEmpty()) "Nenhuma" else auths.joinToString(", ")}",
                                                        fontSize = 11.sp,
                                                        color = RevelaPurple,
                                                        fontWeight = FontWeight.SemiBold,
                                                        modifier = Modifier.padding(top = 6.dp)
                                                    )
                                                }
                                            }

                                            // Ações para o Usuário
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                // Botão para configurar permissões (apenas se tiver autoridade canManageAdmins e não for a si próprio)
                                                if (canManageAdmins && !isSelf && user.email != "frontwork08@gmail.com") {
                                                    IconButton(
                                                        onClick = {
                                                            expandedUserUidForPermissions = if (isEditingThisUser) null else user.uid
                                                        },
                                                        modifier = Modifier.testTag("edit_permissions_button_${user.uid}")
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Settings,
                                                            contentDescription = "Configurar Nível/Autoridades",
                                                            tint = RevelaPurple
                                                        )
                                                    }
                                                }

                                                // Botão para banir/unban (apenas se tiver autoridade canBan e não for a si próprio)
                                                if (!isSelf && user.email != "frontwork08@gmail.com") {
                                                    if (canBan) {
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
                                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                            modifier = Modifier.testTag("ban_button_${user.uid}")
                                                        ) {
                                                            Icon(
                                                                imageVector = if (isBanned) Icons.Default.LockOpen else Icons.Default.Lock,
                                                                contentDescription = null,
                                                                modifier = Modifier.size(14.dp).padding(end = 2.dp)
                                                            )
                                                            Text(
                                                                text = if (isBanned) "Ativar" else "Banir",
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                    } else {
                                                        // Informa de forma sutil que não possui autoridade para banir
                                                        Box(
                                                            modifier = Modifier
                                                                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
                                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Lock,
                                                                contentDescription = null,
                                                                tint = Color.Gray,
                                                                modifier = Modifier.size(14.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        // Painel de permissões expandido inline para gerenciamento
                                        if (isEditingThisUser) {
                                            var tempIsAdmin by remember(user.uid) { mutableStateOf(user.isAdmin) }
                                            var tempCanBan by remember(user.uid) { mutableStateOf(user.adminPermissions.canBanUsers) }
                                            var tempCanViewChats by remember(user.uid) { mutableStateOf(user.adminPermissions.canViewChats) }
                                            var tempCanDeletePosts by remember(user.uid) { mutableStateOf(user.adminPermissions.canDeletePosts) }
                                            var tempCanManageAdmins by remember(user.uid) { mutableStateOf(user.adminPermissions.canManageAdmins) }

                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(top = 12.dp)
                                                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                                    .padding(12.dp)
                                            ) {
                                                Text(
                                                    text = "Definir Nível e Autoridades de Moderação",
                                                    fontWeight = FontWeight.Bold,
                                                    color = RevelaPurple,
                                                    fontSize = 13.sp,
                                                    modifier = Modifier.padding(bottom = 8.dp)
                                                )

                                                // Ativação do Cargo
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column {
                                                        Text("Cargo de Administrador", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                                        Text("Habilita o acesso a este Painel de Moderação", fontSize = 10.sp, color = Color.Gray)
                                                    }
                                                    Switch(
                                                        checked = tempIsAdmin,
                                                        onCheckedChange = { tempIsAdmin = it },
                                                        modifier = Modifier.testTag("admin_role_switch_${user.uid}")
                                                    )
                                                }

                                                if (tempIsAdmin) {
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    
                                                    Text(
                                                        text = "Marque cada autoridade que deseja conceder:",
                                                        fontSize = 11.sp,
                                                        color = RevelaPurple,
                                                        fontWeight = FontWeight.SemiBold,
                                                        modifier = Modifier.padding(bottom = 6.dp)
                                                    )

                                                    // Opção: Banir Usuários
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clickable { tempCanBan = !tempCanBan }
                                                            .padding(vertical = 4.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Checkbox(
                                                            checked = tempCanBan, 
                                                            onCheckedChange = { tempCanBan = it },
                                                            modifier = Modifier.testTag("perm_ban_checkbox_${user.uid}")
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Column {
                                                            Text("Suspender / Reativar Contas ⚡", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                            Text("Permite banir e desbanir usuários cadastrados.", fontSize = 10.sp, color = Color.Gray)
                                                        }
                                                    }

                                                    // Opção: Inspecionar Conversas
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clickable { tempCanViewChats = !tempCanViewChats }
                                                            .padding(vertical = 4.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Checkbox(
                                                            checked = tempCanViewChats, 
                                                            onCheckedChange = { tempCanViewChats = it },
                                                            modifier = Modifier.testTag("perm_chats_checkbox_${user.uid}")
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Column {
                                                            Text("Inspecionar Conversas Privadas 💬", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                            Text("Permite ler chats e identificar remetentes anônimos em denúncias.", fontSize = 10.sp, color = Color.Gray)
                                                        }
                                                    }

                                                    // Opção: Excluir Posts
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clickable { tempCanDeletePosts = !tempCanDeletePosts }
                                                            .padding(vertical = 4.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Checkbox(
                                                            checked = tempCanDeletePosts, 
                                                            onCheckedChange = { tempCanDeletePosts = it },
                                                            modifier = Modifier.testTag("perm_posts_checkbox_${user.uid}")
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Column {
                                                            Text("Excluir Publicações do Feed 🎭", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                            Text("Permite apagar postagens inadequadas do feed geral.", fontSize = 10.sp, color = Color.Gray)
                                                        }
                                                    }

                                                    // Opção: Gerenciar outros ADMs
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clickable { tempCanManageAdmins = !tempCanManageAdmins }
                                                            .padding(vertical = 4.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Checkbox(
                                                            checked = tempCanManageAdmins, 
                                                            onCheckedChange = { tempCanManageAdmins = it },
                                                            modifier = Modifier.testTag("perm_manage_checkbox_${user.uid}")
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Column {
                                                            Text("Gerenciar Outros ADMs 🛡️", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                            Text("Permite promover novos administradores e alterar autoridades.", fontSize = 10.sp, color = Color.Gray)
                                                        }
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(10.dp))

                                                // Botões Salvar e Cancelar
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.End
                                                ) {
                                                    TextButton(onClick = { expandedUserUidForPermissions = null }) {
                                                        Text("Cancelar", color = Color.Gray, fontSize = 12.sp)
                                                    }
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Button(
                                                        onClick = {
                                                            coroutineScope.launch {
                                                                RevelaRepository.updateAdminPermissions(
                                                                    uid = user.uid,
                                                                    isAdmin = tempIsAdmin,
                                                                    permissions = AdminPermissions(
                                                                        canBanUsers = tempCanBan,
                                                                        canViewChats = tempCanViewChats,
                                                                        canDeletePosts = tempCanDeletePosts,
                                                                        canManageAdmins = tempCanManageAdmins
                                                                    )
                                                                )
                                                                expandedUserUidForPermissions = null
                                                            }
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = RevelaPurple),
                                                        shape = RoundedCornerShape(8.dp),
                                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                                        modifier = Modifier.testTag("save_permissions_button_${user.uid}")
                                                    ) {
                                                        Text("Salvar Nível 💾", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                1 -> {
                    // TAB 1: LISTA DE DENÚNCIAS & ANÁLISE GEMINI AI
                    if (reportsList.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Nenhuma denúncia cadastrada.", color = Color.Gray)
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize().testTag("admin_reports_list")
                        ) {
                            items(reportsList) { report ->
                                val reportedUser = usersMap[report.denunciadoId]
                                val isBanned = reportedUser?.status == "banido"
                                
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (report.status.contains("falsa_denuncia")) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                        else if (report.status.contains("válida")) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = if (report.status.contains("falsa_denuncia")) Icons.Default.CheckCircle
                                                    else if (report.status.contains("válida")) Icons.Default.Warning
                                                    else Icons.Default.Info,
                                                    contentDescription = "Status",
                                                    tint = if (report.status.contains("falsa_denuncia")) Color(0xFF4CAF50)
                                                    else if (report.status.contains("válida")) MaterialTheme.colorScheme.error
                                                    else Color.Gray,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = report.status.uppercase(),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp,
                                                    color = if (report.status.contains("falsa_denuncia")) Color(0xFF4CAF50)
                                                    else if (report.status.contains("válida")) MaterialTheme.colorScheme.error
                                                    else Color.Gray
                                                )
                                            }
                                            Text(
                                                text = android.text.format.DateFormat.format("dd/MM HH:mm", report.dataCriacao).toString(),
                                                fontSize = 10.sp,
                                                color = Color.Gray
                                            )
                                        }
                                        
                                        Spacer(modifier = Modifier.height(8.dp))
                                        
                                        Text(
                                            text = "Denunciado: ${reportedUser?.nome ?: "Desconhecido"} (@${reportedUser?.apelido ?: "anon"})",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                        
                                        Spacer(modifier = Modifier.height(4.dp))
                                        
                                        // Motivo / Descrição com quebra de linhas para ler justificativa do Gemini
                                        Text(
                                            text = report.motivo,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
                                            lineHeight = 16.sp
                                        )
                                        
                                        Spacer(modifier = Modifier.height(12.dp))
                                        
                                        // Ações rápidas sobre o usuário denunciado
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (reportedUser != null && reportedUser.email != "frontwork08@gmail.com") {
                                                Button(
                                                    onClick = {
                                                        coroutineScope.launch {
                                                            RevelaRepository.toggleBanUser(reportedUser.uid)
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = if (isBanned) Color.Gray else MaterialTheme.colorScheme.error
                                                    ),
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                                    modifier = Modifier.padding(end = 8.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Gavel,
                                                        contentDescription = "Banir",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(14.dp).padding(end = 4.dp)
                                                    )
                                                    Text(
                                                        text = if (isBanned) "Desbanir" else "Banir Infrator",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
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
                2 -> {
                    // TAB 2: LISTA DE CONVERSAS & LEITOR DE MENSAGENS (Verifica canViewChats)
                    if (!canViewChats) {
                        // Restrição amigável por falta de autoridades
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Bloqueado",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Acesso Restrito 🛡️",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Você não possui a autoridade 'Inspecionar Conversas Privadas' habilitada em seu nível de administrador para ler as mensagens privadas dos usuários.",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Solicite esta autoridade ao Administrador Geral (frontwork08@gmail.com).",
                                        fontSize = 11.sp,
                                        color = Color.Gray,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    } else {
                        // Exibe a visualização normal se o administrador tiver permissão
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
                                                        val realSender = usersMap[selectedConv.participantes.getOrNull(0)]
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
                }
                3 -> {
                    // TAB 2: POSTS DO FEED & EXCLUSÃO (Verifica canDeletePosts)
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

                                            if (canDeletePosts) {
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
                                            } else {
                                                // Informa sutilmente que não possui autoridade para excluir posts
                                                Box(
                                                    modifier = Modifier
                                                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Lock,
                                                        contentDescription = "Bloqueado",
                                                        tint = Color.Gray,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
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
                4 -> {
                    // TAB 4: ATUALIZAÇÕES DO APP EM TEMPO REAL (Opção 3)
                    var updateTitleInput by remember { mutableStateOf("Nova Versão Crítica Disponível!") }
                    var updateMessageInput by remember { mutableStateOf("Lançamos uma atualização crítica na loja de aplicativos para corrigir bugs e melhorar o sistema de moderação assistida por Inteligência Artificial (Gemini). Atualize imediatamente para continuar usando.") }
                    var isMandatoryInput by remember { mutableStateOf(true) }
                    val currentUpdateConfig by RevelaRepository.appUpdateConfig.collectAsState()

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .testTag("admin_updates_panel")
                    ) {
                        Text(
                            text = "Controle de Atualização em Tempo Real (Opção 3) 🚀",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Configure e dispare uma notificação/tela de atualização forçada ou opcional que se propaga IMEDIATAMENTE (em tempo real) para todos os usuários ativos do aplicativo.",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Status Atual do Servidor:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("• Alerta Ativo: ${if (currentUpdateConfig.active) "SIM 🟢" else "NÃO 🔴"}", fontSize = 13.sp)
                                Text("• Tipo de Forçamento: ${if (currentUpdateConfig.isMandatory) "Mandatório (Bloqueante) ⚠️" else "Opcional (Descartável) ✅"}", fontSize = 13.sp)
                                Text("• Título: ${currentUpdateConfig.updateTitle}", fontSize = 13.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = updateTitleInput,
                            onValueChange = { updateTitleInput = it },
                            label = { Text("Título da Atualização") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = updateMessageInput,
                            onValueChange = { updateMessageInput = it },
                            label = { Text("Mensagem Explicativa") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = isMandatoryInput,
                                onCheckedChange = { isMandatoryInput = it }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Forçar Atualização Mandatória", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("O usuário não poderá ignorar a tela de atualização para continuar navegando.", fontSize = 11.sp, color = Color.Gray)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    RevelaRepository.triggerSimulatedUpdate(
                                        isMandatory = isMandatoryInput,
                                        title = updateTitleInput,
                                        message = updateMessageInput
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Disparar Alerta 🚀")
                            }

                            OutlinedButton(
                                onClick = {
                                    RevelaRepository.dismissUpdate()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Parar / Resetar 🛑")
                            }
                        }
                    }
                }
            }
        }
    }
}
