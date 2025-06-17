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
    suspend fun insert(item: SearchHistoryItem)

    @Delete
    suspend fun delete(item: SearchHistoryItem)

    @Query("DELETE FROM search_history")
    suspend fun deleteAll()

    @Query("DELETE FROM search_history WHERE time in (SELECT time FROM search_history ORDER BY time DESC LIMIT 50 OFFSET 200)")
    suspend fun deleteOld()

    @Query("DELETE FROM search_history WHERE `query` = :query AND `lang` = :lang")
    suspend fun deduplicateSearch(query: String, lang: String)

    @Query("SELECT * FROM search_history ORDER BY time DESC LIMIT 200")
    fun getSearchHistory(): Flow<List<SearchHistoryItem>>
}

@Dao
interface ViewHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ViewHistoryItem)

    @Delete
    suspend fun delete(item: ViewHistoryItem)

    @Query("DELETE FROM view_history")
    suspend fun deleteAll()

    @Query("DELETE FROM view_history WHERE time in (SELECT time FROM view_history ORDER BY time DESC LIMIT 50 OFFSET 200)")
    suspend fun deleteOld()

    @Query("SELECT * FROM view_history ORDER BY time DESC LIMIT 200")
    fun getViewHistory(): Flow<List<ViewHistoryItem>>
}