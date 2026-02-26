package com.example.instagramclone.sharedUtils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import com.example.instagramclone.routes.DestinationScreen
import com.example.instagramclone.viewModel.AuthViewModel

fun navigateTo(navController: NavController, destScreen: DestinationScreen) {
  navController.navigate(destScreen.route) {
    popUpTo(destScreen.route)
    launchSingleTop = true
  }
}

@Composable
fun CheckSignedIn(navController: NavController, vm: AuthViewModel) {
  val alreadyLoggedIn = remember { mutableStateOf(false) }
  val signedIn = vm.signedIn.value

  if (signedIn && !alreadyLoggedIn.value) {
    alreadyLoggedIn.value = true
    navController.navigate(DestinationScreen.Feed.route) { popUpTo(0) }
  }
}
