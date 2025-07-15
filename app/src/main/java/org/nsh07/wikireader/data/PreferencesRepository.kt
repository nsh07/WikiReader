package org.nsh07.wikireader.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

interface PreferencesRepository {
    suspend fun saveStringPreference(key: String, value: String): String
    suspend fun saveIntPreference(key: String, value: Int): Int
    suspend fun saveBooleanPreference(key: String, value: Boolean): Boolean

    suspend fun readStringPreference(key: String): String?
    suspend fun readIntPreference(key: String): Int?
    suspend fun readBooleanPreference(key: String): Boolean?

    suspend fun resetSettings()
}

class AppPreferencesRepository(
    private val ioDispatcher: CoroutineDispatcher,
    private val preferenceDao: PreferenceDao
) : PreferencesRepository {

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

    override suspend fun resetSettings() =
        withContext(ioDispatcher) {
            preferenceDao.resetStringPreferences()
            preferenceDao.resetIntPreferences()
            preferenceDao.resetBooleanPreferences()
        }

}