package org.nsh07.wikireader.ui.scaffoldComponents

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import org.nsh07.wikireader.R
import org.nsh07.wikireader.data.WikiPhotoDesc

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScreenImageTopBar(
    photoDesc: WikiPhotoDesc?,
    onBack: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                photoDesc?.label?.get(0) ?: "",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = stringResource(R.string.back)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0f, 0f, 0f, 0.5f),
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White
        )
    )
}