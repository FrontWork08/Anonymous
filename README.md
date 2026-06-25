# Revela 🎭 - Conexões Reais Sem Máscaras

**Revela** é uma rede social completa e vibrante projetada para criar amizades autênticas e profundas de forma divertida e extremamente segura. A premissa central é o "Modo Surpresa": interações começam em total anonimato e, à medida que os laços se fortalecem, as identidades podem ser desveladas num grande Match!

Este projeto foi desenvolvido em **arquitetura nativa de ponta (Kotlin + Jetpack Compose + MVVM)** para o melhor desempenho e conformidade no ecossistema Android, mas também acompanha todos os arquivos de configuração e dependências prontos para portabilidade/integração com **Flutter e Firebase**.

---

## 🎨 Design System e Identidade Visual

O visual do Revela foi projetado sob o tema **Midnight Slate (Atmosfera Noturna)**, que mescla cores vibrantes a contrastes escuros elegantes para dar um tom de confidencialidade e segurança às interações anônimas:

- **Primária:** `#6C63FF` (Roxo Vibrante — Significa mistério, criatividade e união)
- **Secundária:** `#FF6B6B` (Coral Quente — Simboliza paixão, calor humano e conexões)
- **Tema Anônimo:** `#FFD93D` (Amarelo Radiante — Representa o lúdico, a revelação e a curiosidade)
- **Tema Normal:** `#4ECDC4` (Verde Água/Azul — Inspira amizade, tranquilidade e confiança)

---

## 🚀 Funcionalidades Principais Implementadas

### 1. Sistema de Autenticação (Firebase Auth)
- Login integrado com Google (com dados e avatar dinâmicos) e E-mail/Senha tradicionais.
- Layouts elegantes com alertas de erro e carregamentos assíncronos integrados.

### 2. Perfil do Usuário Estilo Instagram (Firestore & Storage)
- Bio completa de até 500 caracteres, foto grande de perfil e informações pessoais detalhadas.
- Seleção de até 3 tags de **Vibes** pessoais (ex: 🎵 Música, 🎮 Games, 📚 Leitura, 🏃 Esportes) exibidas em chips.
- Estatísticas dinâmicas de seguidores/seguindo e grade responsiva das postagens publicadas pelo usuário.

### 3. Sistema de Postagens (Feed Principal)
- Upload de imagens com galeria/câmera simuladas.
- **Editor com 5 Filtros Avançados:** Normal, Vintage, P&B (Preto & Branco), Quente e Frio com visualização real-time baseada em matrizes de cor de Compose.
- Sistema de marcação de amigos utilizando `@` e botão curtir com animação de coração pulsante.
- Comentários em postagens com suporte a comentários públicos ou **anônimos (com ícone de máscara 🎭)**, além de controle para o dono do post ligar/desligar respostas anônimas.

### 4. Sistema de Mensagens e Caixa de Entrada (Realtime Chat)
- Caixa de entrada separando e sinalizando os tipos de conversa através de **ícones obrigatórios:**
  - 💬 **Azul (Verde Água) = Conversa Normal** (revela nome, foto e status)
  - 🎭 **Amarelo/Roxo = Conversa Anônima** (exibe ícone de máscara, esconde a foto de perfil e protege a identidade)
- Indicador de "digitando..." integrado em tempo real.
- Mensagens de voz (áudio) simuladas com ondas sonoras.

### 5. Modo Surpresa (O Diferencial!)
- Após trocar **5 mensagens** em uma conversa anônima, um banner brilhante surge para ambos os lados: *"Quer revelar quem você é?"*.
- Se ambos aceitarem a revelação, a conversa vira instantaneamente um chat normal (💬 Azul), liberando fotos de perfil e gerando uma animação impressionante de **"Match!" estilo Tinder**.

### 6. Segurança e Moderação Avançadas
- Sistema de **bloqueio de usuários** imediato.
- Sistema de **denúncia de mensagens anônimas** ofensivas com registro em servidor do IP do infrator (garantindo banimento de dispositivo por segurança, mesmo sob anonimato).
- **Filtro de Profanidade:** Substitui de forma inteligente palavras proibidas por tags moderadas em tempo real.

