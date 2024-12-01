package org.nsh07.wikireader.ui.settingsScreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.nsh07.wikireader.data.LanguageData
import org.nsh07.wikireader.data.langCodeToName
import org.nsh07.wikireader.data.langNameToCode
import org.nsh07.wikireader.ui.theme.WikiReaderTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageBottomSheet(
    lang: String,
    setShowSheet: (Boolean) -> Unit,
    setLang: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedOption by remember { mutableStateOf(langCodeToName(lang)) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val bottomSheetState =
        rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = { setShowSheet(false) },
        sheetState = bottomSheetState,
        modifier = modifier
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Choose Wikipedia Language",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            LazyColumn(state = listState) {
                items(LanguageData.langNames, key = { it }) {
                    ListItem(
                        headlineContent = {
                            Text(
                                it,
                                color =
                                if (selectedOption == it) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface
                            )
                        },
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
                                setLang(langNameToCode(it))
                                scope
                                    .launch { bottomSheetState.hide() }
                                    .invokeOnCompletion {
                                        if (!bottomSheetState.isVisible) {
                                            setShowSheet(false)
                                        }
                                    }
                            })
                    )
                }
            }
        }
    }
    LaunchedEffect(null) {
        var index = LanguageData.langNames.binarySearch(selectedOption)
        if (index >= 2) index -= 2
        listState.scrollToItem(index)
    }
}

@Preview
@Composable
fun LanguageDialogPreview() {
    WikiReaderTheme {
        LanguageBottomSheet(lang = "en", setShowSheet = {}, setLang = {})
    }
}