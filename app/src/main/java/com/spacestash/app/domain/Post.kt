package com.spacestash.app.domain

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Post(
    @DocumentId val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val imageUrl: String = "",
    val description: String = "",
    val likesCount: Int = 0,
    val likedBy: List<String> = emptyList(),
    @ServerTimestamp val timestamp: Date? = null
)