package org.nsh07.wikireader.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PreferenceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStringPreference(preference: StringPreference)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIntPreference(preference: IntPreference)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooleanPreference(preference: BooleanPreference)

    @Query("DELETE FROM string_preference")
    suspend fun resetStringPreferences()

    @Query("DELETE FROM int_preference")
    suspend fun resetIntPreferences()

    @Query("DELETE FROM boolean_preference")
    suspend fun resetBooleanPreferences()

    @Query("SELECT * FROM string_preference WHERE `key` = :key")
    suspend fun getStringPreference(key: String): StringPreference?

    @Query("SELECT * FROM int_preference WHERE `key` = :key")
    suspend fun getIntPreference(key: String): IntPreference?

    @Query("SELECT * FROM boolean_preference WHERE `key` = :key")
    suspend fun getBooleanPreference(key: String): BooleanPreference?
}