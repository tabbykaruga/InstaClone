package com.example.instagramclone.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.instagramclone.R
import com.example.instagramclone.sharedUtils.ProfileImageCard

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
