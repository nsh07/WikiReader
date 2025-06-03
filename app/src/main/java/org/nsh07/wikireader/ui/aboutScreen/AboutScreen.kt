package org.nsh07.wikireader.ui.aboutScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.nsh07.wikireader.BuildConfig
import org.nsh07.wikireader.R
import org.nsh07.wikireader.ui.theme.CustomTopBarColors.topBarColors
import org.nsh07.wikireader.ui.theme.WRShapeDefaults.bottomListItemShape
import org.nsh07.wikireader.ui.theme.WRShapeDefaults.middleListItemShape
import org.nsh07.wikireader.ui.theme.WRShapeDefaults.topListItemShape
import org.nsh07.wikireader.ui.theme.WikiReaderTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AboutScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val isAlpha = remember { BuildConfig.VERSION_NAME.contains('a') }

    Scaffold(
        topBar = {
            AboutTopAppBar(scrollBehavior = scrollBehavior, onBack = onBack)
        },
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
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .clip(topListItemShape)
                        .fillMaxWidth()
                        .background(colorScheme.surface)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(painterResource(R.drawable.ic_launcher_foreground), null)
                        Text(
                            if (!isAlpha) AnnotatedString(stringResource(R.string.app_name))
                            else buildAnnotatedString {
                                append(stringResource(R.string.app_name))
                                withStyle(
                                    SpanStyle(
                                        fontSize = typography.bodyLarge.fontSize,
                                        fontStyle = FontStyle.Italic,
                                        baselineShift = BaselineShift.Superscript
                                    )
                                ) {
                                    append(" αlpha")
                                }
                            },
                            style = typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        stringResource(R.string.appTagline),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    ListItem(
                        leadingContent = {
                            Icon(Icons.Outlined.Info, null)
                        },
                        headlineContent = { Text(stringResource(R.string.version)) },
                        supportingContent = { Text(BuildConfig.VERSION_NAME) },
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .clip(middleListItemShape)
                    )
                    ListItem(
                        leadingContent = {
                            Icon(painterResource(R.drawable.code), null)
                        },
                        headlineContent = { Text(stringResource(R.string.sourceCode)) },
                        supportingContent = { Text("GitHub") },
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .clip(middleListItemShape)
                            .clickable(
                                onClick = {
                                    uriHandler.openUri("https://github.com/nsh07/WikiReader")
                                }
                            )
                    )
                    ListItem(
                        leadingContent = {
                            Icon(painterResource(R.drawable.gavel), null)
                        },
                        headlineContent = { Text(stringResource(R.string.license)) },
                        supportingContent = { Text("GPL v3.0") },
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .clip(middleListItemShape)
                            .clickable(
                                onClick = {
                                    uriHandler.openUri("https://github.com/nsh07/WikiReader/blob/main/LICENSE")
                                }
                            )
                    )
                    ListItem(
                        leadingContent = {
                            Icon(painterResource(R.drawable.update), null)
                        },
                        headlineContent = { Text(stringResource(R.string.releases)) },
                        supportingContent = { Text(stringResource(R.string.releasesDesc)) },
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .clip(middleListItemShape)
                            .clickable(
                                onClick = {
                                    uriHandler.openUri("https://github.com/nsh07/WikiReader/releases")
                                }
                            )
                    )
                    ListItem(
                        leadingContent = {
                            Icon(painterResource(R.drawable.translate), null)
                        },
                        headlineContent = { Text(stringResource(R.string.translate)) },
                        supportingContent = { Text(stringResource(R.string.translateDesc)) },
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .clip(bottomListItemShape)
                            .clickable(
                                onClick = {
                                    uriHandler.openUri("https://hosted.weblate.org/engage/wikireader/")
                                }
                            )
                    )
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        stringResource(R.string.author),
                        style = typography.labelLarge,
                        color = colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                    )
                    ListItem(
                        leadingContent = {
                            Icon(
                                painterResource(R.drawable.github), null
                            )
                        },
                        headlineContent = { Text("Nishant Mishra") },
                        supportingContent = { Text(stringResource(R.string.otherProjectsDesc)) },
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .clip(topListItemShape)
                            .clickable(
                                onClick = {
                                    uriHandler.openUri("https://github.com/nsh07")
                                }
                            )
                    )
                    ListItem(
                        leadingContent = {
                            Icon(painterResource(R.drawable.heart), null)
                        },
                        headlineContent = { Text(stringResource(R.string.donate)) },
                        supportingContent = { Text(stringResource(R.string.supportMyWork)) },
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .clip(bottomListItemShape)
                            .clickable(
                                onClick = {
                                    uriHandler.openUri("https://github.com/sponsors/nsh07")
                                }
                            )
                    )
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        stringResource(R.string.wikipedia),
                        style = typography.labelLarge,
                        color = colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                    )
                    ListItem(
                        leadingContent = {
                            Icon(
                                painterResource(R.drawable.wikipedia_s_w), null
                            )
                        },
                        headlineContent = { Text(stringResource(R.string.wikipedia)) },
                        supportingContent = { Text(stringResource(R.string.wikipediaWebsiteDesc)) },
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .clip(topListItemShape)
                            .clickable(
                                onClick = {
                                    uriHandler.openUri("https://wikipedia.org")
                                }
                            )
                    )
                    ListItem(
                        leadingContent = {
                            Icon(
                                painterResource(R.drawable.wikimedia_logo_black), null
                            )
                        },
                        headlineContent = { Text(stringResource(R.string.supportWikipedia)) },
                        supportingContent = { Text(stringResource(R.string.donateToWikipedia)) },
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .clip(bottomListItemShape)
                            .clickable(
                                onClick = {
                                    uriHandler.openUri("https://wikimediafoundation.org/support/")
                                }
                            )
                    )
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Preview
@Composable
fun AboutScreenPreview() {
    WikiReaderTheme(darkTheme = true) {
        AboutScreen {}
    }
}
