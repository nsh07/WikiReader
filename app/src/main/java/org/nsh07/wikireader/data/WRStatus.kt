package org.nsh07.wikireader.data

enum class WRStatus {
    SUCCESS, NETWORK_ERROR, NO_SEARCH_RESULT, UNINITIALIZED,
    FEED_LOADED, FEED_NETWORK_ERROR, OTHER, DATABASE_ERROR
}

enum class SavedStatus {
    NOT_SAVED, SAVING, SAVED
}

enum class AppStatus {
    INITIALIZED, NOT_INITIALIZED
}