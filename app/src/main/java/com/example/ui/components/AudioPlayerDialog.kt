package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Podcasts
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.Episode
import com.example.player.AudioPlayerController
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.GoldGlow
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GoldSecondary
import com.example.ui.theme.StatusRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AudioPlayerDialog(
    episode: Episode,
    onDismiss: () -> Unit
) {
    val isPlaying by AudioPlayerController.isPlaying.collectAsState()
    val isLoading by AudioPlayerController.isLoading.collectAsState()
    val currentPosMs by AudioPlayerController.currentPositionMs.collectAsState()
    val durationMs by AudioPlayerController.durationMs.collectAsState()
    val playbackSpeed by AudioPlayerController.playbackSpeed.collectAsState()
    val errorMessage by AudioPlayerController.errorMessage.collectAsState()

    var isDraggingSlider by remember { mutableStateOf(false) }
    var draggingSliderPosition by remember { mutableFloatStateOf(0f) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .testTag("audio_player_dialog"),
            color = DarkBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("close_player_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar player",
                            tint = TextSecondary
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "TOCANDO AGORA",
                            color = GoldPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "Stronger Cast Oficial",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Podcasts,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Album Art / Capa
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .aspectRatio(1f)
                        .shadow(elevation = 20.dp, shape = RoundedCornerShape(24.dp), ambientColor = GoldPrimary)
                        .clip(RoundedCornerShape(24.dp))
                        .background(DarkSurface)
                        .border(1.5.dp, GoldSecondary.copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!episode.capa.isNullOrBlank()) {
                        AsyncImage(
                            model = episode.capa,
                            contentDescription = episode.titulo,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Placeholder Oficial Stronger Cast
                        Image(
                            painter = painterResource(id = R.drawable.ic_stronger_logo_square),
                            contentDescription = "Stronger Cast Oficial",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.65f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = GoldPrimary,
                                strokeWidth = 3.dp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Informações do Episódio
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = episode.titulo,
                        color = TextPrimary,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    if (!episode.convidado.isNullOrBlank()) {
                        Text(
                            text = "Com ${episode.convidado}",
                            color = GoldPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }

                    Text(
                        text = "Publicado em ${episode.data}",
                        color = TextMuted,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }

                // Mensagem de Erro se houver
                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = StatusRed,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                // Barra de Progresso
                Column(modifier = Modifier.fillMaxWidth()) {
                    val currentVal = if (isDraggingSlider) {
                        draggingSliderPosition
                    } else {
                        currentPosMs.toFloat()
                    }
                    val maxVal = durationMs.toFloat().coerceAtLeast(1f)

                    Slider(
                        value = currentVal.coerceIn(0f, maxVal),
                        onValueChange = {
                            isDraggingSlider = true
                            draggingSliderPosition = it
                        },
                        onValueChangeFinished = {
                            isDraggingSlider = false
                            AudioPlayerController.seekTo(draggingSliderPosition.toLong())
                        },
                        valueRange = 0f..maxVal,
                        colors = SliderDefaults.colors(
                            thumbColor = GoldPrimary,
                            activeTrackColor = GoldPrimary,
                            inactiveTrackColor = DarkCardBorder
                        ),
                        modifier = Modifier.testTag("audio_seek_slider")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = AudioPlayerController.formatDuration(currentVal.toLong()),
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = AudioPlayerController.formatDuration(durationMs),
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                // Controles Principais de Áudio (Rewind 10s, Play/Pause, Forward 30s)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Voltar 10s
                    IconButton(
                        onClick = { AudioPlayerController.rewindBack(10) },
                        modifier = Modifier
                            .size(52.dp)
                            .testTag("rewind_10_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay10,
                            contentDescription = "Voltar 10 segundos",
                            tint = TextPrimary,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    // Botão Principal Play/Pause
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(GoldPrimary, GoldSecondary)
                                )
                            )
                            .clickable { AudioPlayerController.togglePlayPause() }
                            .testTag("toggle_play_pause_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pausar áudio" else "Tocar áudio",
                            tint = Color(0xFF141414),
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    // Avançar 30s
                    IconButton(
                        onClick = { AudioPlayerController.skipForward(30) },
                        modifier = Modifier
                            .size(52.dp)
                            .testTag("forward_30_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FastForward,
                            contentDescription = "Avançar 30 segundos",
                            tint = TextPrimary,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }

                // Seletor de Velocidade (1.0x, 1.25x, 1.5x, 2.0x)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(DarkSurfaceElevated)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                        val isSelected = playbackSpeed == speed
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) GoldPrimary else Color.Transparent)
                                .clickable { AudioPlayerController.setSpeed(speed) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${speed}x",
                                color = if (isSelected) Color(0xFF141414) else TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
