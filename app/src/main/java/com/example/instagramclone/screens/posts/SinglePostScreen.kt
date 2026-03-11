package com.example.instagramclone.screens.posts

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.instagramclone.data.PostData
import com.example.instagramclone.sharedUtils.CloudinaryImage
import com.example.instagramclone.sharedUtils.Divider
import com.example.instagramclone.viewModel.AuthViewModel

@Composable
fun SinglePostScreen(navController: NavController, vm: AuthViewModel, post: PostData) {
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
      SinglePostDisplay(navController, vm, post)
    }
  }
}

@Composable
fun SinglePostDisplay(navController: NavController, vm: AuthViewModel, post: PostData) {
  val isLiked = false
  val userData = vm.userData.value

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
      Text(".", modifier = Modifier.padding(8.dp))

      if (userData?.userId != post.userId) {
        Text("Follow", color = Color.Blue, modifier = Modifier.clickable {})
      }

      Spacer(modifier = Modifier.weight(1f))
      if (userData?.userId == post.userId) {
        Icon(
            imageVector = Icons.Outlined.DeleteOutline,
            contentDescription = "delete",
            modifier = Modifier.size(24.dp).clickable {},
        )
      }
    }
  }
  Box {
    CloudinaryImage(
        data = post.postImage,
        modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 150.dp),
        contentScale = ContentScale.FillWidth,
    )
  }
  Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
    Icon(
        imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
        contentDescription = "Like",
        tint = if (isLiked) Color.Red else Color.Black,
        modifier = Modifier.size(24.dp).clickable {},
    )
    Text(" ${post.likes?.size ?: 0} likes", modifier = Modifier.padding(start = 0.dp))
  }
  Row(modifier = Modifier.padding(8.dp)) {
    Text(post.userName ?: "", fontWeight = FontWeight.Bold)
    Text(post.postDescription ?: "", modifier = Modifier.padding(start = 8.dp))
  }
  Row(modifier = Modifier.padding(8.dp)) {
    Text("10 Comments", color = Color.Gray, modifier = Modifier.padding(8.dp))
  }
}
