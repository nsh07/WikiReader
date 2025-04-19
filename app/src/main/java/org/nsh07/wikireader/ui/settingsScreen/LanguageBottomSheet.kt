package org.nsh07.wikireader.ui.settingsScreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.nsh07.wikireader.data.LanguageData.langCodes
import org.nsh07.wikireader.data.LanguageData.langNames
import org.nsh07.wikireader.data.LanguageData.wikipediaNames
import org.nsh07.wikireader.data.langCodeToName
import org.nsh07.wikireader.ui.theme.WikiReaderTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageBottomSheet(
    lang: String,
    searchStr: String,
    searchQuery: String,
    setShowSheet: (Boolean) -> Unit,
    setLang: (String) -> Unit,
    setSearchStr: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedOption by remember { mutableStateOf(langCodeToName(lang)) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val bottomSheetState =
        rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val insets = WindowInsets.safeDrawing.asPaddingValues()

    ModalBottomSheet(
        onDismissRequest = {
            setShowSheet(false)
            setSearchStr("")
        },
        sheetState = bottomSheetState,
        contentWindowInsets = {
            WindowInsets(
                left = insets.calculateLeftPadding(LocalLayoutDirection.current),
                right = insets.calculateRightPadding(LocalLayoutDirection.current)
            )
        },
        modifier = modifier.padding(top = insets.calculateTopPadding())
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Choose Wikipedia language",
                style = MaterialTheme.typography.labelLarge
            )
            LanguageSearchBar(
                searchStr = searchStr,
                setSearchStr = setSearchStr,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
            HorizontalDivider()
            LazyColumn(state = listState) {
                itemsIndexed(
                    langNames,
                    key = { _: Int, it: String -> it }
                ) { index: Int, it: String ->
                    if (it.contains(searchQuery, ignoreCase = true))
                        ListItem(
                            headlineContent = {
                                Text(
                                    it,
                                    color =
                                    if (selectedOption == it) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            supportingContent = { Text(wikipediaNames[index]) },
                            trailingContent = {
                                if (selectedOption == it) Icon(
                                    Icons.Outlined.Check,
                                    tint = MaterialTheme.colorScheme.primary,
                                    contentDescription = "Selected"
                                )
                            },
                            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                            modifier = Modifier
                                .clickable(onClick = {
                                    setLang(langCodes[index])
                                    scope
                                        .launch { bottomSheetState.hide() }
                                        .invokeOnCompletion {
                                            if (!bottomSheetState.isVisible) {
                                                setShowSheet(false)
                                                setSearchStr("")
                                            }
                                        }
                                })
                        )
                }
                item { Spacer(Modifier.height(insets.calculateBottomPadding())) }
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
        LanguageBottomSheet(lang = "en", searchStr = "", searchQuery = "",
            {}, {}, {})
    }
}
