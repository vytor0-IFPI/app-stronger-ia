package com.example.data.model

data class Episode(
    val id: Long,
    val titulo: String,
    val convidado: String? = null,
    val data: String,
    val youtube: String? = null,
    val spotify: String? = null,
    val mp3: String? = null,
    val capa: String? = null,
    val publicado: Boolean = true
)

data class Devotional(
    val id: Long,
    val titulo: String,
    val versiculo: String,
    val texto: String,
    val data: String,
    val publicado: Boolean = true
)

data class RecommendedChannel(
    val id: Long,
    val titulo: String,
    val canal: String,
    val link: String,
    val tipo: String, // "video" ou "canal"
    val publicado: Boolean = true
)

data class UserProfile(
    val id: String,
    val email: String,
    val nome: String = "",
    val admin: Boolean = false
)

data class BibleBook(
    val id: Int,
    val name: String,
    val abbrev: String,
    val testament: String, // "AT" (Antigo Testamento) ou "NT" (Novo Testamento)
    val chaptersCount: Int,
    val group: String
)

data class BibleVerse(
    val number: Int,
    val text: String
)

data class BibleChapter(
    val bookId: Int,
    val bookName: String,
    val chapterNumber: Int,
    val verses: List<BibleVerse>
)
