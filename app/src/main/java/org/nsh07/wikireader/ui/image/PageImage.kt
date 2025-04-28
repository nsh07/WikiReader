package org.nsh07.wikireader.ui.image

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.SubcomposeAsyncImage
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
    contentScale: ContentScale,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    SubcomposeAsyncImage(
        model = ImageRequest.Builder(context)
            .data(photo.source)
            .crossfade(true)
            .build(),
        loading = {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(
                        photo.width.toFloat() / photo.height.toFloat()
                    )
            ) {
                CircularWavyProgressIndicator()
            }
        },
        error = {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
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
                    tint = MaterialTheme.colorScheme.error
                )
            }
        },
        contentDescription = photoDesc.description?.get(0) ?: "",
        imageLoader = imageLoader,
        contentScale = contentScale,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PageImage(
    uri: String,
    description: String,
    imageLoader: ImageLoader,
    contentScale: ContentScale,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    SubcomposeAsyncImage(
        model = ImageRequest.Builder(context)
            .data(uri)
            .crossfade(true)
            .build(),
        loading = {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                CircularWavyProgressIndicator()
            }
        },
        error = {
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
                    tint = MaterialTheme.colorScheme.error
                )
            }
        },
        contentDescription = description,
        imageLoader = imageLoader,
        contentScale = contentScale,
        modifier = modifier
    )
}