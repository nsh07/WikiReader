package org.nsh07.wikireader.ui.homeScreen

import android.util.Log
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.nsh07.wikireader.R
import org.nsh07.wikireader.data.WikiLang
import org.nsh07.wikireader.data.langCodeToName
import org.nsh07.wikireader.ui.settingsScreen.LanguageSearchBar
import org.nsh07.wikireader.ui.theme.WRShapeDefaults.bottomListItemShape
import org.nsh07.wikireader.ui.theme.WRShapeDefaults.middleListItemShape
import org.nsh07.wikireader.ui.theme.WRShapeDefaults.topListItemShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleLanguageBottomSheet(
    langs: List<WikiLang>,
    recentLangs: List<String>,
    currentLang: WikiLang,
    searchStr: String,
    searchQuery: String,
    setShowSheet: (Boolean) -> Unit,
    setLang: (String) -> Unit,
    loadPage: (String) -> Unit,
    setSearchStr: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState()

    val articleLangs =
        remember(recentLangs, langs) { langs.partition { it.lang in recentLangs } }

    ModalBottomSheet(
        onDismissRequest = {
            setShowSheet(false)
            setSearchStr("")
        },
        sheetState = bottomSheetState,
        modifier = modifier
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.chooseWikipediaLanguage),
                style = typography.labelLarge
            )
            LanguageSearchBar(
                searchStr = searchStr,
                setSearchStr = setSearchStr,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(shapes.large)
            ) {
                item {
                    Text(
                        stringResource(R.string.currentLanguage),
                        style = typography.titleSmall,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                    )
                }
                item {
                    val langName: String? = try {
                        langCodeToName(currentLang.lang)
                    } catch (_: Exception) {
                        Log.e("Language", "Language not found: ${currentLang.lang}")
                        null
                    }
                    ListItem(
                        headlineContent = { Text(langName ?: currentLang.lang) },
                        supportingContent = { Text(currentLang.title) },
                        trailingContent = {
                            Icon(
                                Icons.Outlined.Check,
                                contentDescription = stringResource(R.string.selectedLabel)
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = colorScheme.primaryContainer),
                        modifier = Modifier
                            .clip(shapes.large)
                            .clickable(
                                onClick = {
                                    scope
                                        .launch { bottomSheetState.hide() }
                                        .invokeOnCompletion {
                                            if (!bottomSheetState.isVisible) {
                                                setShowSheet(false)
                                            }
                                        }
                                }
                            )
                    )
                }
                if (articleLangs.first.isNotEmpty()) {
                    item {
                        Text(
                            stringResource(R.string.recentLanguages),
                            style = typography.titleSmall,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    itemsIndexed(
                        articleLangs.first,
                        key = { index: Int, it: WikiLang -> "recent-" + it.lang }
                    ) { index, it ->
                        val langName: String? = try {
                            langCodeToName(it.lang)
                        } catch (_: Exception) {
                            Log.e("Language", "Language not found: ${it.lang}")
                            null
                        }
                        if (langName != null && langName.contains(searchQuery, ignoreCase = true)) {
                            ListItem(
                                headlineContent = { Text(langName) },
                                supportingContent = { Text(it.title) },
                                modifier = Modifier
                                    .clip(
                                        if (articleLangs.first.size == 1) shapes.large
                                        else {
                                            when (index) {
                                                0 -> topListItemShape
                                                articleLangs.first.size - 1 -> bottomListItemShape
                                                else -> middleListItemShape
                                            }
                                        }
                                    )
                                    .clickable(
                                        onClick = {
                                            setLang(it.lang)
                                            loadPage(it.title)
                                            scope
                                                .launch { bottomSheetState.hide() }
                                                .invokeOnCompletion {
                                                    if (!bottomSheetState.isVisible) {
                                                        setShowSheet(false)
                                                        setSearchStr("")
                                                    }
                                                }
                                        }
                                    )
                            )
                            Spacer(Modifier.height(2.dp))
                        }
                    }
                }
                item {
                    Text(
                        stringResource(R.string.otherLanguages),
                        style = typography.titleSmall,
                        modifier = Modifier.padding(
                            top = 14.dp,
                            bottom = 16.dp,
                            start = 16.dp,
                            end = 16.dp
                        )
                    )
                }
                itemsIndexed(
                    articleLangs.second,
                    key = { index: Int, it: WikiLang -> it.lang }) { index, it ->
                    val langName: String? = try {
                        langCodeToName(it.lang)
                    } catch (_: Exception) {
                        Log.e("Language", "Language not found: ${it.lang}")
                        null
                    }
                    if (langName != null && langName.contains(searchQuery, ignoreCase = true)) {
                        ListItem(
                            headlineContent = { Text(langName) },
                            supportingContent = { Text(it.title) },
                            modifier = Modifier
                                .clip(
                                    if (articleLangs.second.size == 1) shapes.large
                                    else {
                                        when (index) {
                                            0 -> topListItemShape
                                            articleLangs.second.size - 1 -> bottomListItemShape
                                            else -> middleListItemShape
                                        }
                                    }
                                )
                                .clickable(
                                    onClick = {
                                        setLang(it.lang)
                                        loadPage(it.title)
                                        scope
                                            .launch { bottomSheetState.hide() }
                                            .invokeOnCompletion {
                                                if (!bottomSheetState.isVisible) {
                                                    setShowSheet(false)
                                                    setSearchStr("")
                                                }
                                            }
                                    }
                                )
                        )
                        Spacer(Modifier.height(2.dp))
                    }
                }
            }
            Spacer(Modifier.weight(1f))
        }
    }
    LaunchedEffect(searchQuery) {
        listState.scrollToItem(0)
    }
}
