package org.nsh07.wikireader.ui.settingsScreen

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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsTopBar(
    scrollBehavior: TopAppBarScrollBehavior,
    onBack: () -> Unit,
    onResetSettings: () -> Unit
) {
    LargeFlexibleTopAppBar(
        title = { Text(stringResource(R.string.settings)) },
        navigationIcon = {
            FilledTonalIconButton(shapes = IconButtonDefaults.shapes(), onClick = onBack) {
                Icon(
                    painterResource(R.drawable.arrow_back),
                    contentDescription = stringResource(R.string.back)
                )
            }
        },
        actions = {
            IconButton(shapes = IconButtonDefaults.shapes(), onClick = onResetSettings) {
                Icon(
                    painterResource(R.drawable.reset_settings),
                    contentDescription = stringResource(R.string.resetSettingsIconDesc)
                )
            }
        },
        colors = topBarColors,
        scrollBehavior = scrollBehavior
    )
}