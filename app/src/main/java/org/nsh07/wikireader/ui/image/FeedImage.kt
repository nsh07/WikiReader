package org.nsh07.wikireader.ui.image

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import coil3.ImageLoader
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import org.nsh07.wikireader.R

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FeedImage(
    source: String?,
    modifier: Modifier = Modifier,
    description: String? = null,
    width: Int? = null,
    height: Int? = null,
    imageLoader: ImageLoader,
    loadingIndicator: Boolean,
    colorFilter: ColorFilter? = null,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(source)
            .crossfade(true)
            .build(),
        imageLoader = imageLoader,
        contentScale = contentScale,
    )

    val painterState by painter.state.collectAsState()

    if (painterState is AsyncImagePainter.State.Success) {
        Image(
            painter = painter,
            contentDescription = description,
            contentScale = contentScale,
            colorFilter = colorFilter,
            modifier =
                if (width != null && height != null)
                    modifier
                        .fillMaxWidth()
                        .aspectRatio(width.toFloat() / height.toFloat())
                else
                    modifier
                        .fillMaxSize()
        )
    } else if (painterState is AsyncImagePainter.State.Loading) {
        Box(
            contentAlignment = Alignment.Center,
            modifier =
                if (width != null && height != null)
                    modifier
                        .fillMaxWidth()
                        .aspectRatio(width.toFloat() / height.toFloat())
                else
                    modifier
                        .fillMaxSize()
        ) {
            if (loadingIndicator) LoadingIndicator()
            else CircularWavyProgressIndicator()
        }
    } else {
        Box(
            contentAlignment = Alignment.Center,
            modifier =
                if (width != null && height != null)
                    modifier
                        .fillMaxWidth()
                        .aspectRatio(width.toFloat() / height.toFloat())
                else
                    modifier.fillMaxSize()
        ) {
            Icon(
                painterResource(R.drawable.error),
                contentDescription = stringResource(R.string.errorLoadingImage),
                tint = colorScheme.error
            )
        }
    }
}