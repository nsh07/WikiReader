package org.nsh07.wikireader.ui.homeScreen

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.models.AnimationMode
import ir.ehsannarmani.compose_charts.models.DividerProperties
import ir.ehsannarmani.compose_charts.models.DotProperties
import ir.ehsannarmani.compose_charts.models.DrawStyle
import ir.ehsannarmani.compose_charts.models.GridProperties
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.LabelHelperProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties
import ir.ehsannarmani.compose_charts.models.Line
import ir.ehsannarmani.compose_charts.models.PopupProperties

@Composable
fun ArticleViewsGraph(viewCounts: List<Int>, modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    LineChart(
        modifier = modifier,
        data = remember {
            listOf(
                Line(
                    label = "",
                    values = viewCounts.map { it.toDouble() },
                    color = SolidColor(colorScheme.primary),
                    strokeAnimationSpec = tween(1500, easing = FastOutSlowInEasing),
                    drawStyle = DrawStyle.Stroke(width = 2.dp)
                )
            )
        },
        animationMode = AnimationMode.Together(delayBuilder = {
            it * 500L
        }),
        labelHelperProperties = LabelHelperProperties(enabled = false),
        gridProperties = GridProperties(enabled = false),
        popupProperties = PopupProperties(enabled = false),
        labelProperties = LabelProperties(enabled = false),
        indicatorProperties = HorizontalIndicatorProperties(enabled = false),
        dotsProperties = DotProperties(enabled = false),
        dividerProperties = DividerProperties(enabled = false)
    )
}