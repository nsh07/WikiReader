package org.nsh07.wikireader.ui.image

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import coil3.compose.AsyncImagePainter
import org.nsh07.wikireader.R
import org.nsh07.wikireader.data.WikiPhoto

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.PageImage(
    photo: WikiPhoto,
    photoDesc: String,
    painter: AsyncImagePainter,
    painterState: AsyncImagePainter.State,
    background: Boolean,
    contentScale: ContentScale,
    modifier: Modifier = Modifier
) {
    when (painterState) {
        is AsyncImagePainter.State.Success -> {
            Image(
                painter = painter,
                contentDescription = photoDesc,
                contentScale = contentScale,
                modifier = Modifier
                    .sharedBounds(
                        sharedContentState = rememberSharedContentState(photo.source),
                        animatedVisibilityScope = LocalNavAnimatedContentScope.current
                    )
                    .then(modifier)
                    .aspectRatio(photo.width.toFloat() / photo.height.toFloat())
                    .background(if (background) Color.White else Color.Transparent)
            )
        }

        is AsyncImagePainter.State.Loading -> {
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
        }

        else -> {
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
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.PageImage(
    uri: String,
    description: String,
    background: Boolean,
    painter: AsyncImagePainter,
    painterState: AsyncImagePainter.State,
    contentScale: ContentScale,
    modifier: Modifier = Modifier
) {
    when (painterState) {
        is AsyncImagePainter.State.Success -> {
            Image(
                painter = painter,
                contentDescription = description,
                contentScale = contentScale,
                modifier = Modifier
                    .sharedBounds(
                        sharedContentState = rememberSharedContentState(uri),
                        animatedVisibilityScope = LocalNavAnimatedContentScope.current
                    )
                    .then(modifier)
                    .background(if (background) Color.White else Color.Transparent)
            )
        }

        is AsyncImagePainter.State.Loading -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                CircularWavyProgressIndicator()
            }
        }

        else -> {
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
}