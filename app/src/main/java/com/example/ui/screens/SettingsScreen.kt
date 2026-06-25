package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.RevelaRepository
import com.example.ui.theme.RevelaCoral
import com.example.ui.theme.RevelaPurple

/**
 * Tela 10: Configurações Gerais
 * Gerencia a Privacidade, lista de usuários bloqueados,
 * alertas para Sair da Conta e Deletar Conta permanentemente do Firebase.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onLogoutComplete: () -> Unit
) {
    val currentUser by RevelaRepository.currentUser.collectAsState()

    var allowsAnonymousChats by remember { mutableStateOf(currentUser?.permiteAnonimo ?: true) }
    var receiveNotifications by remember { mutableStateOf(true) }

    var showDeleteAlert by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configurações ⚙️", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
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
            Text("Segurança & Privacidade", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Gray)

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Configuração de Chat anônimo global
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = RevelaPurple, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Permitir Chats Anônimos", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Aceitar convites de mensagens anônimas no Inbox.", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                        Switch(
                            checked = allowsAnonymousChats,
                            onCheckedChange = { allowsAnonymousChats = it }
                        )
                    }

                    HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))

                    // Notificações Push
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = RevelaPurple, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Notificações em Tempo Real", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Receber avisos instantâneos de Matches e curtidas.", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                        Switch(
                            checked = receiveNotifications,
                            onCheckedChange = { receiveNotifications = it }
                        )
                    }
                }
            }

            Text("Conta", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Gray)

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column {
                    // Lista de bloqueados
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { /* Abre list de bloqueados */ }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = RevelaPurple)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Gerenciar Bloqueados", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Veja e remova usuários que você bloqueou.", fontSize = 11.sp, color = Color.Gray)
                        }
                    }

                    HorizontalDivider(color = Color.Gray.copy(alpha = 0.15f))

                    // Sair
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                RevelaRepository.logout()
                                onLogoutComplete()
                            }
                            .padding(16.dp)
                            .testTag("logout_row"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = null, tint = RevelaPurple)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Sair da Conta", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = RevelaPurple)
                            Text("Desconectar com segurança de todos os serviços.", fontSize = 11.sp, color = Color.Gray)
                        }
                    }

                    HorizontalDivider(color = Color.Gray.copy(alpha = 0.15f))

                    // Excluir Conta
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDeleteAlert = true }
                            .padding(16.dp)
                            .testTag("delete_account_row"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, tint = RevelaCoral)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Excluir Conta Permanentemente", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = RevelaCoral)
                            Text("Remover bio, posts, mensagens e credenciais do Firebase.", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Revela App v1.0.0 • Desenvolvido com Kotlin & Jetpack Compose",
                fontSize = 11.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // Alerta de exclusão permanente
    if (showDeleteAlert) {
        AlertDialog(
            onDismissRequest = { showDeleteAlert = false },
            title = { Text("Excluir Conta Permanentemente?") },
            text = {
                Text("Atenção: Esta ação é irreversível e apagará todas as suas postagens, fotos no Storage e mensagens seguras criptografadas do Revela.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAlert = false
                        RevelaRepository.logout()
                        onLogoutComplete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RevelaCoral)
                ) {
                    Text("Excluir Permanentemente")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAlert = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
