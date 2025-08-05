package org.nsh07.wikireader.ui.savedArticlesScreen

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import org.nsh07.wikireader.R
import org.nsh07.wikireader.ui.theme.CustomTopBarColors.topBarColors

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
fun SavedArticlesTopBar(
    articlesInfo: String,
    scrollBehavior: TopAppBarScrollBehavior,
    deleteEnabled: Boolean,
    onBack: () -> Unit,
    onDeleteAll: () -> Unit
) {
    LargeFlexibleTopAppBar(
        title = { Text(stringResource(R.string.savedArticles)) },
        subtitle = { Text(articlesInfo) },
        navigationIcon = {
            IconButton(
                shapes = IconButtonDefaults.shapes(),
                onClick = onBack
            ) {
                Icon(
                    painterResource(R.drawable.arrow_back),
                    contentDescription = stringResource(R.string.back)
                )
            }
        },
        actions = {
            FilledTonalIconButton(
                enabled = deleteEnabled,
                shapes = IconButtonDefaults.shapes(),
                onClick = onDeleteAll
            ) {
                Icon(
                    painterResource(R.drawable.delete),
                    contentDescription = stringResource(R.string.deleteAll)
                )
            }
        },
        colors = topBarColors,
        scrollBehavior = scrollBehavior
    )
}