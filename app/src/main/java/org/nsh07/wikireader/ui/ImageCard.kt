package org.nsh07.wikireader.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.SubcomposeAsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import org.nsh07.wikireader.R
import org.nsh07.wikireader.data.WikiPhoto
import org.nsh07.wikireader.data.WikiPhotoDesc

/**
 * Composable for displaying a Wikipedia image with its associated text
 *
 * Displays a [SubcomposeAsyncImage] composable with a [CircularProgressIndicator] while the image is
 * loading, with the image title and description at the bottom of a card.
 *
 * @param photo A (nullable) WikiPhoto object. The image url and aspect ratio are provided by this
 * object
 * @param photoDesc A WikiPhotoDesc object that provides the image title and description
 */

@Composable
fun WikiImageCard(
    photo: WikiPhoto?,
    photoDesc: WikiPhotoDesc,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Card(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .animateContentSize(tween(easing = FastOutSlowInEasing))
        ) {
            if (photo != null) {
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
                    contentDescription = photoDesc.description[0],
                    imageLoader = ImageLoader.Builder(LocalContext.current)
                        .components {
                            add(SvgDecoder.Factory())
                        }
                        .build(),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Text(
                text = photoDesc.label[0],
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp, bottom = 8.dp)
                    .fillMaxWidth()
            )
            Text(
                text = photoDesc.description[0],
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
                    .fillMaxWidth()
            )
        }
    }
}