package org.nsh07.wikireader.ui

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import org.nsh07.wikireader.R
import org.nsh07.wikireader.ui.scaffoldComponents.SettingsTopBar
import org.nsh07.wikireader.ui.viewModel.PreferencesState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    preferencesState: PreferencesState,
    onThemeChanged: (String) -> Unit,
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

    val showThemeDialog = remember { mutableStateOf(false) }

    AnimatedVisibility(showThemeDialog.value) {
        val selectedOption =
            remember { mutableStateOf(themeMap[theme]!!.second) }
        BasicAlertDialog(
            onDismissRequest = { showThemeDialog.value = false }
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
                    TextButton(
                        onClick = {
                            showThemeDialog.value = false
                            onThemeChanged(reverseThemeMap[selectedOption.value]!!)
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Ok")
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
                    .clickable(onClick = { showThemeDialog.value = true })
            )
        }
    }
}