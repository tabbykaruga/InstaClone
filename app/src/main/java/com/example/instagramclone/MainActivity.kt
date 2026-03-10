package com.example.instagramclone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.instagramclone.data.PostData
import com.example.instagramclone.routes.DestinationScreen
import com.example.instagramclone.screens.FeedScreen
import com.example.instagramclone.screens.SearchScreen
import com.example.instagramclone.screens.auth.LoginScreen
import com.example.instagramclone.screens.auth.SignUpScreen
import com.example.instagramclone.screens.posts.MyPostScreen
import com.example.instagramclone.screens.posts.NewPostScreen
import com.example.instagramclone.screens.posts.SinglePostScreen
import com.example.instagramclone.screens.profile.ProfileScreen
import com.example.instagramclone.sharedUtils.NotificationMessage
import com.example.instagramclone.ui.theme.InstagramCloneTheme
import com.example.instagramclone.viewModel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true
    CloudinaryManager.init(this)
    setContent {
      InstagramCloneTheme {
        Scaffold(containerColor = Color.White) { padding ->
          InstagramApp(modifier = Modifier.fillMaxSize().padding(padding))
        }
      }
    }
  }
}

@Composable
fun InstagramApp(
    modifier: Modifier = Modifier,
) {
  val vm = hiltViewModel<AuthViewModel>()
  val navController = rememberNavController()

  NotificationMessage(vm)

  NavHost(navController = navController, startDestination = DestinationScreen.Login.route) {
    composable(DestinationScreen.Login.route) {
      LoginScreen(navController = navController, vm = vm)
    }
    composable(DestinationScreen.Signup.route) {
      SignUpScreen(navController = navController, vm = vm)
    }
    composable(DestinationScreen.Feed.route) { FeedScreen(navController = navController, vm = vm) }
    composable(DestinationScreen.Search.route) {
      SearchScreen(navController = navController, vm = vm)
    }
    composable(DestinationScreen.MyPost.route) {
      MyPostScreen(navController = navController, vm = vm)
    }
    composable(DestinationScreen.Profile.route) {
      ProfileScreen(navController = navController, vm = vm)
    }
    composable(DestinationScreen.NewPost.route) { navBackStackEntry ->
      val imageUri = navBackStackEntry.arguments?.getString("imageUri")
      imageUri?.let { NewPostScreen(navController = navController, vm = vm, encodedUri = it) }
    }
    composable(DestinationScreen.SinglePost.route) {
      val postData =
          navController.previousBackStackEntry?.arguments?.getParcelable<PostData>("post")
      postData?.let { SinglePostScreen(navController = navController, vm = vm, post = postData) }
    }
  }
}
