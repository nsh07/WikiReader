package org.nsh07.wikireader.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [SearchHistoryItem::class, SavedArticle::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun historyDao(): SearchHistoryDao
    abstract fun savedArticleDao(): SavedArticleDao

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