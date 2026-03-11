package com.example.instagramclone.viewModel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.example.instagramclone.data.CommentsData
import com.example.instagramclone.data.Event
import com.example.instagramclone.data.PostData
import com.example.instagramclone.data.UserData
import com.example.instagramclone.sharedUtils.ImageUploader
import com.example.instagramclone.sharedUtils.fillerWords
import com.example.instagramclone.sharedUtils.handleException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.toObject
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

const val USERS = "users"
const val POSTS = "posts"
const val COMMENTS = "comments"

@HiltViewModel
class AuthViewModel
@Inject
constructor(val auth: FirebaseAuth, val db: FirebaseFirestore, application: Application) :
    AndroidViewModel(application) {
  val signedIn = mutableStateOf(false)
  val inProgress = mutableStateOf(false)
  val userData = mutableStateOf<UserData?>(null)
  val popupNotification = mutableStateOf<Event<String>?>(null)

  // POST
  val refreshPostProgress = mutableStateOf(false)
  val posts = mutableStateOf<List<PostData>>(listOf())
  val comment = mutableStateOf<List<CommentsData>>(listOf())
  val commentProgress = mutableStateOf(false)

  // SEARCH
  val searchedPosts = mutableStateOf<List<PostData>>(listOf())
  val searchedPostProgress = mutableStateOf(false)

  // FEED
  //  val postFeed = mutableStateOf<List<PostData>>(listOf())
  private val _userFeed = mutableStateOf<List<PostData>>(emptyList())
  val userFeed: State<List<PostData>> = _userFeed
  val postFeedProgress = mutableStateOf(false)

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
    signedIn.value = false
    userData.value = null
    popupNotification.value = Event("Logged Out")
    searchedPosts.value = emptyList()
    _userFeed.value = emptyList()
    comment.value = listOf()
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
                  .addOnFailureListener { e ->
                    handleException(e, "Cannot Update user")
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
          // refresh post
          refreshPost()
          // update feed a new due to following
          getPersonalizedFeed()
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

  fun uploadProfileImageAndSave(name: String, userName: String, bio: String, uri: Uri) {
    val userId = auth.currentUser?.uid ?: return
    inProgress.value = true
    val localPath = saveImageLocalStorage(uri, userId)
    if (localPath != null) {
      val imageLoader = coil.Coil.imageLoader(getApplication())
      imageLoader.memoryCache?.clear()
      createOrUpdateProfile(
          name = name,
          username = userName,
          bio = bio,
          imageUrl = localPath,
      )
      userData.value = userData.value?.copy(imageUrl = localPath)
    } else {
      handleException(null, "Failed to save image locally")
      inProgress.value = false
    }
  }

  private fun uriBitmap(uri: Uri): Bitmap {
    val appContext = getApplication<Application>().applicationContext
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
      ImageDecoder.decodeBitmap(ImageDecoder.createSource(appContext.contentResolver, uri))
    } else {
      @Suppress("DEPRECATION") MediaStore.Images.Media.getBitmap(appContext.contentResolver, uri)
    }
  }

  private fun saveImageLocalStorage(uri: Uri, userId: String): String? {
    return try {
      val appContext = getApplication<Application>().applicationContext
      val bitmap = uriBitmap(uri) // clean, no deprecation warning

      val filename = "profile_$userId.jpg"
      val file = File(appContext.filesDir, filename)

      if (file.exists()) file.delete()

      FileOutputStream(file).use { stream ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
      }

      file.absolutePath
    } catch (e: Exception) {
      handleException(e, "Failed to save image")
      null
    }
  }

  // ----------------- POST --------------

  fun onCreateNewPost(
      imageUri: Uri,
      description: String,
      location: String,
      onPostSuccess: () -> Unit,
  ) {
    inProgress.value = true
    ImageUploader.uploadImage(
        imageUri,
        onSuccess = { imageUrl ->
          val currentUid = auth.currentUser?.uid
          val currentUsername = userData.value?.username
          val currentUserImage = userData.value?.imageUrl

          if (currentUid != null) {

            val postUuid = UUID.randomUUID().toString()

            val searchTerms =
                description
                    .split(" ", ".", ",", "?", "!", "#")
                    .map { it.lowercase() }
                    .filter { it.isNotEmpty() and !fillerWords.contains(it) }

            val post =
                PostData(
                    postId = postUuid,
                    userId = currentUid,
                    userName = currentUsername,
                    userImage = currentUserImage,
                    postImage = imageUrl, // ⭐ CLOUDINARY URL
                    postDescription = description,
                    postLocation = location,
                    time = System.currentTimeMillis(),
                    likes = listOf(),
                    searchTerms = searchTerms,
                )

            db.collection(POSTS)
                .document(postUuid)
                .set(post)
                .addOnSuccessListener {
                  popupNotification.value = Event("Post successfully created")
                  inProgress.value = false
                  refreshPost()
                  onPostSuccess.invoke()
                }
                .addOnFailureListener { e ->
                  handleException(e, "Unable to create a post")
                  inProgress.value = false
                }
          } else {

            handleException(customMessage = "User not logged in")
            inProgress.value = false
          }
        },
        onError = {
          handleException(customMessage = "Image upload failed")
          inProgress.value = false
        },
    )
  }

  private fun refreshPost() {
    val currentUid = auth.currentUser?.uid
    if (currentUid != null) {
      refreshPostProgress.value = true

      db.collection(POSTS)
          .whereEqualTo("userId", currentUid)
          .get()
          .addOnSuccessListener { documents ->
            convertPosts(documents, posts)
            refreshPostProgress.value = false
          }
          .addOnFailureListener { e ->
            handleException(e, "Cannot fetch Post")
            refreshPostProgress.value = false
          }
    } else {
      handleException(customMessage = "Error :Username unavailable.Unable to create a new post")
      onLogOut()
    }
  }

  // Build posts on top of each other
  private fun convertPosts(documents: QuerySnapshot, outState: MutableState<List<PostData>>) {
    val newPosts = mutableListOf<PostData>()

    // add on to the previous list
    documents.forEach { doc ->
      val post = doc.toObject<PostData>()
      newPosts.add(post)
    }

    val sortedPosts = newPosts.sortedByDescending { it.time }
    outState.value = sortedPosts
  }

  fun searchPost(searchTerm: String) {
    if (searchTerm.isNotEmpty()) {
      searchedPostProgress.value = true

      db.collection(POSTS)
          .whereArrayContains("searchTerms", searchTerm.trim().lowercase(Locale.ROOT))
          .get()
          .addOnSuccessListener {
            convertPosts(it, searchedPosts)
            searchedPostProgress.value = false
          }
          .addOnFailureListener { e ->
            handleException(e, "Cannot search post")
            searchedPostProgress.value = false
          }
    }
  }

  // -------------------FOLLOWING /FOLLOWERS-----------------
  fun onFollowClick(userId: String) {
    auth.currentUser?.uid?.let { currentUser ->
      val following = arrayListOf<String>()
      userData.value?.following?.let { following.addAll(it) }
      if (following.contains(userId)) {
        following.remove(userId)
      } else {
        following.add(userId)
      }

      db.collection(USERS)
          .document(currentUser)
          .update("following", following)
          .addOnSuccessListener { getUserData(currentUser) }
          .addOnFailureListener { e ->
            handleException(e, "Unable to follow/Unfollow. Please try again later.")
          }
    }
  }

  // -------------------FEED -----------------

  private fun getPersonalizedFeed() {
    val following = userData.value?.following

    if (!following.isNullOrEmpty()) {
      postFeedProgress.value = true
      db.collection(POSTS)
          .whereIn("userId", following)
          .get()
          .addOnSuccessListener {
            convertPosts(documents = it, _userFeed)
            if (userFeed.value.isEmpty()) {
              getGeneralUserFeed()
            } else {
              postFeedProgress.value = false
            }
          }
          .addOnFailureListener { e ->
            handleException(
                e,
                "Unable to get feed at the moment.Please try again later.",
            )
            postFeedProgress.value = false
          }
    } else {
      getGeneralUserFeed()
    }
  }

  fun getGeneralUserFeed() {
    postFeedProgress.value = true

    // limit to last day not the whole db
    val currentTime = System.currentTimeMillis()
    val difference = 24 * 60 * 60 * 1000 // 1 day in milli sec

    db.collection(POSTS)
        .whereGreaterThan("time", currentTime - difference)
        .get()
        .addOnSuccessListener {
          convertPosts(documents = it, _userFeed)
          postFeedProgress.value = false
        }
        .addOnFailureListener { e ->
          handleException(
              e,
              "Unable to get feed at the moment.Please try again later.",
          )
          postFeedProgress.value = false
        }
  }

  fun onLikePost(postData: PostData) {
    val userId = auth.currentUser?.uid ?: return
    val currentLikes = postData.likes?.toMutableList() ?: mutableListOf()

    val newLikes =
        if (currentLikes.contains(userId)) {
          currentLikes.filter { it != userId }.toMutableList() // unlike
        } else {
          currentLikes.apply { add(userId) } // like
        }

    // update local state immediately
    val updatedPosts =
        _userFeed.value.map { if (it.postId == postData.postId) it.copy(likes = newLikes) else it }
    _userFeed.value = updatedPosts

    // update firestore
    postData.postId?.let { postId ->
      db.collection(POSTS)
          .document(postId)
          .set(mapOf("likes" to newLikes), SetOptions.merge())
          .addOnFailureListener { e -> handleException(e, "Unable to like post") }
    }
  }

  fun createComment(postId: String, comment: String) {
    userData.value?.username?.let { username ->
      val commentId = UUID.randomUUID().toString()
      val comment =
          CommentsData(
              commentId = commentId,
              postId = postId,
              username = username,
              comment = comment,
              time = System.currentTimeMillis(),
          )

      db.collection(COMMENTS)
          .document(commentId)
          .set(comment)
          .addOnSuccessListener {
            // get existing comments
            getPreviousComments()
          }
          .addOnFailureListener { e ->
            handleException(e, "Cannot be able to add a comment.Please try again later")
          }
    }
  }

  fun getPreviousComments() {}
}
