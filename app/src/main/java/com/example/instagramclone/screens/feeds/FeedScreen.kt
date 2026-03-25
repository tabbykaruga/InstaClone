package com.example.instagramclone.screens.feeds

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.instagramclone.data.PostData
import com.example.instagramclone.routes.DestinationScreen
import com.example.instagramclone.screens.BottomNavItem
import com.example.instagramclone.screens.BottomNavigationMenu
import com.example.instagramclone.screens.posts.CommentsScreen
import com.example.instagramclone.sharedUtils.FeedScreenShimmer
import com.example.instagramclone.sharedUtils.GeneralPostImage
import com.example.instagramclone.sharedUtils.LikeAnimation
import com.example.instagramclone.sharedUtils.NavParam
import com.example.instagramclone.sharedUtils.ProfileImageCard
import com.example.instagramclone.sharedUtils.navigateTo
import com.example.instagramclone.viewModel.AuthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun FeedScreen(
    navController: NavController,
    vm: AuthViewModel,
) {
  // userdata loading at the beginning
  val userDataLoading = vm.inProgress.value
  val userData = vm.userData.value
  val userFeed = vm.postsFeed.value
  val userFeedLoading = vm.postsFeedProgress.value
  val currentUserName = userData?.username
  val currentName = userData?.name

  // refresh
  var isRefreshing by remember { mutableStateOf(false) }
  val coroutineScope = rememberCoroutineScope()

  Scaffold(
      containerColor = Color(0xFFF0F0F0),
      bottomBar = { BottomNavigationMenu(BottomNavItem.FEED, navController) },
  ) { padding ->
    Column(modifier = Modifier.padding(padding).background(Color(0xFFF0F0F0))) {
      Card(
          modifier =
              Modifier.padding(16.dp).clickable {
                navigateTo(navController, DestinationScreen.MyPost)
              },
          elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
      ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
          ProfileImageCard(
              userImg = userData?.imageUrl,
              modifier = Modifier.size(100.dp),
          )
          Column(
              modifier = Modifier.padding(start = 16.dp),
              verticalArrangement = Arrangement.Center,
          ) {
            Text(
                currentName ?: "",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
            )
            Text(
                currentUserName ?: "",
                fontStyle = FontStyle.Italic,
                fontSize = 14.sp,
                color = Color.Gray,
            )
          }
        }
      }
      PullToRefreshBox(
          isRefreshing = isRefreshing,
          onRefresh = {
            coroutineScope.launch {
              isRefreshing = true
              vm.refreshFeed()
              isRefreshing = false
            }
          },
          modifier = Modifier.weight(1f),
      ) {
        FeedPostList(
            posts = userFeed,
            modifier = Modifier.fillMaxSize(),
            loading = userFeedLoading or userDataLoading,
            navController = navController,
            vm = vm,
            currentUserId = userData?.userId ?: "",
        )
      }
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
    LazyColumn(modifier = Modifier.fillMaxSize()) {
      if (loading) {
        items(3) { FeedScreenShimmer() }
      } else {
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
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
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
  val comments = vm.commentsMap[post.postId] ?: emptyList()
  val sheetState = rememberModalBottomSheetState()
  var showCommentsSheet by remember { mutableStateOf(false) }
  var userProfileImage by remember { mutableStateOf<String?>(null) }
  var userName by remember { mutableStateOf<String?>("") }

  LaunchedEffect(post.postId) { vm.getComments(post.postId) }
  LaunchedEffect(post.userId) {
    post.userId?.let { userId ->
      vm.getUserById(userId) { user ->
        userProfileImage = user?.imageUrl
        userName = user?.username
      }
    }
  }

  Card(
      shape = RoundedCornerShape(corner = CornerSize(16.dp)),
      modifier =
          Modifier.fillMaxWidth().wrapContentHeight().padding(horizontal = 12.dp, vertical = 6.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), // ← elevation
      colors = CardDefaults.cardColors(containerColor = Color.White),
  ) {
    Column {
      Row(
          modifier = Modifier.fillMaxWidth().height(50.dp),
          verticalAlignment = Alignment.CenterVertically,
      ) {
        Card(shape = CircleShape, modifier = Modifier.padding(4.dp).size(32.dp)) {
          AsyncImage(
              model = userProfileImage,
              contentDescription = null,
              modifier = Modifier.fillMaxSize(),
              contentScale = ContentScale.Crop,
          )
        }
        Column {
          Text(userName ?: "")
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
      Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { vm.onLikePost(post) }, modifier = Modifier.size(24.dp)) {
          Icon(
              imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
              contentDescription = "Like",
              tint = if (isLiked) Color.Red else Color.Black,
          )
        }
        Text("$likesCount likes", modifier = Modifier.padding(start = 8.dp))

        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = { showCommentsSheet = true }, modifier = Modifier.size(24.dp)) {
          Icon(
              imageVector = Icons.Outlined.ChatBubbleOutline,
              contentDescription = "Comments",
              tint = Color.Black,
          )
        }
        Text(
            "${comments.size} comment",
            modifier = Modifier.padding(start = 4.dp),
        )
      }
      Text(
          text = "#${post.postDescription}",
          fontStyle = FontStyle.Italic,
          fontSize = 12.sp,
          modifier = Modifier.padding(start = 20.dp, top = 0.dp, bottom = 4.dp),
      )
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
