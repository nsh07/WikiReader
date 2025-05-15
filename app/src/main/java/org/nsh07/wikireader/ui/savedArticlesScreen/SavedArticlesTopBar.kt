package org.nsh07.wikireader.ui.savedArticlesScreen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.nsh07.wikireader.R

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
fun SavedArticlesTopBar(
    articlesInfo: String,
    scrollBehavior: TopAppBarScrollBehavior,
    onBack: () -> Unit,
    onDeleteAll: () -> Unit
) {
    LargeFlexibleTopAppBar(
        title = { Text(stringResource(R.string.savedArticles)) },
        subtitle = { Text(articlesInfo) },
        navigationIcon = {
            IconButton(shapes = IconButtonDefaults.shapes(), onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = stringResource(R.string.back)
                )
            }
        },
        actions = {
            IconButton(shapes = IconButtonDefaults.shapes(), onClick = onDeleteAll) {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = stringResource(R.string.deleteAll)
                )
            }
        },
        scrollBehavior = scrollBehavior
    )
}