package com.example.instagramclone.screens.posts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.instagramclone.data.PostData
import com.example.instagramclone.routes.DestinationScreen
import com.example.instagramclone.sharedUtils.Divider
import com.example.instagramclone.sharedUtils.GeneralPostImage
import com.example.instagramclone.sharedUtils.LikeAnimation
import com.example.instagramclone.viewModel.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun SinglePostScreen(navController: NavController, vm: AuthViewModel, post: PostData) {
  val comments = vm.comments.value

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
      SinglePostDisplay(navController, vm, post, noOfComments = comments.size)
    }
  }
}

@Composable
fun SinglePostDisplay(
    navController: NavController,
    vm: AuthViewModel,
    post: PostData,
    noOfComments: Int,
) {
  val userData = vm.userData.value
  val currentUserId = userData?.userId
  val isLiked = post.likes?.contains(currentUserId) == true
  val likesCount = post.likes?.size ?: 0
  val showLikeAnimation = remember { mutableStateOf(false) }
  val showDislikeAnimation = remember { mutableStateOf(false) }

  Box(modifier = Modifier.fillMaxWidth().height(48.dp)) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
      Card(shape = CircleShape, modifier = Modifier.padding(8.dp).size(32.dp)) {
        AsyncImage(
            model = post.userImage,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
      }
      Column {
        Text(post.userName ?: "")
        Text(
            text = post.postLocation ?: "",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
        )
      }
      Spacer(modifier = Modifier.weight(1f))

      val isOwner = userData?.userId == post.userId
      val isFollowing = userData?.following?.contains(post.userId) == true

      if (isOwner) {
        Icon(
            imageVector = Icons.Outlined.DeleteOutline,
            contentDescription = "Delete",
            modifier =
                Modifier.size(24.dp).clickable {
                  vm.onDeletePost(post, onSuccess = { navController.popBackStack() })
                },
        )
      } else {

        val buttonText = if (isFollowing) "Unfollow" else "Follow"
        val buttonColor = if (isFollowing) Color.Gray else Color.Blue

        Button(
            onClick = { vm.onFollowClick(post.userId!!) },
            modifier = Modifier.padding(8.dp),
            shape = RectangleShape,
            colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
        ) {
          Text(buttonText, color = Color.White)
        }
      }
    }
  }
  Box {
    GeneralPostImage(
        data = post.postImage,
        modifier =
            Modifier.fillMaxWidth().defaultMinSize(minHeight = 150.dp).pointerInput(Unit) {
              detectTapGestures(
                  onDoubleTap = {
                    if (post.likes?.contains(currentUserId) == true) {
                      showDislikeAnimation.value = true
                    } else {
                      showLikeAnimation.value = true
                    }
                    vm.onLikePost(post)
                  },
              )
            },
        contentScale = ContentScale.FillWidth,
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
      horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    IconButton(
        onClick = {
          if (isLiked) {
            showDislikeAnimation.value = true
          } else {
            showLikeAnimation.value = true
          }
          vm.onLikePost(post)
        },
        modifier = Modifier.size(24.dp),
    ) {
      Icon(
          imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
          contentDescription = "Like",
          tint = if (isLiked) Color.Red else Color.Black,
      )
    }
    Text(
        " $likesCount likes",
        modifier =
            Modifier.padding(
                start = 0.dp,
            ),
    )
    Spacer(modifier = Modifier.weight(1f))
    Icon(
        imageVector = Icons.Outlined.ChatBubbleOutline,
        contentDescription = "Comments",
        tint = Color.Black,
    )
    Text(" $noOfComments comments", modifier = Modifier.padding(start = 8.dp))
  }
  Row(modifier = Modifier.padding(8.dp)) {
    Text(post.userName ?: "", fontWeight = FontWeight.Bold)
    Text(post.postDescription ?: "", modifier = Modifier.padding(start = 8.dp))
  }
  Row(modifier = Modifier.padding(8.dp)) {
    Text(
        "$noOfComments Comments",
        color = Color.Gray,
        modifier =
            Modifier.padding(8.dp).clickable {
              post.postId?.let { navController.navigate(DestinationScreen.Comment.createRoute(it)) }
            },
    )
  }
}
