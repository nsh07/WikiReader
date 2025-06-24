package org.nsh07.wikireader.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "string_preference")
data class StringPreference(
    @PrimaryKey
    val key: String,
    val value: String
)

@Entity(tableName = "int_preference")
data class IntPreference(
    @PrimaryKey
    val key: String,
    val value: Int
)

@Entity(tableName = "boolean_preference")
data class BooleanPreference(
    @PrimaryKey
    val key: String,
    val value: Boolean
)