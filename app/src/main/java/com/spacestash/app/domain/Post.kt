package com.spacestash.app.domain

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

// data class to taka "foremka" na nasze dane
data class Post(
    @DocumentId val id: String = "", // Firebase sam wstrzyknie tu unikalne ID dokumentu
    val authorId: String = "",       // ID użytkownika z Firebase Auth (żeby wiedzieć, czyj to post)
    val authorName: String = "",     // Imię z DataStore, żeby ładnie wyglądało nad zdjęciem
    val imageUrl: String = "",       // Link do zdjęcia, które wyślemy na Storage
    val description: String = "",    // Opis / notatka do zdjęcia
    val likesCount: Int = 0,         // Licznik serduszek
    val likedBy: List<String> = emptyList(), // Lista ID osób, które dały lajka (żeby nikt nie lajkował 100 razy)
    @ServerTimestamp val timestamp: Date? = null // Firebase sam podstawi dokładny czas serwera!
)