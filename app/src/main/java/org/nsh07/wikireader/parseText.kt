package org.nsh07.wikireader

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

    return out
}