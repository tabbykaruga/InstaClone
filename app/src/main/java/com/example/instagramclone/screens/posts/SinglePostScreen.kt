package com.example.instagramclone.screens.posts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.instagramclone.data.PostData
import com.example.instagramclone.sharedUtils.Divider
import com.example.instagramclone.sharedUtils.GeneralPostImage
import com.example.instagramclone.sharedUtils.LikeAnimation
import com.example.instagramclone.sharedUtils.ShimmerEffect
import com.example.instagramclone.viewModel.AuthViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SinglePostScreen(navController: NavController, vm: AuthViewModel, post: PostData) {
  val comments = vm.commentsMap[post.postId] ?: emptyList()
  val sheetState = rememberModalBottomSheetState()
  var showCommentsSheet by remember { mutableStateOf(false) }

  // LaunchedEffect is for calling a single call
  LaunchedEffect(key1 = Unit) { vm.getComments(post.postId) }

  post.userId?.let {
    Column(
        modifier =
            Modifier.fillMaxWidth()
                .wrapContentHeight()
                .padding(8.dp)
                .statusBarsPadding()
                .imePadding()
    ) {
      Text("Back", modifier = Modifier.clickable { navController.popBackStack() })
      Divider()
      SinglePostDisplay(
          navController,
          vm,
          post,
          noOfComments = comments.size,
      ) {
        showCommentsSheet = true
      }
    }
    if (showCommentsSheet) {
      ModalBottomSheet(
          onDismissRequest = { showCommentsSheet = false },
          sheetState = sheetState,
          containerColor = Color.White,
          modifier = Modifier.fillMaxHeight(0.635f),
          dragHandle = null,
          contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
      ) {
        CommentsScreen(vm = vm, postId = post.postId ?: "")
      }
    }
  }
}

@Composable
fun SinglePostDisplay(
    navController: NavController,
    vm: AuthViewModel,
    post: PostData,
    noOfComments: Int,
    onCommentsClick: () -> Unit,
) {
  val userData = vm.userData.value
  val currentUserId = userData?.userId
  var userProfileImage by remember { mutableStateOf<String?>(null) }
  var userName by remember { mutableStateOf<String?>(null) }

  LaunchedEffect(post.userId) {
    post.userId?.let { userId ->
      vm.getUserById(userId) { user ->
        userProfileImage = user?.imageUrl
        userName = user?.username
      }
    }
  }
  val currentPost =
      vm.postsFeed.value.find { it.postId == post.postId }
          ?: vm.posts.value.find { it.postId == post.postId }
          ?: post
  val isLiked = currentPost.likes?.contains(currentUserId) == true
  val likesCount = currentPost.likes?.size ?: 0
  val showLikeAnimation = remember { mutableStateOf(false) }
  val showDislikeAnimation = remember { mutableStateOf(false) }
  val onFollowProgress = vm.onFollowProgress.value

  Box(modifier = Modifier.fillMaxWidth().height(48.dp)) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
      Card(shape = CircleShape, modifier = Modifier.padding(8.dp).size(32.dp)) {
        AsyncImage(
            model = userProfileImage,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
      }
      Column {
        if (userName != null) {
          Text("$userName" ?: "")
        } else {
          ShimmerEffect(
              modifier = Modifier.width(140.dp).height(14.dp).clip(RoundedCornerShape(4.dp))
          )
        }
        Text(
            text = post.postLocation ?: "",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
        )
      }
      Spacer(modifier = Modifier.weight(1f))

      val isOwner = userData?.userId == post.userId
      val isFollowing = userData?.following?.contains(post.userId) == true
      var showDeleteDialog by remember { mutableStateOf(false) }

      if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Post", fontWeight = FontWeight.W500) },
            text = { Text("Are you sure you want to delete this post?") },
            confirmButton = {
              Button(
                  onClick = {
                    showDeleteDialog = false
                    vm.onDeletePost(post, onSuccess = { navController.popBackStack() })
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = Color.Blue),
              ) {
                Text("Delete", color = Color.White)
              }
            },
            dismissButton = {
              OutlinedButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            },
        )
      }
      if (isOwner) {
        Icon(
            imageVector = Icons.Outlined.DeleteOutline,
            contentDescription = "Delete",
            modifier = Modifier.size(24.dp).clickable { showDeleteDialog = true },
        )
      } else {

        val buttonText = if (isFollowing) "Unfollow" else "Follow"
        val buttonColor = if (isFollowing) Color.Gray else Color.Blue

        Button(
            onClick = { vm.onFollowClick(post.userId!!) },
            modifier = Modifier.padding(8.dp),
            enabled = !onFollowProgress,
            shape = RectangleShape,
            colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
        ) {
          if (onFollowProgress) {
            CircularProgressIndicator(
                color = Color.Blue,
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp,
            )
          } else {

            Text(buttonText, color = Color.White)
          }
        }
      }
    }
  }
  Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
    GeneralPostImage(
        data = currentPost.postImage,
        modifier =
            Modifier.fillMaxWidth().aspectRatio(1f).pointerInput(Unit) {
              detectTapGestures(
                  onDoubleTap = {
                    if (currentPost.likes?.contains(currentUserId) == true) {
                      showDislikeAnimation.value = true
                    } else {
                      showLikeAnimation.value = true
                    }
                    vm.onLikePost(currentPost)
                  },
              )
            },
        contentScale = ContentScale.Crop,
    )
    if (showLikeAnimation.value) {
      LaunchedEffect(showLikeAnimation.value) {
        delay(1000L)
        showLikeAnimation.value = false
      }
      LikeAnimation()
    }
    if (showDislikeAnimation.value) {
      LaunchedEffect(showDislikeAnimation.value) {
        delay(1000L)
        showDislikeAnimation.value = false
      }
      LikeAnimation(false)
    }
  }
  Row(
      modifier = Modifier.fillMaxWidth().padding(8.dp),
      verticalAlignment = Alignment.CenterVertically,
  ) {
    IconButton(
        onClick = { vm.onLikePost(currentPost) },
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = "Like",
            tint = if (isLiked) Color.Red else Color.Black,
        )
        Text(
            "$likesCount",
            modifier =
                Modifier.padding(
                    start = 0.dp,
                ),
        )
      }
    }

    IconButton(
        onClick = { onCommentsClick() },
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Outlined.ChatBubbleOutline,
            contentDescription = "Comments",
            tint = Color.Black,
        )
        Text(
            "$noOfComments",
            modifier = Modifier.padding(start = 4.dp),
        )
      }
    }
  }
  Row(modifier = Modifier.padding(8.dp)) {
    Text(userName ?: "", fontWeight = FontWeight.Bold)
    Text(post.postDescription ?: "", modifier = Modifier.padding(start = 8.dp))
  }
  Row(modifier = Modifier.padding(8.dp)) {
    Text(
        "$noOfComments Comments",
        color = Color.Gray,
        modifier = Modifier.padding(8.dp).clickable { onCommentsClick() },
    )
  }
}
