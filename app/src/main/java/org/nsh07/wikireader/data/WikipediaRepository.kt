package org.nsh07.wikireader.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.nsh07.wikireader.network.WikipediaApiService
import java.time.LocalDate
import java.time.format.DateTimeFormatter

interface WikipediaRepository {
    suspend fun getSearchResult(query: String): WikiApiResponse
    suspend fun getPageContent(title: String): String
    suspend fun getRandomResult(): WikiApiResponse
    suspend fun getFeed(
        date: String = LocalDate.now()
            .format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
    ): FeedApiResponse
}

class NetworkWikipediaRepository(
    private val wikipediaApiService: WikipediaApiService,
    private val wikipediaPageApiService: WikipediaApiService,
    private val ioDispatcher: CoroutineDispatcher
) : WikipediaRepository {
    override suspend fun getSearchResult(query: String): WikiApiResponse =
        withContext(ioDispatcher) {
            return@withContext wikipediaApiService.getSearchResult(query)
        }

    override suspend fun getPageContent(title: String): String =
        withContext(ioDispatcher) {
            return@withContext wikipediaPageApiService.getPageContent(title)
        }

    override suspend fun getRandomResult(): WikiApiResponse =
        withContext(ioDispatcher) {
            wikipediaApiService.getRandomResult()
        }

    override suspend fun getFeed(
        date: String
    ): FeedApiResponse =
        withContext(ioDispatcher) {
            wikipediaApiService.getFeed(date)
        }
}