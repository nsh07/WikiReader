package org.nsh07.wikireader.network

import org.nsh07.wikireader.data.FeedApiResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface FeedApiService {
    @GET("feed/v1/wikipedia/{lang}/featured/{date}")
    suspend fun getFeed(
        @Path("lang", encoded = true) lang: String,
        @Path("date", encoded = true) date: String,
    ): FeedApiResponse
}