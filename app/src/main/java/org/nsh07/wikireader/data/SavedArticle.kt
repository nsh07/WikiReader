package org.nsh07.wikireader.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_article")
data class SavedArticle(
    @PrimaryKey
    val pageId: Int,
    val lang: String,
    val langName: String,
    val title: String,
    val thumbnail: String?,
    val description: String?,
    val apiResponse: String,
    val pageContent: String
)

data class ArticleInfo(
    val pageId: Int,
    val lang: String,
    val langName: String,
    val title: String,
    val thumbnail: String?,
    val description: String?
)

data class LanguageInfo(
    val lang: String,
    val langName: String
)