package org.nsh07.wikireader.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        SearchHistoryItem::class,
        ViewHistoryItem::class,
        SavedArticle::class,
        UserLanguage::class,
        StringPreference::class,
        IntPreference::class,
        BooleanPreference::class
    ],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun savedArticleDao(): SavedArticleDao
    abstract fun viewHistoryDao(): ViewHistoryDao
    abstract fun userLanguageDao(): UserLanguageDao
    abstract fun preferenceDao(): PreferenceDao

    companion object {

        @Volatile
        private var Instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "app_database")
                    .build()
                    .also { Instance = it }
            }
        }
    }
}