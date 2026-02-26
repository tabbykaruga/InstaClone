package com.example.instagramclone.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.instagramclone.R
import com.example.instagramclone.routes.DestinationScreen
import com.example.instagramclone.screens.BottomNavItem
import com.example.instagramclone.screens.BottomNavigationMenu
import com.example.instagramclone.sharedUtils.CommonProgressSpinner
import com.example.instagramclone.sharedUtils.ProfileImageCard
import com.example.instagramclone.sharedUtils.navigateTo
import com.example.instagramclone.viewModel.AuthViewModel

@Composable
fun MyPostScreen(navController: NavController, vm: AuthViewModel) {

  val userData = vm.userData.value
  val isLoading = vm.inProgress.value

  Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
    Column(modifier = Modifier.weight(1f).padding(top = 20.dp)) {
      Row(modifier = Modifier.padding()) {
        ProfileImage(userData?.imageUrl, onClick = {})
        Text(
            "6 \nposts",
            modifier = Modifier.weight(1f).align(Alignment.CenterVertically),
            textAlign = TextAlign.Center,
        )
        Text(
            "100 \nfollowers",
            modifier = Modifier.weight(1f).align(Alignment.CenterVertically),
            textAlign = TextAlign.Center,
        )
        Text(
            "6 \nfollowing",
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
      Column(modifier = Modifier.weight(1f)) { Text("POST LIST") }
    }
    BottomNavigationMenu(BottomNavItem.POSTS, navController)
  }

  if (isLoading) CommonProgressSpinner()
}

@Composable
fun ProfileImage(imageUrl: String?, onClick: () -> Unit) {
  Box(modifier = Modifier.padding(top = 16.dp).clickable { onClick.invoke() }) {
    ProfileImageCard(userImg = imageUrl, modifier = Modifier.padding(8.dp).size(100.dp))
    Card(
        shape = CircleShape,
        border =
            BorderStroke(
                width = 2.dp,
                color = Color.White,
            ),
        modifier =
            Modifier.size(32.dp).align(Alignment.BottomEnd).padding(bottom = 8.dp, end = 8.dp),
    ) {
      Image(
          painter = painterResource(R.drawable.ic_add),
          contentDescription = null,
          modifier = Modifier.background(Color.Blue),
          colorFilter = ColorFilter.tint(Color.White),
      )
    }
  }
}
