package org.nsh07.wikireader.data

import androidx.compose.ui.graphics.Color

/**
 * Extension function for [String] to convert it to a [Color]
 *
 * The base string must be of the format produced by [Color.toString],
 * i.e, the color black with 100% opacity in sRGB would be represented by:
 *
 *      Color(0.0, 0.0, 0.0, 1.0, sRGB IEC61966-2.1)
 */
fun String.toColor(): Color {
    // Sample string: Color(0.0, 0.0, 0.0, 1.0, sRGB IEC61966-2.1)
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
 * Note that subheadings are *not* parsed, and are left as is (surrounded by three or more '=' signs
 * on either side)
 */
fun parseSections(text: String): List<String> {
    val out = text.split("\n==(?!=)|(?<!=)==[\n ]".toRegex()).toMutableList()

    for (i in out.lastIndex downTo 1) {
        val curr = out[i].trim()
        if (curr in listOf(
                "External links",
                "Footnotes",
                "Bibliography",
                "Notes",
                "Reference notes"
            )
        ) {
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

/**
 * Function to convert a number of bytes into a human-readable format
 *
 * @param bytes The number of bytes to convert
 * @return String in the format: (number) (unit), where unit is one of:
 *
 *      bytes, kB, MB, GB
 * For example, 1024 bytes would be converted to "1 kB" and 2^20 bytes to "1.0 MB". Note that kB and
 * bytes don't have a decimal part but MB and GB have a single decimal digit.
 */
fun bytesToHumanReadableSize(bytes: Double) = when {
    bytes >= 1 shl 30 -> "%.1f GB".format(bytes / (1 shl 30))
    bytes >= 1 shl 20 -> "%.1f MB".format(bytes / (1 shl 20))
    bytes >= 1 shl 10 -> "%.0f kB".format(bytes / (1 shl 10))
    else -> "$bytes bytes"
}

/**
 * Converts a Wikipedia URL language code (e.g. "en" in en.wikipedia.org for the English Wikipedia)
 * into its corresponding language name (e.g. "English" for en)
 */
fun langCodeToName(langCode: String): String {
    return try {
        LanguageData.langNames[LanguageData.langCodes.binarySearch(langCode)]
    } catch (_: Exception) {
        langCode
    }
}

/**
 * Converts a Wikipedia URL language code (e.g. "en" in en.wikipedia.org for the English Wikipedia)
 * into its corresponding Wikipedia name (e.g. "English Wikipedia" for en)
 */
fun langCodeToWikiName(langCode: String): String {
    return try {
        LanguageData.wikipediaNames[LanguageData.langCodes.binarySearch(langCode)]
    } catch (_: Exception) {
        langCode
    }
}

/**
 * Checks whether a Wikipedia language code corresponds to an RTL language.
 *
 * @param langCode The language code to check
 * @return True if the language is RTL, false otherwise
 */
fun isRtl(langCode: String): Boolean {
    return langCode in LanguageData.rtlLangCodes
}

/**
 * Converts an ISO 3166-1 alpha-2 country code to a flag emoji. Taken from
 * [Gist by asissuthar on GitHub](https://gist.github.com/asissuthar/cf8fcf0b3be968b1f341e537eb423163)
 *
 * @param code The ISO 3166-1 alpha-2 country code
 * @return Flag emoji of the country specified by [code]
 *
 * ```kotlin
 * countryFlag("in")
 * ```
 */
fun countryFlag(code: String) = code
    .uppercase()
    .split("")
    .filter { it.isNotBlank() }
    .map { it.codePointAt(0) + 0x1F1A5 }
    .joinToString("") { String(Character.toChars(it)) }
