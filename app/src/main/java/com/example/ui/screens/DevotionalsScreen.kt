package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Devotional
import com.example.data.repository.StrongerCastRepository
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GoldSecondary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun DevotionalsScreen(
    onOpenAiWithPrompt: (String) -> Unit = {}
) {
    val devotionals by StrongerCastRepository.devotionals.collectAsState()
    val context = LocalContext.current

    // Exibição do mais recente para o mais antigo
    val publishedDevotionals = devotionals.filter { it.publicado }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("devotionals_screen")
    ) {
        // Banner introdutório edificante
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(DarkSurfaceElevated, DarkSurface)
                    )
                )
                .border(1.dp, DarkCardBorder, RoundedCornerShape(14.dp))
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.FormatQuote,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Alimento Diário para o seu Espírito",
                        color = GoldPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Meditações práticas baseadas na Palavra para fortalecer sua jornada.",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(publishedDevotionals, key = { it.id }) { devotional ->
                DevotionalCard(
                    devotional = devotional,
                    onCopy = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText(
                            devotional.titulo,
                            "${devotional.titulo}\nVersículo: ${devotional.versiculo}\n\n${devotional.texto}\n— Stronger Cast Devocionais"
                        )
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Devocional copiado!", Toast.LENGTH_SHORT).show()
                    },
                    onShare = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "✨ *${devotional.titulo}*\n📖 *${devotional.versiculo}*\n\n${devotional.texto}\n\nCompartilhado via Stronger Cast App"
                            )
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Compartilhar Devocional"))
                    },
                    onAskAi = {
                        val prompt = "Gostaria de uma reflexão devocional aprofundada baseada no devocional '${devotional.titulo}' (${devotional.versiculo}). Como aplicar essa mensagem prática aos desafios dos jovens de hoje?"
                        onOpenAiWithPrompt(prompt)
                    }
                )
            }
        }
    }
}

@Composable
fun DevotionalCard(
    devotional: Devotional,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onAskAi: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DarkCardBorder, RoundedCornerShape(16.dp))
            .testTag("devotional_card_${devotional.id}"),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Data e Ações
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = GoldSecondary,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = devotional.data,
                        color = GoldSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row {
                    IconButton(
                        onClick = onCopy,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copiar texto",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onShare,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Compartilhar devocional",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Título do Devocional
            Text(
                text = devotional.titulo,
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Pill Dourada do Versículo (ex.: "Hebreus 11.1")
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurfaceElevated)
                    .border(1.dp, GoldSecondary.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = devotional.versiculo,
                        color = GoldPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = DarkCardBorder.copy(alpha = 0.5f), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(14.dp))

            // Texto Puro do Devocional
            Text(
                text = devotional.texto,
                color = TextSecondary,
                fontSize = 14.sp,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Botão Refletir com Stronger IA
            OutlinedButton(
                onClick = onAskAi,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("devotional_ask_ai_${devotional.id}"),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = GoldPrimary
                ),
                border = BorderStroke(1.dp, GoldSecondary.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp),
                    tint = GoldPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Aprofundar com Stronger IA",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
