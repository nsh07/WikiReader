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
import androidx.compose.ui.unit.dp
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
                    text = if (articleName != null) "Delete saved article?"
                    else "Delete all articles?",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.padding(16.dp))
                Text(
                    text =
                    if (articleName != null)
                        "\"$articleName\" will be permanently deleted from your device"
                    else
                        "All articles will be permanently deleted from your device",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.align(Alignment.End)) {
                    TextButton(onClick = { setShowDeleteDialog(false) }) {
                        Text(text = "Cancel")
                    }
                    TextButton(onClick = {
                        setShowDeleteDialog(false)
                        val status = if (articleFileName != null) deleteArticle(articleFileName)
                        else deleteAll()
                        if (status == WRStatus.SUCCESS)
                            showSnackbar("Article deleted")
                        else
                            showSnackbar("Unable to delete article: ${status.name}")
                    }
                    ) {
                        Text(text = "Delete")
                    }
                }
            }
        }
    }
}