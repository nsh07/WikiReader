package org.nsh07.wikireader.ui.settingsScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastAll
import kotlinx.coroutines.launch
import org.nsh07.wikireader.R
import org.nsh07.wikireader.data.LanguageData.langCodes
import org.nsh07.wikireader.data.LanguageData.langNames
import org.nsh07.wikireader.data.LanguageData.wikipediaNames
import org.nsh07.wikireader.data.UserLanguage
import org.nsh07.wikireader.data.langCodeToName
import org.nsh07.wikireader.data.langCodeToWikiName
import org.nsh07.wikireader.ui.homeScreen.LanguageListItem
import org.nsh07.wikireader.ui.theme.WikiReaderTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageBottomSheet(
    recentLangs: List<String>,
    lang: String,
    searchStr: String,
    searchQuery: String,
    setShowSheet: (Boolean) -> Unit,
    setLang: (String) -> Unit,
    setSearchStr: (String) -> Unit,
    modifier: Modifier = Modifier,
    userLanguageSelectionMode: Boolean = false,
    insertUserLanguage: ((UserLanguage) -> Unit)? = null,
    deleteUserLanguage: ((String) -> Unit)? = null,
) {
    // Require that either user lang selection is disabled OR none of the user lang lambdas are null
    require(
        !userLanguageSelectionMode || listOf(
            insertUserLanguage,
            deleteUserLanguage
        ).fastAll { it != null }
    )
    var selectedOption by remember { mutableStateOf(langCodeToName(lang)) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState()

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
                if (recentLangs.isNotEmpty() && searchQuery.isEmpty()) {
                    item {
                        Text(
                            stringResource(R.string.recentLanguages),
                            style = typography.titleSmall,
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                        )
                    }
                    itemsIndexed(
                        recentLangs,
                        key = { _: Int, it: String -> it }
                    ) { index: Int, it: String ->
                        if (it.contains(searchQuery, ignoreCase = true)) {
                            val langName = remember(it) { langCodeToName(it) }
                            val selected = selectedOption == langName
                            LanguageListItem(
                                headlineContent = {
                                    Text(langName)
                                },
                                supportingContent = { Text(remember(it) { langCodeToWikiName(it) }) },
                                selected = selected,
                                items = recentLangs.size,
                                index = index
                            ) {
                                setLang(it)
                                scope
                                    .launch {
                                        if (userLanguageSelectionMode) {
                                            insertUserLanguage?.invoke(
                                                UserLanguage(
                                                    it,
                                                    langName,
                                                    true
                                                )
                                            )
                                        }
                                        bottomSheetState.hide()
                                    }
                                    .invokeOnCompletion {
                                        if (!bottomSheetState.isVisible) {
                                            setShowSheet(false)
                                            setSearchStr("")
                                        }
                                    }
                            }
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
                    langNames,
                    key = { _: Int, it: String -> it }
                ) { index: Int, it: String ->
                    if (it.contains(searchQuery, ignoreCase = true)) {
                        val selected = selectedOption == it
                        LanguageListItem(
                            headlineContent = {
                                Text(it)
                            },
                            supportingContent = { Text(wikipediaNames[index]) },
                            selected = selected,
                            items = langNames.size,
                            index = index
                        ) {
                            setLang(langCodes[index])
                            scope
                                .launch {
                                    if (userLanguageSelectionMode) {
                                        insertUserLanguage?.invoke(
                                            UserLanguage(
                                                langCodes[index],
                                                it,
                                                true
                                            )
                                        )
                                    }
                                    bottomSheetState.hide()
                                }
                                .invokeOnCompletion {
                                    if (!bottomSheetState.isVisible) {
                                        setShowSheet(false)
                                        setSearchStr("")
                                    }
                                }
                        }
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

@Preview
@Composable
fun LanguageSheetPreview() {
    WikiReaderTheme {
        LanguageBottomSheet(
            emptyList(), lang = "en", searchStr = "", searchQuery = "",
            {}, {}, {})
    }
}
