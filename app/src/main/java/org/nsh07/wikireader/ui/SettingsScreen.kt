package org.nsh07.wikireader.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import org.nsh07.wikireader.R
import org.nsh07.wikireader.ui.scaffoldComponents.SettingsTopBar
import org.nsh07.wikireader.ui.viewModel.PreferencesState
import kotlin.math.round

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    preferencesState: PreferencesState,
    onThemeChanged: (String) -> Unit,
    onFontSizeChangeFinished: (Int) -> Unit,
    onExpandedSectionsChanged: (Boolean) -> Unit,
    onDataSaverChanged: (Boolean) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
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

    val theme = preferencesState.theme
    var expandedSections by remember { mutableStateOf(preferencesState.expandedSections) }
    var dataSaver by remember { mutableStateOf(preferencesState.dataSaver) }

    val expandedIcon =
        if (expandedSections) R.drawable.expand_all
        else R.drawable.collapse_all
    val dataSaverIcon =
        if (dataSaver) R.drawable.data_saver_on
        else R.drawable.data_saver_off

    val (showThemeDialog, setShowThemeDialog) = remember { mutableStateOf(false) }

    var fontSizeFloat by remember { mutableFloatStateOf(preferencesState.fontSize.toFloat()) }

    if (showThemeDialog) {
        val selectedOption =
            remember { mutableStateOf(themeMap[theme]!!.second) }
        BasicAlertDialog(
            onDismissRequest = { setShowThemeDialog(false) }
        ) {
            Surface(
                modifier = Modifier
                    .wrapContentWidth()
                    .wrapContentHeight(),
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = AlertDialogDefaults.TonalElevation
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(text = "Choose theme", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(Modifier.selectableGroup()) {
                        themeMap.forEach { pair ->
                            val text = pair.value.second
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .clip(MaterialTheme.shapes.large)
                                    .selectable(
                                        selected = (text == selectedOption.value),
                                        onClick = {
                                            selectedOption.value = text
                                        },
                                        role = Role.RadioButton
                                    )
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = (text == selectedOption.value),
                                    onClick = null // null recommended for accessibility with screenreaders
                                )
                                Text(
                                    text = text,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(start = 16.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(modifier = Modifier.align(Alignment.End)) {
                        TextButton(onClick = { setShowThemeDialog(false) }) {
                            Text("Cancel")
                        }
                        TextButton(
                            onClick = {
                                setShowThemeDialog(false)
                                onThemeChanged(reverseThemeMap[selectedOption.value]!!)
                            }
                        ) {
                            Text("OK")
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = { SettingsTopBar(onBack = onBack) },
        modifier = modifier.fillMaxSize()
    ) { insets ->
        Column(modifier = Modifier.padding(top = insets.calculateTopPadding())) {
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
                    .fillMaxWidth()
                    .clickable(onClick = { setShowThemeDialog(true) })
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
                                onFontSizeChangeFinished(round(fontSizeFloat).toInt())
                            }
                        )
                    }
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
                            onExpandedSectionsChanged(it)
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
                supportingContent = { Text("Only load page image in fullscreen view") },
                trailingContent = {
                    Switch(
                        checked = dataSaver,
                        onCheckedChange = {
                            dataSaver = it
                            onDataSaverChanged(it)
                        }
                    )
                }
            )
        }
    }
}