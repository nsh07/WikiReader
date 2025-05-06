package org.nsh07.wikireader.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.nsh07.wikireader.R.drawable
import org.nsh07.wikireader.R.string
import kotlin.reflect.KClass

@Composable
fun AppNavigationDrawer(
    drawerState: DrawerState,
    hasRoute: (KClass<out Any>) -> Boolean,
    onAboutClick: () -> Unit,
    onSavedArticlesClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    ModalDrawerSheet(modifier = modifier, drawerState = drawerState) {
        Text(
            "App",
            style = typography.titleSmall,
            modifier = Modifier
                .padding(NavigationDrawerItemDefaults.ItemPadding)
                .padding(16.dp)
        )
        NavigationDrawerItem(
            label = { Text("Home", style = typography.labelLarge) },
            icon = {
                Icon(
                    Icons.Outlined.Home,
                    contentDescription = null
                )
            },
            selected = hasRoute(Home::class),
            onClick = {
                coroutineScope.launch {
                    drawerState.close()
                }
            },
            modifier = Modifier
                .padding(NavigationDrawerItemDefaults.ItemPadding)
        )
        NavigationDrawerItem(
            label = {
                Text(
                    stringResource(string.savedArticles),
                    style = typography.labelLarge
                )
            },
            icon = {
                Icon(
                    painterResource(drawable.download_done),
                    contentDescription = null
                )
            },
            selected = hasRoute(SavedArticles::class),
            onClick = {
                coroutineScope.launch {
                    onSavedArticlesClick()
                    drawerState.close()
                }
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
        NavigationDrawerItem(
            label = {
                Text(
                    stringResource(string.settings),
                    style = typography.labelLarge
                )
            },
            icon = {
                Icon(
                    Icons.Outlined.Settings,
                    contentDescription = null
                )
            },
            selected = hasRoute(Settings::class),
            onClick = {
                coroutineScope.launch {
                    onSettingsClick()
                    drawerState.close()
                }
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
        NavigationDrawerItem(
            label = {
                Text(
                    stringResource(string.about),
                    style = typography.labelLarge
                )
            },
            icon = {
                Icon(
                    Icons.Outlined.Info,
                    contentDescription = null
                )
            },
            selected = hasRoute(About::class),
            onClick = {
                coroutineScope.launch {
                    onAboutClick()
                    drawerState.close()
                }
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
    }
}