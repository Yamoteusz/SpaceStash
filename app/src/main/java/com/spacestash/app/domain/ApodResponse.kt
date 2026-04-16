package com.spacestash.app.domain

import com.google.gson.annotations.SerializedName

data class ApodResponse(
    val title: String,
    val explanation: String,
    val url: String,
    val date: String,
    @SerializedName("media_type") val mediaType: String
)