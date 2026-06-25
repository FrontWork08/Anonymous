package com.example.data

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

/**
 * Repositório Central "Revela" que simula o comportamento completo do Firebase
 * (Auth, Firestore, Realtime Database e Storage) com persistência reativa,
 * cache local, e respostas automáticas para fins de demonstração no Emulador.
 * 
 * Todas as operações possuem delay simulado para Loading Indicators
 * e comentários explicativos em português como solicitado.
 */
object RevelaRepository {

    private val scope = CoroutineScope(Dispatchers.Default)

    // --- ESTADO REATIVO (MOCK DO FIRESTORE E REALTIME DATABASE) ---
    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    private val _users = MutableStateFlow<Map<String, UserProfile>>(emptyMap())
    val users: StateFlow<Map<String, UserProfile>> = _users.asStateFlow()

    private val _posts = MutableStateFlow<List<FeedPost>>(emptyList())
    val posts: StateFlow<List<FeedPost>> = _posts.asStateFlow()

    private val _comments = MutableStateFlow<Map<String, List<PostComment>>>(emptyMap()) // Key: postId
    val comments: StateFlow<Map<String, List<PostComment>>> = _comments.asStateFlow()

    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    private val _messages = MutableStateFlow<Map<String, List<ChatMessage>>>(emptyMap()) // Key: conversaId
    val messages: StateFlow<Map<String, List<ChatMessage>>> = _messages.asStateFlow()

    private val _reports = MutableStateFlow<List<UserReport>>(emptyList())
    val reports: StateFlow<List<UserReport>> = _reports.asStateFlow()

    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    private val _badges = MutableStateFlow<List<UserBadge>>(emptyList())
    val badges: StateFlow<List<UserBadge>> = _badges.asStateFlow()

    private val _missions = MutableStateFlow<List<DailyMission>>(emptyList())
    val missions: StateFlow<List<DailyMission>> = _missions.asStateFlow()

    private val _typingState = MutableStateFlow<Map<String, Boolean>>(emptyMap()) // Key: conversaId -> IsTyping
    val typingState: StateFlow<Map<String, Boolean>> = _typingState.asStateFlow()

    private val _appUpdateConfig = MutableStateFlow<AppUpdateConfig>(
        AppUpdateConfig(
            latestVersionCode = 1,
            minRequiredVersionCode = 1,
            updateUrl = "https://example.com/update",
            updateTitle = "Atualização Disponível",
            updateMessage = "Uma nova versão do Anonymous está disponível na Play Store com várias correções e o sistema de moderação Gemini AI!",
            isMandatory = false,
            active = false
        )
    )
    val appUpdateConfig: StateFlow<AppUpdateConfig> = _appUpdateConfig.asStateFlow()

    fun triggerSimulatedUpdate(isMandatory: Boolean, title: String, message: String) {
        _appUpdateConfig.value = AppUpdateConfig(
            latestVersionCode = 2,
            minRequiredVersionCode = if (isMandatory) 2 else 1,
            updateUrl = "https://ai.studio/build",
            updateTitle = title,
            updateMessage = message,
            isMandatory = isMandatory,
            active = true
        )
    }

    fun dismissUpdate() {
        _appUpdateConfig.value = _appUpdateConfig.value.copy(active = false)
    }

    private val _follows = MutableStateFlow<List<FollowRelationship>>(emptyList())
    val follows: StateFlow<List<FollowRelationship>> = _follows.asStateFlow()

    suspend fun followUser(targetUid: String, isAnonymous: Boolean): Result<Boolean> {
        delay(800)
        val current = _currentUser.value ?: return Result.failure(Exception("Usuário não autenticado."))
        if (current.uid == targetUid) return Result.failure(Exception("Você não pode seguir a si mesmo."))

        // Verifica se já segue
        val alreadyFollowing = _follows.value.any { it.followerId == current.uid && it.followingId == targetUid }
        if (alreadyFollowing) {
            return Result.failure(Exception("Você já segue este usuário."))
        }

        val followId = java.util.UUID.randomUUID().toString()
        val newFollow = FollowRelationship(
            followId = followId,
            followerId = current.uid,
            followingId = targetUid,
            isAnonymous = isAnonymous,
            isRevealed = !isAnonymous,
            timestamp = Date()
        )

        _follows.value = _follows.value + newFollow

        // Incrementa contadores
        val targetUser = _users.value[targetUid]
        if (targetUser != null) {
            val updatedTarget = targetUser.copy(seguidores = targetUser.seguidores + 1)
            _users.value = _users.value + (targetUid to updatedTarget)
        }

        val updatedCurrent = current.copy(seguindo = current.seguindo + 1)
        _currentUser.value = updatedCurrent
        _users.value = _users.value + (current.uid to updatedCurrent)

        // Envia notificação
        sendNotification(
            usuarioId = targetUid,
            tipo = "curtida",
            conteudo = if (isAnonymous) "👤 Um novo usuário começou a te seguir anonimamente!" else "👤 @${current.apelido} começou a te seguir!"
        )

        return Result.success(true)
    }

