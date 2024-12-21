package org.nsh07.wikireader.ui.settingsScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.nsh07.wikireader.R
import org.nsh07.wikireader.ui.theme.WikiReaderTheme

@Composable
fun ColorPickerButton(
    color: Color,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .clip(CircleShape)
            .background(color)
    ) {
        when (isSelected) {
            true -> Icon(Icons.Outlined.Check, tint = Color.Black, contentDescription = null)
            false ->
                if (color == Color.White) Icon(
                    painterResource(R.drawable.colors),
                    tint = Color.Black,
                    contentDescription = null
                )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorSchemePickerDialog(
    currentColor: Color,
    modifier: Modifier = Modifier,
    setShowDialog: (Boolean) -> Unit,
    onColorChange: (Color) -> Unit,
) {
    val colorSchemes = listOf(
        Color(0xfffeb4a7), Color(0xffffb3c0), Color(0xfffcaaff), Color(0xffb9c3ff),
        Color(0xff62d3ff), Color(0xff44d9f1), Color(0xff52dbc9), Color(0xff78dd77),
        Color(0xff9fd75c), Color(0xffc1d02d), Color(0xfffabd00), Color(0xffffb86e),
        Color.White
    )

    BasicAlertDialog(
        onDismissRequest = { setShowDialog(false) },
        modifier = modifier
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
                    text = "Choose color scheme",
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(Modifier.height(16.dp))

                (0..11 step 4).forEach {
                    Row {
                        colorSchemes.slice(it..it + 3).forEach { color ->
                            ColorPickerButton(
                                color,
                                color == currentColor,
                                modifier = Modifier.padding(8.dp)
                            ) {
                                onColorChange(color)
                            }
                        }
                    }
                }
                ColorPickerButton(
                    colorSchemes.last(),
                    colorSchemes.last() == currentColor,
                    modifier = Modifier.padding(8.dp)
                ) {
                    onColorChange(colorSchemes.last())
                }

                Spacer(Modifier.height(24.dp))

                TextButton(
                    onClick = { setShowDialog(false) },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("OK")
                }
            }
        }
    }
}

@Preview
@Composable
fun ColorPickerDialogPreview() {
    WikiReaderTheme(darkTheme = true) {
        ColorSchemePickerDialog(
            Color(0xfffeb4a7),
            setShowDialog = {},
            onColorChange = {}
        )
    }
}
