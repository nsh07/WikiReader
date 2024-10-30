package org.nsh07.wikireader.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

interface PreferencesRepository {
    suspend fun savePreference(key: String, value: String): String
    suspend fun readPreference(key: String): String?
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
    override suspend fun savePreference(key: String, value: String): String {
        val dataStoreKey = stringPreferencesKey(key)
        context.dataStore.edit { preferences ->
            preferences[dataStoreKey] = value
        }
        return value
    }

    /**
     * Reads the preference value for a given key in the app's [DataStore]
     *
     * @param key The key of the required associated value
     *
     * @return a [String] with the value corresponding to the [key]
     */
    override suspend fun readPreference(key: String): String? {
        val dataStoreKey = stringPreferencesKey(key)
        return context.dataStore.data.first()[dataStoreKey]
    }
}