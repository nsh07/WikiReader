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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import org.nsh07.wikireader.R
import org.nsh07.wikireader.R.string
import org.nsh07.wikireader.data.langCodeToName
import org.nsh07.wikireader.data.toColor
import org.nsh07.wikireader.ui.homeScreen.viewModel.HomeAction
import org.nsh07.wikireader.ui.homeScreen.viewModel.HomeSubscreen
import org.nsh07.wikireader.ui.settingsScreen.viewModel.PreferencesState
import org.nsh07.wikireader.ui.settingsScreen.viewModel.SettingsAction
import org.nsh07.wikireader.ui.settingsScreen.viewModel.SettingsViewModel
import org.nsh07.wikireader.ui.theme.CustomColors.topBarColors
import org.nsh07.wikireader.ui.theme.WRShapeDefaults.bottomListItemShape
import org.nsh07.wikireader.ui.theme.WRShapeDefaults.cardShape
import org.nsh07.wikireader.ui.theme.WRShapeDefaults.middleListItemShape
import org.nsh07.wikireader.ui.theme.WRShapeDefaults.topListItemShape
import org.nsh07.wikireader.ui.theme.WikiReaderTheme
import kotlin.math.round

@Composable
fun SettingsScreenRoot(
    preferencesState: PreferencesState,
    lastBackStackEntry: HomeSubscreen,
    recentLangs: List<String>,
    languageSearchStr: String,
    languageSearchQuery: String,
    onHomeAction: (HomeAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
) {
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

    SettingsScreen(
        preferencesState = preferencesState,
        lastBackStackEntry = lastBackStackEntry,
        recentLangs = recentLangs,
        languageSearchStr = languageSearchStr,
        languageSearchQuery = languageSearchQuery,
        themeMap = themeMap,
        reverseThemeMap = reverseThemeMap,
        fontStyles = fontStyles,
        fontStyleMap = fontStyleMap,
        reverseFontStyleMap = reverseFontStyleMap,
        onAction = viewModel::onAction,
        onHomeAction = onHomeAction,
        onBack = onBack,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen(
    preferencesState: PreferencesState,
    lastBackStackEntry: HomeSubscreen,
    recentLangs: List<String>,
    languageSearchStr: String,
    languageSearchQuery: String,
    themeMap: Map<String, Pair<Int, String>>,
    reverseThemeMap: Map<String, String>,
    fontStyles: List<String>,
    fontStyleMap: Map<String, String>,
    reverseFontStyleMap: Map<String, String>,
    onAction: (SettingsAction) -> Unit,
    onHomeAction: (HomeAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
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

    // Compose Material 3 uses the 3P spec, which hasn't yet been updated with the new
    // switch tokens, which are already used across Google products.
    // Therefore we must override certain colors to let the Switch remain accessible
    // with the new 2025 color spec.
    // See https://github.com/nsh07/WikiReader/issues/272 for more info
    //
    // Once Compose Material 3 updates Switch tokens, this should be removed.
    val switchColors = SwitchDefaults.colors(
        checkedIconColor = colorScheme.primary,
    )

    val switchItems = remember(preferencesState) {
        listOf(
            SettingsSwitchItem(
                preferencesState.blackTheme,
                R.drawable.contrast,
                string.settingBlackTheme,
                string.settingBlackThemeDesc,
                SettingsAction::SaveBlackTheme
            ),
            SettingsSwitchItem(
                preferencesState.dataSaver,
                R.drawable.data_saver_on,
                string.settingDataSaver,
                string.settingDataSaverDesc,
                SettingsAction::SaveDataSaver
            ),
            SettingsSwitchItem(
                preferencesState.feedEnabled,
                R.drawable.feed,
                string.settingFeed,
                string.settingFeedDesc,
                SettingsAction::SaveFeedEnabled,
                enabled = !preferencesState.dataSaver
            ),
            SettingsSwitchItem(
                preferencesState.expandedSections,
                R.drawable.expand_all,
                string.settingExpandSections,
                string.settingExpandSectionsDesc,
                SettingsAction::SaveExpandedSections
            ),
            SettingsSwitchItem(
                preferencesState.imageBackground,
                R.drawable.texture,
                string.settingImageBackground,
                string.settingImageBackgroundDesc,
                SettingsAction::SaveImageBackground
            ),
            SettingsSwitchItem(
                preferencesState.immersiveMode,
                R.drawable.open_in_full,
                string.settingImmersiveMode,
                string.settingImmersiveModeDesc,
                SettingsAction::SaveImmersiveMode
            ),
            SettingsSwitchItem(
                preferencesState.renderMath,
                R.drawable.function,
                string.settingRenderMath,
                string.settingRenderMathDesc,
                SettingsAction::SaveRenderMath
            ),
            SettingsSwitchItem(
                preferencesState.browsingHistory,
                R.drawable.manage_history,
                string.history,
                string.historyDesc,
                SettingsAction::SaveHistory
            ),
            SettingsSwitchItem(
                preferencesState.searchHistory,
                R.drawable.search_history,
                string.settingSearchHistory,
                string.settingSearchHistoryDesc,
                SettingsAction::SaveSearchHistory
            )
        )
    }

    if (showThemeDialog)
        ThemeDialog(
            themeMap = themeMap,
            reverseThemeMap = reverseThemeMap,
            theme = theme,
            setShowThemeDialog = setShowThemeDialog,
            setTheme = { onAction(SettingsAction.SaveTheme(it)) }
        )
    if (showColorSchemeDialog)
        ColorSchemePickerDialog(
            currentColor = color,
            onColorChange = { onAction(SettingsAction.SaveColorScheme(it.toString())) },
            setShowDialog = setShowColorSchemeDialog
        )
    if (showResetSettingsDialog)
        ResetSettingsDialog(
            onResetSettings = { onAction(SettingsAction.ResetSettings) },
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
                onAction(SettingsAction.SaveLang(it))
                if (lastBackStackEntry is HomeSubscreen.Article)
                    onHomeAction(HomeAction.ReloadPage())
                else
                    onHomeAction(HomeAction.LoadFeed())
            },
            setSearchStr = { onHomeAction(HomeAction.UpdateLanguageSearchStr(it)) }
        )
    if (showAppLocaleSheet && currentLocales != null)
        AppLocaleBottomSheet(
            searchStr = languageSearchStr,
            currentLocales = currentLocales,
            setSearchStr = { onHomeAction(HomeAction.UpdateLanguageSearchStr(it)) },
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
                            onCheckedChange = { onAction(switchItems[0].actionConstructor(it)) },
                            thumbContent = {
                                if (switchItems[0].checked) {
                                    Icon(
                                        painter = painterResource(R.drawable.check),
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize),
                                    )
                                } else {
                                    Icon(
                                        painter = painterResource(R.drawable.clear),
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize),
                                    )
                                }
                            },
                            enabled = switchItems[0].enabled,
                            colors = switchColors
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
                                        onAction(
                                            SettingsAction.SaveFontStyle(
                                                reverseFontStyleMap[label] ?: "sans"
                                            )
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
                                                fadeIn(),
                                        exit = scaleOut(motionScheme.fastSpatialSpec()) +
                                                shrinkHorizontally(motionScheme.fastSpatialSpec()) +
                                                fadeOut()
                                    ) {
                                        Icon(
                                            painterResource(R.drawable.check),
                                            contentDescription = null
                                        )
                                    }
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
                            val intSize = round(fontSizeFloat).toInt()
                            Text(intSize.toString())
                            Slider(
                                value = fontSizeAnimated,
                                onValueChange = {
                                    animateFontSize = false
                                    if (round(it).toInt() != intSize)
                                        haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                    fontSizeFloat = it
                                },
                                valueRange = 10f..22f,
                                onValueChangeFinished = {
                                    animateFontSize = true
                                    onAction(SettingsAction.SaveFontSize(round(fontSizeFloat).toInt()))
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
                            onCheckedChange = { onAction(item.actionConstructor(it)) },
                            thumbContent = {
                                if (item.checked && item.enabled) {
                                    Icon(
                                        painter = painterResource(R.drawable.check),
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize),
                                    )
                                } else {
                                    Icon(
                                        painter = painterResource(R.drawable.clear),
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize),
                                    )
                                }
                            },
                            enabled = item.enabled,
                            colors = switchColors
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
                            when (index) {
                                0 -> topListItemShape
                                switchItems.lastIndex - 1 -> bottomListItemShape
                                else -> middleListItemShape
                            }
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
                                painterResource(R.drawable.filled_info),
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
            lastBackStackEntry = HomeSubscreen.Logo,
            recentLangs = emptyList(),
            languageSearchStr = "",
            languageSearchQuery = "",
            themeMap = themeMap,
            reverseThemeMap = reverseThemeMap,
            fontStyles = fontStyles,
            fontStyleMap = fontStyleMap,
            reverseFontStyleMap = reverseFontStyleMap,
            onAction = {},
            onHomeAction = {},
            onBack = {},
        )
    }
}

data class SettingsSwitchItem(
    val checked: Boolean,
    val icon: Int,
    val label: Int,
    val description: Int,
    val actionConstructor: (Boolean) -> SettingsAction,
    val enabled: Boolean = true
)