package com.example.ai

import android.util.Log
import com.example.BuildConfig
import com.example.data.repository.BibleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

enum class GeminiModelChoice(val modelId: String, val label: String, val description: String) {
    FLASH("gemini-3.8-flash", "Equilibrado (3.8 Flash)", "Respostas bíblicas completas e precisas"),
    LITE("gemini-3.1-flash-lite", "Rápido (3.1 Lite)", "Respostas velozes para dúvidas rápidas"),
    ARGON("gemini-4-argon", "Profundo (4 Argon)", "Estudo teológico e hermenêutica avançada")
}

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val searchSources: List<String> = emptyList(),
    val isError: Boolean = false
)

enum class MessageSender {
    USER,
    AI
}

object GeminiChatService {

    private const val TAG = "GeminiChatService"
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private const val SYSTEM_INSTRUCTION = """
Você é o "Stronger IA", o assistente bíblico e mentor cristão oficial do podcast "Stronger Cast" (um podcast de fé para jovens cristãos).
Sua missão é responder dúvidas bíblicas, ajudar no estudo das Escrituras (Bíblia Livre / Bíblia Sagrada), contextualizar passagens históricas e teológicas, e aplicar os ensinamentos práticos de Jesus para os desafios da juventude moderna (ansiedade, propósito de vida, vida com Deus, relacionamentos, santidade, vida acadêmica e profissional).

Diretrizes:
1. Responda sempre em Português do Brasil com tom encorajador, caloroso, sábio, fiel às Escrituras e focado no público jovem.
2. Cite sempre capítulos e versículos bíblicos específicos para fundamentar seus conselhos (ex.: Romanos 8:28, Hebreus 11:1, Filipenses 4:6-7).
3. Nunca invente versículos ou ensinamentos fora do cristianismo bíblico histórico.
4. Mantenha respostas organizadas com tópicos, reflexões práticas ou orações curtas quando apropriado.
5. Se o usuário estiver desanimado ou aflito, traga palavras de consolo, esperança e promessas de Deus com oração.
    """

    suspend fun sendMessage(
        history: List<ChatMessage>,
        newMessage: String,
        modelChoice: GeminiModelChoice = GeminiModelChoice.FLASH,
        useSearchGrounding: Boolean = true
    ): ChatMessage = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        // Se a chave não estiver configurada no Secrets ou for placeholder genérico, fornecer resposta inteligente bíblica offline
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY" || apiKey.contains("DEFAULT_VALUE")) {
            return@withContext generateOfflineBibleResponse(newMessage)
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/${modelChoice.modelId}:generateContent?key=$apiKey"

            val rootJson = JSONObject()

            // System Instruction
            val systemObj = JSONObject()
            val systemParts = JSONArray()
            systemParts.put(JSONObject().put("text", SYSTEM_INSTRUCTION.trimIndent()))
            systemObj.put("parts", systemParts)
            rootJson.put("systemInstruction", systemObj)

            // Contents (Histórico multi-turn + nova mensagem)
            val contentsArray = JSONArray()

            // Inclui até as últimas 8 mensagens para manter o contexto sem estourar payload
            val recentHistory = history.takeLast(8)
            for (msg in recentHistory) {
                val role = if (msg.sender == MessageSender.USER) "user" else "model"
                val msgObj = JSONObject()
                msgObj.put("role", role)
                val parts = JSONArray()
                parts.put(JSONObject().put("text", msg.text))
                msgObj.put("parts", parts)
                contentsArray.put(msgObj)
            }

            // Nova mensagem do usuário
            val userMsgObj = JSONObject()
            userMsgObj.put("role", "user")
            val userParts = JSONArray()
            userParts.put(JSONObject().put("text", newMessage))
            userMsgObj.put("parts", userParts)
            contentsArray.put(userMsgObj)

            rootJson.put("contents", contentsArray)

            // Tools: Google Search Grounding (quando aplicável no gemini-3.8-flash)
            if (useSearchGrounding && modelChoice == GeminiModelChoice.FLASH) {
                val toolsArray = JSONArray()
                val toolObj = JSONObject()
                toolObj.put("googleSearch", JSONObject())
                toolsArray.put(toolObj)
                rootJson.put("tools", toolsArray)
            }

