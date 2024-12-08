package org.nsh07.wikireader.data

import android.util.Log
import androidx.compose.ui.graphics.Color

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
    val out = emptyList<String>().toMutableList()

    var start = 0

    var i = 0
    val l = text.length

    while (i < l) {
        if (text[i] == '=') {
            if (i + 2 >= l || i + 1 >= l) break
            if (text[i + 1] == '=' && text[i + 2] != '=' && text[i - 1] != '=') {
                out += text.slice(start..i - 2).trim('\n')
                i += 3
                start = i
                continue
            }
        }
        i++
    }

    out += if (i - 1 >= l)
        text.slice(start..<l)
    else
        text.slice(start..<i)

    val s = out.lastIndex

    for (i in s downTo 1) {
        if (out[i] == "References" || out[i] == "External links" || out[i] == "Gallery") {
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

fun langCodeToName(langCode: String): String {
    Log.d("Language", "CodeToName called")
    return LanguageData.langNames[LanguageData.langCodes.binarySearch(langCode)]
}