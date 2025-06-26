package org.nsh07.wikireader.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserLanguageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(userLanguage: UserLanguage)

    @Query("DELETE FROM user_language WHERE lang = :lang")
    suspend fun delete(lang: String)

    @Query("UPDATE user_language SET selected = 0")
    suspend fun deselectAll()

    @Query("UPDATE user_language SET selected = 1 WHERE lang = :lang")
    suspend fun markSelected(lang: String)

    @Query("SELECT * FROM user_language ORDER BY lang")
    fun getUserLanguages(): Flow<List<UserLanguage>>
}