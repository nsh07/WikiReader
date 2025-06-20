package org.nsh07.wikireader.ui.settingsScreen

import android.app.LocaleManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.nsh07.wikireader.R
import org.nsh07.wikireader.R.string
import org.nsh07.wikireader.data.WRStatus
import org.nsh07.wikireader.data.langCodeToName
import org.nsh07.wikireader.data.toColor
import org.nsh07.wikireader.ui.theme.CustomTopBarColors.topBarColors
import org.nsh07.wikireader.ui.theme.WRShapeDefaults.bottomListItemShape
import org.nsh07.wikireader.ui.theme.WRShapeDefaults.cardShape
import org.nsh07.wikireader.ui.theme.WRShapeDefaults.middleListItemShape
import org.nsh07.wikireader.ui.theme.WRShapeDefaults.topListItemShape
import org.nsh07.wikireader.ui.theme.WikiReaderTheme
import org.nsh07.wikireader.ui.viewModel.HomeScreenState
import org.nsh07.wikireader.ui.viewModel.PreferencesState
import kotlin.math.round

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen(
    preferencesState: PreferencesState,
    homeScreenState: HomeScreenState,
    recentLangs: List<String>,
    languageSearchStr: String,
    languageSearchQuery: String,
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
    saveFeedEnabled: (Boolean) -> Unit,
    saveExpandedSections: (Boolean) -> Unit,
    saveImageBackground: (Boolean) -> Unit,
    saveImmersiveMode: (Boolean) -> Unit,
    saveRenderMath: (Boolean) -> Unit,
    saveSearchHistory: (Boolean) -> Unit,
    updateLanguageSearchStr: (String) -> Unit,
    loadFeed: () -> Unit,
    reloadPage: () -> Unit,
    onBack: () -> Unit,
    onResetSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val appInfoIntent = remember {
        Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
    }

    val currentLocales =
        if (Build.VERSION.SDK_INT >= 33) {
            context
                .getSystemService(LocaleManager::class.java)
                .applicationLocales
        } else null
    val currentLocalesSize = currentLocales?.size() ?: 0

    val theme = preferencesState.theme
    val fontStyle = preferencesState.fontStyle
    val color = preferencesState.colorScheme.toColor()

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val snackBarHostState = remember { SnackbarHostState() }

    val (showThemeDialog, setShowThemeDialog) = remember { mutableStateOf(false) }
    val (showResetSettingsDialog, setShowResetSettingsDialog) = remember { mutableStateOf(false) }
    val (showColorSchemeDialog, setShowColorSchemeDialog) = remember { mutableStateOf(false) }
    val (showLanguageSheet, setShowLanguageSheet) = remember { mutableStateOf(false) }
    val (showAppLocaleSheet, setShowAppLocaleSheet) = remember { mutableStateOf(false) }

    var animateFontSize by remember { mutableStateOf(true) }
    var fontSizeFloat by remember(preferencesState.fontSize) { mutableFloatStateOf(preferencesState.fontSize.toFloat()) }
    val fontSizeAnimated by animateFloatAsState(
        fontSizeFloat,
        animationSpec = if (animateFontSize) motionScheme.defaultSpatialSpec()
        else tween(durationMillis = 0)
    )

    val disabledAlpha = 0.5f

    val listItemColors = ListItemDefaults.colors()
    val disabledListColors =
        ListItemDefaults.colors(
            containerColor = colorScheme.surfaceContainerLow,
            headlineColor = listItemColors.headlineColor.copy(disabledAlpha),
            leadingIconColor = listItemColors.leadingIconColor.copy(disabledAlpha),
            overlineColor = listItemColors.overlineColor.copy(disabledAlpha),
            supportingColor = listItemColors.supportingTextColor.copy(disabledAlpha),
            trailingIconColor = listItemColors.trailingIconColor.copy(disabledAlpha),
        )

    val switchItems = remember(preferencesState) {
        listOf(
            SettingsSwitchItem(
                preferencesState.blackTheme,
                R.drawable.contrast,
                string.settingBlackTheme,
                string.settingBlackThemeDesc,
                saveBlackTheme
            ),
            SettingsSwitchItem(
                preferencesState.dataSaver,
                R.drawable.data_saver_on,
                string.settingDataSaver,
                string.settingDataSaverDesc,
                saveDataSaver
            ),
            SettingsSwitchItem(
                preferencesState.feedEnabled,
                R.drawable.feed,
                string.settingFeed,
                string.settingFeedDesc,
                saveFeedEnabled,
                enabled = !preferencesState.dataSaver
            ),
            SettingsSwitchItem(
                preferencesState.expandedSections,
                R.drawable.expand_all,
                string.settingExpandSections,
                string.settingExpandSectionsDesc,
                saveExpandedSections
            ),
            SettingsSwitchItem(
                preferencesState.imageBackground,
                R.drawable.texture,
                string.settingImageBackground,
                string.settingImageBackgroundDesc,
                saveImageBackground
            ),
            SettingsSwitchItem(
                preferencesState.immersiveMode,
                R.drawable.open_in_full,
                string.settingImmersiveMode,
                string.settingImmersiveModeDesc,
                saveImmersiveMode
            ),
            SettingsSwitchItem(
                preferencesState.renderMath,
                R.drawable.function,
                string.settingRenderMath,
                string.settingRenderMathDesc,
                saveRenderMath
            ),
            SettingsSwitchItem(
                preferencesState.searchHistory,
                R.drawable.search_history,
                string.settingSearchHistory,
                string.settingSearchHistoryDesc,
                saveSearchHistory
            )
        )
    }

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
    if (showResetSettingsDialog)
        ResetSettingsDialog(
            onResetSettings = onResetSettings,
            setShowResetSettingsDialog = setShowResetSettingsDialog,
            showSnackbar = { coroutineScope.launch { snackBarHostState.showSnackbar(it) } }
        )
    if (showLanguageSheet)
        LanguageBottomSheet(
            lang = preferencesState.lang,
            recentLangs = recentLangs,
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
    if (showAppLocaleSheet && currentLocales != null)
        AppLocaleBottomSheet(
            searchStr = languageSearchStr,
            currentLocales = currentLocales,
            setSearchStr = updateLanguageSearchStr,
            setShowSheet = setShowAppLocaleSheet
        )

    Scaffold(
        topBar = {
            SettingsTopBar(
                scrollBehavior = scrollBehavior,
                onBack = onBack,
                onResetSettings = { setShowResetSettingsDialog(true) }
            )
        },
        snackbarHost = { SnackbarHost(snackBarHostState) },
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { insets ->
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            contentPadding = insets,
            modifier = Modifier.background(topBarColors.containerColor)
        ) {
            item { Spacer(Modifier.height(8.dp)) }
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
                        .padding(horizontal = 16.dp)
                        .clip(topListItemShape)
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
                        .padding(horizontal = 16.dp)
                        .clip(middleListItemShape)
                        .clickable(onClick = { setShowThemeDialog(true) })
                )
            }
            item {
                ListItem(
                    leadingContent = {
                        Icon(painterResource(switchItems[0].icon), contentDescription = null)
                    },
                    headlineContent = { Text(stringResource(switchItems[0].label)) },
                    supportingContent = { Text(stringResource(switchItems[0].description)) },
                    trailingContent = {
                        Switch(
                            checked = switchItems[0].checked,
                            onCheckedChange = { switchItems[0].onCheckedChange(it) },
                            thumbContent = {
                                if (switchItems[0].checked) {
                                    Icon(
                                        imageVector = Icons.Outlined.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize),
                                    )
                                }
                            },
                            enabled = switchItems[0].enabled
                        )
                    },
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .clip(bottomListItemShape)
                )
            }
            item { Spacer(Modifier.height(12.dp)) }
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
                        .padding(horizontal = 16.dp)
                        .clip(topListItemShape)
                        .clickable(onClick = { setShowLanguageSheet(true) })
                )
            }
            if (currentLocales != null)
                item {
                    ListItem(
                        leadingContent = {
                            Icon(
                                painterResource(R.drawable.language),
                                contentDescription = null
                            )
                        },
                        headlineContent = { Text(stringResource(string.settingAppLanguage)) },
                        supportingContent = {
                            Text(
                                if (currentLocalesSize > 0) currentLocales.get(0).displayName
                                else stringResource(string.themeSystemDefault)
                            )
                        },
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .clip(middleListItemShape)
                            .clickable(onClick = { setShowAppLocaleSheet(true) })
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
                        Row(
                            horizontalArrangement =
                                Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            fontStyles.forEachIndexed { index, label ->
                                ToggleButton(
                                    checked = label == fontStyleMap[fontStyle],
                                    onCheckedChange = {
                                        saveFontStyle(
                                            reverseFontStyleMap[label] ?: "sans"
                                        )
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(top = 4.dp)
                                        .height(40.dp),
                                    shapes =
                                        when (index) {
                                            0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                            fontStyles.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                            else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                                        }
                                ) {
                                    AnimatedVisibility(
                                        label == fontStyleMap[fontStyle],
                                        enter = scaleIn(motionScheme.fastSpatialSpec()) +
                                                expandHorizontally(motionScheme.fastSpatialSpec()) +
                                                fadeIn(motionScheme.fastEffectsSpec()),
                                        exit = scaleOut(motionScheme.fastSpatialSpec()) +
                                                shrinkHorizontally(motionScheme.fastSpatialSpec()) +
                                                fadeOut(motionScheme.fastEffectsSpec())
                                    ) { Icon(Icons.Outlined.Check, contentDescription = null) }
                                    Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                                    Text(label)
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .clip(middleListItemShape)
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
                                value = fontSizeAnimated,
                                onValueChange = {
                                    animateFontSize = false
                                    fontSizeFloat = it
                                },
                                valueRange = 10f..22f,
                                onValueChangeFinished = {
                                    animateFontSize = true
                                    saveFontSize(round(fontSizeFloat).toInt())
                                    fontSizeFloat = round(fontSizeFloat)
                                }
                            )
                        }
                    },
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .clip(bottomListItemShape)
                )
            }
            item { Spacer(Modifier.height(12.dp)) }
            itemsIndexed(switchItems.drop(1)) { index, item ->
                ListItem(
                    leadingContent = {
                        Icon(painterResource(item.icon), contentDescription = null)
                    },
                    headlineContent = { Text(stringResource(item.label)) },
                    supportingContent = { Text(stringResource(item.description)) },
                    trailingContent = {
                        Switch(
                            checked = item.checked && item.enabled,
                            onCheckedChange = { item.onCheckedChange(it) },
                            thumbContent = {
                                if (item.checked && item.enabled) {
                                    Icon(
                                        imageVector = Icons.Outlined.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize),
                                    )
                                }
                            },
                            enabled = item.enabled
                        )
                    },
                    colors =
                        if (item.enabled)
                            listItemColors
                        else
                            disabledListColors,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .clip(
                            if (index == 0) topListItemShape
                            else if (index == switchItems.lastIndex - 1) bottomListItemShape
                            else middleListItemShape
                        )
                )
            }
            item {
                Card(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    shape = cardShape,
                    colors = CardDefaults.cardColors(containerColor = ListItemDefaults.colors().containerColor)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Spacer(
                                Modifier
                                    .clip(MaterialShapes.Cookie12Sided.toShape())
                                    .background(colorScheme.secondaryContainer)
                                    .padding(8.dp)
                                    .size(24.dp)
                            )
                            Icon(
                                Icons.Outlined.Info,
                                tint = colorScheme.onSecondaryContainer,
                                contentDescription = stringResource(string.information),
                                modifier = Modifier.size(24.dp)
                            )
                        }
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
                            Button(
                                shapes = ButtonDefaults.shapes(),
                                onClick = { context.startActivity(appInfoIntent) }
                            ) {
                                Text(stringResource(string.settings))
                            }
                            FilledTonalButton(
                                shapes = ButtonDefaults.shapes(),
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
    }
}

