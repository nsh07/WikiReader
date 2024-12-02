package org.nsh07.wikireader.ui.homeScreen

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.nsh07.wikireader.data.WikiLang
import org.nsh07.wikireader.data.langCodeToName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleLanguageBottomSheet(
    langs: List<WikiLang>,
    setShowSheet: (Boolean) -> Unit,
    setLang: (String) -> Unit,
    performSearch: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val bottomSheetState =
        rememberModalBottomSheetState()
    val insets = WindowInsets.safeDrawing.asPaddingValues()

    ModalBottomSheet(
        onDismissRequest = { setShowSheet(false) },
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
                text = "Choose Wikipedia Language",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            HorizontalDivider()
            LazyColumn(state = listState) {
                items(langs, key = { it.lang }) {
                    val langName: String? = try {
                        langCodeToName(it.lang)
                    } catch (_: Exception) {
                        Log.e("Language", "Language not found: ${it.lang}")
                        null
                    }
                    if (langName != null)
                        ListItem(
                            headlineContent = { Text(langName) },
                            supportingContent = { Text(it.title) },
                            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                            modifier = Modifier
                                .clickable(onClick = {
                                    setLang(it.lang)
                                    performSearch(it.title)
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
                item { Spacer(Modifier.height(insets.calculateBottomPadding())) }
            }
        }
    }
}
