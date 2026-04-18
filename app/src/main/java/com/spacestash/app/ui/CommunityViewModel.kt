package com.spacestash.app.ui // Upewnij się, że to Twój poprawny pakiet

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.spacestash.app.domain.Post
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

class CommunityViewModel : ViewModel() {

    // Podłączamy narzędzia od Google
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Stan trzymający listę postów (UI będzie go obserwować)
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    // Stan ładowania (żeby pokazać kręcące się kółko podczas wysyłania)
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        // Przy starcie od razu pobieramy posty
        fetchPosts()
    }

    private fun fetchPosts() {
        // Pobieramy kolekcję "posts", sortujemy od najnowszych
        db.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("Błąd pobierania postów: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    // Magicznie zamieniamy dane z serwera na naszą listę obiektów Post
                    val postList = snapshot.toObjects(Post::class.java)
                    _posts.value = postList
                }
            }
    }

    // Funkcja do publikowania nowego wpisu (Wersja Diagnostyczna)
    fun createPost(imageUri: Uri, description: String, authorName: String) {
        val user = auth.currentUser
        if (user == null) {
            println("FIREBASE_LOG: BŁĄD - Użytkownik nie jest zalogowany! Przerywam.")
            return
        }

        println("FIREBASE_LOG: Startujemy! Użytkownik zalogowany jako: ${user.uid}")
        _isLoading.value = true

        val imageRef = storage.reference.child("post_images/${UUID.randomUUID()}.jpg")

        println("FIREBASE_LOG: Próbuję wysłać zdjęcie na serwer...")
        imageRef.putFile(imageUri).addOnSuccessListener {
            println("FIREBASE_LOG: SUKCES! Zdjęcie fizycznie wylądowało w chmurze.")

            imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                println("FIREBASE_LOG: SUKCES! Uzyskano link do zdjęcia: $downloadUrl")

                val newPost = Post(
                    authorId = user.uid,
                    authorName = authorName,
                    imageUrl = downloadUrl.toString(),
                    description = description
                )

                println("FIREBASE_LOG: Próbuję zapisać post w bazie Firestore...")
                db.collection("posts").add(newPost)
                    .addOnSuccessListener {
                        println("FIREBASE_LOG: PEŁNY SUKCES! Post dodany do bazy.")
                        _isLoading.value = false
                    }
                    .addOnFailureListener { e ->
                        println("FIREBASE_LOG: BŁĄD FIRESTORE (Baza): ${e.message}")
                        _isLoading.value = false
                    }
            }.addOnFailureListener { e ->
                println("FIREBASE_LOG: BŁĄD POBIERANIA LINKU: ${e.message}")
                _isLoading.value = false
            }
        }.addOnFailureListener { e ->
            println("FIREBASE_LOG: BŁĄD STORAGE (Zdjęcie): ${e.message}")
            _isLoading.value = false
        }
    }

    // Funkcja do pobrania ID aktualnego użytkownika (żeby UI wiedziało, czy serduszko ma być pełne)
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    // Funkcja do klikania lajków
    fun toggleLike(postId: String, currentLikes: List<String>) {
        val userId = auth.currentUser?.uid ?: return
        val postRef = db.collection("posts").document(postId)

        if (currentLikes.contains(userId)) {
            // Użytkownik już dał lajka -> zabieramy go
            postRef.update(
                "likedBy", com.google.firebase.firestore.FieldValue.arrayRemove(userId),
                "likesCount", com.google.firebase.firestore.FieldValue.increment(-1)
            )
        } else {
            // Użytkownik nie dał lajka -> dodajemy go
            postRef.update(
                "likedBy", com.google.firebase.firestore.FieldValue.arrayUnion(userId),
                "likesCount", com.google.firebase.firestore.FieldValue.increment(1)
            )
        }
    }
}