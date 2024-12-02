package org.nsh07.wikireader.network

import org.nsh07.wikireader.data.WikiApiResponse
import retrofit2.http.GET
import retrofit2.http.Query

private const val API_QUERY =
    "w/api.php?format=json&action=query&prop=extracts|pageimages|pageterms|langlinks&piprop=original&pilicense=any&lllimit=max&explaintext&generator=search&gsrlimit=1&redirects=1&formatversion=2"

private const val RANDOM_QUERY =
    "w/api.php?format=json&action=query&prop=extracts|pageimages|pageterms|langlinks&piprop=original&pilicense=any&lllimit=max&explaintext&generator=random&redirects=1&formatversion=2&grnnamespace=0"

interface WikipediaApiService {
    @GET(API_QUERY)
    suspend fun getSearchResult(@Query("gsrsearch") query: String): WikiApiResponse

    @GET(RANDOM_QUERY)
    suspend fun getRandomResult(): WikiApiResponse
}
