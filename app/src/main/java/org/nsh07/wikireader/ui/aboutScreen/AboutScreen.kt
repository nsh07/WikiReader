package org.nsh07.wikireader.ui.aboutScreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import org.nsh07.wikireader.BuildConfig
import org.nsh07.wikireader.R
import org.nsh07.wikireader.ui.theme.WikiReaderTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    modifier: Modifier = Modifier,
    windowSizeClass: WindowSizeClass,
    onBack: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val isAlpha = remember { BuildConfig.VERSION_NAME.contains('a') }
    val weight = remember {
        if (windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.MEDIUM ||
            windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED
        )
            1f
        else 0f
    }

    Scaffold(
        topBar = { AboutTopAppBar(scrollBehavior = scrollBehavior, onBack = onBack) },
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { insets ->
        Row {
            if (weight != 0f) Spacer(Modifier.weight(weight))
            Column(
                modifier = Modifier
                    .padding(top = insets.calculateTopPadding())
                    .weight(4f)
                    .verticalScroll(
                        rememberScrollState()
                    )
            ) {
                OutlinedCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(painterResource(R.drawable.ic_launcher_foreground), null)
                            Text(
                                if (!isAlpha) AnnotatedString("WikiReader")
                                else buildAnnotatedString {
                                    append("WikiReader")
                                    withStyle(
                                        SpanStyle(
                                            fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                                            fontStyle = FontStyle.Italic,
                                            baselineShift = BaselineShift.Superscript
                                        )
                                    ) {
                                        append(" αlpha")
                                    }
                                },
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            "Read Wikipedia pages distraction-free",
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        ListItem(
                            leadingContent = {
                                Icon(Icons.Outlined.Info, null)
                            },
                            headlineContent = { Text("Version") },
                            supportingContent = { Text(BuildConfig.VERSION_NAME) }
                        )
                        ListItem(
                            leadingContent = {
                                Icon(painterResource(R.drawable.code), null)
                            },
                            headlineContent = { Text("Source code") },
                            supportingContent = { Text("GitHub") },
                            modifier = Modifier.clickable(onClick = {
                                uriHandler.openUri("https://github.com/nsh07/WikiReader")
                            })
                        )
                        ListItem(
                            leadingContent = {
                                Icon(painterResource(R.drawable.gavel), null)
                            },
                            headlineContent = { Text("License") },
                            supportingContent = { Text("GPL v3.0") },
                            modifier = Modifier.clickable(onClick = {
                                uriHandler.openUri("https://github.com/nsh07/WikiReader/blob/main/LICENSE")
                            })
                        )
                        ListItem(
                            leadingContent = {
                                Icon(painterResource(R.drawable.update), null)
                            },
                            headlineContent = { Text("Releases") },
                            supportingContent = { Text("Check out older and newer versions") },
                            modifier = Modifier.clickable(onClick = {
                                uriHandler.openUri("https://github.com/nsh07/WikiReader/releases")
                            })
                        )
                    }
                }
                OutlinedCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Wikipedia",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(16.dp)
                                .align(Alignment.Start)
                        )
                        ListItem(
                            leadingContent = {
                                Icon(
                                    painterResource(R.drawable.wikipedia_s_w), null
                                )
                            },
                            headlineContent = { Text("Wikipedia") },
                            supportingContent = { Text("Visit the Wikipedia website") },
                            modifier = Modifier.clickable(onClick = {
                                uriHandler.openUri("https://wikipedia.org")
                            })
                        )
                        ListItem(
                            leadingContent = {
                                Icon(
                                    painterResource(R.drawable.wikimedia_logo_black), null
                                )
                            },
                            headlineContent = { Text("Support Wikipedia") },
                            supportingContent = { Text("Donate or contribute to Wikipedia") },
                            modifier = Modifier.clickable(onClick = {
                                uriHandler.openUri("https://wikimediafoundation.org/support/")
                            })
                        )
                    }
                }
                OutlinedCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Author",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(16.dp)
                                .align(Alignment.Start)
                        )
                        ListItem(
                            leadingContent = {
                                Icon(
                                    painterResource(R.drawable.github), null
                                )
                            },
                            headlineContent = { Text("Nishant Mishra") },
                            supportingContent = { Text("Check out my other projects on GitHub") },
                            modifier = Modifier.clickable(onClick = {
                                uriHandler.openUri("https://github.com/nsh07")
                            })
                        )
                    }
                }
                Spacer(Modifier.height(insets.calculateBottomPadding()))
            }
            if (weight != 0f) Spacer(Modifier.weight(weight))
        }
    }
}

@Preview
@Composable
fun AboutScreenPreview() {
    WikiReaderTheme(darkTheme = true) {
        AboutScreen(windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass) {}
    }
}
