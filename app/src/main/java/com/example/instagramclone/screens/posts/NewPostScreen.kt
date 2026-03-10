package com.example.instagramclone.screens.posts

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.instagramclone.sharedUtils.CommonProgressSpinner
import com.example.instagramclone.sharedUtils.Divider
import com.example.instagramclone.viewModel.AuthViewModel

@Composable
fun NewPostScreen(navController: NavController, vm: AuthViewModel, encodedUri: String) {
  val imageUri by remember { mutableStateOf(encodedUri) }
  var description by rememberSaveable { mutableStateOf("") }
  var location by rememberSaveable { mutableStateOf("") }
  val scrollState = rememberScrollState()
  // dismiss keyboard
  val focusManager = LocalFocusManager.current

  Column(
      modifier =
          Modifier.verticalScroll(scrollState)
              .fillMaxWidth()
              .padding(16.dp)
              .imePadding()
              .statusBarsPadding()
  ) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Text("Cancel", modifier = Modifier.clickable { navController.popBackStack() })
      Text(
          "Post",
          modifier =
              Modifier.clickable {
                focusManager.clearFocus()
                vm.onCreateNewPost(imageUri.toUri(), description, location) {
                  navController.popBackStack()
                }
              },
      )
    }
    Divider()
    Image(
        painter = rememberAsyncImagePainter(imageUri),
        contentDescription = null,
        modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 150.dp),
        contentScale = ContentScale.FillWidth,
    )

    OutlinedTextField(
        value = description,
        onValueChange = { description = it },
        modifier = Modifier.fillMaxWidth().height(130.dp),
        label = { Text("Description") },
        singleLine = false,
        colors =
            OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
            ),
    )
    OutlinedTextField(
        value = location,
        onValueChange = { location = it },
        modifier = Modifier.fillMaxWidth().height(60.dp),
        label = { Text("Location") },
        singleLine = true,
        colors =
            OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
            ),
    )
  }
  val inProgress = vm.inProgress.value
  if (inProgress) CommonProgressSpinner()
}
