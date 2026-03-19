package com.example.instagramclone.sharedUtils

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.example.instagramclone.R
import java.io.File

@Composable
fun CommonImage(
    data: String?,
    modifier: Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
  val context = LocalContext.current

  key(data) {
    val model =
        remember(data) {
          if (data != null && data.startsWith("/")) {
            val file = File(data)
            ImageRequest.Builder(context)
                .data(file)
                .memoryCacheKey("${data}_${file.lastModified()}")
                .diskCacheKey("${data}_${file.lastModified()}")
                .crossfade(true)
                .build()
          } else {
            ImageRequest.Builder(context).data(data).crossfade(true).build()
          }
        }

    SubcomposeAsyncImage(
        model = model,
        contentDescription = null,
        modifier = modifier,
        contentScale = contentScale,
    ) {
      when (painter.state) {
        is AsyncImagePainter.State.Loading -> {
          ShimmerEffect(modifier = Modifier.fillMaxSize())
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
}

@Composable
fun GeneralPostImage(
    data: String?,
    modifier: Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
  val context = LocalContext.current

  val model =
      remember(data) {
        ImageRequest.Builder(context)
            .data(data) // Cloudinary URL
            .crossfade(true)
            .diskCachePolicy(coil.request.CachePolicy.ENABLED)
            .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
            .build()
      }

  SubcomposeAsyncImage(
      model = model,
      contentDescription = null,
      modifier = modifier,
      contentScale = contentScale,
  ) {
    when (painter.state) {
      is AsyncImagePainter.State.Loading -> {
        ShimmerEffect(modifier = Modifier.fillMaxSize())
      }

      is AsyncImagePainter.State.Error -> {
        Image(
            painter = painterResource(id = R.drawable.ic_user),
            contentDescription = null,
            colorFilter = ColorFilter.tint(Color.Gray),
        )
      }

      else -> {
        SubcomposeAsyncImageContent()
      }
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
