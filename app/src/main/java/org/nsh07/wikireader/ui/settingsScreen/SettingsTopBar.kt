package org.nsh07.wikireader.ui.settingsScreen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.nsh07.wikireader.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTopBar(scrollBehavior: TopAppBarScrollBehavior, onBack: () -> Unit) {
    LargeTopAppBar(
        title = { Text("Settings") },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = stringResource(R.string.back)
                )
            }
        },
        scrollBehavior = scrollBehavior
    )
}