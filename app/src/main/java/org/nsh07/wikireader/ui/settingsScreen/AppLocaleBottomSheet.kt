package org.nsh07.wikireader.ui.settingsScreen

import android.app.LocaleConfig
import android.app.LocaleManager
import android.os.Build
import android.os.LocaleList
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.nsh07.wikireader.R
import org.nsh07.wikireader.ui.theme.WRShapeDefaults.bottomListItemShape
import org.nsh07.wikireader.ui.theme.WRShapeDefaults.middleListItemShape
import org.nsh07.wikireader.ui.theme.WRShapeDefaults.topListItemShape
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppLocaleBottomSheet(
    searchStr: String,
    currentLocales: LocaleList,
    modifier: Modifier = Modifier,
    setSearchStr: (String) -> Unit,
    setShowSheet: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val supportedLocales = remember {
        if (Build.VERSION.SDK_INT >= 33) {
            LocaleConfig(context).supportedLocales
        } else null
    }
    val supportedLocaledSize = remember { supportedLocales?.size() ?: 0 }

    val supportedLocalesList: List<WRLocale>? = remember {
        if (supportedLocales != null) {
            buildList {
                for (i in 0 until supportedLocaledSize) {
                    add(WRLocale(supportedLocales.get(i), supportedLocales.get(i).displayName))
                }
                sortWith(compareBy { it.name })
            }
        } else null
    }

    val bottomSheetState = rememberModalBottomSheetState()
    val listState = rememberLazyListState()

    ModalBottomSheet(
        onDismissRequest = { setShowSheet(false) },
        sheetState = bottomSheetState,
        modifier = modifier
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.chooseAppLanguage),
                style = MaterialTheme.typography.labelLarge
            )
            LanguageSearchBar(
                searchStr = searchStr,
                setSearchStr = setSearchStr,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
            if (supportedLocalesList != null) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    state = listState,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .clip(shapes.large)
                ) {
                    item {
                        ListItem(
                            headlineContent = {
                                Text(stringResource(R.string.themeSystemDefault))
                            },
                            trailingContent = {
                                if (currentLocales.isEmpty)
                                    Icon(
                                        Icons.Outlined.Check,
                                        contentDescription = stringResource(R.string.selectedLabel)
                                    )
                            },
                            colors =
                                if (currentLocales.isEmpty)
                                    ListItemDefaults.colors(containerColor = colorScheme.primaryContainer)
                                else ListItemDefaults.colors(),
                            modifier = Modifier
                                .clip(shapes.large)
                                .clickable(
                                    onClick = {
                                        if (Build.VERSION.SDK_INT >= 33) {
                                            context
                                                .getSystemService(LocaleManager::class.java)
                                                .applicationLocales = LocaleList()
                                        }
                                        scope
                                            .launch { bottomSheetState.hide() }
                                            .invokeOnCompletion { setShowSheet(false) }
                                    }
                                )
                        )
                    }
                    item {
                        Spacer(Modifier.height(12.dp))
                    }
                    itemsIndexed(
                        supportedLocalesList.filter { it.name.contains(searchStr, true) },
                        key = { index: Int, it: WRLocale -> it.name }
                    ) { index, it ->
                        ListItem(
                            headlineContent = {
                                Text(it.name)
                            },
                            trailingContent = {
                                if (!currentLocales.isEmpty && it.locale == currentLocales.get(0))
                                    Icon(
                                        Icons.Outlined.Check,
                                        tint = colorScheme.primary,
                                        contentDescription = stringResource(R.string.selectedLabel)
                                    )
                            },
                            colors =
                                if (!currentLocales.isEmpty && it.locale == currentLocales.get(0))
                                    ListItemDefaults.colors(containerColor = colorScheme.primaryContainer)
                                else ListItemDefaults.colors(),
                            modifier = Modifier
                                .clip(
                                    if (index == 0) topListItemShape
                                    else if (index == supportedLocaledSize - 1) bottomListItemShape
                                    else middleListItemShape
                                )
                                .clickable(
                                    onClick = {
                                        if (Build.VERSION.SDK_INT >= 33) {
                                            context.getSystemService(LocaleManager::class.java)
                                                .applicationLocales =
                                                LocaleList(it.locale)
                                        }
                                        scope
                                            .launch { bottomSheetState.hide() }
                                            .invokeOnCompletion { setShowSheet(false) }
                                    }
                                )
                        )
                    }
                }
            }
            Spacer(Modifier.weight(1f))
        }
    }
    LaunchedEffect(searchStr) {
        listState.scrollToItem(0)
    }
}

data class WRLocale(
    val locale: Locale,
    val name: String
)
