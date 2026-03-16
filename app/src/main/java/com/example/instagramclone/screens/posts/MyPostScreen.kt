package com.example.instagramclone.screens.posts

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.instagramclone.data.PostData
import com.example.instagramclone.routes.DestinationScreen
import com.example.instagramclone.screens.BottomNavItem
import com.example.instagramclone.screens.BottomNavigationMenu
import com.example.instagramclone.screens.profile.ProfileImage
import com.example.instagramclone.sharedUtils.CommonProgressSpinner
import com.example.instagramclone.sharedUtils.GeneralPostImage
import com.example.instagramclone.sharedUtils.NavParam
import com.example.instagramclone.sharedUtils.navigateTo
import com.example.instagramclone.viewModel.AuthViewModel

data class PostRow(
    var post1: PostData? = null,
    var post2: PostData? = null,
    var post3: PostData? = null,
) {
  fun isFull() = post1 != null && post2 != null && post3 != null

  fun add(post: PostData) {
    if (post1 == null) {
      post1 = post
    } else if (post2 == null) {
      post2 = post
    } else if (post3 == null) {
      post3 = post
    }
  }
}

@Composable
fun MyPostScreen(navController: NavController, vm: AuthViewModel) {

  val newPostImageLauncher =
      rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
          val encoded = Uri.encode(it.toString())
          val route = DestinationScreen.NewPost.createRoute(encoded)
          navController.navigate(route)
        }
      }
  val userData = vm.userData.value
  val isLoading = vm.inProgress.value
  val isPostLoading = vm.refreshPostProgress.value
  val posts = vm.posts.value
  val following = userData?.following?.size ?: 0
  val followers = vm.followers.value
  val noOfPost = posts.size

  Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
    Column(modifier = Modifier.weight(1f).padding(top = 20.dp)) {
      Row(modifier = Modifier.padding()) {
        ProfileImage(userData?.imageUrl) { newPostImageLauncher.launch("image/*") }

        Text(
            "$noOfPost \nposts",
            modifier = Modifier.weight(1f).align(Alignment.CenterVertically),
            textAlign = TextAlign.Center,
        )
        Text(
            "$followers \nfollowers",
            modifier = Modifier.weight(1f).align(Alignment.CenterVertically),
            textAlign = TextAlign.Center,
        )
        Text(
            "$following \nfollowing",
            modifier = Modifier.weight(1f).align(Alignment.CenterVertically),
            textAlign = TextAlign.Center,
        )
      }
      Column(modifier = Modifier.padding(18.dp)) {
        val userNameDisplay = if (userData?.username == null) "" else "@${userData.username}"
        Text(userData?.name ?: "", fontWeight = FontWeight.Bold)
        Text(userNameDisplay)
        Text(userData?.bio ?: "")
      }
      OutlinedButton(
          onClick = { navigateTo(navController, DestinationScreen.Profile) },
          modifier = Modifier.padding(18.dp).fillMaxWidth(),
          colors = ButtonDefaults.buttonColors(Color.Transparent),
          elevation =
              ButtonDefaults.buttonElevation(
                  defaultElevation = 0.dp,
                  pressedElevation = 0.dp,
                  disabledElevation = 0.dp,
              ),
          shape = RoundedCornerShape(10),
      ) {
        Text("Edit Profile", color = Color.Black)
      }
      PostLists(
          isContextLoading = isLoading,
          postLoading = isPostLoading,
          posts = posts,
          modifier = Modifier.weight(1f).padding(1.dp).fillMaxSize(),
      ) { post ->
        navigateTo(
            navController = navController,
            DestinationScreen.SinglePost,
            NavParam("post", post),
        )
      }
    }
    BottomNavigationMenu(BottomNavItem.POSTS, navController)
  }

  if (isLoading) CommonProgressSpinner()
}

@Composable
fun PostLists(
    isContextLoading: Boolean,
    postLoading: Boolean,
    posts: List<PostData>,
    modifier: Modifier,
    onPostClick: (PostData) -> Unit,
) {
  if (postLoading) {
    CommonProgressSpinner()
  } else if (posts.isEmpty()) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
      if (!isContextLoading) Text("No post Available")
    }
  } else {
    LazyColumn(modifier = Modifier) {
      val rows = arrayListOf<PostRow>()
      var currentRow = PostRow()
      rows.add(currentRow)

      for (post in posts) {
        if (currentRow.isFull()) {
          currentRow = PostRow()
          rows.add(currentRow)
        }
        currentRow.add(post = post)
      }

      items(items = rows) { row -> PostsRow(item = row, onPostClick = onPostClick) }
    }
  }
}

@Composable
fun PostsRow(item: PostRow, onPostClick: (PostData) -> Unit) {
  Row(modifier = Modifier.fillMaxWidth().height(120.dp)) {
    PostImage(
        imageUrl = item.post1?.postImage,
        modifier = Modifier.weight(1f).clickable { item.post1?.let { post -> onPostClick(post) } },
    )
    PostImage(
        imageUrl = item.post2?.postImage,
        modifier = Modifier.weight(1f).clickable { item.post2?.let { post -> onPostClick(post) } },
    )
    PostImage(
        imageUrl = item.post3?.postImage,
        modifier = Modifier.weight(1f).clickable { item.post3?.let { post -> onPostClick(post) } },
    )
  }
}

@Composable
fun PostImage(imageUrl: String?, modifier: Modifier) {
  Box(modifier = modifier) {
    val modifier = Modifier.padding(1.dp).fillMaxSize()
    if (imageUrl != null) {
      GeneralPostImage(data = imageUrl, modifier = modifier, contentScale = ContentScale.Crop)
    }
  }
}
