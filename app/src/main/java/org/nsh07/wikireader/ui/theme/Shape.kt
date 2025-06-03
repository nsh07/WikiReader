package org.nsh07.wikireader.ui.theme

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.runtime.Composable

object WRShapeDefaults {
    val topListItemShape: RoundedCornerShape
        @Composable get() =
            RoundedCornerShape(
                topStart = shapes.large.topStart,
                topEnd = shapes.large.topEnd,
                bottomStart = shapes.extraSmall.bottomStart,
                bottomEnd = shapes.extraSmall.bottomStart
            )

    val middleListItemShape: RoundedCornerShape
        @Composable get() = RoundedCornerShape(shapes.extraSmall.topStart)

    val bottomListItemShape: RoundedCornerShape
        @Composable get() =
            RoundedCornerShape(
                topStart = shapes.extraSmall.topStart,
                topEnd = shapes.extraSmall.topEnd,
                bottomStart = shapes.large.bottomStart,
                bottomEnd = shapes.large.bottomEnd
            )

    val cardShape: CornerBasedShape
        @Composable get() = shapes.large
}