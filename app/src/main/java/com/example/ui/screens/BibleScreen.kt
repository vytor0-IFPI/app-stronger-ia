package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BibleBook
import com.example.data.model.BibleVerse
import com.example.data.repository.BibleRepository
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GoldSecondary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class BibleViewMode {
    BOOKS_LIST,
    CHAPTER_SELECT,
    READING
}

@Composable
fun BibleScreen(
    onOpenAiWithPrompt: (prompt: String) -> Unit = {}
) {
    var viewMode by remember { mutableStateOf(BibleViewMode.BOOKS_LIST) }
    var selectedBook by remember { mutableStateOf<BibleBook?>(null) }
    var selectedChapterNumber by remember { mutableIntStateOf(1) }
    var selectedTestamentTab by remember { mutableIntStateOf(0) } // 0 = AT (39 livros), 1 = NT (27 livros)
    var bookSearchQuery by remember { mutableStateOf("") }
    var fontSizeSp by remember { mutableFloatStateOf(16f) }
    val context = LocalContext.current

    // Suporte ao botão voltar nativo do Android
    BackHandler(enabled = viewMode != BibleViewMode.BOOKS_LIST) {
        when (viewMode) {
            BibleViewMode.READING -> viewMode = BibleViewMode.CHAPTER_SELECT
            BibleViewMode.CHAPTER_SELECT -> viewMode = BibleViewMode.BOOKS_LIST
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("bible_screen")
    ) {
        when (viewMode) {
            BibleViewMode.BOOKS_LIST -> {
                // Abas AT / NT
                TabRow(
                    selectedTabIndex = selectedTestamentTab,
                    containerColor = DarkSurface,
                    contentColor = GoldPrimary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTestamentTab]),
                            color = GoldPrimary,
                            height = 3.dp
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTestamentTab == 0,
                        onClick = { selectedTestamentTab = 0 },
                        text = {
                            Text(
                                text = "Antigo Testamento (39)",
                                fontWeight = if (selectedTestamentTab == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTestamentTab == 0) GoldPrimary else TextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    )
                    Tab(
                        selected = selectedTestamentTab == 1,
                        onClick = { selectedTestamentTab = 1 },
                        text = {
                            Text(
                                text = "Novo Testamento (27)",
                                fontWeight = if (selectedTestamentTab == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTestamentTab == 1) GoldPrimary else TextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    )
                }

                // Campo de pesquisa de livro
                Box(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = bookSearchQuery,
                        onValueChange = { bookSearchQuery = it },
                        placeholder = { Text("Buscar livro da Bíblia...", color = TextMuted, fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = GoldPrimary)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = DarkSurface,
                            unfocusedContainerColor = DarkSurface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_bible_books_input")
                    )
                }

                // Lista de Livros
                val currentTestamentCode = if (selectedTestamentTab == 0) "AT" else "NT"
                val filteredBooks = BibleRepository.allBooks
                    .filter { it.testament == currentTestamentCode }
                    .filter {
                        if (bookSearchQuery.isBlank()) true
                        else it.name.lowercase().contains(bookSearchQuery.lowercase()) ||
                                it.abbrev.lowercase().contains(bookSearchQuery.lowercase())
                    }

                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredBooks, key = { it.id }) { book ->
                        BookRowItem(
                            book = book,
                            onClick = {
                                selectedBook = book
                                viewMode = BibleViewMode.CHAPTER_SELECT
                            }
                        )
                    }
                }
            }

            BibleViewMode.CHAPTER_SELECT -> {
                val book = selectedBook ?: return
                // Top Header com Voltar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewMode = BibleViewMode.BOOKS_LIST },
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("back_to_books_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar aos livros",
                            tint = GoldPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = book.name,
                            color = TextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Selecione um capítulo (Total: ${book.chaptersCount})",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                HorizontalDivider(color = DarkCardBorder, thickness = 1.dp)

                // Grade de Capítulos
                val chapters = (1..book.chaptersCount).toList()
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 60.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(chapters) { ch ->
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkSurface)
                                .border(1.dp, DarkCardBorder, RoundedCornerShape(12.dp))
                                .clickable {
                                    selectedChapterNumber = ch
                                    viewMode = BibleViewMode.READING
                                }
                                .testTag("select_chapter_$ch"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = ch.toString(),
                                color = GoldPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            BibleViewMode.READING -> {
                val book = selectedBook ?: return
                val chapterData = BibleRepository.getChapter(book.id, selectedChapterNumber)

                // Barra superior de leitura
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { viewMode = BibleViewMode.CHAPTER_SELECT },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Voltar aos capítulos",
                                tint = GoldPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "${book.name} $selectedChapterNumber",
                                color = TextPrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Bíblia Livre · Modo Offline",
                                color = GoldSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Controles de Tamanho de Fonte (A- / A+)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(DarkSurfaceElevated)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        IconButton(
                            onClick = { fontSizeSp = (fontSizeSp - 2f).coerceAtLeast(12f) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Text(text = "A-", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        IconButton(
                            onClick = { fontSizeSp = (fontSizeSp + 2f).coerceAtMost(26f) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Text(text = "A+", color = GoldPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                HorizontalDivider(color = DarkCardBorder, thickness = 1.dp)

                // Leitor Versículo por Versículo
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkSurfaceElevated)
                                .border(1.dp, GoldSecondary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .clickable {
                                    onOpenAiWithPrompt("Poderia me explicar o contexto histórico, os temas centrais e lições práticas para jovens de ${book.name} capítulo $selectedChapterNumber?")
                                }
                                .padding(12.dp)
                                .testTag("ai_study_chapter_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = GoldPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Tirar dúvidas com a Stronger IA",
                                            color = GoldPrimary,
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Explicações teológicas e práticas deste capítulo",
                                            color = TextSecondary,
                                            fontSize = 10.5.sp
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = GoldSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    items(chapterData.verses, key = { it.number }) { verse ->
                        VerseItem(
                            verse = verse,
                            bookName = book.name,
                            chapterNumber = selectedChapterNumber,
                            fontSizeSp = fontSizeSp,
                            onShare = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "\"${verse.text}\" — ${book.name} $selectedChapterNumber:${verse.number} (Bíblia Livre via Stronger Cast)"
                                    )
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Compartilhar Versículo"))
                            }
                        )
                    }

                    // Rodapé Obrigatório de Créditos
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkSurfaceElevated)
                                .border(1.dp, DarkCardBorder, RoundedCornerShape(12.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Bíblia Livre · CC BY 3.0 · github.com/blivre/BibliaLivre",
                                    color = TextMuted,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Texto em português moderno disponível offline em sua totalidade.",
                                    color = TextMuted,
                                    fontSize = 10.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun BookRowItem(
    book: BibleBook,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DarkCardBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag("book_item_${book.id}"),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceElevated)
                        .border(1.dp, GoldSecondary.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = book.abbrev,
                        color = GoldPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = book.name,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${book.group} · ${book.chaptersCount} capítulos",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun VerseItem(
    verse: BibleVerse,
    bookName: String,
    chapterNumber: Int,
    fontSizeSp: Float,
    onShare: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Número do Versículo em Destaque Dourado
        Text(
            text = verse.number.toString(),
            color = GoldPrimary,
            fontSize = (fontSizeSp * 0.8f).sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier
                .width(28.dp)
                .padding(top = 2.dp)
        )

        // Texto do Versículo
        Text(
            text = verse.text,
            color = TextPrimary,
            fontSize = fontSizeSp.sp,
            lineHeight = (fontSizeSp * 1.5f).sp,
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
        )

        // Botão Compartilhar Versículo
        IconButton(
            onClick = onShare,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Compartilhar",
                tint = TextMuted,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
