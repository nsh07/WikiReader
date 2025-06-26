package org.nsh07.wikireader.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_language")
data class UserLanguage(
    @PrimaryKey
    val lang: String,
    val langName: String,
    val selected: Boolean = false
)