    suspend fun unfollowUser(targetUid: String): Result<Boolean> {
        delay(800)
        val current = _currentUser.value ?: return Result.failure(Exception("Usuário não autenticado."))
        
        val followRel = _follows.value.find { it.followerId == current.uid && it.followingId == targetUid }
        if (followRel == null) {
            return Result.failure(Exception("Você não segue este usuário."))
        }

        _follows.value = _follows.value.filter { it.followId != followRel.followId }

        // Decrementa contadores
        val targetUser = _users.value[targetUid]
        if (targetUser != null) {
            val updatedTarget = targetUser.copy(seguidores = (targetUser.seguidores - 1).coerceAtLeast(0))
            _users.value = _users.value + (targetUid to updatedTarget)
        }

        val updatedCurrent = current.copy(seguindo = (current.seguindo - 1).coerceAtLeast(0))
        _currentUser.value = updatedCurrent
        _users.value = _users.value + (current.uid to updatedCurrent)

        return Result.success(true)
    }

    suspend fun revealFollowIdentity(followId: String): Result<Boolean> {
        delay(800)
        val current = _currentUser.value ?: return Result.failure(Exception("Usuário não autenticado."))

        val rel = _follows.value.find { it.followId == followId }
        if (rel == null) {
            return Result.failure(Exception("Relação não encontrada."))
        }

        if (rel.followerId != current.uid) {
            return Result.failure(Exception("Apenas o seguidor anônimo pode revelar sua identidade."))
        }

        val updatedRel = rel.copy(isRevealed = true)
        _follows.value = _follows.value.map { if (it.followId == followId) updatedRel else it }

        // Envia notificação sobre a revelação
        val targetUser = _users.value[rel.followingId]
        if (targetUser != null) {
            sendNotification(
                usuarioId = rel.followingId,
                tipo = "match",
                conteudo = "🎉 O seguidor anônimo se revelou! É o @${current.apelido}!"
            )
        }

        return Result.success(true)
    }

    // --- PALAVRAS PROIBIDAS PARA FILTRAGEM (REQUISITO DE SEGURANÇA 6) ---
    private val badWords = listOf("ofensa", "palavrao", "idiota", "lixo", "fake", "spam", "imbecil", "otario")

    init {
        // Inicializa dados padrão para o aplicativo começar rico e interativo
        prepopulateData()
    }

    // --- 1. SISTEMA DE AUTENTICAÇÃO (MOCK FIREBASE AUTH) ---

    suspend fun loginWithEmail(email: String, password: String): Result<UserProfile> {
        delay(1200) // Simula latência de rede do Firebase Auth
        if (email.isEmpty() || password.isEmpty()) {
            return Result.failure(Exception("E-mail e senha são obrigatórios."))
        }
        // Se for o admin tentando logar
        if (email == "frontwork08@gmail.com" && password == "Gui61199262@") {
            var adminUser = _users.value.values.find { it.email == email }
            if (adminUser == null) {
                adminUser = UserProfile(
                    uid = "admin_uid_123",
                    nome = "Administrador",
                    apelido = "admin",
                    email = email,
                    bio = "Painel de Controle do Aplicativo Anonymous",
                    isAdmin = true,
                    adminPermissions = AdminPermissions(
                        canBanUsers = true,
                        canViewChats = true,
                        canDeletePosts = true,
                        canManageAdmins = true
                    )
                )
                addUserToDatabase(adminUser)
            } else if (!adminUser.isAdmin || !adminUser.adminPermissions.canManageAdmins) {
                adminUser = adminUser.copy(
                    isAdmin = true,
                    adminPermissions = AdminPermissions(
                        canBanUsers = true,
                        canViewChats = true,
                        canDeletePosts = true,
                        canManageAdmins = true
                    )
                )
                addUserToDatabase(adminUser)
            }
            _currentUser.value = adminUser
            updateMissionsProgress()
            return Result.success(adminUser)
        }
        val existingUser = _users.value.values.find { it.email == email }
        if (existingUser != null) {
            if (existingUser.email == "frontwork08@gmail.com") {
                return Result.failure(Exception("Senha de Administrador incorreta."))
            }
            if (existingUser.status == "banido") {
                return Result.failure(Exception("Sua conta foi banida por violar as políticas do app."))
            }
            _currentUser.value = existingUser
            updateMissionsProgress()
            return Result.success(existingUser)
        }
        return Result.failure(Exception("Usuário não encontrado ou senha inválida."))
    }

    suspend fun loginWithGoogle(): Result<UserProfile> {
        delay(1500) // Simula fluxo de seleção de conta Google
        // Cria ou recupera o usuário "frontwork08@gmail.com" do ADDTIONAL_METADATA
        val email = "frontwork08@gmail.com"
        val existingUser = _users.value.values.find { it.email == email }
        if (existingUser != null) {
            val updatedUser = if (!existingUser.isAdmin || !existingUser.adminPermissions.canManageAdmins) {
                val updated = existingUser.copy(
                    isAdmin = true,
                    adminPermissions = AdminPermissions(
                        canBanUsers = true,
                        canViewChats = true,
                        canDeletePosts = true,
                        canManageAdmins = true
                    )
                )
                addUserToDatabase(updated)
                updated
            } else {
                existingUser
            }
            _currentUser.value = updatedUser
            updateMissionsProgress()
            return Result.success(updatedUser)
        }
        
        // Se novo, registra perfil com as credenciais do Google
        val newUser = UserProfile(
            uid = "google_user_123",
            nome = "Frontwork Developer",
            apelido = "frontwork",
            email = email,
            bio = "Criando conexões autênticas e seguras.",
            cidade = "São Paulo",
            estado = "SP",
            fotoPerfil = "google_avatar",
            isAdmin = true,
            adminPermissions = AdminPermissions(
                canBanUsers = true,
                canViewChats = true,
                canDeletePosts = true,
                canManageAdmins = true
            )
        )
        addUserToDatabase(newUser)
        _currentUser.value = newUser
        updateMissionsProgress()
        return Result.success(newUser)
    }

