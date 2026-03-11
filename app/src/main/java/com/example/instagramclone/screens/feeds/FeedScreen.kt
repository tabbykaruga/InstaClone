package com.example.instagramclone.screens.feeds

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.instagramclone.data.PostData
import com.example.instagramclone.routes.DestinationScreen
import com.example.instagramclone.screens.BottomNavItem
import com.example.instagramclone.screens.BottomNavigationMenu
import com.example.instagramclone.sharedUtils.CommonProgressSpinner
import com.example.instagramclone.sharedUtils.GeneralPostImage
import com.example.instagramclone.sharedUtils.LikeAnimation
import com.example.instagramclone.sharedUtils.NavParam
import com.example.instagramclone.sharedUtils.ProfileImageCard
import com.example.instagramclone.sharedUtils.navigateTo
import com.example.instagramclone.viewModel.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun FeedScreen(
    navController: NavController,
    vm: AuthViewModel,
) {
  // userdata loading at the beginning
  val userDataLoading = vm.inProgress.value
  val userData = vm.userData.value
  val userFeed = vm.userFeed.value
  val userFeedLoading = vm.postFeedProgress.value

  Scaffold(bottomBar = { BottomNavigationMenu(BottomNavItem.FEED, navController) }) { padding ->
    Column(modifier = Modifier.fillMaxSize().padding(padding)) {
      //      Row(modifier = Modifier.fillMaxSize().wrapContentHeight().background(Color.White)) {
      ProfileImageCard(
          userImg = userData?.imageUrl,
          modifier = Modifier.padding(8.dp).size(100.dp),
      )
      FeedPostList(
          posts = userFeed,
          modifier = Modifier.weight(1f),
          loading = userFeedLoading or userDataLoading,
          navController = navController,
          vm = vm,
          currentUserId = userData?.userId ?: "",
      )
      //      }
    }
  }
}

@Composable
fun FeedPostList(
    posts: List<PostData>,
    modifier: Modifier,
    loading: Boolean,
    navController: NavController,
    vm: AuthViewModel,
    currentUserId: String,
) {
  Box(modifier = modifier) {
    LazyColumn {
      items(items = posts) {
        SingleFeedPost(
            post = it,
            currentUserId = currentUserId,
            vm = vm,
            onPostClick = {
              navigateTo(navController, DestinationScreen.SinglePost, NavParam("post", it))
            },
        )
      }
    }
    if (loading) CommonProgressSpinner()
  }
}

@Composable
fun SingleFeedPost(
    post: PostData,
    currentUserId: String,
    vm: AuthViewModel,
    onPostClick: () -> Unit,
) {
  val showLikeAnimation = remember { mutableStateOf(false) }
  val showDislikeAnimation = remember { mutableStateOf(false) }
  val isLiked = post.likes?.contains(currentUserId) == true
  val likesCount = post.likes?.size ?: 0
  //    val commentsCount = post.comments?.size ?:0

  Card(
      shape = RoundedCornerShape(corner = CornerSize(4.dp)),
      modifier = Modifier.fillMaxWidth().wrapContentHeight().padding(top = 4.dp, bottom = 4.dp),
  ) {
    Column {
      Row(
          modifier = Modifier.fillMaxWidth().height(50.dp),
          verticalAlignment = Alignment.CenterVertically,
      ) {
        Card(shape = CircleShape, modifier = Modifier.padding(4.dp).size(32.dp)) {
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
      }
      Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        GeneralPostImage(
            data = post.postImage,
            modifier =
                Modifier.fillMaxWidth().defaultMinSize(minHeight = 130.dp).pointerInput(Unit) {
                  detectTapGestures(
                      onDoubleTap = {
                        if (post.likes?.contains(currentUserId) == true) {
                          showDislikeAnimation.value = true
                        } else {
                          showLikeAnimation.value = true
                        }
                        vm.onLikePost(post)
                      },
                      onTap = { onPostClick.invoke() },
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
      Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { vm.onLikePost(post) }, modifier = Modifier.size(24.dp)) {
          Icon(
              imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
              contentDescription = "Like",
              tint = if (isLiked) Color.Red else Color.Black,
          )
        }
        Text("$likesCount likes", modifier = Modifier.padding(start = 8.dp))

        //        Text("$commentsCount likes", modifier = Modifier.padding(start = 8.dp))
      }
    }
  }
}
