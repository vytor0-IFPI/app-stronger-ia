package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GoldSecondary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull

@Composable
fun SplashVignette(
    onFinished: () -> Unit
) {
    val scaleAnim = remember { Animatable(0.7f) }
    val alphaAnim = remember { Animatable(0f) }
    val waveScale = remember { Animatable(1f) }
    val waveAlpha = remember { Animatable(0.7f) }

    LaunchedEffect(Unit) {
        // Vinheta de abertura com timeout de segurança absoluto de 12 segundos
        withTimeoutOrNull(12000L) {
            // Animação de entrada
            alphaAnim.animateTo(1f, animationSpec = tween(700, easing = FastOutSlowInEasing))
            scaleAnim.animateTo(1f, animationSpec = tween(900, easing = FastOutSlowInEasing))

            // Efeito de onda sonora pulsante
            waveScale.animateTo(
                targetValue = 1.6f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )
            waveAlpha.animateTo(
                targetValue = 0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )

            // Duração cinematográfica da vinheta (~3.5 segundos)
            delay(3400L)
        }
        // Transição garantida após a vinheta
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("splash_vignette_screen")
            // Safeguard em caso de emergência ou teste
            .clickable(onClick = { onFinished() }),
        contentAlignment = Alignment.Center
    ) {
        // Ondas douradas de áudio ao fundo
        Box(
            modifier = Modifier
                .size(240.dp)
                .scale(waveScale.value)
                .alpha(waveAlpha.value)
                .border(
                    width = 2.dp,
                    brush = Brush.radialGradient(
                        colors = listOf(GoldPrimary.copy(alpha = 0.5f), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(24.dp)
                .scale(scaleAnim.value)
                .alpha(alphaAnim.value)
        ) {
            // Logo circular com imagem gerada ou fallback estático seguro
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(CircleShape)
                    .border(
                        width = 3.dp,
                        brush = Brush.sweepGradient(
                            listOf(GoldPrimary, GoldSecondary, GoldPrimary)
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_stronger_logo_square),
                    contentDescription = "Stronger Cast Logo Oficial",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Logotipo Oficial com Marca e Tipografia
            Image(
                painter = painterResource(id = R.drawable.ic_stronger_logo_official_transparent),
                contentDescription = "Stronger Cast",
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(44.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "PODCAST DE FÉ PARA JOVENS",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 2.5.sp
            )

            Spacer(modifier = Modifier.height(42.dp))

            // Indicador sutil de carregamento da vinheta
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = GoldSecondary,
                strokeWidth = 2.dp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Iniciando experiência...",
                color = TextMuted,
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
