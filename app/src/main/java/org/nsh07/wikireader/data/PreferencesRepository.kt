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

    suspend fun readStringPreference(key: String): String?
    suspend fun readIntPreference(key: String): Int?
    suspend fun readBooleanPreference(key: String): Boolean?

    suspend fun readOldStringPreference(key: String): String?
    suspend fun readOldIntPreference(key: String): Int?
    suspend fun readOldBooleanPreference(key: String): Boolean?
    suspend fun readOldHistory(): Set<String>?

    suspend fun resetSettings()
    suspend fun eraseOldStringPreference(key: String)
    suspend fun eraseOldIntPreference(key: String)
    suspend fun eraseOldBooleanPreference(key: String)
    suspend fun eraseOldHistory()
}

class AppPreferencesRepository(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher,
    private val preferenceDao: PreferenceDao
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
    override suspend fun saveStringPreference(key: String, value: String) =
        withContext(ioDispatcher) {
            preferenceDao.insertStringPreference(StringPreference(key, value))
            value
        }

    override suspend fun saveIntPreference(key: String, value: Int) =
        withContext(ioDispatcher) {
            preferenceDao.insertIntPreference(IntPreference(key, value))
            value
        }

    override suspend fun saveBooleanPreference(key: String, value: Boolean) =
        withContext(ioDispatcher) {
            preferenceDao.insertBooleanPreference(BooleanPreference(key, value))
            value
        }

    override suspend fun readStringPreference(key: String): String? =
        withContext(ioDispatcher) {
            preferenceDao.getStringPreference(key)?.value
        }

    override suspend fun readIntPreference(key: String): Int? =
        withContext(ioDispatcher) {
            preferenceDao.getIntPreference(key)?.value
        }

    override suspend fun readBooleanPreference(key: String): Boolean? =
        withContext(ioDispatcher) {
            preferenceDao.getBooleanPreference(key)?.value
        }

    /**
     * Reads the preference value for a given key in the app's [DataStore]
     *
     * @param key The key of the required associated value
     *
     * @return a [String] with the value corresponding to the [key]
     */
    override suspend fun readOldStringPreference(key: String): String? =
        withContext(ioDispatcher) {
            val dataStoreKey = stringPreferencesKey(key)
            context.dataStore.data.first()[dataStoreKey]
        }

    override suspend fun readOldIntPreference(key: String): Int? =
        withContext(ioDispatcher) {
            val dataStoreKey = intPreferencesKey(key)
            context.dataStore.data.first()[dataStoreKey]
        }

    override suspend fun readOldBooleanPreference(key: String): Boolean? =
        withContext(ioDispatcher) {
            val dataStoreKey = booleanPreferencesKey(key)
            context.dataStore.data.first()[dataStoreKey]
        }

    override suspend fun readOldHistory(): Set<String>? =
        withContext(ioDispatcher) {
            val dataStoreKey = stringSetPreferencesKey("history")
            context.dataStore.data.first()[dataStoreKey]
        }

    override suspend fun resetSettings() =
        withContext(ioDispatcher) {
            preferenceDao.resetStringPreferences()
            preferenceDao.resetIntPreferences()
            preferenceDao.resetBooleanPreferences()
        }

    override suspend fun eraseOldStringPreference(key: String) =
        withContext(ioDispatcher) {
            val dataStoreKey = stringPreferencesKey(key)
            context.dataStore.edit { preferences ->
                preferences.remove(dataStoreKey)
            }
            Unit
        }

    override suspend fun eraseOldIntPreference(key: String) =
        withContext(ioDispatcher) {
            val dataStoreKey = intPreferencesKey(key)
            context.dataStore.edit { preferences ->
                preferences.remove(dataStoreKey)
            }
            Unit
        }

    override suspend fun eraseOldBooleanPreference(key: String) =
        withContext(ioDispatcher) {
            val dataStoreKey = booleanPreferencesKey(key)
            context.dataStore.edit { preferences ->
                preferences.remove(dataStoreKey)
            }
            Unit
        }

    override suspend fun eraseOldHistory() =
        withContext(ioDispatcher) {
            val dataStoreKey = stringSetPreferencesKey("history")
            context.dataStore.edit { preferences ->
                preferences.remove(dataStoreKey)
            }
            Unit
        }
}