package org.nsh07.wikireader.ui.settingsScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ResetSettingsDialog(
    onResetSettings: () -> Unit,
    setShowResetSettingsDialog: (Boolean) -> Unit,
    showSnackbar: (String) -> Unit
) {
    val context = LocalContext.current
    BasicAlertDialog(
        onDismissRequest = { setShowResetSettingsDialog(false) }
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
                    text = stringResource(R.string.resetSettingsDialog),
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.padding(16.dp))
                Text(
                    text = stringResource(R.string.resetSettingsDialogDesc),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.align(Alignment.End)) {
                    TextButton(
                        shapes = ButtonDefaults.shapes(),
                        onClick = { setShowResetSettingsDialog(false) }) {
                        Text(text = stringResource(R.string.cancel))
                    }
                    TextButton(shapes = ButtonDefaults.shapes(), onClick = {
                        setShowResetSettingsDialog(false)
                        onResetSettings()
                        showSnackbar(context.getString(R.string.settingsRestored))
                    }
                    ) {
                        Text(text = stringResource(R.string.reset))
                    }
                }
            }
        }
    }
}