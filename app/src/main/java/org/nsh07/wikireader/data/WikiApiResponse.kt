package org.nsh07.wikireader.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WikiApiResponse(
    val query: WikiApiQuery? = null
)

@Serializable
data class WikiApiQuery(
    val pages: List<WikiApiPage>
)

@Serializable
data class WikiApiPage(
    val title: String,
    val extract: String,
    @SerialName(value = "original") val photo: WikiPhoto? = null,
    @SerialName(value = "terms") val photoDesc: WikiPhotoDesc
)

@Serializable
data class WikiPhoto(
    val source: String,
    val width: Int,
    val height: Int
)

@Serializable
data class WikiPhotoDesc(
    val label: List<String>,
    val description: List<String>
)
