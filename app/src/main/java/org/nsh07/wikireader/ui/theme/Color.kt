package org.nsh07.wikireader.ui.theme

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

object CustomTopBarColors {
    var black = false

    @OptIn(ExperimentalMaterial3Api::class)
    val topBarColors: TopAppBarColors
        @Composable get() {
            return if (!black) TopAppBarDefaults.topAppBarColors(
                containerColor = colorScheme.surfaceContainer,
                scrolledContainerColor = colorScheme.surfaceContainer
            ) else TopAppBarDefaults.topAppBarColors()
        }
}

object ColorConstants {
    val colorMatrixInvert: FloatArray
        @Composable get() = remember {
            floatArrayOf(
                -1f, 0f, 0f, 0f, 255f, // Red
                0f, -1f, 0f, 0f, 255f, // Green
                0f, 0f, -1f, 0f, 255f, // Blue
                0f, 0f, 0f, 1f, 0f   // Alpha
            )
        }
}