// package com.example.instagramclone.viewModel
//
// import android.app.Application
// import android.net.Uri
// import androidx.compose.runtime.mutableStateOf
// import androidx.lifecycle.AndroidViewModel
// import com.example.instagramclone.data.Event
// import com.example.instagramclone.data.UserData
// import com.google.firebase.auth.FirebaseAuth
// import com.google.firebase.firestore.FirebaseFirestore
// import dagger.hilt.android.lifecycle.HiltViewModel
// import javax.inject.Inject
//
// const val POSTS = "posts"
//
// @HiltViewModel
// class NewPostViewModel
// @Inject
// constructor(val auth: FirebaseAuth, val db: FirebaseFirestore, application: Application) :
//    AndroidViewModel(application) {
//  val signedIn = mutableStateOf(false)
//  val inProgress = mutableStateOf(false)
//  val userData = mutableStateOf<UserData?>(null)
//  val popupNotification = mutableStateOf<Event<String>?>(null)
//
//  init {
//    val currentUser = auth.currentUser
//  }
//
//  fun onNewPost(uri: Uri, description: String, location: String, onPostSuccess: () -> Unit) {
//    inProgress.value = true
//    val currentUid = auth.currentUser?.uid
//  }
// }
