package org.nsh07.wikireader.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

interface PreferencesRepository {
    suspend fun saveStringPreference(key: String, value: String): String
    suspend fun saveIntPreference(key: String, value: Int): Int
    suspend fun saveHistory(history: Set<String>)

    suspend fun readStringPreference(key: String): String?
    suspend fun readIntPreference(key: String): Int?
    suspend fun readHistory(): Set<String>?
}

class AppPreferencesRepository(
    private val context: Context
) : PreferencesRepository {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "preferences")

    /**
     * Saves a preference key-value pair into the app's [DataStore]
     *
     * @param key The key of the key-value pair
     * @param value The value of the key-value pair
     *
     * @return a [String] with the same value as [value]
     */
    override suspend fun saveStringPreference(key: String, value: String): String {
        val dataStoreKey = stringPreferencesKey(key)
        context.dataStore.edit { preferences ->
            preferences[dataStoreKey] = value
        }
        return value
    }

    override suspend fun saveIntPreference(key: String, value: Int): Int {
        val dataStoreKey = intPreferencesKey(key)
        context.dataStore.edit { preferences ->
            preferences[dataStoreKey] = value
        }
        return value
    }

    override suspend fun saveHistory(history: Set<String>) {
        val dataStoreKey = stringSetPreferencesKey("history")
        context.dataStore.edit { preferences ->
            preferences[dataStoreKey] = history
        }
    }

    /**
     * Reads the preference value for a given key in the app's [DataStore]
     *
     * @param key The key of the required associated value
     *
     * @return a [String] with the value corresponding to the [key]
     */
    override suspend fun readStringPreference(key: String): String? {
        val dataStoreKey = stringPreferencesKey(key)
        return context.dataStore.data.first()[dataStoreKey]
    }

    override suspend fun readIntPreference(key: String): Int? {
        val dataStoreKey = intPreferencesKey(key)
        return context.dataStore.data.first()[dataStoreKey]
    }

    override suspend fun readHistory(): Set<String>? {
        val dataStoreKey = stringSetPreferencesKey("history")
        return context.dataStore.data.first()[dataStoreKey]
    }
}