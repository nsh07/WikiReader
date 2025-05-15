package org.nsh07.wikireader.ui.image

import android.content.Intent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import org.nsh07.wikireader.R
import org.nsh07.wikireader.data.WikiPhotoDesc
import org.nsh07.wikireader.parser.toWikitextAnnotatedString

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FullScreenImageTopBar(
    photoDesc: WikiPhotoDesc?,
    title: String,
    link: String? = null,
    onBack: () -> Unit,
) {
    val sendIntent: Intent = Intent()
        .apply {
            action = Intent.ACTION_SEND
            putExtra(
                Intent.EXTRA_TEXT,
                link
            )
            type = "text/plain"
        }
    val shareIntent = Intent.createChooser(sendIntent, null)
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    TopAppBar(
        title = {
            Text(
                photoDesc?.label?.get(0) ?: title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(shapes = IconButtonDefaults.shapes(), onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = stringResource(R.string.back)
                )
            }
        },
        actions = {
            if (link != null) {
                IconButton(
                    shapes = IconButtonDefaults.shapes(),
                    onClick = { context.startActivity(shareIntent) }) {
                    Icon(
                        painterResource(R.drawable.share),
                        tint = Color.White,
                        contentDescription = stringResource(R.string.shareLink)
                    )
                }
                IconButton(
                    shapes = IconButtonDefaults.shapes(),
                    onClick = { uriHandler.openUri(link) }) {
                    Icon(
                        painterResource(R.drawable.open_link),
                        tint = Color.White,
                        contentDescription = stringResource(R.string.openInBrowser)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0f, 0f, 0f, 0.5f),
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FullScreenImageTopBar(
    description: String,
    link: String? = null,
    onBack: () -> Unit,
) {
    val sendIntent: Intent = Intent()
        .apply {
            action = Intent.ACTION_SEND
            putExtra(
                Intent.EXTRA_TEXT,
                link
            )
            type = "text/plain"
        }
    val shareIntent = Intent.createChooser(sendIntent, null)
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val colorScheme = colorScheme
    val typography = typography

    TopAppBar(
        title = {
            Text(
                remember(description) {
                    description.toWikitextAnnotatedString(
                        colorScheme = colorScheme,
                        typography = typography,
                        loadPage = {},
                        fontSize = 1
                    ).toString()
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(shapes = IconButtonDefaults.shapes(), onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = stringResource(R.string.back)
                )
            }
        },
        actions = {
            if (link != null) {
                IconButton(
                    shapes = IconButtonDefaults.shapes(),
                    onClick = { context.startActivity(shareIntent) }) {
                    Icon(
                        painterResource(R.drawable.share),
                        tint = Color.White,
                        contentDescription = stringResource(R.string.shareLink)
                    )
                }
                IconButton(
                    shapes = IconButtonDefaults.shapes(),
                    onClick = { uriHandler.openUri(link) }) {
                    Icon(
                        painterResource(R.drawable.open_link),
                        tint = Color.White,
                        contentDescription = stringResource(R.string.openInBrowser)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0f, 0f, 0f, 0.5f),
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White
        )
    )
}