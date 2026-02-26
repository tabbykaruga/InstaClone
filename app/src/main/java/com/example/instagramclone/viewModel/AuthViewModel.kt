package com.example.instagramclone.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.instagramclone.data.Event
import com.example.instagramclone.data.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

const val USERS = "users"

@HiltViewModel
class AuthViewModel
@Inject
constructor(
    val auth: FirebaseAuth,
    val db: FirebaseFirestore,
    val storage: FirebaseStorage,
) : ViewModel() {
  val signedIn = mutableStateOf(false)
  val inProgress = mutableStateOf(false)
  val userData = mutableStateOf<UserData?>(null)
  val popupNotification = mutableStateOf<Event<String>?>(null)

  init {
    //    auth.signOut()
    val currentUser = auth.currentUser

    // checking if signed in
    signedIn.value = currentUser != null

    // get the userdata with uId
    currentUser?.uid?.let { uId -> getUserData(uId) }
  }

  fun onSignUp(username: String, email: String, password: String) {
    if (username.isEmpty() or email.isEmpty() or password.isEmpty()) {
      handleException(customMessage = "Please fill in all fields")
      return
    }

    // CHECK IF USERNAME IS UNIQUE
    inProgress.value = true

    db.collection(USERS)
        .whereEqualTo("username", username)
        .get()
        .addOnSuccessListener { documents ->
          if (documents.size() > 0) {
            handleException(customMessage = "Username Already exist")
            inProgress.value = false
          } else {
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
              if (task.isSuccessful) {
                signedIn.value = true

                // CREATE PROFILE
                createOrUpdateProfile(username = username)
              } else {
                handleException(task.exception, "Sign Up Failed")
              }
              inProgress.value = false
            }
          }
        }
        .addOnFailureListener {}
  }

  fun onLogin(email: String, password: String) {
    if (email.isEmpty() or password.isEmpty()) {
      handleException(customMessage = "Please fill in all fields")
      return
    }

    inProgress.value = true

    auth
        .signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
          if (task.isSuccessful) {
            signedIn.value = true
            inProgress.value = false
            auth.currentUser?.uid?.let { uId -> getUserData(uId) }
          } else {
            handleException(task.exception, "Login failed")
            inProgress.value = false
          }
        }
        .addOnFailureListener { e ->
          handleException(e, "Login Failed")
          inProgress.value = false
        }
  }

  private fun createOrUpdateProfile(
      name: String? = null,
      username: String? = null,
      bio: String? = null,
      imageUrl: String? = null,
  ) {
    val uId = auth.currentUser?.uid
    val userData =
        UserData(
            userId = uId,
            name = name ?: userData.value?.name,
            username = username ?: userData.value?.username,
            bio = bio ?: userData.value?.bio,
            imageUrl = imageUrl ?: userData.value?.imageUrl,
            following = userData.value?.following,
        )

    // checking if user exist
    uId?.let { uId ->
      inProgress.value = true
      db.collection(USERS)
          .document(uId)
          .get()
          .addOnSuccessListener {
            if (it.exists()) {
              it.reference
                  .update(userData.toMap())
                  .addOnSuccessListener {
                    this.userData.value = userData
                    inProgress.value = false
                  }
                  .addOnFailureListener {
                    handleException(it, "Cannot Update user")
                    inProgress.value = false
                  }
            } else {
              db.collection(USERS).document(uId).set(userData)

              // update the igViewModel
              getUserData(uId)

              inProgress.value = false
            }
          }
          .addOnFailureListener { e ->
            handleException(e, "Cannot create user")
            inProgress.value = false
          }
    }
  }

  private fun getUserData(uId: String) {
    inProgress.value = true

    db.collection(USERS)
        .document(uId)
        .get()
        .addOnSuccessListener {
          val user = it.toObject<UserData>()
          userData.value = user
          inProgress.value = false
        }
        .addOnFailureListener { e ->
          handleException(e, "Cannot Retrieve user data")
          inProgress.value = false
        }
  }

  fun handleException(exception: Exception? = null, customMessage: String = "") {
    exception?.printStackTrace()

    val errorMsg = exception?.localizedMessage ?: ""
    val message = if (customMessage.isEmpty()) errorMsg else "$customMessage: $errorMsg"
    popupNotification.value = Event(message)
  }
}
