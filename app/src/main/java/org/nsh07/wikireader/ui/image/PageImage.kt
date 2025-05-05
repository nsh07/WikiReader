package org.nsh07.wikireader.ui.image

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import org.nsh07.wikireader.R
import org.nsh07.wikireader.data.WikiPhoto
import org.nsh07.wikireader.data.WikiPhotoDesc

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PageImage(
    photo: WikiPhoto,
    photoDesc: WikiPhotoDesc,
    imageLoader: ImageLoader,
    background: Boolean,
    contentScale: ContentScale,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(photo.source)
            .crossfade(true)
            .build(),
        imageLoader = imageLoader,
        contentScale = contentScale,
    )

    val painterState by painter.state.collectAsState()

    if (painterState is AsyncImagePainter.State.Success) {
        Image(
            painter = painter,
            contentDescription = photoDesc.description?.get(0) ?: "",
            contentScale = contentScale,
            modifier = modifier
                .aspectRatio(photo.width.toFloat() / photo.height.toFloat())
                .background(if (background) Color.White else Color.Transparent)
        )
    } else if (painterState is AsyncImagePainter.State.Loading) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(
                    photo.width.toFloat() / photo.height.toFloat()
                )
        ) {
            CircularWavyProgressIndicator()
        }
    } else {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(
                    photo.width.toFloat() / photo.height.toFloat()
                )
        ) {
            Icon(
                painterResource(R.drawable.error),
                contentDescription = stringResource(R.string.errorLoadingImage),
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .size(64.dp),
                tint = colorScheme.error
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PageImage(
    uri: String,
    description: String,
    background: Boolean,
    imageLoader: ImageLoader,
    contentScale: ContentScale,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(uri)
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
            modifier = modifier.background(if (background) Color.White else Color.Transparent)
        )
    } else if (painterState is AsyncImagePainter.State.Loading) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            CircularWavyProgressIndicator()
        }
    } else {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Icon(
                painterResource(R.drawable.error),
                contentDescription = stringResource(R.string.errorLoadingImage),
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .size(64.dp),
                tint = colorScheme.error
            )
        }
    }
}