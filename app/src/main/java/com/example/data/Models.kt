package com.example.data

import java.util.Date

/**
 * Modelos de dados para o aplicativo Revela.
 * Representam exatamente a estrutura solicitada para o Firestore.
 */

data class AdminPermissions(
    val canBanUsers: Boolean = false,
    val canViewChats: Boolean = false,
    val canDeletePosts: Boolean = false,
    val canManageAdmins: Boolean = false
)

data class UserProfile(
    val uid: String,
    val nome: String,
    val apelido: String,
    val email: String,
    val bio: String,
    val genero: String = "",
    val dataNascimento: String = "2000-01-01",
    val idade: Int = 26,
    val cidade: String = "São Paulo",
    val estado: String = "SP",
    val sobreMim: String = "",
    val fotoPerfil: String = "", // URL ou iniciais decoradas
    val vibes: List<String> = listOf("🎵 Música", "🎮 Games", "📚 Leitura"),
    val interesses: List<String> = listOf("Tecnologia", "Café", "Séries", "Viagem"),
    val seguidores: Int = 124,
    val seguindo: Int = 89,
    val dataCriacao: Date = Date(),
    val permiteAnonimo: Boolean = true,
    val bloqueados: List<String> = emptyList(),
    val denuncias: Int = 0,
    val status: String = "ativo", // ativo ou banido
    val isAdmin: Boolean = false,
    val adminPermissions: AdminPermissions = AdminPermissions(),
    val xp: Int = 350,
    val nivel: Int = 2,
    val isPremium: Boolean = false
)

data class FeedPost(
    val postId: String,
    val usuarioId: String,
    val autorNome: String,
    val autorApelido: String,
    val autorFoto: String,
    val imagemUrl: String, // Filtro ou foto
    val legenda: String,
    val tags: List<String> = emptyList(), // Usuários marcados com @
    val curtidas: Int = 0,
    val usuariosCurtiram: List<String> = emptyList(),
    val permiteComentarioAnonimo: Boolean = true,
    val dataCriacao: Date = Date(),
    val filterApplied: String = "Normal"
)

data class PostComment(
    val comentarioId: String,
    val postId: String,
    val usuarioId: String?, // nulo se anônimo
    val autorNome: String = "Alguém",
    val autorFoto: String = "🎭",
    val texto: String,
    val isAnonimo: Boolean = false,
    val dataCriacao: Date = Date()
)

data class Conversation(
    val conversaId: String,
    val tipo: String, // 'normal' ou 'anonimo'
    val participantes: List<String>, // [uid1, uid2]
    val ultimaMensagem: String = "",
    val ultimaMensagemData: Date = Date(),
    val matchRevelado: Boolean = false,
    val criadoEm: Date = Date(),
    val revelouUid1: Boolean = false,
    val revelouUid2: Boolean = false,
    val unreadCount: Int = 0,
    val streakCount: Int = 0,
    val streakExpiring: Boolean = false,
    val trustLevel: Int = 1,
    val solicitouRevelacaoUid1: Boolean = false,
    val solicitouRevelacaoUid2: Boolean = false
)

data class ChatMessage(
    val mensagemId: String,
    val conversaId: String,
    val remetenteId: String?, // nulo se anônimo
    val conteudo: String,
    val isAnonimo: Boolean = false,
    val iconeAnonimo: String = "🎭",
    val lida: Boolean = false,
    val tipo: String = "texto", // 'texto', 'audio', 'imagem_preset', 'enquete'
    val audioUrl: String = "",
    val dataEnvio: Date = Date(),
    val replyToId: String? = null,
    val replyToText: String? = null,
    val reactions: Map<String, String> = emptyMap(), // Key: userId -> Emoji string (e.g. "❤️")
    val pollQuestion: String? = null,
    val pollOptions: List<String> = emptyList(),
    val pollVotes: Map<String, Int> = emptyMap() // Key: userId -> Option index
)

data class AppNotification(
    val notificacaoId: String,
    val usuarioId: String, // Destinatário
    val tipo: String, // 'match', 'curtida', 'comentario', 'mensagem'
    val remetenteId: String,
    val remetenteNome: String,
    val conteudo: String,
    val lida: Boolean = false,
    val dataCriacao: Date = Date()
)

data class UserReport(
    val denunciaId: String,
    val denuncianteId: String,
    val denunciadoId: String,
    val mensagemId: String = "",
    val motivo: String,
    val status: String = "pendente", // pendente, analisada, banida
    val dataCriacao: Date = Date()
)

data class UserBadge(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val earned: Boolean
)

data class DailyMission(
    val id: String,
    val title: String,
    val completed: Boolean,
    val progress: String
)

data class AppUpdateConfig(
    val latestVersionCode: Int,
    val minRequiredVersionCode: Int,
    val updateUrl: String,
    val updateTitle: String,
    val updateMessage: String,
    val isMandatory: Boolean,
    val active: Boolean = false
)

data class FollowRelationship(
    val followId: String,
    val followerId: String,
    val followingId: String,
    val isAnonymous: Boolean,
    val isRevealed: Boolean,
    val timestamp: Date = Date()
)

