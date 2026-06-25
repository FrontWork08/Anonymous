package com.example.data

import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiPart(val text: String)

@JsonClass(generateAdapter = true)
data class GeminiContent(val parts: List<GeminiPart>)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(val responseMimeType: String? = null)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig? = null
)

@JsonClass(generateAdapter = true)
data class GeminiPartResponse(val text: String?)

@JsonClass(generateAdapter = true)
data class GeminiContentResponse(val parts: List<GeminiPartResponse>)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(val content: GeminiContentResponse)

@JsonClass(generateAdapter = true)
data class GeminiResponse(val candidates: List<GeminiCandidate>)

@JsonClass(generateAdapter = true)
data class AnalysisResult(
    val isValidReport: Boolean,
    val explanation: String
)

interface GeminiApi {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun analyzeContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiService {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val api = retrofit.create(GeminiApi::class.java)

    private val analysisAdapter = moshi.adapter(AnalysisResult::class.java)

    suspend fun analyzeReport(messageContent: String, reportReason: String): AnalysisResult {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            // Se a chave não estiver configurada ou for placeholder, simula um retorno inteligente local baseado em regras simples
            return simulateLocalAnalysis(messageContent, reportReason)
        }

        val prompt = """
            Você é um moderador especialista em segurança para a rede social "Anonymous".
            Analise a seguinte mensagem denunciada e o motivo fornecido pelo usuário denunciante.
            Determine se a denúncia é VÁLIDA (a mensagem de fato viola políticas de uso: preconceito, assédio, ameaça, bullying, pornografia ou ofensas graves) ou FALSA (a mensagem é inofensiva, brincadeira normal ou desacordo amigável).
            
            Mensagem Denunciada: "$messageContent"
            Motivo da Denúncia: "$reportReason"
            
            Responda EXCLUSIVAMENTE em formato JSON com a seguinte estrutura de exemplo, sem blocos de código markdown ou texto adicional:
            {
              "isValidReport": true,
              "explanation": "A mensagem de fato contém agressões verbais infundadas que atacam a honra pessoal."
            }
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(parts = listOf(GeminiPart(text = prompt)))
            ),
            generationConfig = GeminiGenerationConfig(responseMimeType = "application/json")
        )

        return try {
            val response = api.analyzeContent(apiKey, request)
            val jsonText = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (jsonText != null) {
                analysisAdapter.fromJson(jsonText) ?: AnalysisResult(false, "Falha ao analisar a resposta da IA.")
            } else {
                AnalysisResult(false, "Nenhum conteúdo recebido da IA.")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            AnalysisResult(false, "Erro ao conectar com a IA: ${e.message}")
        }
    }

    private fun simulateLocalAnalysis(messageContent: String, reportReason: String): AnalysisResult {
        val lowerContent = messageContent.lowercase()
        val lowerReason = reportReason.lowercase()
        
        // Regras offline para simulação inteligente de termos proibidos
        val isOffense = lowerContent.contains("lixo") || 
                        lowerContent.contains("idiota") || 
                        lowerContent.contains("imbecil") || 
                        lowerContent.contains("otario") || 
                        lowerContent.contains("ofensa") ||
                        lowerContent.contains("ameaça") ||
                        lowerContent.contains("bater") ||
                        lowerContent.contains("safado")
                        
        return if (isOffense) {
            AnalysisResult(
                isValidReport = true,
                explanation = "[Simulado - Sem chave de API] A IA offline do Anonymous analisou a mensagem e confirmou linguagem abusiva ou ameaçadora ('$messageContent') condizente com a justificativa do denunciante ('$reportReason')."
            )
        } else {
            AnalysisResult(
                isValidReport = false,
                explanation = "[Simulado - Sem chave de API] A mensagem denunciada ('$messageContent') não demonstra sinais objetivos de violação das regras. Parece ser uma falsa denúncia motivada por desentendimento pessoal."
            )
        }
    }
}
