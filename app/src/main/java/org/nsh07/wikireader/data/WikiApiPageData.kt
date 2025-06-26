package org.nsh07.wikireader.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WikiApiPageData(
    val query: WikiApiQuery? = null
)

@Serializable
data class WikiApiQuery(
    val pages: List<WikiApiPage>
)

@Serializable
data class WikiApiPage(
    val title: String,
    val extract: String? = null,
    @SerialName(value = "pageid") val pageId: Int? = null,
    val thumbnail: WikiPhoto? = null,
    @SerialName(value = "original") val photo: WikiPhoto? = null,
    @SerialName(value = "description") val description: String? = null,
    @SerialName(value = "langlinks") val langs: List<WikiLang>? = null
)

@Serializable
data class WikiPhoto(
    val source: String,
    val width: Int,
    val height: Int
)

@Serializable
data class WikiLang(
    val lang: String,
    val title: String
)
