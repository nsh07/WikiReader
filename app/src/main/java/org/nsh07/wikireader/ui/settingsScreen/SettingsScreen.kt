package org.nsh07.wikireader.ui.settingsScreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import org.nsh07.wikireader.R
import org.nsh07.wikireader.data.WRStatus
import org.nsh07.wikireader.data.langCodeToName
import org.nsh07.wikireader.data.toColor
import org.nsh07.wikireader.ui.viewModel.HomeScreenState
import org.nsh07.wikireader.ui.viewModel.PreferencesState
import org.nsh07.wikireader.ui.viewModel.UiViewModel
import kotlin.math.round

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    preferencesState: PreferencesState,
    homeScreenState: HomeScreenState,
    viewModel: UiViewModel,
    onBack: () -> Unit,
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier
) {
    val languageSearchStr = viewModel.languageSearchStr.collectAsState()
    val languageSearchQuery = viewModel.languageSearchQuery.collectAsState("")

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

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    var blackTheme by remember { mutableStateOf(preferencesState.blackTheme) }
    var expandedSections by remember { mutableStateOf(preferencesState.expandedSections) }
    var dataSaver by remember { mutableStateOf(preferencesState.dataSaver) }
    var renderMath by remember { mutableStateOf(preferencesState.renderMath) }
    val weight = remember {
        if (windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.MEDIUM ||
            windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED
        )
            1f
        else 0f
    }

    val expandedIcon =
        if (expandedSections) R.drawable.expand_all
        else R.drawable.collapse_all
    val dataSaverIcon =
        if (dataSaver) R.drawable.data_saver_on
        else R.drawable.data_saver_off

    val (showThemeDialog, setShowThemeDialog) = remember { mutableStateOf(false) }
    val (showColorSchemeDialog, setShowColorSchemeDialog) = remember { mutableStateOf(false) }
    val (showLanguageSheet, setShowLanguageSheet) = remember { mutableStateOf(false) }
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
    if (showLanguageSheet)
        LanguageBottomSheet(
            lang = preferencesState.lang,
            searchStr = languageSearchStr.value,
            searchQuery = languageSearchQuery.value,
            setShowSheet = setShowLanguageSheet,
            setLang = {
                viewModel.saveLang(it)
                if (homeScreenState.status != WRStatus.FEED_NETWORK_ERROR &&
                    homeScreenState.status != WRStatus.FEED_LOADED)
                    viewModel.refreshSearch()
                else
                    viewModel.loadFeed()
            },
            setSearchStr = { viewModel.updateLanguageSearchStr(it) }
        )

    Scaffold(
        topBar = { SettingsTopBar(scrollBehavior = scrollBehavior, onBack = onBack) },
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { insets ->
        Row {
            if (weight != 0f) Spacer(Modifier.weight(weight))
            Column(
                modifier = Modifier
                    .padding(top = insets.calculateTopPadding())
                    .weight(4f)
                    .verticalScroll(
                        rememberScrollState()
                    )
            ) {
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
                            tint = MaterialTheme.colorScheme.primary,
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
                            painterResource(R.drawable.translate),
                            contentDescription = null
                        )
                    },
                    headlineContent = { Text("Wikipedia Language") },
                    supportingContent = { Text(langCodeToName(preferencesState.lang)) },
                    modifier = Modifier
                        .clickable(onClick = { setShowLanguageSheet(true) })
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
                    supportingContent = { Text("Only load page image in fullscreen and disable feed") },
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
                ListItem(
                    leadingContent = {
                        Icon(
                            painterResource(R.drawable.function),
                            contentDescription = null
                        )
                    },
                    headlineContent = { Text("Render math expressions") },
                    supportingContent = {
                        Text("Requires small amounts of additional data. Turn off to improve performance at the cost of readability")
                    },
                    trailingContent = {
                        Switch(
                            checked = renderMath,
                            onCheckedChange = {
                                renderMath = it
                                viewModel.saveRenderMath(it)
                            }
                        )
                    }
                )

                Spacer(Modifier.height(insets.calculateBottomPadding()))
            }
            if (weight != 0f) Spacer(Modifier.weight(weight))
        }
    }
}