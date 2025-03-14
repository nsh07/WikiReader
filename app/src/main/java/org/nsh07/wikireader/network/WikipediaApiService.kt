package org.nsh07.wikireader.network

import org.nsh07.wikireader.data.FeedApiResponse
import org.nsh07.wikireader.data.WikiApiResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

private const val API_QUERY =
    "w/api.php?format=json&action=query&prop=pageimages|pageterms|langlinks&piprop=original&pilicense=any&lllimit=max&generator=search&gsrlimit=1&redirects=1&formatversion=2"

private const val CONTENT_QUERY =
    "wiki/{title}?action=raw"

private const val RANDOM_QUERY =
    "w/api.php?format=json&action=query&prop=pageimages|pageterms|langlinks&piprop=original&pilicense=any&lllimit=max&generator=random&redirects=1&formatversion=2&grnnamespace=0"

private const val FEED_QUERY =
    "api/rest_v1/feed/featured/{date}"

interface WikipediaApiService {
    @GET(API_QUERY)
    suspend fun getSearchResult(@Query("gsrsearch") query: String): WikiApiResponse

    @GET(CONTENT_QUERY)
    suspend fun getPageContent(@Path("title", encoded = true) title: String): String

    @GET(RANDOM_QUERY)
    suspend fun getRandomResult(): WikiApiResponse

    @GET(FEED_QUERY)
    suspend fun getFeed(
        @Path("date", encoded = true) date: String,
    ): FeedApiResponse
}