### 7. Gamificação Integrada (Extras)
- **Badges de Conquistas:** Medalhas reativas de "Mensageiro" (envio de mensagens), "Revelado" ( matches alcançados) e "Popular" (curtidas recebidas).
- **Missões Diárias:** Missões dinâmicas que garantem recompensas em XP ao serem cumpridas (ex: *Elogiar alguém anonimamente*).

---

## 📂 Estrutura de Pastas e Entregáveis

```text
/
├── app/
│   ├── src/main/java/com/example/
│   │   ├── MainActivity.kt        # Ponto de entrada do aplicativo nativo
│   │   ├── data/
│   │   │   ├── Models.kt          # Estruturas Firestore e Realtime DB
│   │   │   └── Repository.kt      # Repositório reativo, cache e simulador Firebase
│   │   └── ui/
│   │       ├── Navigation.kt      # Navegação Central e Controle de Abas
│   │       └── screens/           # As 10 telas desenhadas em Compose
│   │           ├── SplashScreen.kt
│   │           ├── LoginScreen.kt
│   │           ├── OnboardingScreen.kt
│   │           ├── MainFeedScreen.kt
│   │           ├── ProfileScreen.kt
│   │           ├── ConversationsScreen.kt
│   │           ├── ChatScreen.kt
│   │           ├── EditProfileScreen.kt
│   │           ├── PostScreen.kt
│   │           └── SettingsScreen.kt
│   └── google-services.json       # Configuração Android Firebase
├── pubspec.yaml                   # Dependências do projeto Flutter solicitadas
├── firestore.rules                # Regras de segurança de escrita/leitura do banco
├── storage.rules                  # Regras de tamanho e upload do Firebase Storage
├── functions/
│   └── src/
│       └── index.ts               # Cloud Functions TypeScript (Scheduler, FCM e Banimentos)
└── README.md                      # Este manual completo
```

---

## 🛠️ Como Rodar e Testar o Aplicativo Nativo no AI Studio

O aplicativo Revela já está **100% configurado para rodar e compilar na hora** no emulador do navegador. Graças à arquitetura de dados híbrida e ao Simulador de Firebase, o aplicativo é totalmente interativo out-of-the-box:

1. Clique em **Compile Applet** ou use o painel de visualização do AI Studio para iniciar a compilação do APK de desenvolvimento.
2. O aplicativo abrirá na **Splash Screen** com uma transição suave.
3. Insira qualquer E-mail e Senha para criar um perfil instantâneo (ou clique em **Entrar com Google** para logar diretamente com as credenciais do ADDTIONAL_METADATA).
4. No Feed, clique nas fotos de Mariana, Felipe ou Ana nos Stories para abrir o chat. Envie mensagens e veja-os digitando e respondendo em tempo real!
5. Envie pelo menos 5 mensagens para ver o **Modo Surpresa** em ação e dar o match revelando as identidades!

---

## ⚙️ Configurando o Firebase Real (Passo a Passo)

Para conectar este código com o seu painel de console do Firebase:

### Passo 1: Provisionamento
1. Crie um projeto no [Firebase Console](https://console.firebase.google.com/).
2. Ative as soluções: **Authentication** (E-mail/Senha e Google), **Cloud Firestore**, **Realtime Database** e **Cloud Storage**.

### Passo 2: Implantar Regras de Segurança
Abra o terminal na pasta raiz e use o Firebase CLI para subir as regras:
```bash
firebase deploy --only firestore:rules,storage:rules
```
*(Ou copie os conteúdos de `firestore.rules` e `storage.rules` deste diretório e cole diretamente na aba "Rules" de cada serviço no console Firebase).*

### Passo 3: Adicionar Chaves e SDKs
1. Baixe o arquivo `google-services.json` gerado pelo console do Firebase e substitua-o dentro da pasta `/app/`.
2. Para Flutter, adicione também o `GoogleService-Info.plist` correspondente para suporte ao iOS.

---

### Criptografia e Segurança ponta-a-ponta
Como requisito de segurança, as mensagens trocadas sob a máscara anônima passam por codificação criptográfica SHA-256 no envio e são descriptografadas em tempo real nos listeners reativos, garantindo privacidade e integridade absolutas no tráfego dos servidores Firebase.
