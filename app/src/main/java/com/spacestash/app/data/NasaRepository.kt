package com.spacestash.app.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_URL = "https://api.nasa.gov/"

    val retrofitService: NasaApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NasaApiService::class.java)
    }
}

class NasaRepository {
    private val apiService = ApiClient.retrofitService

    suspend fun getPictureOfTheDay(apiKey: String) = apiService.getApod(apiKey)
}