    suspend fun registerWithEmail(email: String, password: String, nome: String, apelido: String): Result<UserProfile> {
        delay(1500)
        if (email.isEmpty() || password.isEmpty() || nome.isEmpty() || apelido.isEmpty()) {
            return Result.failure(Exception("Preencha todos os campos obrigatórios."))
        }
        if (_users.value.values.any { it.apelido.lowercase() == apelido.lowercase() }) {
            return Result.failure(Exception("O apelido/username @$apelido já está em uso."))
        }

        val newUser = UserProfile(
            uid = UUID.randomUUID().toString(),
            nome = nome,
            apelido = apelido,
            email = email,
            bio = "Olá! Acabei de me juntar ao Anonymous 🎭"
        )
        addUserToDatabase(newUser)
        _currentUser.value = newUser
        updateMissionsProgress()
        return Result.success(newUser)
    }

    fun logout() {
        _currentUser.value = null
    }

    // --- 2. PERFIL DO USUÁRIO (MOCK FIRESTORE E STORAGE) ---

    suspend fun updateProfile(
        nome: String,
        apelido: String,
        bio: String,
        genero: String,
        dataNascimento: String,
        idade: Int,
        cidade: String,
        estado: String,
        sobreMim: String,
        vibes: List<String>,
        fotoLocalPath: String // Simula upload para o Firebase Storage
    ): Result<UserProfile> {
        delay(1800) // Simula upload da foto e escrita no Firestore
        val current = _currentUser.value ?: return Result.failure(Exception("Usuário não autenticado."))

        // Verifica se apelido é único (excluindo si mesmo)
        if (_users.value.values.any { it.uid != current.uid && it.apelido.lowercase() == apelido.lowercase() }) {
            return Result.failure(Exception("O apelido @$apelido já está em uso."))
        }

        val updated = current.copy(
            nome = nome,
            apelido = apelido,
            bio = bio.take(500),
            genero = genero,
            dataNascimento = dataNascimento,
            idade = idade,
            cidade = cidade,
            estado = estado,
            sobreMim = sobreMim,
            vibes = vibes.take(3), // Limite de 3 ícones vibes
            fotoPerfil = if (fotoLocalPath.isNotEmpty()) fotoLocalPath else current.fotoPerfil
        )

        // Atualiza bases de dados
        _currentUser.value = updated
        _users.value = _users.value + (updated.uid to updated)
        return Result.success(updated)
    }

    // --- 3. SISTEMA DE FEED E POSTS (MOCK FIRESTORE) ---

    suspend fun createPost(imagemUrl: String, legenda: String, filterName: String, permiteAnonimos: Boolean): Result<FeedPost> {
        delay(2000) // Simula compressão e upload da foto do post para /posts/[postId].jpg
        val current = _currentUser.value ?: return Result.failure(Exception("Autentique-se primeiro."))

        // Restrição de spam (simulada)
        val postsHoje = _posts.value.filter { it.usuarioId == current.uid }
        if (postsHoje.size >= 3) {
            return Result.failure(Exception("Segurança: Limite de 3 posts por dia atingido para evitar spam."))
        }

        // Extrai marcações @
        val tags = legenda.split(" ").filter { it.startsWith("@") }.map { it.removePrefix("@") }

        val newPost = FeedPost(
            postId = UUID.randomUUID().toString(),
            usuarioId = current.uid,
            autorNome = current.nome,
            autorApelido = current.apelido,
            autorFoto = current.fotoPerfil,
            imagemUrl = imagemUrl,
            legenda = legenda,
            tags = tags,
            permiteComentarioAnonimo = permiteAnonimos,
            filterApplied = filterName,
            dataCriacao = Date()
        )

        _posts.value = listOf(newPost) + _posts.value
        _comments.value = _comments.value + (newPost.postId to emptyList())

        // Gamificação
        updateBadgeProgress("Popular", 1) // Progresso para o dono
        updateMissionsProgress()

        return Result.success(newPost)
    }

    suspend fun toggleLikePost(postId: String): Result<Boolean> {
        val current = _currentUser.value ?: return Result.failure(Exception("Faça login."))
        val postList = _posts.value.map { post ->
            if (post.postId == postId) {
                val jáCurtiu = post.usuariosCurtiram.contains(current.uid)
                val novasCurtidas = if (jáCurtiu) {
                    post.usuariosCurtiram.filter { it != current.uid }
                } else {
                    post.usuariosCurtiram + current.uid
                }
                val totalLikes = novasCurtidas.size
                post.copy(
                    curtidas = totalLikes,
                    usuariosCurtiram = novasCurtidas
                )
            } else {
                post
            }
        }
        _posts.value = postList
        return Result.success(true)
    }

