package com.example.instagramclone.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.instagramclone.R
import com.example.instagramclone.routes.DestinationScreen
import com.example.instagramclone.sharedUtils.navigateTo

enum class BottomNavItem(val icon: Int, val navDestination: DestinationScreen) {
  FEED(R.drawable.ic_home, DestinationScreen.Feed),
  SEARCH(R.drawable.ic_search, DestinationScreen.Search),
  POSTS(R.drawable.ic_post, DestinationScreen.MyPost),
}

@Composable
fun BottomNavigationMenu(selectedItem: BottomNavItem, navController: NavController) {
  Row(
      modifier =
          Modifier.fillMaxWidth()
              .wrapContentHeight()
              .navigationBarsPadding()
              .padding(top = 4.dp)
              .background(Color.White)
  ) {
    for (item in BottomNavItem.entries) {
      Image(
          painter = painterResource(id = item.icon),
          contentDescription = null,
          modifier =
              Modifier.size(40.dp).padding(5.dp).weight(1f).clickable {
                navigateTo(navController, item.navDestination)
              },
          colorFilter =
              if (item == selectedItem) ColorFilter.tint(Color.Black)
              else ColorFilter.tint(Color.Gray),
      )
    }
  }
}
