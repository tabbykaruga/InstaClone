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
import com.google.firebase.firestore.QuerySnapshot
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
  val onFollowProgress = mutableStateOf(false)

  // POST
  val refreshPostProgress = mutableStateOf(false)
  val posts = mutableStateOf<List<PostData>>(listOf())
  val comments = mutableStateOf<List<CommentsData>>(listOf())
  val commentsMap = mutableStateMapOf<String, List<CommentsData>>()
  val commentProgress = mutableStateOf(false)

  // SEARCH
  val searchedPosts = mutableStateOf<List<PostData>>(listOf())
  val searchedPostProgress = mutableStateOf(false)
  var randomPosts = mutableStateOf<List<PostData>>(emptyList())
  var randomPostsProgress = mutableStateOf(false)

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

    // CHECK IF USERNAME IS UNIQUE
    inProgress.value = true

    db.collection(USERS)
        .whereEqualTo("username", username)
        .get()
        .addOnSuccessListener { documents ->
          if (documents.size() > 0) {
            handleException(
                popupNotification,
                null,
                customMessage = "Username Already exist",
            )
            inProgress.value = false
          } else {
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
              if (task.isSuccessful) {
                signedIn.value = true

                // CREATE PROFILE
                createOrUpdateProfile(username = username)
              } else {
                handleException(popupNotification, task.exception, "Sign Up Failed")
              }
              inProgress.value = false
            }
          }
        }
        .addOnFailureListener { e ->
          handleException(popupNotification, e, "Failed to check username")
          inProgress.value = false
        }
  }

  fun onLogin(email: String, password: String) {
    inProgress.value = true
    auth
        .signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
          if (task.isSuccessful) {
            signedIn.value = true
            inProgress.value = false
            auth.currentUser?.uid?.let { uId -> getUserData(uId) }
          } else {
            handleException(popupNotification, task.exception, "Login failed")
            inProgress.value = false
          }
        }
        .addOnFailureListener { e ->
          handleException(popupNotification, e, "Login Failed")
          inProgress.value = false
        }
  }

  fun onPasswordMismatch() {
    handleException(popupNotification, customMessage = "Passwords do not match")
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

  fun getUserById(userId: String, onResult: (UserData?) -> Unit) {
    db.collection(USERS)
        .document(userId)
        .get()
        .addOnSuccessListener { document ->
          val user = document.toObject(UserData::class.java)
          onResult(user)
        }
        .addOnFailureListener { onResult(null) }
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
                    handleException(popupNotification, e, "Cannot Update user")
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
            handleException(popupNotification, e, "Cannot create user")
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
          handleException(popupNotification, e, "Cannot Retrieve user data")
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
              imageUrl = imageUrl,
          )
          userData.value = userData.value?.copy(imageUrl = imageUrl)
          inProgress.value = false
        },
        onError = {
          handleException(popupNotification, customMessage = "Profile Image upload failed")
          createOrUpdateProfile(
              name = name,
              username = userName,
              bio = bio,
              imageUrl = userData.value?.imageUrl,
          )
          inProgress.value = false
        },
    )
  }

  // ----------------- POST --------------
  fun refreshFeed() {
    postsFeedProgress.value = true
    getPersonalizedFeed() // your existing feed function
  }

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
            val username = userData.value?.username?.lowercase() ?: ""
            val name = userData.value?.name?.lowercase() ?: ""

            val searchTerms =
                description
                    .split(" ", ".", ",", "?", "!", "#")
                    .map { it.lowercase() }
                    .filter { it.isNotEmpty() and !fillerWords.contains(it) }
                    .toMutableList()
                    .apply {
                      if (username.isNotEmpty()) add(username)
                      if (name.isNotEmpty()) addAll(name.split(" "))
                    }

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
                  handleException(popupNotification, e, "Unable to create a post")
                  inProgress.value = false
                }
          } else {

            handleException(popupNotification, customMessage = "User not logged in")
            inProgress.value = false
          }
        },
        onError = {
          handleException(popupNotification, customMessage = "Image upload failed")
          inProgress.value = false
        },
    )
  }

  fun onDeletePost(post: PostData, onSuccess: () -> Unit) {
    val currentUid = auth.currentUser?.uid

    if (currentUid == null || currentUid != post.userId) {
      handleException(popupNotification, customMessage = "Unauthorized to delete this post")
      return
    }

    val postId =
        post.postId
            ?: run {
              handleException(popupNotification, customMessage = "Invalid post")
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
          handleException(popupNotification, e, "Unable to delete post")
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
            handleException(popupNotification, e, "Cannot fetch Post")
            refreshPostProgress.value = false
          }
    } else {
      handleException(
          popupNotification,
          customMessage = "Error :Username unavailable.Unable to create a new post",
      )
      onLogOut()
    }
  }

  fun getRandomPosts() {
    randomPostsProgress.value = true
    val currentUserId = auth.currentUser?.uid

    db.collection(POSTS)
        .get()
        .addOnSuccessListener { documents ->
          val allPosts = documents.toObjects(PostData::class.java)
          randomPosts.value =
              allPosts
                  .filter { it.userId != currentUserId } // exclude current user's posts
                  .shuffled() // randomize order
                  .take(20) // limit to 20 posts
          randomPostsProgress.value = false
        }
        .addOnFailureListener { e ->
          handleException(popupNotification, e, "Unable to fetch posts")
          randomPostsProgress.value = false
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
      searchedPosts.value = emptyList()

      db.collection(POSTS)
          .whereArrayContains("searchTerms", searchTerm.trim().lowercase(Locale.ROOT))
          .get()
          .addOnSuccessListener {
            convertPosts(it, searchedPosts)
            searchedPostProgress.value = false
          }
          .addOnFailureListener { e ->
            handleException(popupNotification, e, "Cannot search post")
            searchedPostProgress.value = false
          }
    }
  }

  // -------------------FOLLOWING /FOLLOWERS-----------------
  fun onFollowClick(userId: String) {
    onFollowProgress.value = true
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
          .addOnSuccessListener {
            getUserData(currentUser)
            onFollowProgress.value = false
          }
          .addOnFailureListener { e ->
            handleException(
                popupNotification,
                e,
                "Unable to follow/Unfollow. Please try again later.",
            )
            onFollowProgress.value = false
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
    val currentUserId = auth.currentUser?.uid
    postsFeedProgress.value = true

    val randomPostsTask =
        db.collection(POSTS).whereNotEqualTo("userId", currentUserId).limit(20).get()

    if (!following.isNullOrEmpty()) {
      db.collection(POSTS)
          .whereIn("userId", following)
          .get()
          .addOnSuccessListener { followingDocs ->
            val followingPosts = followingDocs.toObjects(PostData::class.java)

            randomPostsTask
                .addOnSuccessListener { randomDocs ->
                  val randomPosts =
                      randomDocs
                          .toObjects(PostData::class.java)
                          .filter { it.userId !in following }
                          .shuffled()

                  val merged = (followingPosts + randomPosts).distinctBy { it.postId }
                  postsFeed.value = intervalPosts(merged) // ✅ interleave
                  postsFeedProgress.value = false
                }
                .addOnFailureListener {
                  postsFeed.value = intervalPosts(followingPosts) // ✅ interleave
                  postsFeedProgress.value = false
                }
          }
          .addOnFailureListener { e ->
            handleException(popupNotification, e, "Unable to get feed")
            postsFeedProgress.value = false
          }
    } else {
      randomPostsTask
          .addOnSuccessListener { randomDocs ->
            val posts = randomDocs.toObjects(PostData::class.java).shuffled()
            postsFeed.value = intervalPosts(posts) // ✅ interleave
            postsFeedProgress.value = false
          }
          .addOnFailureListener { e ->
            handleException(popupNotification, e, "Unable to get feed")
            postsFeedProgress.value = false
          }
    }
  }

  private fun intervalPosts(posts: List<PostData>): List<PostData> {
    // group posts by userId
    val groupedByUser = posts.groupBy { it.userId }.values.map { it.toMutableList() }
    val result = mutableListOf<PostData>()
    val queues = groupedByUser.map { ArrayDeque(it) }.toMutableList()

    // round robin - take one post from each user at a time
    while (queues.any { it.isNotEmpty() }) {
      queues.filter { it.isNotEmpty() }.forEach { queue -> result.add(queue.removeFirst()) }
    }
    return result
  }

  fun onLikePost(postData: PostData) {
    val userId = auth.currentUser?.uid ?: return

    // Get the most current version of the post from the feed
    val currentPost = postsFeed.value.find { it.postId == postData.postId } ?: postData
    val currentLikes = currentPost.likes?.toMutableList() ?: mutableListOf()

    val newLikes =
        if (currentLikes.contains(userId)) {
          currentLikes.filter { it != userId }.toMutableList() // unlike
        } else {
          currentLikes.apply { add(userId) } // like
        }

    // Update local state immediately
    postsFeed.value =
        postsFeed.value.map { if (it.postId == postData.postId) it.copy(likes = newLikes) else it }

    // Update Firestore
    postData.postId?.let { postId ->
      db.collection(POSTS)
          .document(postId)
          .update("likes", newLikes) // ← use update() instead of set() with merge
          .addOnFailureListener { e ->
            handleException(popupNotification, e, "Unable to like post")
          }
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
            handleException(
                popupNotification,
                e,
                "Cannot be able to add a comment.Please try again later",
            )
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
          handleException(popupNotification, e, "Unable to fetch comments. Please try again later.")
          commentProgress.value = false
        }
  }
}
