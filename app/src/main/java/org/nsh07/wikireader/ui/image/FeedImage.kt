package org.nsh07.wikireader.ui.image

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade

@Composable
fun FeedImage(
    source: String?,
    modifier: Modifier = Modifier,
    description: String? = null,
    width: Int? = null,
    height: Int? = null,
    imageLoader: ImageLoader,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(source)
            .crossfade(true)
            .build(),
        contentDescription = description,
        imageLoader = imageLoader,
        contentScale = contentScale,
        modifier =
        if (width != null && height != null)
            modifier
                .fillMaxWidth()
                .aspectRatio(width.toFloat() / height.toFloat())
        else
            modifier.fillMaxWidth()
    )
}