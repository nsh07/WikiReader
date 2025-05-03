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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import org.nsh07.wikireader.R
import org.nsh07.wikireader.R.string
import org.nsh07.wikireader.data.WRStatus
import org.nsh07.wikireader.data.langCodeToName
import org.nsh07.wikireader.data.toColor
import org.nsh07.wikireader.ui.viewModel.HomeScreenState
import org.nsh07.wikireader.ui.viewModel.PreferencesState
import kotlin.math.round

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    preferencesState: PreferencesState,
    homeScreenState: HomeScreenState,
    languageSearchStr: String,
    languageSearchQuery: String,
    windowSizeClass: WindowSizeClass,
    themeMap: Map<String, Pair<Int, String>>,
    reverseThemeMap: Map<String, String>,
    fontStyles: List<String>,
    fontStyleMap: Map<String, String>,
    reverseFontStyleMap: Map<String, String>,
    saveTheme: (String) -> Unit,
    saveColorScheme: (String) -> Unit,
    saveLang: (String) -> Unit,
    saveFontStyle: (String) -> Unit,
    saveFontSize: (Int) -> Unit,
    saveBlackTheme: (Boolean) -> Unit,
    saveDataSaver: (Boolean) -> Unit,
    saveExpandedSections: (Boolean) -> Unit,
    saveImmersiveMode: (Boolean) -> Unit,
    saveRenderMath: (Boolean) -> Unit,
    saveSearchHistory: (Boolean) -> Unit,
    updateLanguageSearchStr: (String) -> Unit,
    loadFeed: () -> Unit,
    reloadPage: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current

    val appInfoIntent = remember {
        Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
    }

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
            setTheme = saveTheme
        )
    if (showColorSchemeDialog)
        ColorSchemePickerDialog(
            currentColor = color,
            onColorChange = { saveColorScheme(it.toString()) },
            setShowDialog = setShowColorSchemeDialog
        )
    if (showLanguageSheet)
        LanguageBottomSheet(
            lang = preferencesState.lang,
            searchStr = languageSearchStr,
            searchQuery = languageSearchQuery,
            setShowSheet = setShowLanguageSheet,
            setLang = {
                saveLang(it)
                if (homeScreenState.status != WRStatus.FEED_NETWORK_ERROR &&
                    homeScreenState.status != WRStatus.FEED_LOADED
                )
                    reloadPage()
                else
                    loadFeed()
            },
            setSearchStr = updateLanguageSearchStr
        )

    Scaffold(
        topBar = { SettingsTopBar(scrollBehavior = scrollBehavior, onBack = onBack) },
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { insets ->
        Row {
            if (weight != 0f) Spacer(Modifier.weight(weight))
            LazyColumn(
                contentPadding = insets,
                modifier = Modifier.weight(4f)
            ) {
                item {
                    ListItem(
                        leadingContent = {
                            Icon(
                                painterResource(R.drawable.palette),
                                tint = colorScheme.primary,
                                contentDescription = null
                            )
                        },
                        headlineContent = { Text(stringResource(string.settingColorScheme)) },
                        supportingContent = {
                            if (color == Color.White) Text(stringResource(string.colorSchemeDynamic))
                            else Text(stringResource(string.colorSchemeColor))
                        },
                        modifier = Modifier
                            .clickable(onClick = { setShowColorSchemeDialog(true) })
                    )
                }
                item {
                    ListItem(
                        leadingContent = {
                            Icon(
                                painterResource(themeMap[theme]!!.first),
                                contentDescription = null
                            )
                        },
                        headlineContent = { Text(stringResource(string.settingTheme)) },
                        supportingContent = { Text(themeMap[theme]!!.second) },
                        modifier = Modifier
                            .clickable(onClick = { setShowThemeDialog(true) })
                    )
                }
                item {
                    ListItem(
                        leadingContent = {
                            Icon(
                                painterResource(R.drawable.translate),
                                contentDescription = null
                            )
                        },
                        headlineContent = { Text(stringResource(string.settingWikipediaLanguage)) },
                        supportingContent = { Text(langCodeToName(preferencesState.lang)) },
                        modifier = Modifier
                            .clickable(onClick = { setShowLanguageSheet(true) })
                    )
                }
                item {
                    ListItem(
                        leadingContent = {
                            Icon(
                                painterResource(R.drawable.serif),
                                contentDescription = null
                            )
                        },
                        headlineContent = { Text(stringResource(string.settingFontStyle)) },
                        supportingContent = {
                            SingleChoiceSegmentedButtonRow {
                                fontStyles.forEachIndexed { index, label ->
                                    SegmentedButton(
                                        shape = SegmentedButtonDefaults.itemShape(
                                            index = index,
                                            count = fontStyles.size
                                        ),
                                        onClick = {
                                            saveFontStyle(
                                                reverseFontStyleMap[label] ?: "sans"
                                            )
                                        },
                                        selected = label == fontStyleMap[fontStyle],
                                        label = { Text(label) },
                                        modifier = if (weight != 0f) Modifier.width(160.dp)
                                        else Modifier.width(512.dp)
                                    )
                                }
                            }
                        }
                    )
                }
                item {
                    ListItem(
                        leadingContent = {
                            Icon(
                                painterResource(R.drawable.format_size),
                                contentDescription = null
                            )
                        },
                        headlineContent = { Text(stringResource(string.settingFontSize)) },
                        supportingContent = {
                            Column {
                                Text(round(fontSizeFloat).toInt().toString())
                                Slider(
                                    value = fontSizeFloat,
                                    onValueChange = { fontSizeFloat = it },
                                    valueRange = 10f..22f,
                                    steps = 5,
                                    onValueChangeFinished = {
                                        saveFontSize(round(fontSizeFloat).toInt())
                                    }
                                )
                            }
                        }
                    )
                }
                item {
                    ListItem(
                        leadingContent = {
                            Icon(
                                painterResource(R.drawable.contrast),
                                contentDescription = null
                            )
                        },
                        headlineContent = { Text(stringResource(string.settingBlackTheme)) },
                        supportingContent = { Text(stringResource(string.settingBlackThemeDesc)) },
                        trailingContent = {
                            Switch(
                                checked = blackTheme,
                                onCheckedChange = {
                                    blackTheme = it
                                    saveBlackTheme(it)
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
                }
                item {
                    ListItem(
                        leadingContent = {
                            Icon(
                                painterResource(R.drawable.data_saver_on),
                                contentDescription = null
                            )
                        },
                        headlineContent = { Text(stringResource(string.settingDataSaver)) },
                        supportingContent = { Text(stringResource(string.settingDataSaverDesc)) },
                        trailingContent = {
                            Switch(
                                checked = dataSaver,
                                onCheckedChange = {
                                    dataSaver = it
                                    saveDataSaver(it)
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
                }
                item {
                    ListItem(
                        leadingContent = {
                            Icon(
                                painterResource(R.drawable.expand_all),
                                contentDescription = null
                            )
                        },
                        headlineContent = { Text(stringResource(string.settingExpandSections)) },
                        supportingContent = { Text(stringResource(string.settingExpandSectionsDesc)) },
                        trailingContent = {
                            Switch(
                                checked = expandedSections,
                                onCheckedChange = {
                                    expandedSections = it
                                    saveExpandedSections(it)
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
                }
                item {
                    ListItem(
                        leadingContent = {
                            Icon(
                                painterResource(R.drawable.open_in_full),
                                contentDescription = null
                            )
                        },
                        headlineContent = { Text(stringResource(string.settingImmersiveMode)) },
                        supportingContent = { Text(stringResource(string.settingImmersiveModeDesc)) },
                        trailingContent = {
                            Switch(
                                checked = immersiveMode,
                                onCheckedChange = {
                                    immersiveMode = it
                                    saveImmersiveMode(it)
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
                }
                item {
                    ListItem(
                        leadingContent = {
                            Icon(
                                painterResource(R.drawable.function),
                                contentDescription = null
                            )
                        },
                        headlineContent = { Text(stringResource(string.settingRenderMath)) },
                        supportingContent = {
                            Text(stringResource(string.settingRenderMathDesc))
                        },
                        trailingContent = {
                            Switch(
                                checked = renderMath,
                                onCheckedChange = {
                                    renderMath = it
                                    saveRenderMath(it)
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
                }
                item {
                    ListItem(
                        leadingContent = {
                            Icon(
                                painterResource(R.drawable.history),
                                contentDescription = null
                            )
                        },
                        headlineContent = { Text(stringResource(string.settingSearchHistory)) },
                        supportingContent = {
                            Text(stringResource(string.settingSearchHistoryDesc))
                        },
                        trailingContent = {
                            Switch(
                                checked = searchHistory,
                                onCheckedChange = {
                                    searchHistory = it
                                    saveSearchHistory(it)
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
                }

                item {
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
                                contentDescription = stringResource(string.information),
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                stringResource(string.setAsDefault),
                                style = typography.headlineSmall
                            )
                            Text(
                                text = stringResource(string.setAsDefaultDesc),
                                style = typography.bodyLarge,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(
                                modifier = Modifier.align(Alignment.End),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(onClick = { context.startActivity(appInfoIntent) }) {
                                    Text(stringResource(string.settings))
                                }
                                FilledTonalButton(
                                    onClick = {
                                        uriHandler.openUri("https://gist.github.com/nsh07/ed7571f3e2014b412037626a39d68ecd")
                                    }
                                ) {
                                    Text(stringResource(string.instructions))
                                }
                            }
                        }
                    }
                }
            }
            if (weight != 0f) Spacer(Modifier.weight(weight))
        }
    }
}