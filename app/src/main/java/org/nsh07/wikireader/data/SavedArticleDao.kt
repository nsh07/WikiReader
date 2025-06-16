package org.nsh07.wikireader.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedArticleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(savedArticle: SavedArticle)

    @Query("DELETE FROM saved_article WHERE pageId = :pageId AND lang = :lang")
    suspend fun delete(pageId: Int, lang: String)

    @Query("DELETE FROM saved_article")
    suspend fun deleteAll()

    @Query("SELECT EXISTS(SELECT * FROM saved_article WHERE pageId = :pageId AND lang = :lang)")
    suspend fun isSaved(pageId: Int, lang: String): Boolean

    @Query("SELECT * FROM saved_article WHERE pageId = :pageId AND lang = :lang LIMIT 1")
    suspend fun getSavedArticle(pageId: Int, lang: String): SavedArticle?

    @Query("SELECT DISTINCT lang, langName FROM saved_article")
    fun getSavedArticleLanguages(): Flow<List<LanguageInfo>>

    @Query("SELECT pageId, lang, langName, title, description FROM saved_article ORDER BY lang, title")
    fun getAllSavedArticles(): Flow<List<ArticleInfo>>
}