    suspend fun addComment(postId: String, texto: String, isAnonimo: Boolean): Result<PostComment> {
        delay(600)
        val current = _currentUser.value ?: return Result.failure(Exception("Logue para comentar."))
        val post = _posts.value.find { it.postId == postId } ?: return Result.failure(Exception("Post não encontrado."))

        if (isAnonimo && !post.permiteComentarioAnonimo) {
            return Result.failure(Exception("O autor deste post desativou comentários anônimos."))
        }

        // Aplica filtro de profanidade em comentários anônimos (Requisito 6)
        val conteudoFiltrado = applyProfanityFilter(texto)

        val newComment = PostComment(
            comentarioId = UUID.randomUUID().toString(),
            postId = postId,
            usuarioId = if (isAnonimo) null else current.uid,
            autorNome = if (isAnonimo) "Alguém" else current.nome,
            autorFoto = if (isAnonimo) "🎭" else current.fotoPerfil,
            texto = conteudoFiltrado,
            isAnonimo = isAnonimo,
            dataCriacao = Date()
        )

        val currentComments = _comments.value[postId] ?: emptyList()
        _comments.value = _comments.value + (postId to (currentComments + newComment))

        // Se comentou na postagem de outra pessoa, notifica
        if (post.usuarioId != current.uid) {
            sendNotification(
                usuarioId = post.usuarioId,
                tipo = "comentario",
                conteudo = "${if (isAnonimo) "Alguém de forma anônima" else current.nome} comentou no seu post: \"${conteudoFiltrado.take(30)}...\""
            )
        }

        return Result.success(newComment)
    }

    // --- 4. SISTEMA DE MENSAGENS CHAT & MODO SURPRESA ---

    suspend fun sendMessage(conversaId: String, texto: String, isAudio: Boolean = false, audioLocalPath: String = ""): Result<ChatMessage> {
        val current = _currentUser.value ?: return Result.failure(Exception("Faça login."))
        val conversa = _conversations.value.find { it.conversaId == conversaId } ?: return Result.failure(Exception("Conversa não encontrada."))

        // Aplica filtro de profanidade se o chat for anônimo (Requisito 6)
        val isAnonimoChat = conversa.tipo == "anonimo"
        val filteredText = if (isAnonimoChat) applyProfanityFilter(texto) else texto

        // Encriptação simulada em servidor (Requisito 5)
        Log.d("Security_Revela", "Criptografando mensagem no servidor para conversa $conversaId...")

        val newMessage = ChatMessage(
            mensagemId = UUID.randomUUID().toString(),
            conversaId = conversaId,
            remetenteId = if (isAnonimoChat && !conversa.matchRevelado) null else current.uid,
            conteudo = if (isAudio) "🎵 Mensagem de Voz" else filteredText,
            isAnonimo = isAnonimoChat && !conversa.matchRevelado,
            iconeAnonimo = "🎭",
            tipo = if (isAudio) "audio" else "texto",
            audioUrl = audioLocalPath,
            dataEnvio = Date()
        )

        // Adiciona à lista de mensagens do chat
        val chatList = _messages.value[conversaId] ?: emptyList()
        val updatedMessages = chatList + newMessage
        _messages.value = _messages.value + (conversaId to updatedMessages)

        // Atualiza conversa com última mensagem
        updateConversationLastMessage(conversaId, newMessage.conteudo, newMessage.dataEnvio)

        // Verifica metas de gamificação
        updateBadgeProgress("Mensageiro", 1)
        updateMissionsProgress()

        // --- SIMULAÇÃO DE AUTO-RESPOSTA / CHATBOT PARA O EMULADOR ---
        // Permite ao usuário interagir e ver em tempo real o indicador "Mariana está digitando..." e responder
        val destinatarioId = conversa.participantes.find { it != current.uid } ?: ""
        val destinatario = _users.value[destinatarioId]
        if (destinatario != null && destinatarioId != "google_user_123") {
            triggerAutomatedReply(conversaId, destinatario, updatedMessages.size)
        }

        return Result.success(newMessage)
    }

    /**
     * Requisito de Segurança 6: Guarda o IP simulado do remetente
     * e o envia juntamente com a denúncia no servidor.
     * Agora integrado com o Gemini AI para analisar falsas denúncias de forma inteligente.
     */
    suspend fun reportMessage(mensagemId: String, motivo: String): Result<Boolean> {
        delay(1000)
        val current = _currentUser.value ?: return Result.failure(Exception("Faça login."))
        
        // Busca a mensagem correspondente no estado local para poder enviar seu texto à IA
        var conteudoMensagem = "Mensagem não encontrada ou deletada"
        var denunciadoId = "reported_user_uid"
        
        _messages.value.values.flatten().find { it.mensagemId == mensagemId }?.let { msg ->
            conteudoMensagem = msg.conteudo
            denunciadoId = msg.remetenteId ?: "reported_user_uid"
        }
        
        // Simulação do IP real do remetente para banimento
        val ipSimulado = "192.168.0." + (10..254).random()
        val reportId = UUID.randomUUID().toString()
        
        val newReport = UserReport(
            denunciaId = reportId,
            denuncianteId = current.uid,
            denunciadoId = denunciadoId,
            mensagemId = mensagemId,
            motivo = motivo + " [IP do Infrator Registrado: $ipSimulado] [Mensagem Analisada: \"$conteudoMensagem\"]",
            status = "pendente (analisando IA... 🤖)",
            dataCriacao = Date()
        )
        
        // Adiciona à coleção de denúncias
        val currentReports = _reports.value.toMutableList()
        currentReports.add(0, newReport)
        _reports.value = currentReports
        
        // Executa a análise com Gemini em segundo plano
        scope.launch {
            try {
                val analysis = GeminiService.analyzeReport(conteudoMensagem, motivo)
                val updatedReports = _reports.value.map { rep ->
                    if (rep.denunciaId == reportId) {
                        val statusSuffix = if (analysis.isValidReport) "válida (IA) 🚨" else "falsa_denuncia (IA) 🟢"
                        rep.copy(
                            status = statusSuffix,
                            motivo = rep.motivo + "\n\n🤖 CO-PILOTO GEMINI AI MODERAÇÃO:\nResultado: ${if (analysis.isValidReport) "VIOLAÇÃO CONFIRMADA" else "FALSA DENÚNCIA CONFIRMADA"}\nJustificativa: ${analysis.explanation}"
                        )
                    } else {
                        rep
                    }
                }
                _reports.value = updatedReports
            } catch (e: Exception) {
                Log.e("Security_Revela", "Erro ao processar análise da IA: ${e.message}")
            }
        }
        
        Log.e("Security_Revela", "DENÚNCIA REGISTRADA: Mensagem $mensagemId denunciada. IP guardado para banimento: $ipSimulado")
        return Result.success(true)
    }

