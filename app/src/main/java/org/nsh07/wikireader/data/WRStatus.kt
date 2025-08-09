package org.nsh07.wikireader.data

enum class WRStatus {
    SUCCESS, NETWORK_ERROR, NO_SEARCH_RESULT, UNINITIALIZED,
    FEED_LOADED, FEED_NETWORK_ERROR, OTHER, DATABASE_ERROR
}

enum class SavedStatus {
    NOT_SAVED, SAVING, SAVED
}

/**
 * Enum class to track the app's startup status. On startup, the app status is
 * [AppStatus.NOT_INITIALIZED]. After all the settings are loaded and applied, the status is set to
 * [AppStatus.INITIALIZED]. This status is used to control the visibility of the splash screen
 */
enum class AppStatus {
    INITIALIZED, NOT_INITIALIZED
}