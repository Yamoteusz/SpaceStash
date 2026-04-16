package com.spacestash.app.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Klient API (Konfiguracja Retrofit)
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

// Repozytorium (Pośrednik do pobierania danych)
class NasaRepository {
    private val apiService = ApiClient.retrofitService

    suspend fun getPictureOfTheDay(apiKey: String) = apiService.getApod(apiKey)
}