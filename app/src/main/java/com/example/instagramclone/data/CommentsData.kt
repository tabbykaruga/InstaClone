package com.example.instagramclone.data

import com.google.firebase.firestore.Exclude

data class CommentsData(
    val commentId: String? = null,
    val postId: String? = null,
    val userId: String? = null,
    val comment: String? = null,
    val timeStamp: Long? = null,
    @Exclude val userName: String = "",
    @Exclude val userImage: String = "",
)