@Preview
@Composable
fun SettingsPreview() {
    val context = LocalContext.current
    val themeMap: Map<String, Pair<Int, String>> = remember {
        mapOf(
            "auto" to Pair(
                R.drawable.brightness_auto,
                context.getString(string.themeSystemDefault)
            ),
            "light" to Pair(R.drawable.light_mode, context.getString(string.themeLight)),
            "dark" to Pair(R.drawable.dark_mode, context.getString(string.themeDark))
        )
    }
    val reverseThemeMap: Map<String, String> = remember {
        mapOf(
            context.getString(string.themeSystemDefault) to "auto",
            context.getString(string.themeLight) to "light",
            context.getString(string.themeDark) to "dark"
        )
    }
    val fontStyleMap: Map<String, String> = remember {
        mapOf(
            "sans" to context.getString(string.fontStyleSansSerif),
            "serif" to context.getString(string.fontStyleSerif)
        )
    }
    val reverseFontStyleMap: Map<String, String> = remember {
        mapOf(
            context.getString(string.fontStyleSansSerif) to "sans",
            context.getString(string.fontStyleSerif) to "serif"
        )
    }
    val fontStyles = remember {
        listOf(
            context.getString(string.fontStyleSansSerif),
            context.getString(string.fontStyleSerif)
        )
    }
    WikiReaderTheme {
        SettingsScreen(
            preferencesState = PreferencesState(),
            homeScreenState = HomeScreenState(),
            recentLangs = emptyList(),
            languageSearchStr = "",
            languageSearchQuery = "",
            themeMap = themeMap,
            reverseThemeMap = reverseThemeMap,
            fontStyles = fontStyles,
            fontStyleMap = fontStyleMap,
            reverseFontStyleMap = reverseFontStyleMap,
            saveTheme = {},
            saveColorScheme = {},
            saveLang = {},
            saveFontStyle = {},
            saveFontSize = {},
            saveBlackTheme = {},
            saveDataSaver = {},
            saveFeedEnabled = {},
            saveExpandedSections = {},
            saveImageBackground = {},
            saveImmersiveMode = {},
            saveRenderMath = {},
            saveSearchHistory = {},
            updateLanguageSearchStr = {},
            loadFeed = {},
            reloadPage = {},
            onBack = {},
            onResetSettings = {}
        )
    }
}

data class SettingsSwitchItem(
    val checked: Boolean,
    val icon: Int,
    val label: Int,
    val description: Int,
    val onCheckedChange: (Boolean) -> Unit,
    val enabled: Boolean = true
)