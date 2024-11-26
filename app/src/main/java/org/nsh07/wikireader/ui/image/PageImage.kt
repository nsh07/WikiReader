package org.nsh07.wikireader.ui.image

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import org.nsh07.wikireader.R
import org.nsh07.wikireader.data.WikiPhoto
import org.nsh07.wikireader.data.WikiPhotoDesc

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
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(
                        photo.width.toFloat() / photo.height.toFloat()
                    )
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        },
        error = {
            Icon(
                painterResource(R.drawable.error),
                contentDescription = "Error loading image",
                modifier = Modifier.padding(vertical = 16.dp)
            )
        },
        contentDescription = photoDesc.description?.get(0) ?: "",
        imageLoader = imageLoader,
        contentScale = contentScale,
        modifier = modifier
    )
}