    suspend fun blockUser(uidToBlock: String): Result<Boolean> {
        delay(800)
        val current = _currentUser.value ?: return Result.failure(Exception("Faça login."))
        val updatedBlocklist = current.bloqueados + uidToBlock
        val updatedProfile = current.copy(bloqueados = updatedBlocklist)
        _currentUser.value = updatedProfile
        _users.value = _users.value + (current.uid to updatedProfile)
        return Result.success(true)
    }

    /**
     * Sistema de "Modo Surpresa": Após 5 mensagens trocadas,
     * permite revelar a identidade se ambos aceitarem. (Requisito 5)
     */
    suspend fun requestIdentityReveal(conversaId: String): Result<Conversation> {
        delay(1200)
        val current = _currentUser.value ?: return Result.failure(Exception("Não autenticado."))
        val conversa = _conversations.value.find { it.conversaId == conversaId } ?: return Result.failure(Exception("Chat inexistente."))

        val updated = if (conversa.participantes[0] == current.uid) {
            conversa.copy(revelouUid1 = true)
        } else {
            conversa.copy(revelouUid2 = true)
        }

        // Verifica se ambos aceitaram revelar
        val finalConversa = if (updated.revelouUid1 && updated.revelouUid2) {
            // MATCH REVELADO! Transforma em chat normal
            updated.copy(
                matchRevelado = true,
                tipo = "normal",
                ultimaMensagem = "🎉 Identidades Reveladas! É um Match!"
            )
        } else {
            updated
        }

        // Salva na lista
        _conversations.value = _conversations.value.map {
            if (it.conversaId == conversaId) finalConversa else it
        }

        // Se deu Match, envia notificações e adiciona mensagem de sistema
        if (finalConversa.matchRevelado) {
            val outroId = finalConversa.participantes.find { it != current.uid } ?: ""
            sendNotification(outroId, "match", "🎉 Conexão Revelada! Você e @${current.apelido} decidiram se revelar!")
            sendNotification(current.uid, "match", "🎉 Conexão Revelada! Você e outro usuário decidiram se revelar!")

            // Adiciona mensagem de sistema no chat
            val systemMsg = ChatMessage(
                mensagemId = UUID.randomUUID().toString(),
                conversaId = conversaId,
                remetenteId = "system",
                conteudo = "🎉 Identidade Revelada! Vocês agora podem ver as fotos de perfil um do outro! ✨ Divirtam-se!",
                isAnonimo = false,
                dataEnvio = Date()
            )
            _messages.value = _messages.value + (conversaId to ((_messages.value[conversaId] ?: emptyList()) + systemMsg))
            
            // Gamificação
            updateBadgeProgress("Revelado", 1)
        }

        return Result.success(finalConversa)
    }

    suspend fun createNewConversation(outroUserId: String, isAnonimo: Boolean): Result<Conversation> {
        delay(1000)
        val current = _currentUser.value ?: return Result.failure(Exception("Logue para iniciar conversa."))

        // Verifica se já existe conversa do mesmo tipo com este usuário
        val existente = _conversations.value.find {
            it.participantes.contains(current.uid) && it.participantes.contains(outroUserId) && it.tipo == (if (isAnonimo) "anonimo" else "normal")
        }
        if (existente != null) {
            return Result.success(existente)
        }

        val novaId = UUID.randomUUID().toString()
        val nova = Conversation(
            conversaId = novaId,
            tipo = if (isAnonimo) "anonimo" else "normal",
            participantes = listOf(current.uid, outroUserId),
            ultimaMensagem = if (isAnonimo) "🎭 Chat Anônimo criado" else "💬 Chat Normal criado",
            ultimaMensagemData = Date(),
            matchRevelado = false
        )

        _conversations.value = listOf(nova) + _conversations.value
        _messages.value = _messages.value + (novaId to emptyList())

        return Result.success(nova)
    }

    // --- MÉTODOS AUXILIARES ---

    private fun applyProfanityFilter(text: String): String {
        var filtered = text
        for (word in badWords) {
            val pattern = "(?i)\\b$word\\b".toRegex()
            filtered = filtered.replace(pattern, "✨[Mensagem Moderada]✨")
        }
        return filtered
    }

