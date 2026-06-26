package com.example.data

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.Date

object FirebaseSync {
    private const val TAG = "FirebaseSync_Revela"

    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore get() = FirebaseFirestore.getInstance()

    /**
     * Verifica se há um usuário Firebase autenticado e sincroniza-o com o repositório local.
     */
    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun getFirebaseUid(): String? {
        return auth.currentUser?.uid
    }

    /**
     * Salva ou atualiza um perfil de usuário no Firestore.
     */
    suspend fun saveUserProfile(profile: UserProfile): Boolean {
        return try {
            firestore.collection("users")
                .document(profile.uid)
                .set(profile.toMap())
                .await()
            Log.d(TAG, "Perfil de usuário ${profile.uid} salvo com sucesso no Firestore.")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao salvar perfil no Firestore: ", e)
            false
        }
    }

    /**
     * Carrega um perfil de usuário do Firestore.
     */
    suspend fun loadUserProfile(uid: String): UserProfile? {
        return try {
            val snapshot = firestore.collection("users").document(uid).get().await()
            if (snapshot.exists()) {
                val data = snapshot.data
                if (data != null) {
                    return data.toUserProfile()
                }
            }
            null
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao carregar perfil do Firestore: ", e)
            null
        }
    }

    /**
     * Carrega todos os perfis de usuário do Firestore.
     */
    suspend fun loadAllUsers(): List<UserProfile> {
        return try {
            val snapshot = firestore.collection("users").get().await()
            snapshot.documents.mapNotNull { it.data?.toUserProfile() }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao carregar usuários do Firestore: ", e)
            emptyList()
        }
    }

    /**
     * Salva uma postagem no Firestore.
     */
    suspend fun savePost(post: FeedPost): Boolean {
        return try {
            firestore.collection("posts")
                .document(post.postId)
                .set(post.toMap())
                .await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao salvar post no Firestore: ", e)
            false
        }
    }

    /**
     * Carrega todas as postagens do Firestore ordenadas por data de criação descrescente.
     */
    suspend fun loadAllPosts(): List<FeedPost> {
        return try {
            val snapshot = firestore.collection("posts")
                .orderBy("dataCriacao", Query.Direction.DESCENDING)
                .get()
                .await()
            snapshot.documents.mapNotNull { it.data?.toFeedPost() }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao carregar posts do Firestore: ", e)
            emptyList()
        }
    }

    /**
     * Salva um comentário de postagem no Firestore.
     */
    suspend fun saveComment(comment: PostComment): Boolean {
        return try {
            firestore.collection("comments")
                .document(comment.comentarioId)
                .set(comment.toMap())
                .await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao salvar comentário no Firestore: ", e)
            false
        }
    }

    /**
     * Carrega todos os comentários de uma postagem do Firestore.
     */
    suspend fun loadComments(postId: String): List<PostComment> {
        return try {
            val snapshot = firestore.collection("comments")
                .whereEqualTo("postId", postId)
                .orderBy("dataCriacao", Query.Direction.ASCENDING)
                .get()
                .await()
            snapshot.documents.mapNotNull { it.data?.toPostComment() }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao carregar comentários do Firestore: ", e)
            emptyList()
        }
    }

    /**
     * Salva uma conversa no Firestore.
     */
    suspend fun saveConversation(conversation: Conversation): Boolean {
        return try {
            firestore.collection("conversations")
                .document(conversation.conversaId)
                .set(conversation.toMap())
                .await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao salvar conversa no Firestore: ", e)
            false
        }
    }

    /**
     * Carrega as conversas do usuário autenticado do Firestore.
     */
    suspend fun loadUserConversations(uid: String): List<Conversation> {
        return try {
            val snapshot = firestore.collection("conversations")
                .whereArrayContains("participantes", uid)
                .get()
                .await()
            snapshot.documents.mapNotNull { it.data?.toConversation() }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao carregar conversas do Firestore: ", e)
            emptyList()
        }
    }

    /**
     * Salva uma mensagem no Firestore.
     */
    suspend fun saveMessage(message: ChatMessage): Boolean {
        return try {
            firestore.collection("messages")
                .document(message.mensagemId)
                .set(message.toMap())
                .await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao salvar mensagem no Firestore: ", e)
            false
        }
    }

