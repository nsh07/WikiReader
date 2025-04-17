package org.nsh07.wikireader.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

interface PreferencesRepository {
    suspend fun saveStringPreference(key: String, value: String): String
    suspend fun saveIntPreference(key: String, value: Int): Int
    suspend fun saveBooleanPreference(key: String, value: Boolean): Boolean
    suspend fun saveHistory(history: Set<String>)

    suspend fun readStringPreference(key: String): String?
    suspend fun readIntPreference(key: String): Int?
    suspend fun readBooleanPreference(key: String): Boolean?
    suspend fun readHistory(): Set<String>?
}

class AppPreferencesRepository(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher
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
    override suspend fun saveStringPreference(key: String, value: String): String =
        withContext(ioDispatcher) {
            val dataStoreKey = stringPreferencesKey(key)
            context.dataStore.edit { preferences ->
                preferences[dataStoreKey] = value
            }
            value
        }

    override suspend fun saveIntPreference(key: String, value: Int): Int =
        withContext(ioDispatcher) {
            val dataStoreKey = intPreferencesKey(key)
            context.dataStore.edit { preferences ->
                preferences[dataStoreKey] = value
            }
            value
        }

    override suspend fun saveBooleanPreference(key: String, value: Boolean): Boolean =
        withContext(ioDispatcher) {
            val dataStoreKey = booleanPreferencesKey(key)
            context.dataStore.edit { preferences ->
                preferences[dataStoreKey] = value
            }
            value
        }

    override suspend fun saveHistory(history: Set<String>) =
        withContext(ioDispatcher) {
            val dataStoreKey = stringSetPreferencesKey("history")
            context.dataStore.edit { preferences ->
                preferences[dataStoreKey] = history
            }
            Unit
        }

    /**
     * Reads the preference value for a given key in the app's [DataStore]
     *
     * @param key The key of the required associated value
     *
     * @return a [String] with the value corresponding to the [key]
     */
    override suspend fun readStringPreference(key: String): String? =
        withContext(ioDispatcher) {
            val dataStoreKey = stringPreferencesKey(key)
            context.dataStore.data.first()[dataStoreKey]
        }

    override suspend fun readIntPreference(key: String): Int? =
        withContext(ioDispatcher) {
            val dataStoreKey = intPreferencesKey(key)
            context.dataStore.data.first()[dataStoreKey]
        }

    override suspend fun readBooleanPreference(key: String): Boolean? =
        withContext(ioDispatcher) {
            val dataStoreKey = booleanPreferencesKey(key)
            context.dataStore.data.first()[dataStoreKey]
        }

    override suspend fun readHistory(): Set<String>? =
        withContext(ioDispatcher) {
            val dataStoreKey = stringSetPreferencesKey("history")
            context.dataStore.data.first()[dataStoreKey]
        }
}