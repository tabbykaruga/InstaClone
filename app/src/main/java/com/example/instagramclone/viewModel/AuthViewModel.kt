package com.example.instagramclone.viewModel

import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.instagramclone.data.Event
import com.example.instagramclone.data.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
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

  fun onLogOut() {
    auth.signOut()
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

  fun updateProfileData(
      name: String,
      userName: String,
      bio: String,
  ) {
    createOrUpdateProfile(name = name, username = userName, bio = bio)
  }

  private fun uploadImage(uri: Uri, onSuccess: (Uri) -> Unit) {
    inProgress.value = true
    val storageRef = storage.reference
    val uuid = UUID.randomUUID()
    val imageRef = storageRef.child("images/$uuid")
    val uploadTask = imageRef.putFile(uri)

    uploadTask
        .addOnSuccessListener {
          val result = it.metadata?.reference?.downloadUrl
          result?.addOnSuccessListener { onSuccess }
        }
        .addOnFailureListener { e ->
          handleException(e, "")
          inProgress.value = false
        }
  }

  fun uploadProfileImage(
      uri: Uri,
  ) {
    uploadImage(uri) { createOrUpdateProfile(imageUrl = it.toString()) }
  }

  fun handleException(exception: Exception? = null, customMessage: String = "") {
    exception?.printStackTrace()

    val errorMsg = exception?.localizedMessage ?: ""
    val message = if (customMessage.isEmpty()) errorMsg else "$customMessage: $errorMsg"
    popupNotification.value = Event(message)
  }
}
