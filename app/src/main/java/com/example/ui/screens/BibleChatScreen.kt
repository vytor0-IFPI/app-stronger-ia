package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ai.ChatMessage
import com.example.ai.GeminiChatService
import com.example.ai.GeminiModelChoice
import com.example.ai.MessageSender
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GoldSecondary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun BibleChatScreen(
    initialPrompt: String? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var modelChoice by remember { mutableStateOf(GeminiModelChoice.FLASH) }
    var useSearchGrounding by remember { mutableStateOf(true) }
    var showModelMenu by remember { mutableStateOf(false) }

    var inputText by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }

    // Lista de mensagens do chat multi-turn
    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                sender = MessageSender.AI,
                text = "Graça e paz! Sou o **Stronger IA**, seu assistente bíblico e mentor no Stronger Cast.\n\nComo posso te ajudar hoje? Você pode tirar dúvidas sobre versículos, pedir explicações sobre capítulos da Bíblia ou conversar sobre desafios práticos da fé na juventude."
            )
        )
    }

    // Processa prompt inicial se veio de outra tela (ex: leitor da Bíblia)
    LaunchedEffect(initialPrompt) {
        if (!initialPrompt.isNullOrBlank()) {
            inputText = initialPrompt
        }
    }

    // Rolagem automática para a última mensagem
    LaunchedEffect(messages.size, isSending) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    fun handleSend(textToSend: String) {
        val prompt = textToSend.trim()
        if (prompt.isBlank() || isSending) return

        inputText = ""
        val userMsg = ChatMessage(sender = MessageSender.USER, text = prompt)
        messages.add(userMsg)
        isSending = true

        coroutineScope.launch {
            try {
                val aiResponse = GeminiChatService.sendMessage(
                    history = messages.dropLast(1),
                    newMessage = prompt,
                    modelChoice = modelChoice,
                    useSearchGrounding = useSearchGrounding
                )
                messages.add(aiResponse)
            } catch (e: Exception) {
                messages.add(
                    ChatMessage(
                        sender = MessageSender.AI,
                        text = "Não foi possível carregar a resposta no momento. ${e.localizedMessage ?: "Tente novamente."}",
                        isError = true
                    )
                )
            } finally {
                isSending = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .imePadding()
            .testTag("bible_chat_screen")
    ) {
        // Barra Superior do Chat com o Logo Oficial e Seletores
        Surface(
            color = DarkSurface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Logo Oficial do Stronger Cast
                        Image(
                            painter = painterResource(id = R.drawable.ic_stronger_logo_official_transparent),
                            contentDescription = "Stronger Cast Oficial",
                            modifier = Modifier
                                .height(26.dp)
                                .width(90.dp),
                            contentScale = ContentScale.Fit
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(GoldSecondary.copy(alpha = 0.2f))
                                .border(0.5.dp, GoldPrimary, RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "IA BÍBLICA",
                                color = GoldPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Seletor de Modelo Gemini
                        Box {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(DarkSurfaceElevated)
                                    .border(1.dp, DarkCardBorder, RoundedCornerShape(12.dp))
                                    .clickable { showModelMenu = true }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = modelChoice.label.substringBefore(" ("),
                                    color = TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            DropdownMenu(
                                expanded = showModelMenu,
                                onDismissRequest = { showModelMenu = false },
                                modifier = Modifier.background(DarkSurface)
                            ) {
                                GeminiModelChoice.values().forEach { choice ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(
                                                    text = choice.label,
                                                    color = if (choice == modelChoice) GoldPrimary else TextPrimary,
                                                    fontWeight = if (choice == modelChoice) FontWeight.Bold else FontWeight.Normal,
                                                    fontSize = 13.sp
                                                )
                                                Text(
                                                    text = choice.description,
                                                    color = TextMuted,
                                                    fontSize = 10.sp
                                                )
                                            }
                                        },
                                        onClick = {
                                            modelChoice = choice
                                            showModelMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Botão Limpar Histórico
                        IconButton(
                            onClick = {
                                messages.clear()
                                messages.add(
                                    ChatMessage(
                                        sender = MessageSender.AI,
                                        text = "Histórico limpo. Como posso te edificar ou esclarecer a Bíblia agora?"
                                    )
                                )
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Limpar conversa",
                                tint = TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Toggle para Google Search Grounding
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (useSearchGrounding) GoldPrimary.copy(alpha = 0.15f) else DarkSurfaceElevated)
                            .border(0.5.dp, if (useSearchGrounding) GoldPrimary else DarkCardBorder, RoundedCornerShape(8.dp))
                            .clickable { useSearchGrounding = !useSearchGrounding }
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = null,
                            tint = if (useSearchGrounding) GoldPrimary else TextMuted,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (useSearchGrounding) "Google Search Grounding Ativo" else "Pesquisa Web Desativada",
                            color = if (useSearchGrounding) GoldPrimary else TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Text(
                        text = "100% bíblico & seguro",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }
            }
        }

        HorizontalDivider(color = DarkCardBorder, thickness = 1.dp)

        // Lista de Mensagens em Thread Rolável
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(messages, key = { it.id }) { msg ->
                ChatMessageItem(
                    message = msg,
                    onCopy = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Mensagem Bíblica", msg.text))
                        Toast.makeText(context, "Texto copiado!", Toast.LENGTH_SHORT).show()
                    },
                    onShare = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "${msg.text}\n\n— Compartilhado via Stronger Cast IA")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Compartilhar Resposta Bíblica"))
                    }
                )
            }

            // Indicador de Carregamento da IA
            if (isSending) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(DarkSurface)
                            .border(1.dp, DarkCardBorder, RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        CircularProgressIndicator(
                            color = GoldPrimary,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Stronger IA consultando as Escrituras...",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Sugestões Rápidas de Perguntas Bíblicas
        val promptSuggestions = listOf(
            "Como vencer a ansiedade com a Bíblia?",
            "O que a Bíblia diz sobre propósito?",
            "Explicar Romanos 8:28",
            "Como orar com sinceridade?",
            "Dicas para ler a Bíblia diariamente",
            "O que significa ter fé genuína?"
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(promptSuggestions) { prompt ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkSurfaceElevated)
                        .border(1.dp, DarkCardBorder, RoundedCornerShape(16.dp))
                        .clickable(enabled = !isSending) { handleSend(prompt) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = GoldSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = prompt,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Campo de Digitação e Botão de Envio
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = {
                    Text(
                        text = "Pergunte sobre a Bíblia ou peça conselho...",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                },
                maxLines = 4,
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = DarkCardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = DarkSurfaceElevated,
                    unfocusedContainerColor = DarkSurfaceElevated
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_field")
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Botão Enviar
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        if (inputText.isNotBlank() && !isSending)
                            Brush.linearGradient(listOf(GoldPrimary, GoldSecondary))
                        else
                            Brush.linearGradient(listOf(DarkSurfaceElevated, DarkSurfaceElevated))
                    )
                    .clickable(enabled = inputText.isNotBlank() && !isSending) {
                        handleSend(inputText)
                    }
                    .testTag("send_chat_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Enviar",
                    tint = if (inputText.isNotBlank() && !isSending) Color(0xFF141414) else TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    onCopy: () -> Unit,
    onShare: () -> Unit
) {
    val isUser = message.sender == MessageSender.USER

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (!isUser) {
                // Ícone da IA com Emblema Dourado
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(GoldPrimary, GoldSecondary))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "S",
                        color = Color(0xFF141414),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            // Balão da Mensagem
            Surface(
                color = if (isUser) DarkSurfaceElevated else DarkSurface,
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = if (isUser) GoldSecondary.copy(alpha = 0.5f) else DarkCardBorder
                ),
                modifier = Modifier
                    .widthIn(max = 310.dp)
                    .testTag(if (isUser) "user_message_bubble" else "ai_message_bubble")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    if (!isUser) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Stronger IA",
                                color = GoldPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Row {
                                IconButton(onClick = onCopy, modifier = Modifier.size(24.dp)) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copiar",
                                        tint = TextMuted,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                                IconButton(onClick = onShare, modifier = Modifier.size(24.dp)) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Compartilhar",
                                        tint = TextMuted,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    Text(
                        text = message.text,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        lineHeight = 21.sp
                    )

                    // Fontes de Pesquisa do Google Grounding se houver
                    if (message.searchSources.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Fontes consultadas:",
                            color = GoldSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        message.searchSources.take(3).forEach { source ->
                            Text(
                                text = "• $source",
                                color = TextMuted,
                                fontSize = 9.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
