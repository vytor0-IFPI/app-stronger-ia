package com.example.data.repository

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.Devotional
import com.example.data.model.Episode
import com.example.data.model.RecommendedChannel
import com.example.data.model.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object StrongerCastRepository {

    private const val TAG = "StrongerCastRepo"
    private val scope = CoroutineScope(Dispatchers.IO)
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    // Supabase Settings (Chave do usuário e URL)
    var supabaseUrl: String = try {
        val url = BuildConfig.SUPABASE_URL
        if (url.isNotBlank() && !url.contains("your-project")) url else "https://your-project.supabase.co"
    } catch (e: Throwable) {
        "https://your-project.supabase.co"
    }

    var supabaseAnonKey: String = try {
        val key = BuildConfig.SUPABASE_ANON_KEY
        if (key.isNotBlank()) key else "sb_publishable_-LkakXZwDxeGNffxIPvuXg_z7_sODC-"
    } catch (e: Throwable) {
        "sb_publishable_-LkakXZwDxeGNffxIPvuXg_z7_sODC-"
    }

    private val _isConnectedToSupabase = MutableStateFlow(false)
    val isConnectedToSupabase: StateFlow<Boolean> = _isConnectedToSupabase.asStateFlow()

    // Estado do Usuário Logado
    private val _currentUser = MutableStateFlow<UserProfile?>(
        UserProfile(
            id = "demo-admin-id",
            email = "admin@strongercast.com",
            nome = "Jovem Cristão",
            admin = true // Pré-configurado como admin para teste e gestão imediata
        )
    )
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    // Episódios
    private val _episodes = MutableStateFlow<List<Episode>>(
        listOf(
            Episode(
                id = 1,
                titulo = "#01 · Identidade Inabalável na Juventude",
                convidado = "Pr. Lucas Santana",
                data = "24/10/2026",
                youtube = "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
                spotify = "https://open.spotify.com/show/strongercast",
                mp3 = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                capa = "https://images.unsplash.com/photo-1519791883288-dc8bd696e667?w=800&auto=format&fit=crop&q=80",
                publicado = true
            ),
            Episode(
                id = 2,
                titulo = "#02 · Vencendo a Ansiedade com Fé Real",
                convidado = "Dra. Gabriela Nunes (Psicóloga)",
                data = "17/10/2026",
                youtube = "https://www.youtube.com/watch?v=ScMzIvxBSi4",
                spotify = "https://open.spotify.com/show/strongercast",
                mp3 = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
                capa = "https://images.unsplash.com/photo-1499209974431-9dddcece7f88?w=800&auto=format&fit=crop&q=80",
                publicado = true
            ),
            Episode(
                id = 3,
                titulo = "#03 · Propósito & Carreira: Onde Deus Te Quer?",
                convidado = "Felipe Rocha (Empreendedor)",
                data = "10/10/2026",
                youtube = "https://www.youtube.com/watch?v=9bZkp7q19f0",
                spotify = "https://open.spotify.com/show/strongercast",
                mp3 = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
                capa = "https://images.unsplash.com/photo-1507679799987-c73779587ccf?w=800&auto=format&fit=crop&q=80",
                publicado = true
            ),
            Episode(
                id = 4,
                titulo = "#04 · Relacionamentos e Santidade na Prática",
                convidado = "Sarah & Tiago Oliveira",
                data = "03/10/2026",
                youtube = "https://www.youtube.com/watch?v=kXYiU_JCYtU",
                spotify = "https://open.spotify.com/show/strongercast",
                mp3 = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
                capa = "https://images.unsplash.com/photo-1529156069898-49953e39b3ac?w=800&auto=format&fit=crop&q=80",
                publicado = true
            )
        )
    )
    val episodes: StateFlow<List<Episode>> = _episodes.asStateFlow()

    // Devocionais (do mais recente para o mais antigo)
    private val _devotionals = MutableStateFlow<List<Devotional>>(
        listOf(
            Devotional(
                id = 1,
                titulo = "A Certeza daquilo que não vemos",
                versiculo = "Hebreus 11.1",
                texto = "Ora, a fé é a certeza daquilo que esperamos e a convicção do que não vemos. Quando o mundo cobra provas imediatas e palpáveis, a fé nos ancora na fidelidade eterna de Deus. Não baseie sua caminhada pelo que seus olhos naturais observam hoje; firme seus passos na promessa Daquele que é fiel para cumprir.",
                data = "25/10/2026",
                publicado = true
            ),
            Devotional(
                id = 2,
                titulo = "Coragem no Meio da Tempestade",
                versiculo = "Josué 1.9",
                texto = "Não fui eu que lhe ordenei? Seja forte e corajoso! Não se apavore nem desanime, pois o Senhor, o seu Deus, estará com você por onde você andar. A coragem não é ausência de medo, mas a certeza inegociável de que Deus é maior que qualquer desafio na sua universidade, trabalho ou família.",
                data = "23/10/2026",
                publicado = true
            ),
            Devotional(
                id = 3,
                titulo = "Renovando a Mente Diariamente",
                versiculo = "Romanos 12.2",
                texto = "E não vos conformeis com este século, mas transformai-vos pela renovação da vossa mente, para que experimenteis qual seja a boa, agradável e perfeita vontade de Deus. Filtre o que entra pelas suas telas e ouvidos. Alimente seu espírito com a Verdade da Palavra viva.",
                data = "20/10/2026",
                publicado = true
            ),
            Devotional(
                id = 4,
                titulo = "Força Sobrenatural em Cristo",
                versiculo = "Filipenses 4.13",
                texto = "Tudo posso naquele que me fortalece. Este versículo não fala de superpoderes egoístas, mas sobre ter contentamento e perseverança santa tanto na abundância quanto na escassez, sabendo que a graça de Cristo é o nosso suprimento diário inesgotável.",
                data = "16/10/2026",
                publicado = true
            )
        )
    )
    val devotionals: StateFlow<List<Devotional>> = _devotionals.asStateFlow()

    // Canais e Vídeos Recomendados
    private val _channels = MutableStateFlow<List<RecommendedChannel>>(
        listOf(
            RecommendedChannel(
                id = 1,
                titulo = "Como Ouvir a Voz de Deus com Clareza",
                canal = "Deive Leonardo",
                link = "https://www.youtube.com/watch?v=aircAruvnKk",
                tipo = "video",
                publicado = true
            ),
            RecommendedChannel(
                id = 2,
                titulo = "Dunamis Movement Oficial",
                canal = "Dunamis Movement",
                link = "https://www.youtube.com/@DunamisMovement",
                tipo = "canal",
                publicado = true
            ),
            RecommendedChannel(
                id = 3,
                titulo = "Lidando com Pressão e Expectativas",
                canal = "Teo Hayashi",
                link = "https://www.youtube.com/watch?v=kJQP7kiw5Fk",
                tipo = "video",
                publicado = true
            ),
            RecommendedChannel(
                id = 4,
                titulo = "Canal JesusCopy",
                canal = "Douglas Gonçalves",
                link = "https://www.youtube.com/@Jesuscopy",
                tipo = "canal",
                publicado = true
            )
        )
    )
    val channels: StateFlow<List<RecommendedChannel>> = _channels.asStateFlow()

    // =========================================================================
    // CONEXÃO & SINCRONIZAÇÃO SUPABASE (REST API)
    // =========================================================================

    suspend fun testAndSyncSupabase(url: String, key: String): Result<String> = withContext(Dispatchers.IO) {
        val cleanUrl = url.trim().removeSuffix("/")
        val cleanKey = key.trim()

        if (cleanUrl.isBlank() || cleanUrl.contains("your-project")) {
            return@withContext Result.failure(Exception("Por favor, forneça a URL do seu projeto Supabase (ex.: https://xyz.supabase.co)"))
        }

        try {
            // Testa conexão consultando a tabela episodes via REST
            val request = Request.Builder()
                .url("$cleanUrl/rest/v1/episodes?select=*&limit=10")
                .addHeader("apikey", cleanKey)
                .addHeader("Authorization", "Bearer $cleanKey")
                .addHeader("Content-Type", "application/json")
                .get()
                .build()

            val response = httpClient.newCall(request).execute()
            val code = response.code
            val body = response.body?.string().orEmpty()

            if (response.isSuccessful) {
                supabaseUrl = cleanUrl
                supabaseAnonKey = cleanKey
                _isConnectedToSupabase.value = true

                // Processa episódios remotos se a tabela tiver registros
                try {
                    val array = JSONArray(body)
                    if (array.length() > 0) {
                        val remoteEpisodes = mutableListOf<Episode>()
                        for (i in 0 until array.length()) {
                            val obj = array.getJSONObject(i)
                            remoteEpisodes.add(
                                Episode(
                                    id = obj.optLong("id", (i + 1).toLong()),
                                    titulo = obj.optString("titulo", "Sem título"),
                                    convidado = obj.optString("convidado").takeIf { it.isNotBlank() },
                                    data = obj.optString("data", ""),
                                    youtube = obj.optString("youtube").takeIf { it.isNotBlank() },
                                    spotify = obj.optString("spotify").takeIf { it.isNotBlank() },
                                    mp3 = obj.optString("mp3").takeIf { it.isNotBlank() },
                                    capa = obj.optString("capa").takeIf { it.isNotBlank() },
                                    publicado = obj.optBoolean("publicado", true)
                                )
                            )
                        }
                        _episodes.value = remoteEpisodes
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Parsing remote episodes warning", e)
                }

                // Sincroniza Devocionais em background
                fetchDevotionalsFromSupabase(cleanUrl, cleanKey)
                fetchChannelsFromSupabase(cleanUrl, cleanKey)

                Result.success("Conectado com sucesso ao banco Supabase!")
            } else if (code == 401 || code == 403) {
                Result.failure(Exception("Chave de API inválida ou sem permissão RLS (HTTP $code)."))
            } else if (code == 404) {
                // A tabela ainda não foi criada no Supabase
                supabaseUrl = cleanUrl
                supabaseAnonKey = cleanKey
                _isConnectedToSupabase.value = true
                Result.success("Conectado ao Supabase! Lembre-se de rodar o script SQL para criar as tabelas.")
            } else {
                Result.failure(Exception("Supabase respondeu com código $code: $body"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error connecting to Supabase", e)
            Result.failure(Exception("Falha na conexão: ${e.localizedMessage ?: "Verifique a URL e sua conexão."}"))
        }
    }

    private suspend fun fetchDevotionalsFromSupabase(url: String, key: String) {
        try {
            val request = Request.Builder()
                .url("$url/rest/v1/devotionals?select=*&order=id.desc&limit=20")
                .addHeader("apikey", key)
                .addHeader("Authorization", "Bearer $key")
                .get()
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val array = JSONArray(response.body?.string().orEmpty())
                if (array.length() > 0) {
                    val remoteDevs = mutableListOf<Devotional>()
                    for (i in 0 until array.length()) {
                        val obj = array.getJSONObject(i)
                        remoteDevs.add(
                            Devotional(
                                id = obj.optLong("id", (i + 1).toLong()),
                                titulo = obj.optString("titulo", ""),
                                versiculo = obj.optString("versiculo", ""),
                                texto = obj.optString("texto", ""),
                                data = obj.optString("data", ""),
                                publicado = obj.optBoolean("publicado", true)
                            )
                        )
                    }
                    _devotionals.value = remoteDevs
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "fetchDevotionalsFromSupabase error", e)
        }
    }

    private suspend fun fetchChannelsFromSupabase(url: String, key: String) {
        try {
            val request = Request.Builder()
                .url("$url/rest/v1/recommended_channels?select=*&order=id.asc&limit=20")
                .addHeader("apikey", key)
                .addHeader("Authorization", "Bearer $key")
                .get()
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val array = JSONArray(response.body?.string().orEmpty())
                if (array.length() > 0) {
                    val remoteChannels = mutableListOf<RecommendedChannel>()
                    for (i in 0 until array.length()) {
                        val obj = array.getJSONObject(i)
                        remoteChannels.add(
                            RecommendedChannel(
                                id = obj.optLong("id", (i + 1).toLong()),
                                titulo = obj.optString("titulo", ""),
                                canal = obj.optString("canal", ""),
                                link = obj.optString("link", ""),
                                tipo = obj.optString("tipo", "video"),
                                publicado = obj.optBoolean("publicado", true)
                            )
                        )
                    }
                    _channels.value = remoteChannels
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "fetchChannelsFromSupabase error", e)
        }
    }

    // =========================================================================
    // AUTH METHODS
    // =========================================================================

    fun login(email: String, pass: String): Result<UserProfile> {
        if (email.isBlank() || pass.length < 6) {
            return Result.failure(Exception("E-mail inválido ou senha com menos de 6 caracteres."))
        }
        val isAdmin = email.contains("admin", ignoreCase = true)
        val profile = UserProfile(
            id = "user-${System.currentTimeMillis()}",
            email = email,
            nome = email.substringBefore("@"),
            admin = isAdmin
        )
        _currentUser.value = profile
        return Result.success(profile)
    }

    fun register(email: String, pass: String, nome: String): Result<UserProfile> {
        if (email.isBlank() || pass.length < 6) {
            return Result.failure(Exception("Preencha um e-mail válido e senha mínima de 6 caracteres."))
        }
        val isAdmin = email.contains("admin", ignoreCase = true)
        val profile = UserProfile(
            id = "user-${System.currentTimeMillis()}",
            email = email,
            nome = if (nome.isNotBlank()) nome else email.substringBefore("@"),
            admin = isAdmin
        )
        _currentUser.value = profile
        return Result.success(profile)
    }

    fun logout() {
        _currentUser.value = null
    }

    fun toggleAdmin() {
        val user = _currentUser.value ?: return
        _currentUser.value = user.copy(admin = !user.admin)
    }

    // =========================================================================
    // EPISODES CRUD
    // =========================================================================

    fun saveEpisode(episode: Episode) {
        val currentList = _episodes.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == episode.id }
        if (index >= 0) {
            currentList[index] = episode
        } else {
            val newId = (currentList.maxOfOrNull { it.id } ?: 0L) + 1L
            currentList.add(0, episode.copy(id = newId))
        }
        _episodes.value = currentList

        // Envia para o Supabase de forma assíncrona se conectado
        if (_isConnectedToSupabase.value) {
            scope.launch {
                pushEpisodeToSupabase(episode)
            }
        }
    }

    fun deleteEpisode(id: Long) {
        _episodes.value = _episodes.value.filterNot { it.id == id }

        if (_isConnectedToSupabase.value) {
            scope.launch {
                deleteFromSupabase("episodes", id)
            }
        }
    }

    // =========================================================================
    // DEVOTIONALS CRUD
    // =========================================================================

    fun saveDevotional(devotional: Devotional) {
        val currentList = _devotionals.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == devotional.id }
        if (index >= 0) {
            currentList[index] = devotional
        } else {
            val newId = (currentList.maxOfOrNull { it.id } ?: 0L) + 1L
            currentList.add(0, devotional.copy(id = newId))
        }
        _devotionals.value = currentList

        if (_isConnectedToSupabase.value) {
            scope.launch {
                pushDevotionalToSupabase(devotional)
            }
        }
    }

    fun deleteDevotional(id: Long) {
        _devotionals.value = _devotionals.value.filterNot { it.id == id }

        if (_isConnectedToSupabase.value) {
            scope.launch {
                deleteFromSupabase("devotionals", id)
            }
        }
    }

    // =========================================================================
    // CHANNELS CRUD
    // =========================================================================

    fun saveChannel(channel: RecommendedChannel) {
        val currentList = _channels.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == channel.id }
        val autoTipo = if (isYouTubeVideoLink(channel.link)) "video" else "canal"
        val itemWithTipo = channel.copy(tipo = autoTipo)
        if (index >= 0) {
            currentList[index] = itemWithTipo
        } else {
            val newId = (currentList.maxOfOrNull { it.id } ?: 0L) + 1L
            currentList.add(itemWithTipo.copy(id = newId))
        }
        _channels.value = currentList

        if (_isConnectedToSupabase.value) {
            scope.launch {
                pushChannelToSupabase(itemWithTipo)
            }
        }
    }

    fun deleteChannel(id: Long) {
        _channels.value = _channels.value.filterNot { it.id == id }

        if (_isConnectedToSupabase.value) {
            scope.launch {
                deleteFromSupabase("recommended_channels", id)
            }
        }
    }

    // Operações de Escrita Remota no Supabase
    private fun pushEpisodeToSupabase(episode: Episode) {
        try {
            val json = JSONObject().apply {
                put("titulo", episode.titulo)
                put("convidado", episode.convidado ?: "")
                put("data", episode.data)
                put("youtube", episode.youtube ?: "")
                put("spotify", episode.spotify ?: "")
                put("mp3", episode.mp3 ?: "")
                put("capa", episode.capa ?: "")
                put("publicado", episode.publicado)
            }
            val body = json.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/episodes")
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Authorization", "Bearer $supabaseAnonKey")
                .addHeader("Prefer", "resolution=merge-duplicates")
                .post(body)
                .build()
            httpClient.newCall(request).execute()
        } catch (e: Exception) {
            Log.e(TAG, "Error pushing episode to Supabase", e)
        }
    }

    private fun pushDevotionalToSupabase(dev: Devotional) {
        try {
            val json = JSONObject().apply {
                put("titulo", dev.titulo)
                put("versiculo", dev.versiculo)
                put("texto", dev.texto)
                put("data", dev.data)
                put("publicado", dev.publicado)
            }
            val body = json.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/devotionals")
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Authorization", "Bearer $supabaseAnonKey")
                .post(body)
                .build()
            httpClient.newCall(request).execute()
        } catch (e: Exception) {
            Log.e(TAG, "Error pushing devotional to Supabase", e)
        }
    }

    private fun pushChannelToSupabase(ch: RecommendedChannel) {
        try {
            val json = JSONObject().apply {
                put("titulo", ch.titulo)
                put("canal", ch.canal)
                put("link", ch.link)
                put("tipo", ch.tipo)
                put("publicado", ch.publicado)
            }
            val body = json.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/recommended_channels")
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Authorization", "Bearer $supabaseAnonKey")
                .post(body)
                .build()
            httpClient.newCall(request).execute()
        } catch (e: Exception) {
            Log.e(TAG, "Error pushing channel to Supabase", e)
        }
    }

    private fun deleteFromSupabase(table: String, id: Long) {
        try {
            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/$table?id=eq.$id")
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Authorization", "Bearer $supabaseAnonKey")
                .delete()
                .build()
            httpClient.newCall(request).execute()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting from Supabase table $table", e)
        }
    }

    // Identificação de Links de Vídeo vs Canal
    fun isYouTubeVideoLink(url: String): Boolean {
        val lower = url.lowercase().trim()
        return lower.contains("watch?v=") ||
                lower.contains("youtu.be/") ||
                lower.contains("/shorts/") ||
                lower.contains("youtube.com/embed/")
    }

    fun extractYouTubeVideoId(url: String): String? {
        val trimmed = url.trim()
        return try {
            when {
                trimmed.contains("youtu.be/") -> trimmed.substringAfter("youtu.be/").substringBefore("?").substringBefore("&")
                trimmed.contains("watch?v=") -> trimmed.substringAfter("watch?v=").substringBefore("&")
                trimmed.contains("/shorts/") -> trimmed.substringAfter("/shorts/").substringBefore("?").substringBefore("&")
                trimmed.contains("/embed/") -> trimmed.substringAfter("/embed/").substringBefore("?").substringBefore("&")
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }
}
