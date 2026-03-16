package com.example.instagramclone.screens.posts

import android.widget.Button
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.instagramclone.sharedUtils.CommonProgressSpinner
import com.example.instagramclone.viewModel.AuthViewModel

@Composable
fun CommentsScreen(navController: NavController, vm: AuthViewModel, postId: String) {
  var commentText by rememberSaveable { mutableStateOf("") }
  val focusManager = LocalFocusManager.current

  val comments = vm.comments.value
  val commentsProgress = vm.commentProgress.value

  LaunchedEffect(postId) { vm.getComments(postId) }

  Column(
      modifier =
          Modifier.fillMaxSize()
              .navigationBarsPadding()
              .statusBarsPadding()
              .imePadding()
              .padding(8.dp)
  ) {
    Column(modifier = Modifier.weight(1f)) {
      if (commentsProgress) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          CommonProgressSpinner()
        }
      } else if (comments.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Text("No Comments Available")
        }
      } else {
        LazyColumn(modifier = Modifier.weight(1f)) {
          items(items = comments) { comment ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
              Card(shape = CircleShape, modifier = Modifier.padding(4.dp).size(32.dp)) {
                AsyncImage(
                    model = comment.userImage,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
              }
              Text(comment.userName, fontWeight = FontWeight.Bold)
              Text(" :  ${ comment.comment ?:"" }", modifier = Modifier.padding(start = 8.dp))
            }
          }
        }
      }
    }

    Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
      TextField(
          value = commentText,
          onValueChange = { commentText = it },
          modifier = Modifier.weight(1f).border(1.dp, Color.LightGray),
          colors =
              TextFieldDefaults.colors(
                  focusedContainerColor = Color.Transparent,
                  focusedTextColor = Color.Black,
                  focusedIndicatorColor = Color.Transparent,
                  unfocusedIndicatorColor = Color.Transparent,
                  disabledIndicatorColor = Color.Transparent,
              ),
      )
      Button(
          onClick = {
            vm.createComment(postId = postId, comment = commentText)
            commentText = ""
            focusManager.clearFocus()
          },
          modifier = Modifier.padding(start = 8.dp),
      ) {
        Text("Comment")
      }
    }
  }
}
