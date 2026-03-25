package com.example.instagramclone.screens.posts

import android.net.Uri
import android.widget.Button
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.instagramclone.sharedUtils.CommonProgressSpinner
import com.example.instagramclone.sharedUtils.Divider
import com.example.instagramclone.sharedUtils.cropAndResizeImage
import com.example.instagramclone.viewModel.AuthViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun NewPostScreen(navController: NavController, vm: AuthViewModel, encodedUri: String) {
  val context = LocalContext.current
  val imageUri by remember { mutableStateOf(encodedUri) }
  var description by rememberSaveable { mutableStateOf("") }
  var location by rememberSaveable { mutableStateOf("") }
  val scrollState = rememberScrollState()
  // dismiss keyboard
  val focusManager = LocalFocusManager.current

  val scope = rememberCoroutineScope()
  var croppedUri by remember { mutableStateOf<Uri?>(null) }

  // ← crop image when screen loads
  LaunchedEffect(imageUri) {
    scope.launch(Dispatchers.IO) {
      val cropped = cropAndResizeImage(context, Uri.parse(imageUri))
      croppedUri = cropped
    }
  }

  Column(
      modifier =
          Modifier.verticalScroll(scrollState)
              .fillMaxWidth()
              .padding(16.dp)
              .imePadding()
              .statusBarsPadding()
  ) {
    Text("Cancel", modifier = Modifier.clickable { navController.popBackStack() })
    Divider()
    Image(
        painter = rememberAsyncImagePainter(croppedUri ?: Uri.parse(imageUri)),
        contentDescription = null,
        modifier = Modifier.fillMaxWidth().aspectRatio(1f),
        contentScale = ContentScale.Crop,
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
    Button(
        onClick = {
          focusManager.clearFocus()
          vm.onCreateNewPost(imageUri.toUri(), description, location) {
            navController.popBackStack()
          }
        },
        modifier =
            Modifier.align(Alignment.CenterHorizontally)
                .padding(horizontal = 20.dp, vertical = 20.dp),
        shape = RoundedCornerShape(8.dp),
    ) {
      Text("Upload", fontSize = 15.sp)
    }
  }
  val inProgress = vm.inProgress.value
  if (inProgress) CommonProgressSpinner()
}
