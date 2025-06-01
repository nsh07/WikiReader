package org.nsh07.wikireader.ui.homeScreen

import android.content.Context
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.size.Size
import org.nsh07.wikireader.R
import org.nsh07.wikireader.ui.theme.ColorConstants.colorMatrixInvert

@Composable
fun EquationImage(
    context: Context,
    dpi: Float,
    latex: String,
    fontSize: Int,
    darkTheme: Boolean
) {
    Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(
                    "https://latex.codecogs.com/png.image?\\dpi{${
                        (dpi * 160 * (fontSize / 16.0)).toInt()
                    }}${latex}"
                )
                .size(Size.ORIGINAL)
                .build(),
            placeholder = painterResource(R.drawable.more_horiz),
            error = painterResource(R.drawable.error),
            contentDescription = null,
            colorFilter = if (darkTheme) // Invert colors in dark theme
                ColorFilter.colorMatrix(ColorMatrix(colorMatrixInvert))
            else null,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
    }
}