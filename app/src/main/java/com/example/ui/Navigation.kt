package com.example.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.RevelaRepository
import com.example.data.UserProfile
import com.example.ui.screens.*
import com.example.ui.theme.RevelaPurple
import kotlinx.coroutines.launch

/**
 * Arquivo de Navegação e Arquitetura Central do Aplicativo Revela.
 * Define rotas, gerencia backstack e unifica telas integrando com
 * a Barra de Navegação Inferior (Bottom Navigation) no padrão Material 3.
 */
@Composable
fun RevelaAppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "splash",
        modifier = Modifier.fillMaxSize()
    ) {
        // 1. Tela Splash
        composable("splash") {
            SplashScreen(
                onSplashComplete = {
                    val user = RevelaRepository.currentUser.value
                    if (user != null) {
                        if (user.status == "banido") {
                            RevelaRepository.logout()
                            navController.navigate("login") {
                                popUpTo("splash") { inclusive = true }
                            }
                        } else {
                            navController.navigate("main") {
                                popUpTo("splash") { inclusive = true }
                            }
                        }
                    } else {
                        navController.navigate("login") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                }
            )
        }

        // 2. Tela Login
        composable("login") {
            LoginScreen(
                onLoginSuccess = { isNewUser ->
                    if (isNewUser) {
                        navController.navigate("onboarding") {
                            popUpTo("login") { inclusive = true }
                        }
                    } else {
                        navController.navigate("main") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                }
            )
        }

        // 3. Tela Onboarding
        composable("onboarding") {
            OnboardingScreen(
                onOnboardingComplete = {
                    navController.navigate("main") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }

        // 4. Tela Central Main (Contém Abas Feed, Mensagens, Perfil)
        composable("main") {
            MainContainerScreen(
                onNavigateToPost = { navController.navigate("post") },
                onNavigateToChat = { conversaId -> navController.navigate("chat/$conversaId") },
                onNavigateToChatWithUser = { otherUser ->
                    // Abre conversa com este usuário de forma anônima por padrão para puxar as amizades
                    com.example.data.RevelaRepository.apply {
                        // Cria conversa anônima reativa e navega para lá
                        kotlinx.coroutines.MainScope().run {
                            val scope = this
                            scope.launch {
                                val result = createNewConversation(otherUser.uid, isAnonimo = true)
                                if (result.isSuccess) {
                                    val conversaId = result.getOrNull()?.conversaId ?: ""
                                    navController.navigate("chat/$conversaId")
                                }
                            }
                        }
                    }
                },
                onNavigateToEditProfile = { navController.navigate("edit_profile") },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }

        // 5. Tela de Chat Individual
        composable(
            route = "chat/{conversaId}",
            arguments = listOf(navArgument("conversaId") { type = NavType.StringType })
        ) { backStackEntry ->
            val conversaId = backStackEntry.arguments?.getString("conversaId") ?: ""
            ChatScreen(
                conversaId = conversaId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 6. Tela Editar Perfil
        composable("edit_profile") {
            EditProfileScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 7. Tela Postar Foto
        composable("post") {
            PostScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 8. Tela de Configurações
        composable("settings") {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onLogoutComplete = {
                    navController.navigate("login") {
                        popUpTo("main") { inclusive = true }
                    }
                }
            )
        }
    }
}

/**
 * Container Central do Aplicativo que gerencia as Abas Inferiores
 * usando o componente Scaffold e NavigationBar do Material Design 3.
 */
@Composable
fun MainContainerScreen(
    onNavigateToPost: () -> Unit,
    onNavigateToChat: (String) -> Unit,
    onNavigateToChatWithUser: (UserProfile) -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val currentUser by RevelaRepository.currentUser.collectAsState()
    val isAdmin = currentUser?.isAdmin == true
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)),
                tonalElevation = 8.dp
            ) {
                // Aba 1: Feed Principal
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Feed") },
                    label = { Text("Feed") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF21005D),
                        selectedTextColor = MaterialTheme.colorScheme.onBackground,
                        indicatorColor = Color(0xFFEADDFF),
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag("tab_feed")
                )

                // Aba 3: Conversas / Inbox
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.ChatBubble, contentDescription = "Mensagens") },
                    label = { Text("Conversas") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF21005D),
                        selectedTextColor = MaterialTheme.colorScheme.onBackground,
                        indicatorColor = Color(0xFFEADDFF),
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag("tab_conversations")
                )

                // Aba 2: Perfil do Usuário
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF21005D),
                        selectedTextColor = MaterialTheme.colorScheme.onBackground,
                        indicatorColor = Color(0xFFEADDFF),
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag("tab_profile")
                )

                // Aba 4: Administrador (Opcional)
                if (isAdmin) {
                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        icon = { Icon(Icons.Default.Lock, contentDescription = "Painel Admin") },
                        label = { Text("Admin") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF21005D),
                            selectedTextColor = MaterialTheme.colorScheme.onBackground,
                            indicatorColor = Color(0xFFEADDFF),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("tab_admin")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> MainFeedScreen(
                    onNavigateToPost = onNavigateToPost,
                    onNavigateToChatWithUser = onNavigateToChatWithUser,
                    onNavigateToProfile = { selectedTab = 2 }
                )
                1 -> ConversationsScreen(
                    onNavigateToChat = onNavigateToChat
                )
                2 -> ProfileScreen(
                    onNavigateToEditProfile = onNavigateToEditProfile,
                    onNavigateToSettings = onNavigateToSettings
                )
                3 -> if (isAdmin) {
                    AdminPanelScreen()
                }
            }
        }
    }
}