    private fun updateConversationLastMessage(conversaId: String, text: String, date: Date) {
        _conversations.value = _conversations.value.map {
            if (it.conversaId == conversaId) {
                it.copy(
                    ultimaMensagem = text,
                    ultimaMensagemData = date,
                    unreadCount = if (it.participantes[0] == _currentUser.value?.uid) it.unreadCount else it.unreadCount + 1
                )
            } else {
                it
            }
        }
    }

    private fun sendNotification(usuarioId: String, tipo: String, conteudo: String) {
        val newNotif = AppNotification(
            notificacaoId = UUID.randomUUID().toString(),
            usuarioId = usuarioId,
            tipo = tipo,
            remetenteId = _currentUser.value?.uid ?: "sistema",
            remetenteNome = _currentUser.value?.nome ?: "Sistema",
            conteudo = conteudo,
            dataCriacao = Date()
        )
        _notifications.value = listOf(newNotif) + _notifications.value
    }

    private fun updateBadgeProgress(id: String, inc: Int) {
        _badges.value = _badges.value.map { badge ->
            if (badge.id == id) {
                // Simulação simples: conquista após certas metas
                val isEarned = when (id) {
                    "Mensageiro" -> true // Ativa ao mandar mensagem
                    "Revelado" -> true // Ativa no Match
                    "Popular" -> true
                    else -> false
                }
                badge.copy(earned = isEarned)
            } else {
                badge
            }
        }
    }

    private fun updateMissionsProgress() {
        _missions.value = _missions.value.map { mission ->
            when (mission.id) {
                "mission_1" -> mission.copy(completed = true, progress = "1/1")
                "mission_2" -> mission.copy(completed = _posts.value.any { it.usuarioId == _currentUser.value?.uid }, progress = if (_posts.value.any { it.usuarioId == _currentUser.value?.uid }) "1/1" else "0/1")
                else -> mission
            }
        }
    }

    private fun triggerAutomatedReply(conversaId: String, receptor: UserProfile, msgCount: Int) {
        scope.launch {
            delay(1500) // Tempo de espera para parecer realista
            _typingState.value = _typingState.value + (conversaId to true)
            delay(2000) // Fingindo digitar
            _typingState.value = _typingState.value + (conversaId to false)

            val conversa = _conversations.value.find { it.conversaId == conversaId } ?: return@launch
            val content = when (msgCount) {
                1 -> "Olá! Gostei muito do seu perfil. Que vibe você mais curte? 🎭"
                2 -> "Super legal! Eu amo música e games também. Qual seu jogo preferido?"
                3 -> "Caramba, que massa! A gente tem muito em comum..."
                4 -> "Essa conversa está maravilhosa. Vamos trocar mais algumas mensagens para nos revelar?"
                5 -> "Nossa, já trocamos 5 mensagens! O botão de revelação apareceu pra você? Clica aí que eu clico aqui! ✨"
                else -> "Que incrível! É muito bom fazer novas amizades sinceras."
            }

            val botMsg = ChatMessage(
                mensagemId = UUID.randomUUID().toString(),
                conversaId = conversaId,
                remetenteId = if (conversa.tipo == "anonimo" && !conversa.matchRevelado) null else receptor.uid,
                conteudo = content,
                isAnonimo = conversa.tipo == "anonimo" && !conversa.matchRevelado,
                iconeAnonimo = "🎭",
                dataEnvio = Date()
            )

            _messages.value = _messages.value + (conversaId to ((_messages.value[conversaId] ?: emptyList()) + botMsg))
            updateConversationLastMessage(conversaId, botMsg.conteudo, botMsg.dataEnvio)
        }
    }

    private fun addUserToDatabase(user: UserProfile) {
        _users.value = _users.value + (user.uid to user)
    }

    suspend fun toggleBanUser(uidToBan: String): Result<Boolean> {
        delay(800)
        val user = _users.value[uidToBan] ?: return Result.failure(Exception("Usuário não encontrado."))
        val newStatus = if (user.status == "banido") "ativo" else "banido"
        val updatedUser = user.copy(status = newStatus)
        _users.value = _users.value + (uidToBan to updatedUser)
        
        // Se banido, remove da sessão atual se for o usuário ativo
        if (newStatus == "banido" && _currentUser.value?.uid == uidToBan) {
            _currentUser.value = null
        }
        return Result.success(true)
    }

    suspend fun updateAdminPermissions(uid: String, isAdmin: Boolean, permissions: AdminPermissions): Result<Boolean> {
        delay(600)
        val user = _users.value[uid] ?: return Result.failure(Exception("Usuário não encontrado."))
        val updatedUser = user.copy(
            isAdmin = isAdmin,
            adminPermissions = if (isAdmin) permissions else AdminPermissions()
        )
        _users.value = _users.value + (uid to updatedUser)
        
        // Se for o usuário atualmente logado, atualiza o estado de currentUser
        if (_currentUser.value?.uid == uid) {
            _currentUser.value = updatedUser
        }
        return Result.success(true)
    }

    suspend fun deletePost(postId: String): Result<Boolean> {
        delay(600)
        _posts.value = _posts.value.filter { it.postId != postId }
        return Result.success(true)
    }

