package org.nsh07.wikireader.data

enum class WRStatus {
    SUCCESS, NETWORK_ERROR, IO_ERROR, NO_SEARCH_RESULT, UNINITIALIZED,
    FEED_LOADED, FEED_NETWORK_ERROR, OTHER
}

enum class SavedStatus {
    NOT_SAVED, SAVING, SAVED
}