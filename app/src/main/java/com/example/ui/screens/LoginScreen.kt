package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.RevelaRepository
import com.example.ui.theme.RevelaCoral
import com.example.ui.theme.RevelaPurple
import kotlinx.coroutines.launch

/**
 * Tela 2: Login e Cadastro integrado
 * Suporta Login por E-mail, Cadastro Completo e Entrar com Google.
 * Inclui Loading, Tratamento de erros e Termos de Uso.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: (isNewUser: Boolean) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isSignUpMode by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Campos de texto
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var nome by remember { mutableStateOf("") }
    var apelido by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .testTag("login_screen_root"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Símbolo do App
            Text(
                text = "🎭",
                fontSize = 64.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Anonymous",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Text(
                text = if (isSignUpMode) "Crie sua conta anônima segura" else "Entre para se conectar anonimamente",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            if (errorMessage.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Formulário
            if (isSignUpMode) {
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome Completo") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("nome_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = apelido,
                    onValueChange = { apelido = it },
                    label = { Text("Apelido/Username (Único)") },
                    leadingIcon = { Text(" @ ", fontWeight = FontWeight.Bold, color = RevelaPurple) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("apelido_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("E-mail") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("email_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Senha") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
                    .testTag("senha_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Botão Principal de Ação
            Button(
                onClick = {
                    if (isSignUpMode) {
                        isLoading = true
                        errorMessage = ""
                        coroutineScope.launch {
                            val res = RevelaRepository.registerWithEmail(email, password, nome, apelido)
                            isLoading = false
                            if (res.isSuccess) {
                                onLoginSuccess(true) // Vai para Onboarding
                            } else {
                                errorMessage = res.exceptionOrNull()?.message ?: "Erro desconhecido."
                            }
                        }
                    } else {
                        isLoading = true
                        errorMessage = ""
                        coroutineScope.launch {
                            val res = RevelaRepository.loginWithEmail(email, password)
                            isLoading = false
                            if (res.isSuccess) {
                                onLoginSuccess(false) // Vai direto para o Feed
                            } else {
                                errorMessage = res.exceptionOrNull()?.message ?: "Usuário incorreto ou senha inválida."
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("submit_button"),
                colors = ButtonDefaults.buttonColors(containerColor = RevelaPurple),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = if (isSignUpMode) "Criar Conta Segura" else "Entrar na Rede",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Divisor para Login Google
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f))
                Text(
                    text = " ou ",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    modifier = Modifier.padding(horizontal = 12.dp),
                    fontSize = 12.sp
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f))
            }

            // Entrar com Google
            OutlinedButton(
                onClick = {
                    isLoading = true
                    errorMessage = ""
                    coroutineScope.launch {
                        val res = RevelaRepository.loginWithGoogle()
                        isLoading = false
                        if (res.isSuccess) {
                            onLoginSuccess(false)
                        } else {
                            errorMessage = res.exceptionOrNull()?.message ?: "Falha ao entrar com Google."
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("google_login_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = RevelaPurple),
                enabled = !isLoading
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🔴", fontSize = 16.sp, modifier = Modifier.padding(end = 8.dp))
                    Text("Entrar com Google", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Alternar Modo Login / Cadastro
            Text(
                text = if (isSignUpMode) "Já tem uma conta? Conectar-se" else "Novo por aqui? Criar conta",
                color = RevelaCoral,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable {
                        isSignUpMode = !isSignUpMode
                        errorMessage = ""
                    }
                    .testTag("toggle_mode_text"),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Termos de Uso
            Text(
                text = "Ao continuar, você concorda com os nossos Termos de Uso e Políticas de Privacidade de dados criptografados do Anonymous.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                textAlign = TextAlign.Center,
                lineHeight = 16.sp,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable { /* Abre Termos */ }
            )
        }
    }
}
