package com.example.instagramclone.sharedUtils

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.example.instagramclone.R

@Composable
fun CommonImage(
    data: String?,
    modifier: Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
  SubcomposeAsyncImage(
      model = data,
      contentDescription = null,
      modifier = modifier,
      contentScale = contentScale,
  ) {
    when (painter.state) {
      is AsyncImagePainter.State.Loading -> {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          CommonProgressSpinner()
        }
      }
      is AsyncImagePainter.State.Error -> {
        Image(
            painter = painterResource(id = R.drawable.ic_user),
            contentDescription = null,
            colorFilter = ColorFilter.tint(Color.Gray),
        )
      }
      else -> SubcomposeAsyncImageContent()
    }
  }
}

@Composable
fun ProfileImageCard(userImg: String?, modifier: Modifier) {
  Card(shape = CircleShape, modifier = modifier) {
    if (userImg.isNullOrEmpty()) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(id = R.drawable.ic_user),
            contentDescription = null,
            colorFilter = ColorFilter.tint(Color.Gray),
            modifier = Modifier.size(48.dp),
        )
      }
    } else {
      CommonImage(data = userImg, modifier = Modifier.wrapContentSize())
    }
  }
}
