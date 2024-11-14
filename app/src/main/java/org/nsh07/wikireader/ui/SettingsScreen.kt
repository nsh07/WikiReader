package org.nsh07.wikireader.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import org.nsh07.wikireader.R
import org.nsh07.wikireader.data.toColor
import org.nsh07.wikireader.ui.dialogs.ColorSchemePickerDialog
import org.nsh07.wikireader.ui.dialogs.ThemeDialog
import org.nsh07.wikireader.ui.scaffoldComponents.SettingsTopBar
import org.nsh07.wikireader.ui.viewModel.PreferencesState
import org.nsh07.wikireader.ui.viewModel.UiViewModel
import kotlin.math.round

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    preferencesState: PreferencesState,
    viewModel: UiViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeMap: Map<String, Pair<Int, String>> = mapOf(
        "auto" to Pair(R.drawable.brightness_auto, "System default"),
        "light" to Pair(R.drawable.light_mode, "Light"),
        "dark" to Pair(R.drawable.dark_mode, "Dark")
    )
    val reverseThemeMap: Map<String, String> = mapOf(
        "System default" to "auto",
        "Light" to "light",
        "Dark" to "dark"
    )

    val theme = preferencesState.theme
    val color = preferencesState.colorScheme.toColor()

    var blackTheme by remember { mutableStateOf(preferencesState.blackTheme) }
    var expandedSections by remember { mutableStateOf(preferencesState.expandedSections) }
    var dataSaver by remember { mutableStateOf(preferencesState.dataSaver) }

    val expandedIcon =
        if (expandedSections) R.drawable.expand_all
        else R.drawable.collapse_all
    val dataSaverIcon =
        if (dataSaver) R.drawable.data_saver_on
        else R.drawable.data_saver_off

    val (showThemeDialog, setShowThemeDialog) = remember { mutableStateOf(false) }
    val (showColorSchemeDialog, setShowColorSchemeDialog) = remember { mutableStateOf(false) }
    var fontSizeFloat by remember { mutableFloatStateOf(preferencesState.fontSize.toFloat()) }

    if (showThemeDialog)
        ThemeDialog(
            themeMap = themeMap,
            reverseThemeMap = reverseThemeMap,
            theme = theme,
            setShowThemeDialog = setShowThemeDialog,
            setTheme = { viewModel.saveTheme(it) }
        )
    if (showColorSchemeDialog)
        ColorSchemePickerDialog(
            currentColor = color,
            onColorChange = { viewModel.saveColorScheme(it.toString()) },
            setShowDialog = setShowColorSchemeDialog
        )

    Scaffold(
        topBar = { SettingsTopBar(onBack = onBack) },
        modifier = modifier.fillMaxSize()
    ) { insets ->
        Column(modifier = Modifier.padding(top = insets.calculateTopPadding())) {
            ListItem(
                leadingContent = {
                    Icon(
                        painterResource(themeMap[theme]!!.first),
                        contentDescription = null
                    )
                },
                headlineContent = { Text("Theme") },
                supportingContent = { Text(themeMap[theme]!!.second) },
                modifier = Modifier
                    .clickable(onClick = { setShowThemeDialog(true) })
            )
            ListItem(
                leadingContent = {
                    Icon(
                        painterResource(R.drawable.palette),
                        contentDescription = null
                    )
                },
                headlineContent = { Text("Color scheme") },
                supportingContent = {
                    if (color == Color.White) Text("Dynamic")
                    else Text("Color")
                },
                modifier = Modifier
                    .clickable(onClick = { setShowColorSchemeDialog(true) })
            )
            ListItem(
                leadingContent = {
                    Icon(
                        painterResource(R.drawable.format_size),
                        contentDescription = null
                    )
                },
                headlineContent = { Text("Font size") },
                supportingContent = {
                    Column {
                        Text("${round(fontSizeFloat).toInt()}pt")
                        Slider(
                            value = fontSizeFloat,
                            onValueChange = { fontSizeFloat = it },
                            valueRange = 10f..22f,
                            steps = 5,
                            onValueChangeFinished = {
                                viewModel.saveFontSize(round(fontSizeFloat).toInt())
                            }
                        )
                    }
                }
            )
            ListItem(
                leadingContent = {
                    Icon(
                        painterResource(R.drawable.contrast),
                        contentDescription = null
                    )
                },
                headlineContent = { Text("Black theme") },
                supportingContent = { Text("Use a pure-black dark theme") },
                trailingContent = {
                    Switch(
                        checked = blackTheme,
                        onCheckedChange = {
                            blackTheme = it
                            viewModel.saveBlackTheme(it)
                        }
                    )
                }
            )
            ListItem(
                leadingContent = {
                    Icon(
                        painterResource(expandedIcon),
                        contentDescription = null
                    )
                },
                headlineContent = { Text("Expand sections") },
                supportingContent = { Text("Expand all sections by default") },
                trailingContent = {
                    Switch(
                        checked = expandedSections,
                        onCheckedChange = {
                            expandedSections = it
                            viewModel.saveExpandedSections(it)
                        }
                    )
                }
            )
            ListItem(
                leadingContent = {
                    Icon(
                        painterResource(dataSaverIcon),
                        contentDescription = null
                    )
                },
                headlineContent = { Text("Data saver") },
                supportingContent = { Text("Only load page image in fullscreen view") },
                trailingContent = {
                    Switch(
                        checked = dataSaver,
                        onCheckedChange = {
                            dataSaver = it
                            viewModel.saveDataSaver(it)
                        }
                    )
                }
            )
        }
    }
}