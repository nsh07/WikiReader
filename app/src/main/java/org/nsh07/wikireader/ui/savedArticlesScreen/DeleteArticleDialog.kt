package org.nsh07.wikireader.ui.savedArticlesScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.nsh07.wikireader.R
import org.nsh07.wikireader.data.WRStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteArticleDialog(
    articleFileName: String?,
    showSnackbar: (String) -> Unit,
    setShowDeleteDialog: (Boolean) -> Unit,
    deleteArticle: (String) -> WRStatus,
    deleteAll: () -> WRStatus
) {
    val articleName: String? = articleFileName?.substringBeforeLast('.')?.substringBeforeLast('.')
    val context = LocalContext.current
    BasicAlertDialog(
        onDismissRequest = { setShowDeleteDialog(false) }
    ) {
        Surface(
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = if (articleName != null) stringResource(R.string.deleteSavedArticle)
                    else stringResource(R.string.deleteAllArticles),
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.padding(16.dp))
                Text(
                    text =
                    if (articleName != null)
                        stringResource(R.string.deleteSavedArticleDesc, articleName)
                    else
                        stringResource(R.string.deleteAllArticlesDesc),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.align(Alignment.End)) {
                    TextButton(onClick = { setShowDeleteDialog(false) }) {
                        Text(text = stringResource(R.string.cancel))
                    }
                    TextButton(onClick = {
                        setShowDeleteDialog(false)
                        val status = if (articleFileName != null) deleteArticle(articleFileName)
                        else deleteAll()
                        if (status == WRStatus.SUCCESS)
                            showSnackbar(context.getString(R.string.articleDeleted))
                        else
                            showSnackbar(
                                context.getString(
                                    R.string.unableToDeleteArticle,
                                    status.name
                                )
                            )
                    }
                    ) {
                        Text(text = stringResource(R.string.delete))
                    }
                }
            }
        }
    }
}