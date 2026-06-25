import * as functions from "firebase-functions";
import * as admin from "firebase-admin";

admin.initializeApp();
const db = admin.firestore();

/**
 * Cloud Function 1: Banir Usuários Automaticamente com +5 Denúncias Analisadas
 * Executado ao criar/atualizar denúncias no Firestore.
 */
export const monitorAndBanUsers = functions.firestore
  .document("denuncias/{denunciaId}")
  .onWrite(async (change, context) => {
    const data = change.after.exists ? change.after.data() : null;
    if (!data) return null;

    const denunciadoId = data.denunciadoId;
    if (!denunciadoId) return null;

    // Busca todas as denúncias confirmadas para este usuário denunciado
    const querySnapshot = await db
      .collection("denuncias")
      .where("denunciadoId", "==", denunciadoId)
      .where("status", "in", ["analisada", "banida"])
      .get();

    const totalDenunciasValidas = querySnapshot.size;

    // Se o usuário ultrapassar o limite de 5 denúncias, altera status para banido
    if (totalDenunciasValidas >= 5) {
      console.warn(`Usuário ${denunciadoId} atingiu ${totalDenunciasValidas} denúncias. Banindo conta...`);
      
      // Atualiza o perfil no Firestore
      await db.collection("users").doc(denunciadoId).update({
        status: "banido"
      });

      // Revoga o acesso do usuário no Firebase Authentication
      try {
        await admin.auth().updateUser(denunciadoId, {
          disabled: true
        });
        console.log(`Sucesso: Acesso do usuário ${denunciadoId} desativado no Firebase Auth.`);
      } catch (authError) {
        console.error("Erro ao desativar conta de usuário no Auth:", authError);
      }
    }

    return null;
  });

/**
 * Cloud Function 2: Enviar Notificação Push (FCM) via Notificações Gravadas
 * Disparado ao criar uma nova notificação na coleção "notificacoes".
 */
export const sendFcmPushNotification = functions.firestore
  .document("notificacoes/{notifId}")
  .onCreate(async (snapshot, context) => {
    const notifData = snapshot.data();
    if (!notifData) return null;

    const targetUserId = notifData.usuarioId;
    const content = notifData.conteudo;
    const title = notifData.tipo === "match" ? "🎭 Conexão Revelada!" : "Nova interação no Revela";

    // Busca o FCM Token do usuário destinatário no Firestore
    const userSnap = await db.collection("users").doc(targetUserId).get();
    if (!userSnap.exists) return null;

    const userData = userSnap.data();
    const fcmToken = userData?.fcmToken; // Token FCM guardado ao logar no mobile

    if (!fcmToken) {
      console.log(`Aviso: Usuário ${targetUserId} não possui Token FCM registrado. Push não enviado.`);
      return null;
    }

    // Configura o payload do push
    const message = {
      token: fcmToken,
      notification: {
        title: title,
        body: content,
      },
      data: {
        tipo: notifData.tipo,
        remetenteId: notifData.remetenteId || ""
      }
    };

    // Envia o push usando Firebase Cloud Messaging (FCM)
    try {
      const response = await admin.messaging().send(message);
      console.log("Notificação push enviada com sucesso via FCM:", response);
    } catch (error) {
      console.error("Erro ao disparar push notification FCM:", error);
    }

    return null;
  });

/**
 * Cloud Function 3: Limpeza Periódica de Mensagens Antigas (Scheduler de 30 dias)
 * Executa uma rotina diária para limpar logs de conversas muito antigas
 * economizando espaço e cumprindo regras de privacidade efêmera.
 */
export const cleanOldMessagesDaily = functions.pubsub
  .schedule("every 24 hours")
  .onRun(async (context) => {
    const dataLimite = new Date();
    // Mensagens com mais de 30 dias são apagadas do servidor de chat
    dataLimite.setDate(dataLimite.getDate() - 30);

    console.log("Iniciando varredura diária de mensagens obsoletas...");

    const conversasSnap = await db.collection("conversas").get();

    for (const conversaDoc of conversasSnap.docs) {
      const mensagensQuery = await conversaDoc.ref
        .collection("mensagens")
        .where("dataEnvio", "<", admin.firestore.Timestamp.fromDate(dataLimite))
        .get();

      if (mensagensQuery.empty) continue;

      const batch = db.batch();
      mensagensQuery.docs.forEach((msgDoc) => {
        batch.delete(msgDoc.ref);
      });

      await batch.commit();
      console.log(`Deletadas ${mensagensQuery.size} mensagens antigas da conversa ${conversaDoc.id}.`);
    }

    console.log("Limpeza de banco concluída com sucesso.");
    return null;
  });
