package com.example.instagramclone.routes

sealed class DestinationScreen(val route: String) {
  object Signup : DestinationScreen("signup")

  object Login : DestinationScreen("login")

  object Feed : DestinationScreen("feed")

  object Search : DestinationScreen("search")

  object MyPost : DestinationScreen("myPost")

  object Profile : DestinationScreen("profile")

  object NewPost : DestinationScreen("newPost/{imageUri}") {
    fun createRoute(uri: String) = "newPost/$uri"
  }

  object SinglePost : DestinationScreen("singlePost")

  object Comment : DestinationScreen("comments/{postId}") {
    fun createRoute(postId: String) = "comments/$postId"
  }
}
