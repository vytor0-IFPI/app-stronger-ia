package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Podcasts
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Devotional
import com.example.data.model.Episode
import com.example.data.model.RecommendedChannel
import com.example.data.repository.StrongerCastRepository
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GoldSecondary
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminPanelScreen() {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Episódios, 1 = Devocionais, 2 = Canais

    val episodes by StrongerCastRepository.episodes.collectAsState()
    val devotionals by StrongerCastRepository.devotionals.collectAsState()
    val channels by StrongerCastRepository.channels.collectAsState()

    // Modais e Estados de Criação/Edição
    var showEpisodeDialog by remember { mutableStateOf(false) }
    var editingEpisode by remember { mutableStateOf<Episode?>(null) }
    var episodeToDeleteId by remember { mutableLongStateOf(-1L) }

    var showDevotionalDialog by remember { mutableStateOf(false) }
    var editingDevotional by remember { mutableStateOf<Devotional?>(null) }
    var devotionalToDeleteId by remember { mutableLongStateOf(-1L) }

    var showChannelDialog by remember { mutableStateOf(false) }
    var editingChannel by remember { mutableStateOf<RecommendedChannel?>(null) }
    var channelToDeleteId by remember { mutableLongStateOf(-1L) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("admin_panel_screen")
    ) {
        // Banner Superior Admin
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(GoldSecondary.copy(alpha = 0.15f))
                .border(1.dp, GoldSecondary, RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AdminPanelSettings,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Painel de Administração Stronger",
                        color = GoldPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Crie, edite ou apague conteúdos com sincronização direta.",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Abas: Episódios, Devocionais, Canais
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = DarkSurface,
            contentColor = GoldPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = GoldPrimary,
                    height = 3.dp
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Text(
                        text = "Episódios (${episodes.size})",
                        color = if (selectedTab == 0) GoldPrimary else TextSecondary,
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 12.sp
                    )
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Text(
                        text = "Devocionais (${devotionals.size})",
                        color = if (selectedTab == 1) GoldPrimary else TextSecondary,
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 12.sp
                    )
                }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = {
                    Text(
                        text = "Canais (${channels.size})",
                        color = if (selectedTab == 2) GoldPrimary else TextSecondary,
                        fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 12.sp
                    )
                }
            )
        }

        // Conteúdo da Aba Selecionada
        when (selectedTab) {
            0 -> {
                // ABA EPISÓDIOS
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Gerenciar Episódios",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Button(
                            onClick = {
                                editingEpisode = null
                                showEpisodeDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color(0xFF141414)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("add_episode_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Novo Episódio", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(episodes, key = { it.id }) { ep ->
                            AdminItemRow(
                                title = ep.titulo,
                                subtitle = "${ep.convidado ?: "Sem convidado"} · ${ep.data}",
                                isPublished = ep.publicado,
                                onEdit = {
                                    editingEpisode = ep
                                    showEpisodeDialog = true
                                },
                                onDelete = { episodeToDeleteId = ep.id }
                            )
                        }
                    }
                }
            }

            1 -> {
                // ABA DEVOCIONAIS
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Gerenciar Devocionais",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Button(
                            onClick = {
                                editingDevotional = null
                                showDevotionalDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color(0xFF141414)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("add_devotional_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Novo Devocional", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(devotionals, key = { it.id }) { dev ->
                            AdminItemRow(
                                title = dev.titulo,
                                subtitle = "${dev.versiculo} · ${dev.data}",
                                isPublished = dev.publicado,
                                onEdit = {
                                    editingDevotional = dev
                                    showDevotionalDialog = true
                                },
                                onDelete = { devotionalToDeleteId = dev.id }
                            )
                        }
                    }
                }
            }

            2 -> {
                // ABA CANAIS
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Gerenciar Canais & Vídeos",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Button(
                            onClick = {
                                editingChannel = null
                                showChannelDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color(0xFF141414)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("add_channel_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Novo Canal", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(channels, key = { it.id }) { ch ->
                            AdminItemRow(
                                title = ch.titulo,
                                subtitle = "${ch.canal} · Tipo: ${ch.tipo.uppercase()}",
                                isPublished = ch.publicado,
                                onEdit = {
                                    editingChannel = ch
                                    showChannelDialog = true
                                },
                                onDelete = { channelToDeleteId = ch.id }
                            )
                        }
                    }
                }
            }
        }
    }

    // DIÁLOGOS DE CONFIRMAÇÃO DE APAGAR OBRIGATÓRIOS ("Apagar este episódio? Não dá pra desfazer")
    if (episodeToDeleteId != -1L) {
        AlertDialog(
            onDismissRequest = { episodeToDeleteId = -1L },
            title = { Text("Apagar este episódio?", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Não dá pra desfazer esta ação. O episódio será excluído permanentemente.", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        StrongerCastRepository.deleteEpisode(episodeToDeleteId)
                        episodeToDeleteId = -1L
                        Toast.makeText(context, "Episódio apagado com sucesso.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusRed, contentColor = Color.White),
                    modifier = Modifier.testTag("confirm_delete_button")
                ) {
                    Text("Apagar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { episodeToDeleteId = -1L }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (devotionalToDeleteId != -1L) {
        AlertDialog(
            onDismissRequest = { devotionalToDeleteId = -1L },
            title = { Text("Apagar este devocional?", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Não dá pra desfazer esta ação. O devocional será removido.", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        StrongerCastRepository.deleteDevotional(devotionalToDeleteId)
                        devotionalToDeleteId = -1L
                        Toast.makeText(context, "Devocional apagado.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusRed, contentColor = Color.White)
                ) {
                    Text("Apagar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { devotionalToDeleteId = -1L }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (channelToDeleteId != -1L) {
        AlertDialog(
            onDismissRequest = { channelToDeleteId = -1L },
            title = { Text("Apagar recomendação?", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Não dá pra desfazer. O canal ou vídeo será excluído da lista pública.", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        StrongerCastRepository.deleteChannel(channelToDeleteId)
                        channelToDeleteId = -1L
                        Toast.makeText(context, "Canal apagado.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusRed, contentColor = Color.White)
                ) {
                    Text("Apagar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { channelToDeleteId = -1L }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // MODAL FORMULÁRIO DE EPISÓDIO
    if (showEpisodeDialog) {
        EpisodeEditDialog(
            episode = editingEpisode,
            onDismiss = { showEpisodeDialog = false },
            onSave = { ep ->
                StrongerCastRepository.saveEpisode(ep)
                showEpisodeDialog = false
                Toast.makeText(context, "Episódio salvo com sucesso!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // MODAL FORMULÁRIO DE DEVOCIONAL
    if (showDevotionalDialog) {
        DevotionalEditDialog(
            devotional = editingDevotional,
            onDismiss = { showDevotionalDialog = false },
            onSave = { dev ->
                StrongerCastRepository.saveDevotional(dev)
                showDevotionalDialog = false
                Toast.makeText(context, "Devocional salvo com sucesso!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // MODAL FORMULÁRIO DE CANAL
    if (showChannelDialog) {
        ChannelEditDialog(
            channel = editingChannel,
            onDismiss = { showChannelDialog = false },
            onSave = { ch ->
                StrongerCastRepository.saveChannel(ch)
                showChannelDialog = false
                Toast.makeText(context, "Canal salvo com sucesso!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun AdminItemRow(
    title: String,
    subtitle: String,
    isPublished: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DarkCardBorder, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isPublished) StatusGreen else TextMuted)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isPublished) "Publicado" else "Rascunho",
                        color = if (isPublished) StatusGreen else TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = subtitle,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }

            Row {
                IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = GoldPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Botão Vermelho "Apagar"
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Apagar",
                        tint = StatusRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// DIÁLOGO DE EDIÇÃO DE EPISÓDIO
@Composable
fun EpisodeEditDialog(
    episode: Episode?,
    onDismiss: () -> Unit,
    onSave: (Episode) -> Unit
) {
    val currentDateStr = remember {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    }

    var titulo by remember { mutableStateOf(episode?.titulo ?: "") }
    var convidado by remember { mutableStateOf(episode?.convidado ?: "") }
    var data by remember { mutableStateOf(episode?.data ?: currentDateStr) }
    var youtube by remember { mutableStateOf(episode?.youtube ?: "") }
    var spotify by remember { mutableStateOf(episode?.spotify ?: "") }
    var mp3 by remember { mutableStateOf(episode?.mp3 ?: "") }
    var capa by remember { mutableStateOf(episode?.capa ?: "") }
    var publicado by remember { mutableStateOf(episode?.publicado ?: true) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DarkCardBorder, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (episode == null) "Novo Episódio" else "Editar Episódio",
                    color = GoldPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                AdminTextField("Título", titulo) { titulo = it }
                Spacer(modifier = Modifier.height(10.dp))

                AdminTextField("Convidado (opcional)", convidado) { convidado = it }
                Spacer(modifier = Modifier.height(10.dp))

                AdminTextField("Data (dd/mm/aaaa)", data) { data = it }
                Spacer(modifier = Modifier.height(10.dp))

                AdminTextField("Link do YouTube (Assistir)", youtube) { youtube = it }
                Spacer(modifier = Modifier.height(10.dp))

                AdminTextField("Link do Spotify", spotify) { spotify = it }
                Spacer(modifier = Modifier.height(10.dp))

                AdminTextField("URL do MP3 (Upload Supabase até 50MB)", mp3) { mp3 = it }
                Spacer(modifier = Modifier.height(10.dp))

                AdminTextField("URL da Capa da Imagem", capa) { capa = it }
                Spacer(modifier = Modifier.height(14.dp))

                // Toggle Publicado / Rascunho
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Status de Publicação", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text(if (publicado) "Publicado no feed" else "Salvo como Rascunho", color = TextSecondary, fontSize = 11.sp)
                    }
                    Switch(
                        checked = publicado,
                        onCheckedChange = { publicado = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF141414), checkedTrackColor = GoldPrimary)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (titulo.isNotBlank()) {
                                onSave(
                                    Episode(
                                        id = episode?.id ?: 0L,
                                        titulo = titulo.trim(),
                                        convidado = convidado.trim().ifEmpty { null },
                                        data = data.trim(),
                                        youtube = youtube.trim().ifEmpty { null },
                                        spotify = spotify.trim().ifEmpty { null },
                                        mp3 = mp3.trim().ifEmpty { null },
                                        capa = capa.trim().ifEmpty { null },
                                        publicado = publicado
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color(0xFF141414))
                    ) {
                        Text("Salvar Episódio", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// DIÁLOGO DE EDIÇÃO DE DEVOCIONAL
@Composable
fun DevotionalEditDialog(
    devotional: Devotional?,
    onDismiss: () -> Unit,
    onSave: (Devotional) -> Unit
) {
    val currentDateStr = remember {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    }

    var titulo by remember { mutableStateOf(devotional?.titulo ?: "") }
    var versiculo by remember { mutableStateOf(devotional?.versiculo ?: "") }
    var texto by remember { mutableStateOf(devotional?.texto ?: "") }
    var data by remember { mutableStateOf(devotional?.data ?: currentDateStr) }
    var publicado by remember { mutableStateOf(devotional?.publicado ?: true) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DarkCardBorder, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (devotional == null) "Novo Devocional" else "Editar Devocional",
                    color = GoldPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                AdminTextField("Título", titulo) { titulo = it }
                Spacer(modifier = Modifier.height(10.dp))

                AdminTextField("Versículo (ex.: Hebreus 11.1)", versiculo) { versiculo = it }
                Spacer(modifier = Modifier.height(10.dp))

                AdminTextField("Data (dd/mm/aaaa)", data) { data = it }
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = texto,
                    onValueChange = { texto = it },
                    label = { Text("Texto Puro do Devocional") },
                    minLines = 4,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = DarkCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = DarkSurfaceElevated,
                        unfocusedContainerColor = DarkSurfaceElevated
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Publicar no Feed", color = TextPrimary, fontSize = 13.sp)
                    Switch(
                        checked = publicado,
                        onCheckedChange = { publicado = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF141414), checkedTrackColor = GoldPrimary)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (titulo.isNotBlank() && versiculo.isNotBlank() && texto.isNotBlank()) {
                                onSave(
                                    Devotional(
                                        id = devotional?.id ?: 0L,
                                        titulo = titulo.trim(),
                                        versiculo = versiculo.trim(),
                                        texto = texto.trim(),
                                        data = data.trim(),
                                        publicado = publicado
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color(0xFF141414))
                    ) {
                        Text("Salvar Devocional", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// DIÁLOGO DE EDIÇÃO DE CANAL
@Composable
fun ChannelEditDialog(
    channel: RecommendedChannel?,
    onDismiss: () -> Unit,
    onSave: (RecommendedChannel) -> Unit
) {
    var titulo by remember { mutableStateOf(channel?.titulo ?: "") }
    var canal by remember { mutableStateOf(channel?.canal ?: "") }
    var link by remember { mutableStateOf(channel?.link ?: "") }
    var publicado by remember { mutableStateOf(channel?.publicado ?: true) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DarkCardBorder, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (channel == null) "Novo Canal Recomendado" else "Editar Canal",
                    color = GoldPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                AdminTextField("Título", titulo) { titulo = it }
                Spacer(modifier = Modifier.height(10.dp))

                AdminTextField("Canal / Autor", canal) { canal = it }
                Spacer(modifier = Modifier.height(10.dp))

                AdminTextField("Link do YouTube (Vídeo ou Canal)", link) { link = it }
                Spacer(modifier = Modifier.height(6.dp))

                val isVideo = StrongerCastRepository.isYouTubeVideoLink(link)
                Text(
                    text = if (link.isBlank()) "Cole o link para detecção automática"
                    else if (isVideo) "Detectado: VÍDEO (toca dentro do app)"
                    else "Detectado: CANAL (abre no YouTube externo)",
                    color = if (isVideo) GoldPrimary else TextSecondary,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Publicar", color = TextPrimary, fontSize = 13.sp)
                    Switch(
                        checked = publicado,
                        onCheckedChange = { publicado = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF141414), checkedTrackColor = GoldPrimary)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (titulo.isNotBlank() && canal.isNotBlank() && link.isNotBlank()) {
                                onSave(
                                    RecommendedChannel(
                                        id = channel?.id ?: 0L,
                                        titulo = titulo.trim(),
                                        canal = canal.trim(),
                                        link = link.trim(),
                                        tipo = if (isVideo) "video" else "canal",
                                        publicado = publicado
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color(0xFF141414))
                    ) {
                        Text("Salvar Canal", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = GoldPrimary,
            unfocusedBorderColor = DarkCardBorder,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            focusedLabelColor = GoldPrimary,
            unfocusedLabelColor = TextMuted,
            focusedContainerColor = DarkSurfaceElevated,
            unfocusedContainerColor = DarkSurfaceElevated
        ),
        modifier = Modifier.fillMaxWidth()
    )
}