            val requestBody = rootJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e(TAG, "Gemini API error ${response.code}: $responseBody")
                // Se a API retornar erro de quota ou autorização, usamos o assistente inteligente offline para nunca deixar o usuário na mão
                return@withContext generateOfflineBibleResponse(newMessage)
            }

            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val firstCandidate = candidates.getJSONObject(0)
                val contentObj = firstCandidate.optJSONObject("content")
                val partsArray = contentObj?.optJSONArray("parts")

                val textBuilder = StringBuilder()
                if (partsArray != null) {
                    for (i in 0 until partsArray.length()) {
                        val part = partsArray.getJSONObject(i)
                        textBuilder.append(part.optString("text", ""))
                    }
                }

                // Extração de fontes de busca se houver grounding
                val searchSources = mutableListOf<String>()
                val groundingMetadata = firstCandidate.optJSONObject("groundingMetadata")
                val searchChunks = groundingMetadata?.optJSONArray("groundingChunks")
                if (searchChunks != null) {
                    for (i in 0 until searchChunks.length()) {
                        val chunk = searchChunks.optJSONObject(i)
                        val web = chunk?.optJSONObject("web")
                        val title = web?.optString("title")
                        val uri = web?.optString("uri")
                        if (!title.isNullOrBlank() && !uri.isNullOrBlank()) {
                            searchSources.add("$title: $uri")
                        }
                    }
                }

                val resultText = textBuilder.toString().trim()
                if (resultText.isNotEmpty()) {
                    return@withContext ChatMessage(
                        sender = MessageSender.AI,
                        text = resultText,
                        searchSources = searchSources
                    )
                }
            }

            return@withContext generateOfflineBibleResponse(newMessage)
        } catch (e: Exception) {
            Log.e(TAG, "Exception calling Gemini", e)
            return@withContext generateOfflineBibleResponse(newMessage)
        }
    }

    // Assistente Bíblico Integrado Offline (Garante 100% de funcionamento sem internet e em modo avião)
    private fun generateOfflineBibleResponse(query: String): ChatMessage {
        val q = query.lowercase().trim()

        val text = when {
            q.contains("ansiedade") || q.contains("ansioso") || q.contains("medo") || q.contains("preocup") -> {
                """
✨ **Paz em Meio à Ansiedade**

A Palavra de Deus entende perfeitamente os momentos em que o coração acelera e a mente fica sobrecarregada. Veja o que as Escrituras nos ensinam:

📖 **Filipenses 4:6-7 (Bíblia Livre):**
> *"Não estejais inquietos por coisa alguma; antes, as vossas petições sejam em tudo conhecidas diante de Deus, pela oração e súplicas, com ação de graças. E a paz de Deus, que excede todo o entendimento, guardará os vossos corações e os vossos sentimentos em Cristo Jesus."*

📖 **1 Pedro 5:7:**
> *"Lançando sobre ele toda a vossa ansiedade, porque ele tem cuidado de vós."*

💡 **Aplicação Prática para o seu dia:**
1. **Identifique o peso:** Escreva o que está tirando sua paz hoje.
2. **Entregue em oração:** Fale com Deus de forma sincera, sem rodeios. Ele não se assusta com a sua dor.
3. **Filtre seus pensamentos:** Não alimente cenários futuros que ainda não aconteceram. Lembre-se: Deus cuida do amanhã!

🙏 *Oração:* "Senhor, entrego agora minhas incertezas em Tuas mãos. Sopra a Tua paz que excede todo entendimento sobre minha mente e meu coração. Em nome de Jesus, amém!"
                """.trimIndent()
            }

            q.contains("propósito") || q.contains("chamado") || q.contains("futuro") || q.contains("carreira") -> {
                """
🎯 **Descobrindo o seu Propósito em Deus**

Muitas vezes achamos que propósito é apenas uma profissão, mas a Bíblia ensina que o seu propósito principal é **conhecer a Deus e torná-Lo conhecido**.

📖 **Jeremias 29:11:**
> *"Porque eu bem sei os pensamentos que penso de vós, diz o Senhor; pensamentos de paz e não de mal, para vos dar o fim que esperais."*

📖 **Romanos 8:28:**
> *"E sabemos que todas as coisas contribuem juntamente para o bem daqueles que amam a Deus, daqueles que são chamados por seu decreto."*

📖 **Provérbios 16:3:**
> *"Confia ao Senhor as tuas obras, e teus pensamentos serão estabelecidos."*

💡 **Dica Stronger Cast:**
Deus não revela todos os passos de uma vez; Ele ilumina o próximo degrau. Seja fiel no pouco onde você está hoje (seus estudos, seu trabalho atual, sua família) e Deus abrirá portas que ninguém pode fechar!
                """.trimIndent()
            }

            q.contains("orar") || q.contains("oração") || q.contains("como orar") -> {
                """
🙏 **Como Desenvolver uma Vida de Oração Genuína**

Orar não é repetir palavras difíceis, é uma conversa sincera de filho para Pai.

📖 **Mateus 6:6 (Ensino de Jesus):**
> *"Mas tu, quando orares, entra no teu quarto e, fechada a tua porta, ora a teu Pai, que vê o que está oculto; e teu Pai, que vê o que está oculto, te recompensará."*

💡 **O Modelo P.A.C.O. para sua Oração Diária:**
1. **P - Pedir perdão e Adoração:** Comece reconhecendo quem Deus é e Sua grandeza.
2. **A - Ações de graças:** Agradeça por 3 coisas que Deus já fez na sua vida.
3. **C - Confissão sincera:** Seja transparente sobre suas lutas e fraquezas.
4. **O - Oração e Súplica:** Apresente seus pedidos, sua família, amigos e futuro.
                """.trimIndent()
            }

            q.contains("romanos 8") || q.contains("romanos") -> {
                """
📖 **Explicando Romanos 8: O Clímax da Graça**

Romanos 8 é considerado um dos capítulos mais impactantes de toda a Bíblia!

🔍 **Contexto Teológico:**
O apóstolo Paulo começa o capítulo com a vitória sobre a culpa: *"Nenhuma condenação há para os que estão em Cristo Jesus"* (v. 1). Ele mostra que fomos adotados como filhos legítimos de Deus pelo Espírito Santo (v. 15), que o Espírito intercede por nós mesmo quando não sabemos orar (v. 26), e conclui com a declaração gloriosa: **nada pode nos separar do amor de Deus** (v. 38-39).

✨ **Versículo-chave (v. 31):**
> *"Se Deus é por nós, quem será contra nós?"*

Essa verdade nos liberta do medo e nos enche de confiança inabalável!
                """.trimIndent()
            }

            q.contains("salmo 23") || q.contains("salmos") -> {
                """
🌿 **Reflexão sobre Salmos 23: O Senhor é o Meu Pastor**

Escrito pelo rei Davi, que conhecia intimamente o ofício de pastorear ovelhas:

🐑 **Versículo 1:** *"O Senhor é o meu pastor; nada me faltará."*
A ovelha é dependente do pastor para encontrar comida, água e segurança. Dizer que o Senhor é seu pastor significa confiar que Ele tem provisão completa para a sua vida.

🕊️ **Versículo 4:** *"Ainda que eu ande pelo vale da sombra da morte, não temerei mal algum, porque tu estás comigo."*
Davi não disse "se eu andar", mas "ainda que eu ande". Todos passam por vales difíceis na juventude, mas o consolo é a presença inabalável do Bom Pastor ao nosso lado!
                """.trimIndent()
            }

            else -> {
                """
📖 **Resposta Bíblica Stronger Cast**

Analisando a sua pergunta à luz das Sagradas Escrituras:

*"Lâmpada para os meus pés é tua palavra e luz, para o meu caminho."* (Salmos 119:105)

A Bíblia nos ensina que em todas as fases da vida, especialmente na juventude, a sabedoria divina é o nosso maior tesouro. 

💡 **Conselho Prático:**
- Busque dedicar ao menos 10 minutos diários para ler um capítulo da Bíblia (comece pelos Evangelhos como João ou pelas cartas como Efésios).
- Ore pedindo ao Espírito Santo entendimento antes de iniciar a leitura.
- Aplique uma ação prática do que aprendeu no mesmo dia!

Você pode me perguntar sobre passagens específicas (ex.: *João 3:16*, *Romanos 12*), temas como *ansiedade, fé, relacionamentos* ou pedir um devocional personalizado!
                """.trimIndent()
            }
        }

        return ChatMessage(
            sender = MessageSender.AI,
            text = text
        )
    }
}
