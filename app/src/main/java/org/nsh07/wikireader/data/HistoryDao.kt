package org.nsh07.wikireader.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(search: SearchHistoryItem)

    @Delete
    suspend fun delete(search: SearchHistoryItem)

    @Query("DELETE FROM search_history")
    suspend fun deleteAll()

    @Query("DELETE FROM search_history WHERE `query` = :query AND `lang` = :lang")
    suspend fun deduplicateSearch(query: String, lang: String)

    @Query("SELECT * FROM search_history ORDER BY time DESC LIMIT 50")
    fun getSearchHistory(): Flow<List<SearchHistoryItem>>
}