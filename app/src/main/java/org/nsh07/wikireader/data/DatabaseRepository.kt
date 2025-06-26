package org.nsh07.wikireader.data

import kotlinx.coroutines.flow.Flow

interface DatabaseRepository {
    suspend fun insertSearchHistory(search: SearchHistoryItem, deduplicate: Boolean = true)
    suspend fun deleteSearchHistory(search: SearchHistoryItem)
    suspend fun deleteAllSearchHistory()
    suspend fun deleteOldSearchHistory()
    fun getSearchHistory(): Flow<List<SearchHistoryItem>>

    suspend fun insertViewHistory(viewHistoryItem: ViewHistoryItem)
    suspend fun deleteViewHistory(viewHistoryItem: ViewHistoryItem)
    suspend fun deleteAllViewHistory()
    suspend fun deleteOldViewHistory()
    fun getViewHistory(): Flow<List<ViewHistoryItem>>
    fun getRecentLanguages(): Flow<List<String>>

    suspend fun insertSavedArticle(savedArticle: SavedArticle)
    suspend fun deleteSavedArticle(pageId: Int, lang: String)
    suspend fun deleteAllSavedArticles()
    suspend fun isArticleSaved(pageId: Int, lang: String): Boolean
    suspend fun getSavedArticle(pageId: Int, lang: String): SavedArticle?
    fun getSavedArticleLanguages(): Flow<List<LanguageInfo>>
    fun getSavedArticles(): Flow<List<ArticleInfo>>

    suspend fun insertUserLanguage(userLanguage: UserLanguage)
    suspend fun deleteUserLanguage(lang: String)
    suspend fun deselectAllUserLanguages()
    suspend fun markUserLanguageSelected(lang: String)
    fun getUserLanguages(): Flow<List<UserLanguage>>
}

class AppDatabaseRepository(
    private val searchHistoryDao: SearchHistoryDao,
    private val savedArticleDao: SavedArticleDao,
    private val viewHistoryDao: ViewHistoryDao,
    private val userLanguageDao: UserLanguageDao
) : DatabaseRepository {
    override suspend fun insertSearchHistory(search: SearchHistoryItem, deduplicate: Boolean) {
        if (deduplicate) searchHistoryDao.deduplicateSearch(search.query, search.lang)
        searchHistoryDao.insert(search)
    }

    override suspend fun deleteSearchHistory(search: SearchHistoryItem) =
        searchHistoryDao.delete(search)

    override suspend fun deleteAllSearchHistory() = searchHistoryDao.deleteAll()
    override suspend fun deleteOldSearchHistory() = searchHistoryDao.deleteOld()
    override fun getSearchHistory(): Flow<List<SearchHistoryItem>> =
        searchHistoryDao.getSearchHistory()

    override suspend fun insertViewHistory(viewHistoryItem: ViewHistoryItem) {
        val last = viewHistoryDao.getLast()
        if (last?.title == viewHistoryItem.title && last.lang == viewHistoryItem.lang) {
            viewHistoryDao.deleteByTime(last.time) // Delete the last inserted item if it's identical
        }
        viewHistoryDao.insert(viewHistoryItem)
    }

    override suspend fun deleteViewHistory(viewHistoryItem: ViewHistoryItem) =
        viewHistoryDao.delete(viewHistoryItem)

    override suspend fun deleteAllViewHistory() = viewHistoryDao.deleteAll()
    override suspend fun deleteOldViewHistory() = viewHistoryDao.deleteOld()
    override fun getViewHistory(): Flow<List<ViewHistoryItem>> = viewHistoryDao.getViewHistory()
    override fun getRecentLanguages(): Flow<List<String>> = viewHistoryDao.getRecentLanguages()

    override suspend fun insertSavedArticle(savedArticle: SavedArticle) =
        savedArticleDao.insert(savedArticle)

    override suspend fun deleteSavedArticle(pageId: Int, lang: String) =
        savedArticleDao.delete(pageId, lang)

    override suspend fun deleteAllSavedArticles() = savedArticleDao.deleteAll()
    override suspend fun isArticleSaved(pageId: Int, lang: String): Boolean =
        savedArticleDao.isSaved(pageId, lang)

    override suspend fun getSavedArticle(pageId: Int, lang: String): SavedArticle? =
        savedArticleDao.getSavedArticle(pageId, lang)

    override fun getSavedArticleLanguages(): Flow<List<LanguageInfo>> =
        savedArticleDao.getSavedArticleLanguages()

    override fun getSavedArticles(): Flow<List<ArticleInfo>> =
        savedArticleDao.getAllSavedArticles()

    override suspend fun insertUserLanguage(userLanguage: UserLanguage) {
        if (userLanguage.selected) deselectAllUserLanguages()
        userLanguageDao.insert(userLanguage)
    }

    override suspend fun deleteUserLanguage(lang: String) =
        userLanguageDao.delete(lang)

    override suspend fun deselectAllUserLanguages() = userLanguageDao.deselectAll()
    override suspend fun markUserLanguageSelected(lang: String) = userLanguageDao.markSelected(lang)
    override fun getUserLanguages(): Flow<List<UserLanguage>> = userLanguageDao.getUserLanguages()
}