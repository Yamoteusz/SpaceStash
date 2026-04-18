package com.spacestash.app.ui

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

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        fetchPosts()
    }

    private fun fetchPosts() {
        db.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val postList = snapshot.toObjects(Post::class.java)
                    _posts.value = postList
                }
            }
    }

    fun createPost(imageUri: Uri, description: String, authorName: String) {
        val user = auth.currentUser ?: return

        _isLoading.value = true

        val imageRef = storage.reference.child("post_images/${UUID.randomUUID()}.jpg")

        imageRef.putFile(imageUri).addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                val newPost = Post(
                    authorId = user.uid,
                    authorName = authorName,
                    imageUrl = downloadUrl.toString(),
                    description = description
                )

                db.collection("posts").add(newPost)
                    .addOnSuccessListener {
                        _isLoading.value = false
                    }
                    .addOnFailureListener {
                        _isLoading.value = false
                    }
            }.addOnFailureListener {
                _isLoading.value = false
            }
        }.addOnFailureListener {
            _isLoading.value = false
        }
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    fun toggleLike(postId: String, currentLikes: List<String>) {
        val userId = auth.currentUser?.uid ?: return
        val postRef = db.collection("posts").document(postId)

        if (currentLikes.contains(userId)) {
            postRef.update(
                "likedBy", com.google.firebase.firestore.FieldValue.arrayRemove(userId),
                "likesCount", com.google.firebase.firestore.FieldValue.increment(-1)
            )
        } else {
            postRef.update(
                "likedBy", com.google.firebase.firestore.FieldValue.arrayUnion(userId),
                "likesCount", com.google.firebase.firestore.FieldValue.increment(1)
            )
        }
    }
}