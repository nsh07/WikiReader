package org.nsh07.wikireader.data

fun parseText(text: String): List<String> {
    val out = emptyList<String>().toMutableList()

    var start = 0

    var i = 0
    val l = text.length

    while (i < l) {
        if (text[i] == '=') {
            if (text[i + 1] == '=' && text[i + 2] != '=' && text[i - 1] != '=') {
                out += text.slice(start..i - 2)
                i += 3
                start = i
                continue
            }
        }
        i++
    }

    out += text.slice(start..i - 1)

    return out
}