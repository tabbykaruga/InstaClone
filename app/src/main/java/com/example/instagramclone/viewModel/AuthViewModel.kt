package com.example.instagramclone.viewModel

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
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
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.toObject
import dagger.hilt.android.lifecycle.HiltViewModel
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
  val followers = mutableIntStateOf(0)

  // POST
  val refreshPostProgress = mutableStateOf(false)
  val posts = mutableStateOf<List<PostData>>(listOf())
  val comments = mutableStateOf<List<CommentsData>>(listOf())
  val commentsMap = mutableStateMapOf<String, List<CommentsData>>()
  val commentProgress = mutableStateOf(false)

  // SEARCH
  val searchedPosts = mutableStateOf<List<PostData>>(listOf())
  val searchedPostProgress = mutableStateOf(false)

  // FEED
  val postsFeed = mutableStateOf<List<PostData>>(listOf())
  val postsFeedProgress = mutableStateOf(false)

  init {
    val currentUser = auth.currentUser

    // checking if signed in
    signedIn.value = currentUser != null

    // get the userdata with uId
    currentUser?.uid?.let { uId -> getUserData(uId) }
  }

  fun onSignUp(username: String, email: String, password: String) {
    if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
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
        .addOnFailureListener { e ->
          handleException(e, "Failed to check username")
          inProgress.value = false
        }
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
    searchedPosts.value = listOf()
    postsFeed.value = listOf()
    comments.value = listOf()
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
          getFollowers(user?.userId)
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

  fun uploadProfileImage(name: String, userName: String, bio: String, uri: Uri) {
    inProgress.value = true

    ImageUploader.uploadImage(
        uri,
        onSuccess = { imageUrl ->
          createOrUpdateProfile(
              name = name,
              username = userName,
              bio = bio,
              imageUrl = imageUrl, // ⭐ Cloudinary URL saved to Firebase
          )
          userData.value = userData.value?.copy(imageUrl = imageUrl)
          inProgress.value = false
        },
        onError = {
          handleException(customMessage = "Image upload failed")
          inProgress.value = false
        },
    )
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

  fun onDeletePost(post: PostData, onSuccess: () -> Unit) {
    val currentUid = auth.currentUser?.uid

    if (currentUid == null || currentUid != post.userId) {
      handleException(customMessage = "Unauthorized to delete this post")
      return
    }

    val postId =
        post.postId
            ?: run {
              handleException(customMessage = "Invalid post")
              return
            }

    inProgress.value = true

    db.collection(POSTS)
        .document(postId)
        .delete()
        .addOnSuccessListener {
          posts.value = posts.value.filter { it.postId != postId }
          postsFeed.value = postsFeed.value.filter { it.postId != postId }

          popupNotification.value = Event("Post deleted successfully")
          inProgress.value = false
          onSuccess.invoke()
        }
        .addOnFailureListener { e ->
          handleException(e, "Unable to delete post")
          inProgress.value = false
        }
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

  private fun getFollowers(uId: String?) {
    db.collection(USERS).whereArrayContains("following", uId ?: "").get().addOnSuccessListener {
        documents ->
      followers.intValue = documents.size()
    }
  }

  // -------------------FEED -----------------

  private fun getPersonalizedFeed() {
    val following = userData.value?.following

    if (!following.isNullOrEmpty()) {
      postsFeedProgress.value = true
      db.collection(POSTS)
          .whereIn("userId", following)
          .get()
          .addOnSuccessListener {
            convertPosts(documents = it, postsFeed)
            changePostUsernames(postsFeed.value, postsFeed) {
              if (postsFeed.value.isEmpty()) getGeneralUserFeed()
              else postsFeedProgress.value = false
            }
          }
          .addOnFailureListener { e ->
            handleException(
                e,
                "Unable to get feed at the moment.Please try again later.",
            )
            postsFeedProgress.value = false
          }
    } else {
      getGeneralUserFeed()
    }
  }

  fun getGeneralUserFeed() {
    postsFeedProgress.value = true

    db.collection(POSTS)
        .orderBy("time", Query.Direction.DESCENDING)
        .limit(50)
        .get()
        .addOnSuccessListener { it ->
          convertPosts(it, postsFeed)

          changePostUsernames(postsFeed.value, postsFeed) {
            if (postsFeed.value.isEmpty()) {
              db.collection(POSTS)
                  .orderBy("time", Query.Direction.DESCENDING)
                  .limit(20)
                  .get()
                  .addOnSuccessListener { docs ->
                    convertPosts(docs, postsFeed)
                    changePostUsernames(postsFeed.value, postsFeed) {
                      postsFeedProgress.value = false
                    }
                  }
            } else {
              postsFeedProgress.value = false
            }
          }
        }
        .addOnFailureListener { e ->
          handleException(e, "Unable to get feed at the moment. Please try again later.")
          postsFeedProgress.value = false
        }
  }

  private fun changePostUsernames(
      posts: List<PostData>,
      outState: MutableState<List<PostData>>,
      onComplete: (() -> Unit)? = null,
  ) {
    val distinctUserIds = posts.mapNotNull { it.userId }.distinct()
    var completed = 0

    data class UserInfo(val username: String, val userImage: String)
    val userInfoMap = mutableMapOf<String, UserInfo>()

    if (distinctUserIds.isEmpty()) {
      onComplete?.invoke()
      return
    }

    distinctUserIds.forEach { userId ->
      db.collection(USERS)
          .document(userId)
          .get()
          .addOnSuccessListener { doc ->
            userInfoMap[userId] =
                UserInfo(
                    username = doc.getString("username") ?: "",
                    userImage = doc.getString("imageUrl") ?: "",
                )
            completed++

            if (completed == distinctUserIds.size) {
              outState.value =
                  posts.map { post ->
                    val info = userInfoMap[post.userId]
                    post.copy(
                        userName = info?.username ?: "",
                        userImage = info?.userImage ?: "",
                    )
                  }
              onComplete?.invoke()
            }
          }
          .addOnFailureListener {
            completed++
            if (completed == distinctUserIds.size) {
              outState.value = posts
              onComplete?.invoke()
            }
          }
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
        postsFeed.value.map { if (it.postId == postData.postId) it.copy(likes = newLikes) else it }
    postsFeed.value = updatedPosts

    // update firestore
    postData.postId?.let { postId ->
      db.collection(POSTS)
          .document(postId)
          .set(mapOf("likes" to newLikes), SetOptions.merge())
          .addOnFailureListener { e -> handleException(e, "Unable to like post") }
    }
  }

  // ---------------------------- COMMENTS -------------------------
  fun createComment(postId: String, comment: String) {
    userData.value?.userId?.let { userId ->
      val commentId = UUID.randomUUID().toString()
      val comment =
          CommentsData(
              commentId = commentId,
              postId = postId,
              userId = userId,
              comment = comment,
              timeStamp = System.currentTimeMillis(),
          )

      db.collection(COMMENTS)
          .document(commentId)
          .set(comment)
          .addOnSuccessListener {
            // get existing comments
            getComments(postId)
          }
          .addOnFailureListener { e ->
            handleException(e, "Cannot be able to add a comment.Please try again later")
          }
    }
  }

  fun getComments(postId: String?) {
    if (postId == null) return

    commentProgress.value = true
    db.collection(COMMENTS)
        .whereEqualTo("postId", postId)
        .get()
        .addOnSuccessListener { documents ->
          val newComments = documents.map { it.toObject<CommentsData>() }
          val sortedComments = newComments.sortedBy { it.timeStamp }

          if (sortedComments.isEmpty()) {
            commentsMap[postId] = emptyList()
            commentProgress.value = false
            return@addOnSuccessListener
          }

          val resolvedComments = mutableListOf<CommentsData>()
          var pendingCount = sortedComments.size // track when all fetches are done

          sortedComments.forEach { comment ->
            db.collection(USERS)
                .whereEqualTo("userId", comment.userId) // match userId in USERS
                .get()
                .addOnSuccessListener { userDocs ->
                  val user = userDocs.documents.firstOrNull()
                  val resolvedComment =
                      comment.copy(
                          userName = user?.getString("username") ?: "Unknown",
                          userImage = user?.getString("imageUrl") ?: "",
                      )
                  resolvedComments.add(resolvedComment)
                  pendingCount--

                  // Only update state when ALL comments are resolved
                  if (pendingCount == 0) {
                    val sorted = resolvedComments.sortedBy { it.timeStamp }
                    commentsMap[postId] = sorted
                    comments.value = sorted
                    commentProgress.value = false
                  }
                }
                .addOnFailureListener {
                  // Still add the comment even if user fetch fails
                  resolvedComments.add(comment)
                  pendingCount--
                  if (pendingCount == 0) {
                    val sorted = resolvedComments.sortedBy { it.timeStamp }
                    commentsMap[postId] = sorted
                    comments.value = sorted
                    commentProgress.value = false
                  }
                }
          }
        }
        .addOnFailureListener { e ->
          handleException(e, "Unable to fetch comments. Please try again later.")
          commentProgress.value = false
        }
  }
}
