package com.spacestash.app.data

import com.spacestash.app.domain.ApodResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface NasaApiService {
    // Adres API, który zwraca Zdjęcie Dnia z NASA
    @GET("planetary/apod")
    suspend fun getApod(
        @Query("api_key") apiKey: String
    ): ApodResponse
}