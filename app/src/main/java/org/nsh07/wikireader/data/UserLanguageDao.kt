package org.nsh07.wikireader.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserLanguageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(userLanguage: UserLanguage)

    @Delete
    suspend fun delete(userLanguage: UserLanguage)

    @Query("UPDATE user_language SET selected = 0")
    suspend fun deselectAll()

    @Query("UPDATE user_language SET selected = 1 WHERE lang = :lang")
    suspend fun markSelected(lang: String)

    @Query("SELECT * FROM user_language")
    fun getUserLanguages(): Flow<List<UserLanguage>>
}