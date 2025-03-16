package org.nsh07.wikireader.ui.image

import android.annotation.SuppressLint
import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.gestures.animateZoomBy
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntSize
import androidx.core.view.WindowCompat
import coil3.ImageLoader
import kotlinx.coroutines.launch
import org.nsh07.wikireader.data.WikiPhoto
import org.nsh07.wikireader.data.WikiPhotoDesc

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScreenImage(
    photo: WikiPhoto?,
    photoDesc: WikiPhotoDesc?,
    title: String,
    imageLoader: ImageLoader,
    modifier: Modifier = Modifier,
    link: String? = null,
    onBack: () -> Unit,
) {
    var currentLightStatusBars = true
    val view = LocalView.current
    DisposableEffect(null) {
        if (!view.isInEditMode) {
            val window = (view.context as Activity).window
            currentLightStatusBars =
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }

        onDispose {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                currentLightStatusBars
        }
    }

    var showTopBar by remember { mutableStateOf(true) }
    Scaffold(
        topBar = {
            AnimatedVisibility(
                showTopBar,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                FullScreenImageTopBar(
                    photoDesc = photoDesc,
                    title = title,
                    link = link,
                    onBack = onBack
                )
            }
        },
        containerColor = Color.Black,
        modifier = modifier
    ) { _ ->
        var scale by remember { mutableFloatStateOf(1f) }
        var offset by remember { mutableStateOf(Offset.Zero) }
        var size by remember { mutableStateOf(IntSize.Zero) }

        // Zoom/Offset logic. Also prevents image from going out of bounds
        val state = rememberTransformableState { scaleChange, offsetChange, _ ->
            val maxX = (size.width * (scale - 1) / 2f)
            val maxY = (size.height * (scale - 1) / 2f)

            scale = (scale * scaleChange).coerceIn(1f..8f)

            offset += offsetChange.times(scale)
            offset = Offset(
                offset.x.coerceIn(-maxX, maxX),
                offset.y.coerceIn(-maxY, maxY)
            )
        }

        val coroutineScope = rememberCoroutineScope()

        if (photo != null && photoDesc != null)
            Box(modifier = Modifier.fillMaxSize()) {
                PageImage(
                    photo = photo,
                    photoDesc = photoDesc,
                    imageLoader = imageLoader,
                    contentScale = ContentScale.Inside,
                    modifier = Modifier
                        .onSizeChanged { size = it }
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        )
                        .transformable(state = state)
                        .align(Alignment.Center)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = {
                                    showTopBar = !showTopBar
                                },
                                onDoubleTap = {
                                    coroutineScope.launch {
                                        if (scale == 1f) // Zoom in only if the image is zoomed out
                                            state.animateZoomBy(4f)
                                        else
                                            state.animateZoomBy(0.25f)
                                    }
                                }
                            )
                        }
                )
            }
    }
}