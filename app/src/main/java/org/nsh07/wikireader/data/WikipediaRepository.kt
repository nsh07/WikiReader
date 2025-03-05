package org.nsh07.wikireader.data

import android.util.Log
import org.nsh07.wikireader.network.FeedApiService
import org.nsh07.wikireader.network.WikipediaApiService
import java.time.LocalDate
import java.time.format.DateTimeFormatter

interface WikipediaRepository {
    suspend fun getSearchResult(query: String): WikiApiResponse
    suspend fun getRandomResult(): WikiApiResponse
    suspend fun getFeed(
        lang: String,
        date: String = LocalDate.now()
            .format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
    ): FeedApiResponse
}

class NetworkWikipediaRepository(
    private val wikipediaApiService: WikipediaApiService,
    private val feedApiService: FeedApiService
) : WikipediaRepository {
    override suspend fun getSearchResult(query: String): WikiApiResponse {
        return wikipediaApiService.getSearchResult(query)
    }

    override suspend fun getRandomResult(): WikiApiResponse {
        return wikipediaApiService.getRandomResult()
    }

    override suspend fun getFeed(
        lang: String, date: String
    ): FeedApiResponse {
        Log.d("FeedDate", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")))
        return feedApiService.getFeed(lang, date)
    }
}