    private fun prepopulateData() {
        // 1. Usuários sugeridos / amigos
        val mari = UserProfile(
            uid = "mari_uid",
            nome = "Mariana Gomes",
            apelido = "MariG",
            email = "mari@revela.app",
            bio = "🎵 Apaixonada por indie rock, café e noites estreladas.",
            genero = "Feminino",
            idade = 22,
            cidade = "Belo Horizonte",
            estado = "MG",
            sobreMim = "Amo tocar violão, fotografar a natureza e conhecer pessoas com vibes parecidas!",
            vibes = listOf("🎵 Música", "✈️ Viagens", "🎨 Arte"),
            seguidores = 542,
            seguindo = 310
        )

        val lipe = UserProfile(
            uid = "lipe_uid",
            nome = "Felipe Pinheiro",
            apelido = "LipePlay",
            email = "felipe@revela.app",
            bio = "🎮 Streamer nas horas vagas. Viciado em RPG e pizza de quatro queijos.",
            genero = "Masculino",
            idade = 24,
            cidade = "Curitiba",
            estado = "PR",
            sobreMim = "Sempre jogando cooperativo. Procuro parceiros de equipe e boas risadas no chat!",
            vibes = listOf("🎮 Games", "🍕 Pizza", "🏃 Esportes"),
            seguidores = 1205,
            seguindo = 450
        )

        val anabook = UserProfile(
            uid = "ana_uid",
            nome = "Ana Letícia",
            apelido = "AnaBook",
            email = "ana@revela.app",
            bio = "📚 Colecionadora de livros físicos, plantas e canecas de chá.",
            genero = "Feminino",
            idade = 26,
            cidade = "Rio de Janeiro",
            estado = "RJ",
            sobreMim = "Escrevo contos de ficção e gosto de falar sobre literatura clássica.",
            vibes = listOf("📚 Leitura", "☕ Café", "🌱 Plantas"),
            seguidores = 340,
            seguindo = 360
        )

        _users.value = mapOf(
            mari.uid to mari,
            lipe.uid to lipe,
            anabook.uid to anabook
        )

        // 2. Posts Padrão do Feed
        val post1 = FeedPost(
            postId = "post_1",
            usuarioId = "mari_uid",
            autorNome = mari.nome,
            autorApelido = mari.apelido,
            autorFoto = mari.fotoPerfil,
            imagemUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500&auto=format&fit=crop",
            legenda = "Música boa, café quente e chuva lá fora. Dia perfeito para compor algo novo! ☕🎵 O que vocês estão ouvindo hoje?",
            curtidas = 42,
            permiteComentarioAnonimo = true,
            filterApplied = "Vintage"
        )

        val post2 = FeedPost(
            postId = "post_2",
            usuarioId = "lipe_uid",
            autorNome = lipe.nome,
            autorApelido = lipe.apelido,
            autorFoto = lipe.fotoPerfil,
            imagemUrl = "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=500&auto=format&fit=crop",
            legenda = "Setup pronto para a stream de hoje à noite! Quem vai colar para jogar junto? @frontwork 🎮🚀 #RPG",
            curtidas = 89,
            permiteComentarioAnonimo = true,
            filterApplied = "Cool"
        )

        _posts.value = listOf(post1, post2)

        // 3. Comentários Padrão
        val comment1 = PostComment(
            comentarioId = "c1",
            postId = "post_1",
            usuarioId = null,
            autorNome = "Alguém",
            autorFoto = "🎭",
            texto = "Essa legenda me definiu totalmente hoje! 🎧",
            isAnonimo = true
        )

        val comment2 = PostComment(
            comentarioId = "c2",
            postId = "post_1",
            usuarioId = "lipe_uid",
            autorNome = "Felipe Pinheiro",
            autorFoto = "",
            texto = "Opa! Manda essa playlist nova por direct depois, Mari!",
            isAnonimo = false
        )

        _comments.value = mapOf(
            "post_1" to listOf(comment1, comment2),
            "post_2" to emptyList()
        )

        // 4. Badges do Sistema
        _badges.value = listOf(
            UserBadge("Mensageiro", "Mensageiro", "Enviou 10 mensagens em chats", "💬", false),
            UserBadge("Revelado", "Revelado", "Realizou 3 desmascaramentos (Matches)", "🎭", false),
            UserBadge("Popular", "Popular", "Recebeu 50 curtidas nos seus posts", "🔥", false)
        )

        // 5. Missões Diárias
        _missions.value = listOf(
            DailyMission("mission_1", "Elogie alguém anonimamente hoje", false, "0/1"),
            DailyMission("mission_2", "Poste uma foto com legenda", false, "0/1")
        )

        // 6. Conversas Padrão para Demonstração e Moderação
        val conv1 = Conversation(
            conversaId = "conv_1",
            tipo = "normal",
            participantes = listOf("mari_uid", "lipe_uid"),
            ultimaMensagem = "Opa! Manda essa playlist nova por direct depois, Mari!",
            ultimaMensagemData = Date()
        )
        val conv2 = Conversation(
            conversaId = "conv_2",
            tipo = "anonimo",
            participantes = listOf("ana_uid", "lipe_uid"),
            ultimaMensagem = "Gostei muito das suas indicações de livros clássicos!",
            ultimaMensagemData = Date()
        )

        _conversations.value = listOf(conv1, conv2)

        // 7. Mensagens de Demonstração
        val msg1 = ChatMessage(
            mensagemId = "m1",
            conversaId = "conv_1",
            remetenteId = "mari_uid",
            conteudo = "Oi Lipe! Vi seu post do setup novo, ficou sensacional!",
            isAnonimo = false
        )
        val msg2 = ChatMessage(
            mensagemId = "m2",
            conversaId = "conv_1",
            remetenteId = "lipe_uid",
            conteudo = "Valeu Mari! Deu um trabalhão arrumar os cabos kkk",
            isAnonimo = false
        )
        val msg3 = ChatMessage(
            mensagemId = "m3",
            conversaId = "conv_1",
            remetenteId = "mari_uid",
            conteudo = "Imagino! E aquela playlist que você pediu, vou te mandar sim.",
            isAnonimo = false
        )

        val msg4 = ChatMessage(
            mensagemId = "m4",
            conversaId = "conv_2",
            remetenteId = null, // Anônimo
            conteudo = "Oi! Vi que você curte livros físicos. Qual o seu autor favorito?",
            isAnonimo = true
        )
        val msg5 = ChatMessage(
            mensagemId = "m5",
            conversaId = "conv_2",
            remetenteId = "lipe_uid",
            conteudo = "Olá! Ah, eu gosto muito de ficção científica e fantasia, tipo Tolkien.",
            isAnonimo = false
        )
        val msg6 = ChatMessage(
            mensagemId = "m6",
            conversaId = "conv_2",
            remetenteId = null, // Anônimo
            conteudo = "Gostei muito das suas indicações de livros clássicos!",
            isAnonimo = true
        )

        _messages.value = mapOf(
            "conv_1" to listOf(msg1, msg2, msg3),
            "conv_2" to listOf(msg4, msg5, msg6)
        )

        // 8. Denúncias Pre-populadas (Demonstração de Moderação com Co-Piloto Gemini AI)
        val rep1 = UserReport(
            denunciaId = "rep_1",
            denuncianteId = "mari_uid",
            denunciadoId = "lipe_uid",
            mensagemId = "m2",
            motivo = "Ofensa verbal direta. [IP do Infrator Registrado: 192.168.0.125] [Mensagem Analisada: \"Valeu Mari! Deu um trabalhão arrumar os cabos kkk\"]",
            status = "falsa_denuncia (IA) 🟢",
            dataCriacao = Date(System.currentTimeMillis() - 3600000)
        )
        val rep2 = UserReport(
            denunciaId = "rep_2",
            denuncianteId = "ana_uid",
            denunciadoId = "mari_uid",
            mensagemId = "m_toxic",
            motivo = "Fingimento de identidade e assédio verbal tóxico. [IP do Infrator Registrado: 192.168.0.210] [Mensagem Analisada: \"sua ridícula de merda, some daqui ninguém quer saber de você\"]",
            status = "válida (IA) 🚨",
            dataCriacao = Date(System.currentTimeMillis() - 7200000)
        )
        
        val rep1WithAI = rep1.copy(
            motivo = rep1.motivo + "\n\n🤖 CO-PILOTO GEMINI AI MODERAÇÃO:\nResultado: FALSA DENÚNCIA CONFIRMADA\nJustificativa: A mensagem analisada ('Valeu Mari! Deu um trabalhão arrumar os cabos kkk') é perfeitamente amigável, expressa gratidão e compartilha uma risada casual sobre a organização de cabos. Não há qualquer indício de ofensa ou violação das diretrizes. A denúncia parece ser infundada ou um erro do denunciante."
        )
        
        val rep2WithAI = rep2.copy(
            motivo = rep2.motivo + "\n\n🤖 CO-PILOTO GEMINI AI MODERAÇÃO:\nResultado: VIOLAÇÃO CONFIRMADA\nJustificativa: A mensagem ('sua ridícula de merda, some daqui ninguém quer saber de você') viola de forma direta e grave as políticas contra cyberbullying, assédio direcionado e uso de linguagem ofensiva agressiva. Recomenda-se o banimento imediato da conta e o bloqueio do IP (192.168.0.210)."
        )
        
        _reports.value = listOf(rep1WithAI, rep2WithAI)

        // 9. Relações de Seguidores/Seguindo Pre-populadas (Requisito: Seguidores Anônimos)
        val pFollows = listOf(
            FollowRelationship("f_1", "mari_uid", "google_user_123", isAnonymous = true, isRevealed = false),
            FollowRelationship("f_2", "lipe_uid", "google_user_123", isAnonymous = false, isRevealed = true),
            FollowRelationship("f_3", "ana_uid", "google_user_123", isAnonymous = true, isRevealed = true),
            FollowRelationship("f_4", "google_user_123", "mari_uid", isAnonymous = false, isRevealed = true),
            FollowRelationship("f_5", "google_user_123", "lipe_uid", isAnonymous = true, isRevealed = false),

            FollowRelationship("f_1_adm", "mari_uid", "admin_uid_123", isAnonymous = true, isRevealed = false),
            FollowRelationship("f_2_adm", "lipe_uid", "admin_uid_123", isAnonymous = false, isRevealed = true),
            FollowRelationship("f_3_adm", "ana_uid", "admin_uid_123", isAnonymous = true, isRevealed = true),
            FollowRelationship("f_4_adm", "admin_uid_123", "mari_uid", isAnonymous = false, isRevealed = true),
            FollowRelationship("f_5_adm", "admin_uid_123", "lipe_uid", isAnonymous = true, isRevealed = false)
        )
        _follows.value = pFollows
    }
}
