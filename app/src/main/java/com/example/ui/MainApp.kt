package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Episode
import com.example.player.AudioPlayerController
import com.example.ui.components.AudioPlayerDialog
import com.example.ui.components.Screen
import com.example.ui.components.SplashVignette
import com.example.ui.components.StrongerDrawerContent
import com.example.ui.components.StrongerHeader
import com.example.ui.components.YouTubePlayerDialog
import com.example.ui.screens.AccountScreen
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.BibleChatScreen
import com.example.ui.screens.BibleScreen
import com.example.ui.screens.ChannelsScreen
import com.example.ui.screens.DevotionalsScreen
import com.example.ui.screens.EpisodesScreen
import com.example.ui.screens.SupabaseConfigScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GoldSecondary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun MainApp() {
    var showVignette by remember { mutableStateOf(true) }

    // Vinheta de abertura aparece em TODA abertura
    if (showVignette) {
        SplashVignette(
            onFinished = { showVignette = false }
        )
        return
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    var currentScreen by remember { mutableStateOf(Screen.EPISODES) }
    var pendingAiChatPrompt by remember { mutableStateOf<String?>(null) }

    // Modais de Reprodução
    var activeYouTubeVideo by remember { mutableStateOf<Pair<String, String>?>(null) } // Pair(Title, Url)
    var showAudioPlayerModal by remember { mutableStateOf(false) }

    // Player de Áudio em background
    val currentAudioEpisode by AudioPlayerController.currentEpisode.collectAsState()
    val isAudioPlaying by AudioPlayerController.isPlaying.collectAsState()

    // Suporte ao botão voltar nativo do Android
    BackHandler(enabled = drawerState.isOpen || currentScreen != Screen.EPISODES) {
        if (drawerState.isOpen) {
            coroutineScope.launch { drawerState.close() }
        } else if (currentScreen != Screen.EPISODES) {
            currentScreen = Screen.EPISODES
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            StrongerDrawerContent(
                currentScreen = currentScreen,
                onNavigate = { screen ->
                    currentScreen = screen
                },
                onCloseDrawer = {
                    coroutineScope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                val screenTitle = when (currentScreen) {
                    Screen.EPISODES -> "Episódios"
                    Screen.BIBLE -> "Bíblia Sagrada (Offline)"
                    Screen.BIBLE_CHAT -> "Stronger IA Bíblia"
                    Screen.DEVOTIONALS -> "Devocionais Diários"
                    Screen.CHANNELS -> "Canais Recomendados"
                    Screen.ADMIN_PANEL -> "Painel do Admin"
                    Screen.ACCOUNT -> "Sua Conta"
                    Screen.SUPABASE_CONFIG -> "Banco & SQL"
                }

                StrongerHeader(
                    title = screenTitle,
                    onOpenDrawer = {
                        coroutineScope.launch { drawerState.open() }
                    },
                    onOpenAiChat = {
                        currentScreen = Screen.BIBLE_CHAT
                    }
                )
            },
            bottomBar = {
                // Mini Player Persistente quando há áudio carregado
                AnimatedVisibility(
                    visible = currentAudioEpisode != null,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it })
                ) {
                    currentAudioEpisode?.let { ep ->
                        MiniAudioPlayerBar(
                            episode = ep,
                            isPlaying = isAudioPlaying,
                            onExpand = { showAudioPlayerModal = true },
                            onTogglePlay = { AudioPlayerController.togglePlayPause() },
                            onClose = { AudioPlayerController.closePlayer() }
                        )
                    }
                }
            },
            containerColor = DarkBackground
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (currentScreen) {
                    Screen.EPISODES -> {
                        EpisodesScreen(
                            onWatchEpisode = { ep ->
                                ep.youtube?.let { url ->
                                activeYouTubeVideo = Pair(ep.titulo, url)
                                }
                            },
                            onListenEpisode = {
                                showAudioPlayerModal = true
                            },
                            onOpenAiChat = {
                                currentScreen = Screen.BIBLE_CHAT
                            }
                        )
                    }

                    Screen.BIBLE -> {
                        BibleScreen(
                            onOpenAiWithPrompt = { prompt ->
                                pendingAiChatPrompt = prompt
                                currentScreen = Screen.BIBLE_CHAT
                            }
                        )
                    }

                    Screen.BIBLE_CHAT -> {
                        BibleChatScreen(
                            initialPrompt = pendingAiChatPrompt
                        )
                    }

                    Screen.DEVOTIONALS -> {
                        DevotionalsScreen(
                            onOpenAiWithPrompt = { prompt ->
                                pendingAiChatPrompt = prompt
                                currentScreen = Screen.BIBLE_CHAT
                            }
                        )
                    }

                    Screen.CHANNELS -> {
                        ChannelsScreen(
                            onPlayInAppVideo = { title, url ->
                                activeYouTubeVideo = Pair(title, url)
                            }
                        )
                    }

                    Screen.ADMIN_PANEL -> {
                        AdminPanelScreen()
                    }

                    Screen.ACCOUNT -> {
                        AccountScreen()
                    }

                    Screen.SUPABASE_CONFIG -> {
                        SupabaseConfigScreen()
                    }
                }
            }
        }
    }

    // Modal de Vídeo do YouTube Embed
    activeYouTubeVideo?.let { (title, url) ->
        YouTubePlayerDialog(
            title = title,
            youtubeUrl = url,
            onDismiss = { activeYouTubeVideo = null }
        )
    }

    // Modal de Áudio Fullscreen
    if (showAudioPlayerModal && currentAudioEpisode != null) {
        AudioPlayerDialog(
            episode = currentAudioEpisode!!,
            onDismiss = { showAudioPlayerModal = false }
        )
    }
}

@Composable
fun MiniAudioPlayerBar(
    episode: Episode,
    isPlaying: Boolean,
    onExpand: () -> Unit,
    onTogglePlay: () -> Unit,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceElevated)
            .border(1.dp, GoldSecondary.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            .clickable(onClick = onExpand)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("mini_audio_player")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Ícone Animado de Áudio
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(GoldPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = Color(0xFF141414),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = episode.titulo,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Toque para abrir o player",
                        color = GoldSecondary,
                        fontSize = 10.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onTogglePlay,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pausar" else "Tocar",
                        tint = GoldPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fechar player",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
