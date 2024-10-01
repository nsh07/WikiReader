package org.nsh07.wikireader.network

import org.nsh07.wikireader.data.WikiApiResponse
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query

private const val BASE_URL = "https://en.wikipedia.org"
private const val API_QUERY =
    "w/api.php?format=json&action=query&prop=extracts|pageimages|pageterms&piprop=original&pilicense=any&explaintext&generator=search&gsrlimit=1&redirects=1&formatversion=2"
private val json = Json { ignoreUnknownKeys = true }

private val retrofit = Retrofit.Builder()
    .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
    .baseUrl(BASE_URL)
    .build()

interface WikipediaApiService {
    @GET(API_QUERY)
    suspend fun searchWikipedia(@Query("gsrsearch") query: String): WikiApiResponse
}

object WikipediaApi {
    val retrofitService: WikipediaApiService by lazy {
        retrofit.create(WikipediaApiService::class.java)
    }
}
