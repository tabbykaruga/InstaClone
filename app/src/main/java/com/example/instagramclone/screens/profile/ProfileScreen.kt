package com.example.instagramclone.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.instagramclone.routes.DestinationScreen
import com.example.instagramclone.sharedUtils.CommonImage
import com.example.instagramclone.sharedUtils.CommonProgressSpinner
import com.example.instagramclone.sharedUtils.Divider
import com.example.instagramclone.sharedUtils.navigateTo
import com.example.instagramclone.viewModel.AuthViewModel

@Composable
fun ProfileScreen(navController: NavController, vm: AuthViewModel) {
  val isLoading = vm.inProgress.value
  val userData = vm.userData.value
  var name by rememberSaveable { mutableStateOf(userData?.name ?: "") }
  var userName by rememberSaveable { mutableStateOf(userData?.username ?: "") }
  var bio by rememberSaveable { mutableStateOf(userData?.bio ?: "") }
  var selectedImageUri by rememberSaveable { mutableStateOf<Uri?>(null) }

  if (isLoading) {
    CommonProgressSpinner()
  } else {
    ProfileContent(
        vm = vm,
        name = name,
        userName = userName,
        bio = bio,
        selectedImageUri = selectedImageUri,
        onNameChange = { name = it },
        onUserNameChange = { userName = it },
        onBioChange = { bio = it },
        onImageSelected = { uri: Uri -> selectedImageUri = uri },
        onSave = {
          if (selectedImageUri != null) {
            vm.uploadProfileImageAndSave(
                uri = selectedImageUri!!,
                name = name,
                userName = userName,
                bio = bio,
            )
            selectedImageUri = null
          } else {
            vm.updateProfileData(name = name, userName = userName, bio = bio)
          }
          navigateTo(navController, DestinationScreen.MyPost)
        },
        onBack = { navigateTo(navController, DestinationScreen.MyPost) },
        onLogOut = {
          vm.onLogOut()
          navigateTo(navController, DestinationScreen.Login)
        },
    )
  }
}

@Composable
fun ProfileContent(
    vm: AuthViewModel,
    name: String,
    userName: String,
    bio: String,
    selectedImageUri: Uri?,
    onNameChange: (String) -> Unit,
    onUserNameChange: (String) -> Unit,
    onBioChange: (String) -> Unit,
    onImageSelected: (Uri) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
    onLogOut: () -> Unit,
) {
  val scrollState = rememberScrollState()
  val imageUrl = vm.userData.value?.imageUrl

  Column(
      modifier =
          Modifier.fillMaxSize()
              .verticalScroll(scrollState)
              .padding(8.dp)
              .statusBarsPadding()
              .imePadding()
  ) {
    Row(
        modifier =
            Modifier.fillMaxWidth()
                .padding(
                    20.dp,
                ),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = "Back",
          modifier = Modifier.clickable { onBack.invoke() },
      )
      Icon(
          imageVector = Icons.AutoMirrored.Filled.Logout,
          contentDescription = "Log Out",
          modifier = Modifier.clickable { onLogOut.invoke() },
      )
    }
    Divider()

    ProfileImage(
        imageUrl = imageUrl,
        selectedImageUri = selectedImageUri,
        vm = vm,
        onImageSelected = onImageSelected,
    )
    Divider()

    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
      Text("User Name", modifier = Modifier.width(100.dp))
      TextField(
          value = userName,
          onValueChange = onUserNameChange,
          colors =
              TextFieldDefaults.colors(
                  unfocusedContainerColor = Color.Transparent,
                  focusedContainerColor = Color.Transparent,
                  focusedTextColor = Color.Black,
                  unfocusedTextColor = Color.Black,
              ),
      )
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
      Text("Name", modifier = Modifier.width(100.dp))
      TextField(
          value = name,
          onValueChange = onNameChange,
          colors =
              TextFieldDefaults.colors(
                  unfocusedContainerColor = Color.Transparent,
                  focusedContainerColor = Color.Transparent,
                  focusedTextColor = Color.Black,
                  unfocusedTextColor = Color.Black,
              ),
      )
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 4.dp),
        verticalAlignment = Alignment.Top,
    ) {
      Text("Bio", modifier = Modifier.width(100.dp))
      TextField(
          value = bio,
          onValueChange = onBioChange,
          colors =
              TextFieldDefaults.colors(
                  unfocusedContainerColor = Color.Transparent,
                  focusedContainerColor = Color.Transparent,
                  focusedTextColor = Color.Black,
                  unfocusedTextColor = Color.Black,
              ),
          singleLine = false,
          modifier = Modifier.height(150.dp),
      )
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
      Button(
          onClick = { onSave.invoke() },
          modifier = Modifier.padding(horizontal = 20.dp),
          shape = RoundedCornerShape(8.dp),
      ) {
        Text("Save")
      }
    }
  }
}

@Composable
fun ProfileImage(
    imageUrl: String?,
    selectedImageUri: Uri?, // preview before save
    vm: AuthViewModel,
    onImageSelected: (Uri) -> Unit,
) {

  val isLoading = vm.inProgress.value
  val launcher =
      rememberLauncherForActivityResult(
          contract = ActivityResultContracts.GetContent(),
      ) { uri: Uri? ->
        uri?.let { onImageSelected(it) }
      }

  Box(modifier = Modifier.height(IntrinsicSize.Min)) {
    Column(
        modifier = Modifier.padding(8.dp).fillMaxWidth().clickable { launcher.launch("image/*") },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Card(shape = CircleShape, modifier = Modifier.padding(8.dp).size(100.dp)) {
        if (selectedImageUri != null) {
          AsyncImage(
              model = selectedImageUri,
              contentDescription = null,
              modifier = Modifier.fillMaxSize(),
              contentScale = ContentScale.Crop,
          )
        } else {
          CommonImage(data = imageUrl, modifier = Modifier.fillMaxSize())
        }
      }
      Text("Change profile Picture", color = Color.Blue)
    }

    if (isLoading) CommonProgressSpinner()
  }
}