    /**
     * Carrega mensagens de uma conversa específica do Firestore.
     */
    suspend fun loadMessages(conversaId: String): List<ChatMessage> {
        return try {
            val snapshot = firestore.collection("messages")
                .whereEqualTo("conversaId", conversaId)
                .orderBy("dataEnvio", Query.Direction.ASCENDING)
                .get()
                .await()
            snapshot.documents.mapNotNull { it.data?.toChatMessage() }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao carregar mensagens do Firestore: ", e)
            emptyList()
        }
    }

    /**
     * Salva um relatório de denúncia no Firestore.
     */
    suspend fun saveReport(report: UserReport): Boolean {
        return try {
            firestore.collection("reports")
                .document(report.denunciaId)
                .set(report.toMap())
                .await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao enviar denúncia para o Firestore: ", e)
            false
        }
    }

    // --- CONVERSÕES BULLETPROOF DE VIA MAP PARA DOCUMENTOS DO FIRESTORE ---

    private fun toDate(value: Any?): Date {
        return when (value) {
            is Timestamp -> value.toDate()
            is Long -> Date(value)
            is Map<*, *> -> {
                val sec = value["seconds"] as? Long ?: 0L
                val nan = value["nanoseconds"] as? Long ?: 0L
                Date(sec * 1000 + nan / 1000000)
            }
            else -> Date()
        }
    }

    fun UserProfile.toMap(): Map<String, Any?> {
        return mapOf(
            "uid" to uid,
            "nome" to nome,
            "apelido" to apelido,
            "email" to email,
            "bio" to bio,
            "genero" to genero,
            "dataNascimento" to dataNascimento,
            "idade" to idade,
            "cidade" to cidade,
            "estado" to estado,
            "sobreMim" to sobreMim,
            "fotoPerfil" to fotoPerfil,
            "vibes" to vibes,
            "interesses" to interesses,
            "seguidores" to seguidores,
            "seguindo" to seguindo,
            "dataCriacao" to dataCriacao,
            "permiteAnonimo" to permiteAnonimo,
            "bloqueados" to bloqueados,
            "denuncias" to denuncias,
            "status" to status,
            "isAdmin" to isAdmin,
            "adminPermissions" to mapOf(
                "canBanUsers" to adminPermissions.canBanUsers,
                "canViewChats" to adminPermissions.canViewChats,
                "canDeletePosts" to adminPermissions.canDeletePosts,
                "canManageAdmins" to adminPermissions.canManageAdmins
            ),
            "xp" to xp,
            "nivel" to nivel,
            "isPremium" to isPremium
        )
    }

    fun Map<String, Any?>.toUserProfile(): UserProfile {
        val adminPermsMap = this["adminPermissions"] as? Map<*, *>
        val adminPermissions = AdminPermissions(
            canBanUsers = adminPermsMap?.get("canBanUsers") as? Boolean ?: false,
            canViewChats = adminPermsMap?.get("canViewChats") as? Boolean ?: false,
            canDeletePosts = adminPermsMap?.get("canDeletePosts") as? Boolean ?: false,
            canManageAdmins = adminPermsMap?.get("canManageAdmins") as? Boolean ?: false
        )
        return UserProfile(
            uid = this["uid"] as? String ?: "",
            nome = this["nome"] as? String ?: "",
            apelido = this["apelido"] as? String ?: "",
            email = this["email"] as? String ?: "",
            bio = this["bio"] as? String ?: "",
            genero = this["genero"] as? String ?: "",
            dataNascimento = this["dataNascimento"] as? String ?: "2000-01-01",
            idade = (this["idade"] as? Long)?.toInt() ?: (this["idade"] as? Int) ?: 26,
            cidade = this["cidade"] as? String ?: "",
            estado = this["estado"] as? String ?: "",
            sobreMim = this["sobreMim"] as? String ?: "",
            fotoPerfil = this["fotoPerfil"] as? String ?: "",
            vibes = (this["vibes"] as? List<*>)?.map { it.toString() } ?: emptyList(),
            interesses = (this["interesses"] as? List<*>)?.map { it.toString() } ?: emptyList(),
            seguidores = (this["seguidores"] as? Long)?.toInt() ?: (this["seguidores"] as? Int) ?: 0,
            seguindo = (this["seguindo"] as? Long)?.toInt() ?: (this["seguindo"] as? Int) ?: 0,
            dataCriacao = toDate(this["dataCriacao"]),
            permiteAnonimo = this["permiteAnonimo"] as? Boolean ?: true,
            bloqueados = (this["bloqueados"] as? List<*>)?.map { it.toString() } ?: emptyList(),
            denuncias = (this["denuncias"] as? Long)?.toInt() ?: (this["denuncias"] as? Int) ?: 0,
            status = this["status"] as? String ?: "ativo",
            isAdmin = this["isAdmin"] as? Boolean ?: false,
            adminPermissions = adminPermissions,
            xp = (this["xp"] as? Long)?.toInt() ?: (this["xp"] as? Int) ?: 0,
            nivel = (this["nivel"] as? Long)?.toInt() ?: (this["nivel"] as? Int) ?: 1,
            isPremium = this["isPremium"] as? Boolean ?: false
        )
    }

