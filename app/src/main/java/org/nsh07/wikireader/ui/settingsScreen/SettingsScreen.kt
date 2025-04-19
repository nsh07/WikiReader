package org.nsh07.wikireader.ui.settingsScreen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
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
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val appInfoIntent =
        Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
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
    val fontStyleMap: Map<String, String> = mapOf(
        "sans" to "Sans-serif",
        "serif" to "Serif"
    )
    val reverseFontStyleMap: Map<String, String> = mapOf(
        "Sans-serif" to "sans",
        "Serif" to "serif"
    )
    val fontStyles = listOf("Sans-serif", "Serif")

    val theme = preferencesState.theme
    val fontStyle = preferencesState.fontStyle
    val color = preferencesState.colorScheme.toColor()

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    var blackTheme by remember { mutableStateOf(preferencesState.blackTheme) }
    var expandedSections by remember { mutableStateOf(preferencesState.expandedSections) }
    var immersiveMode by remember { mutableStateOf(preferencesState.immersiveMode) }
    var dataSaver by remember { mutableStateOf(preferencesState.dataSaver) }
    var renderMath by remember { mutableStateOf(preferencesState.renderMath) }
    var searchHistory by remember { mutableStateOf(preferencesState.searchHistory) }
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
                    homeScreenState.status != WRStatus.FEED_LOADED
                )
                    viewModel.reloadPage()
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
                            painterResource(R.drawable.palette),
                            tint = colorScheme.primary,
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
                            painterResource(R.drawable.translate),
                            contentDescription = null
                        )
                    },
                    headlineContent = { Text("Wikipedia language") },
                    supportingContent = { Text(langCodeToName(preferencesState.lang)) },
                    modifier = Modifier
                        .clickable(onClick = { setShowLanguageSheet(true) })
                )
                ListItem(
                    leadingContent = {
                        Icon(
                            painterResource(R.drawable.serif),
                            contentDescription = null
                        )
                    },
                    headlineContent = { Text("Font style") },
                    supportingContent = {
                        SingleChoiceSegmentedButtonRow {
                            fontStyles.forEachIndexed { index, label ->
                                SegmentedButton(
                                    shape = SegmentedButtonDefaults.itemShape(
                                        index = index,
                                        count = fontStyles.size
                                    ),
                                    onClick = {
                                        viewModel.saveFontStyle(
                                            reverseFontStyleMap[label] ?: "sans"
                                        )
                                    },
                                    selected = label == fontStyleMap[fontStyle],
                                    label = { Text(label) },
                                    modifier = if (weight != 0f) Modifier.width(160.dp) else Modifier.width(
                                        512.dp
                                    )
                                )
                            }
                        }
                    }
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
                    supportingContent = { Text("Use a pure black dark theme") },
                    trailingContent = {
                        Switch(
                            checked = blackTheme,
                            onCheckedChange = {
                                blackTheme = it
                                viewModel.saveBlackTheme(it)
                            },
                            thumbContent = {
                                if (blackTheme) {
                                    Icon(
                                        imageVector = Icons.Outlined.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize),
                                    )
                                }
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
                    supportingContent = { Text("Disable images and feed. Page images can still be opened by clicking the description card.") },
                    trailingContent = {
                        Switch(
                            checked = dataSaver,
                            onCheckedChange = {
                                dataSaver = it
                                viewModel.saveDataSaver(it)
                            },
                            thumbContent = {
                                if (dataSaver) {
                                    Icon(
                                        imageVector = Icons.Outlined.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize),
                                    )
                                }
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
                            },
                            thumbContent = {
                                if (expandedSections) {
                                    Icon(
                                        imageVector = Icons.Outlined.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize),
                                    )
                                }
                            }
                        )
                    }
                )
                ListItem(
                    leadingContent = {
                        Icon(
                            painterResource(R.drawable.open_in_full),
                            contentDescription = null
                        )
                    },
                    headlineContent = { Text("Immersive mode") },
                    supportingContent = { Text("Hide search bar and floating action buttons while scrolling. Enabled by default on small screen sizes.") },
                    trailingContent = {
                        Switch(
                            checked = immersiveMode,
                            onCheckedChange = {
                                immersiveMode = it
                                viewModel.saveImmersiveMode(it)
                            },
                            thumbContent = {
                                if (immersiveMode) {
                                    Icon(
                                        imageVector = Icons.Outlined.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize),
                                    )
                                }
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
                        Text("Requires small amounts of additional data. Turn off to improve performance at the cost of readability.")
                    },
                    trailingContent = {
                        Switch(
                            checked = renderMath,
                            onCheckedChange = {
                                renderMath = it
                                viewModel.saveRenderMath(it)
                            },
                            thumbContent = {
                                if (renderMath) {
                                    Icon(
                                        imageVector = Icons.Outlined.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize),
                                    )
                                }
                            }
                        )
                    }
                )
                ListItem(
                    leadingContent = {
                        Icon(
                            painterResource(R.drawable.history),
                            contentDescription = null
                        )
                    },
                    headlineContent = { Text("Search history") },
                    supportingContent = {
                        Text("Save search history. Existing history is unaffected by this option.")
                    },
                    trailingContent = {
                        Switch(
                            checked = searchHistory,
                            onCheckedChange = {
                                searchHistory = it
                                viewModel.saveSearchHistory(it)
                            },
                            thumbContent = {
                                if (searchHistory) {
                                    Icon(
                                        imageVector = Icons.Outlined.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize),
                                    )
                                }
                            }
                        )
                    }
                )

                OutlinedCard(
                    modifier = Modifier.padding(16.dp),
                    shape = shapes.extraLarge
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Info,
                            tint = colorScheme.secondary,
                            contentDescription = "Information",
                            modifier = Modifier.size(24.dp)
                        )
                        Text("Set as default", style = typography.headlineSmall)
                        Text(
                            text = "You can set WikiReader as your default app for opening Wikipedia links. Click on the buttons below to know more or open settings.",
                            style = typography.bodyLarge,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.align(Alignment.End),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(onClick = { context.startActivity(appInfoIntent) }) {
                                Text("Settings")
                            }
                            FilledTonalButton(
                                onClick = {
                                    uriHandler.openUri("https://gist.github.com/nsh07/ed7571f3e2014b412037626a39d68ecd")
                                }
                            ) {
                                Text("Instructions")
                            }
                        }
                    }
                }

                Spacer(Modifier.height(insets.calculateBottomPadding()))
            }
            if (weight != 0f) Spacer(Modifier.weight(weight))
        }
    }
}