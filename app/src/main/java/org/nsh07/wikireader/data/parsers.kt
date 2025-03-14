package org.nsh07.wikireader.data

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle

/**
 * Extension function for [String] to convert it to a [androidx.compose.ui.graphics.Color]
 *
 * The base string must be of the format produced by [androidx.compose.ui.graphics.Color.toString],
 * i.e, the color black with 100% opacity in sRGB would be represented by:
 *
 *      Color(0.0, 0.0, 0.0, 1.0, sRGB IEC61966-2.1)
 */
fun String.toColor(): Color {
    // Sample string: Color(0.0, 0.0, 0.0, 1.0, sRGB IEC61966-2.1)
    Log.d("Color", this)
    val comma1 = this.indexOf(',')
    val comma2 = this.indexOf(',', comma1 + 1)
    val comma3 = this.indexOf(',', comma2 + 1)
    val comma4 = this.indexOf(',', comma3 + 1)

    val r = this.substringAfter('(').substringBefore(',').toFloat()
    val g = this.slice(comma1 + 1..comma2 - 1).toFloat()
    val b = this.slice(comma2 + 1..comma3 - 1).toFloat()
    val a = this.slice(comma3 + 1..comma4 - 1).toFloat()
    return Color(r, g, b, a)
}

fun String.toWikitextAnnotatedString(
    highlightColor: Color,
    performSearch: (String) -> Unit
): AnnotatedString {
    // TODO: Complete this
    // TODO: Improve formatting handling (headings, italics)
    // Split by [[ and ]], if first line starts with [[ then first is link, second non-link and so on and vice versa
    val input = this
    var isLink = input.trimStart().startsWith("[[")
    return buildAnnotatedString {
        input.split("\\[\\[|]]".toRegex()).forEach { curr ->
            if (isLink) {
                withLink(
                    LinkAnnotation.Url(
                        "",
                        TextLinkStyles(
                            SpanStyle(color = highlightColor)
                        )
                    ) {
                        performSearch(curr.substringBefore('|'))
                    }
                ) { append(curr.substringAfter('|')) }
            } else {
                var bold = curr.startsWith("'''")
                curr.split("'''".toRegex()).forEach {
                    if (bold) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(it)
                        }
                    } else {
                        append(it)
                    }
                    if (it != "") bold = !bold
                }
            }
            if (curr != "") isLink = !isLink
        }
        append('\n')
    }
}

/**
 * Function to parse a string returned by the Wikipedia API and convert it into a [List] of [String]s
 *
 * @param text The [String] returned by the Wikipedia API
 *
 * @return List of the format:
 *
 *      {intro text, heading1, body1, heading2, body2, ...}
 *
 * Note that subheadings are *not* parsed, and are left as is (surrounded by three '=' signs on
 * either side)
 */
fun parseText(text: String): List<String> {
    val out = text.split("\n==(?!=)|(?<!=)==[\n ]".toRegex()).toMutableList()

    for (i in out.lastIndex downTo 1) {
        val curr = out[i].trim()
        if (curr in listOf("References", "External links", "Gallery", "Footnotes")) {
            out.removeAt(i + 1)
            out.removeAt(i)
        } else if (i + 1 <= out.lastIndex) {
            if (out[i + 1].trim('\n') == "") {
                out.removeAt(i + 1)
                out.removeAt(i)
            }
        }
    }

    return out
}

fun bytesToHumanReadableSize(bytes: Double) = when {
    bytes >= 1 shl 30 -> "%.1f GB".format(bytes / (1 shl 30))
    bytes >= 1 shl 20 -> "%.1f MB".format(bytes / (1 shl 20))
    bytes >= 1 shl 10 -> "%.0f kB".format(bytes / (1 shl 10))
    else -> "$bytes bytes"
}

fun langCodeToName(langCode: String): String {
    try {
        return LanguageData.langNames[LanguageData.langCodes.binarySearch(langCode)]
    } catch (_: Exception) {
        Log.e("Language", "Unknown Language: $langCode")
        return langCode
    }
}

fun langCodeToWikiName(langCode: String): String {
    try {
        return LanguageData.wikipediaNames[LanguageData.langCodes.binarySearch(langCode)]
    } catch (_: Exception) {
        Log.e("Language", "Unknown Language: $langCode")
        return langCode
    }
}