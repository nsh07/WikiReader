package org.nsh07.wikireader.ui.homeScreen

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asComposeColorFilter
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import org.nsh07.wikireader.parser.toWikitextAnnotatedString
import org.nsh07.wikireader.ui.image.FeedImage

@Composable
fun ImageWithCaption(
    text: String,
    fontSize: Int,
    darkTheme: Boolean,
    imageLoader: ImageLoader,
    onLinkClick: (String) -> Unit,
    onClick: (String, String) -> Unit
) {
    val uri = remember {
        "https://commons.wikimedia.org/wiki/Special:FilePath/${
            text.substringAfter(':').substringBefore('|')
        }"
    }
    val description = remember { text.substringAfter('|', "").substringBefore('|') }
    val invert = remember { text.contains("|invert") }

    OutlinedCard(
        modifier = Modifier.padding(16.dp),
        onClick = { onClick(uri, description) }
    ) {
        FeedImage(
            source = uri,
            description = description,
            width = 1,
            height = 1,
            imageLoader = imageLoader,
            loadingIndicator = false,
            colorFilter = if (invert && darkTheme) // Invert colors in dark theme
                PorterDuffColorFilter(
                    0xffffffff.toInt(),
                    PorterDuff.Mode.SRC_IN
                ).asComposeColorFilter()
            else null
        )
        Text(
            description.toWikitextAnnotatedString(
                colorScheme = colorScheme,
                fontSize = fontSize,
                loadPage = onLinkClick,
                typography = typography
            ),
            modifier = Modifier.padding(20.dp)
        )
    }
}