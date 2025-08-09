package org.nsh07.wikireader.ui.settingsScreen.viewModel

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class PreferencesState(
    val theme: String = "auto",
    val lang: String = "en",
    val fontStyle: String = "sans",
    val colorScheme: String = Color.Companion.White.toString(),
    val fontSize: Int = 16,
    val blackTheme: Boolean = false,
    val dataSaver: Boolean = false,
    val feedEnabled: Boolean = true,
    val expandedSections: Boolean = false,
    val imageBackground: Boolean = false,
    val immersiveMode: Boolean = true,
    val renderMath: Boolean = true,
    val browsingHistory: Boolean = true,
    val searchHistory: Boolean = true
)