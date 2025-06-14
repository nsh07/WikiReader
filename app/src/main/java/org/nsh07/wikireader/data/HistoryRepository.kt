package org.nsh07.wikireader.data

import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    suspend fun insert(search: SearchHistoryItem)

    suspend fun delete(search: SearchHistoryItem)
    suspend fun deleteAll()

    fun getSearchHistory(): Flow<List<SearchHistoryItem>>
}

class AppHistoryRepository(private val searchHistoryDao: SearchHistoryDao) : HistoryRepository {
    override suspend fun insert(search: SearchHistoryItem) {
        searchHistoryDao.deduplicateSearch(search.query, search.lang)
        searchHistoryDao.insert(search)
    }

    override suspend fun delete(search: SearchHistoryItem) = searchHistoryDao.delete(search)

    override suspend fun deleteAll() = searchHistoryDao.deleteAll()

    override fun getSearchHistory(): Flow<List<SearchHistoryItem>> =
        searchHistoryDao.getSearchHistory()
}