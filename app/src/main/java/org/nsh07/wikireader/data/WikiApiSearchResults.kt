package org.nsh07.wikireader.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WikiApiSearchResults(
    val query: WikiSearchResultsQuery
)

@Serializable
data class WikiSearchResultsQuery(
    @SerialName("searchinfo") val searchInfo: WikiSearchInfo,
    val pages: List<WikiSearchResult> = emptyList()
)

@Serializable
data class WikiSearchInfo(
    @SerialName("totalhits") val totalHits: Int
)

@Serializable
data class WikiSearchResult(
    val ns: Int = 0,
    val title: String,
    @SerialName("titlesnippet") val titleSnippet: String,
    @SerialName("pageid") val pageId: Int,
    val snippet: String,
    val index: Int,
    @SerialName("redirecttitle") val redirectTitle: String? = null,
    val thumbnail: WikiPhoto? = null
)