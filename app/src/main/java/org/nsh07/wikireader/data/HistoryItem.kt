package org.nsh07.wikireader.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "search_history")
data class SearchHistoryItem(
    @PrimaryKey
    val time: Long = System.currentTimeMillis(),
    val query: String,
    val lang: String
)

@Entity(tableName = "view_history")
data class ViewHistoryItem(
    @PrimaryKey
    val time: Long = System.currentTimeMillis(),
    val thumbnail: String?,
    val title: String,
    val lang: String
)