    fun FeedPost.toMap(): Map<String, Any?> {
        return mapOf(
            "postId" to postId,
            "usuarioId" to usuarioId,
            "autorNome" to autorNome,
            "autorApelido" to autorApelido,
            "autorFoto" to autorFoto,
            "imagemUrl" to imagemUrl,
            "legenda" to legenda,
            "tags" to tags,
            "curtidas" to curtidas,
            "usuariosCurtiram" to usuariosCurtiram,
            "permiteComentarioAnonimo" to permiteComentarioAnonimo,
            "dataCriacao" to dataCriacao,
            "filterApplied" to filterApplied
        )
    }

    fun Map<String, Any?>.toFeedPost(): FeedPost {
        return FeedPost(
            postId = this["postId"] as? String ?: "",
            usuarioId = this["usuarioId"] as? String ?: "",
            autorNome = this["autorNome"] as? String ?: "",
            autorApelido = this["autorApelido"] as? String ?: "",
            autorFoto = this["autorFoto"] as? String ?: "",
            imagemUrl = this["imagemUrl"] as? String ?: "",
            legenda = this["legenda"] as? String ?: "",
            tags = (this["tags"] as? List<*>)?.map { it.toString() } ?: emptyList(),
            curtidas = (this["curtidas"] as? Long)?.toInt() ?: (this["curtidas"] as? Int) ?: 0,
            usuariosCurtiram = (this["usuariosCurtiram"] as? List<*>)?.map { it.toString() } ?: emptyList(),
            permiteComentarioAnonimo = this["permiteComentarioAnonimo"] as? Boolean ?: true,
            dataCriacao = toDate(this["dataCriacao"]),
            filterApplied = this["filterApplied"] as? String ?: "Normal"
        )
    }

    fun PostComment.toMap(): Map<String, Any?> {
        return mapOf(
            "comentarioId" to comentarioId,
            "postId" to postId,
            "usuarioId" to usuarioId,
            "autorNome" to autorNome,
            "autorFoto" to autorFoto,
            "texto" to texto,
            "isAnonimo" to isAnonimo,
            "dataCriacao" to dataCriacao
        )
    }

    fun Map<String, Any?>.toPostComment(): PostComment {
        return PostComment(
            comentarioId = this["comentarioId"] as? String ?: "",
            postId = this["postId"] as? String ?: "",
            usuarioId = this["usuarioId"] as? String,
            autorNome = this["autorNome"] as? String ?: "Alguém",
            autorFoto = this["autorFoto"] as? String ?: "🎭",
            texto = this["texto"] as? String ?: "",
            isAnonimo = this["isAnonimo"] as? Boolean ?: false,
            dataCriacao = toDate(this["dataCriacao"])
        )
    }

    fun Conversation.toMap(): Map<String, Any?> {
        return mapOf(
            "conversaId" to conversaId,
            "tipo" to tipo,
            "participantes" to participantes,
            "ultimaMensagem" to ultimaMensagem,
            "ultimaMensagemData" to ultimaMensagemData,
            "matchRevelado" to matchRevelado,
            "criadoEm" to criadoEm,
            "revelouUid1" to revelouUid1,
            "revelouUid2" to revelouUid2,
            "unreadCount" to unreadCount,
            "streakCount" to streakCount,
            "streakExpiring" to streakExpiring,
            "trustLevel" to trustLevel,
            "solicitouRevelacaoUid1" to solicitouRevelacaoUid1,
            "solicitouRevelacaoUid2" to solicitouRevelacaoUid2
        )
    }

