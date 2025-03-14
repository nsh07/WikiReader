package org.nsh07.wikireader.data

class WikitextParser(wikitext: String) {
    var bodyText: String = wikitext

    init {
        bodyText = bodyText
            .replace(
                "<!--.+?-->|<ref[^/]*?>.+?</ref>|<ref.*?/>".toRegex(RegexOption.DOT_MATCHES_ALL),
                ""
            ) // Remove references and comments
            .replace("\n*", "\nꞏ")
        while (bodyText.contains("\\{\\{[^{}]+\\}\\}".toRegex(RegexOption.DOT_MATCHES_ALL))) {
            bodyText =
                bodyText.replace("\\{\\{[^{}]+\\}\\}".toRegex(RegexOption.DOT_MATCHES_ALL), "")
        }
        bodyText = bodyText.replace(" {2,}".toRegex(), " ") // Remove double spaces
    }
}