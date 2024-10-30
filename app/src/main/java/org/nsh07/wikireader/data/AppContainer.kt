package org.nsh07.wikireader.data

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import org.nsh07.wikireader.network.WikipediaApiService
import retrofit2.Retrofit

interface AppContainer {
    val wikipediaRepository: WikipediaRepository
}

class DefaultAppContainer : AppContainer {
    private val baseUrl = "https://en.wikipedia.org"
    private val json = Json { ignoreUnknownKeys = true }

    private val retrofit = Retrofit.Builder()
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .baseUrl(baseUrl)
        .build()

    private val retrofitService: WikipediaApiService by lazy {
        retrofit.create(WikipediaApiService::class.java)
    }

    override val wikipediaRepository: WikipediaRepository by lazy {
        NetworkWikipediaRepository(retrofitService)
    }
}