    fun Map<String, Any?>.toConversation(): Conversation {
        return Conversation(
            conversaId = this["conversaId"] as? String ?: "",
            tipo = this["tipo"] as? String ?: "normal",
            participantes = (this["participantes"] as? List<*>)?.map { it.toString() } ?: emptyList(),
            ultimaMensagem = this["ultimaMensagem"] as? String ?: "",
            ultimaMensagemData = toDate(this["ultimaMensagemData"]),
            matchRevelado = this["matchRevelado"] as? Boolean ?: false,
            criadoEm = toDate(this["criadoEm"]),
            revelouUid1 = this["revelouUid1"] as? Boolean ?: false,
            revelouUid2 = this["revelouUid2"] as? Boolean ?: false,
            unreadCount = (this["unreadCount"] as? Long)?.toInt() ?: (this["unreadCount"] as? Int) ?: 0,
            streakCount = (this["streakCount"] as? Long)?.toInt() ?: (this["streakCount"] as? Int) ?: 0,
            streakExpiring = this["streakExpiring"] as? Boolean ?: false,
            trustLevel = (this["trustLevel"] as? Long)?.toInt() ?: (this["trustLevel"] as? Int) ?: 1,
            solicitouRevelacaoUid1 = this["solicitouRevelacaoUid1"] as? Boolean ?: false,
            solicitouRevelacaoUid2 = this["solicitouRevelacaoUid2"] as? Boolean ?: false
        )
    }

    fun ChatMessage.toMap(): Map<String, Any?> {
        return mapOf(
            "mensagemId" to mensagemId,
            "conversaId" to conversaId,
            "remetenteId" to remetenteId,
            "conteudo" to conteudo,
            "isAnonimo" to isAnonimo,
            "iconeAnonimo" to iconeAnonimo,
            "lida" to lida,
            "tipo" to tipo,
            "audioUrl" to audioUrl,
            "dataEnvio" to dataEnvio,
            "replyToId" to replyToId,
            "replyToText" to replyToText,
            "reactions" to reactions,
            "pollQuestion" to pollQuestion,
            "pollOptions" to pollOptions,
            "pollVotes" to pollVotes
        )
    }

    fun Map<String, Any?>.toChatMessage(): ChatMessage {
        val votesMap = (this["pollVotes"] as? Map<*, *>)?.entries?.associate {
            it.key.toString() to ((it.value as? Long)?.toInt() ?: (it.value as? Int) ?: 0)
        } ?: emptyMap()

        return ChatMessage(
            mensagemId = this["mensagemId"] as? String ?: "",
            conversaId = this["conversaId"] as? String ?: "",
            remetenteId = this["remetenteId"] as? String,
            conteudo = this["conteudo"] as? String ?: "",
            isAnonimo = this["isAnonimo"] as? Boolean ?: false,
            iconeAnonimo = this["iconeAnonimo"] as? String ?: "🎭",
            lida = this["lida"] as? Boolean ?: false,
            tipo = this["tipo"] as? String ?: "texto",
            audioUrl = this["audioUrl"] as? String ?: "",
            dataEnvio = toDate(this["dataEnvio"]),
            replyToId = this["replyToId"] as? String,
            replyToText = this["replyToText"] as? String,
            reactions = (this["reactions"] as? Map<*, *>)?.entries?.associate { it.key.toString() to it.value.toString() } ?: emptyMap(),
            pollQuestion = this["pollQuestion"] as? String,
            pollOptions = (this["pollOptions"] as? List<*>)?.map { it.toString() } ?: emptyList(),
            pollVotes = votesMap
        )
    }

    fun UserReport.toMap(): Map<String, Any?> {
        return mapOf(
            "denunciaId" to denunciaId,
            "denuncianteId" to denuncianteId,
            "denunciadoId" to denunciadoId,
            "mensagemId" to mensagemId,
            "motivo" to motivo,
            "status" to status,
            "dataCriacao" to dataCriacao
        )
    }
}
