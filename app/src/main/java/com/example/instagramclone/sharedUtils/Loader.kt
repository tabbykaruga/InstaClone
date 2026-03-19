package com.example.instagramclone.sharedUtils

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun CommonProgressSpinner() {
  Row(
      modifier =
          Modifier.alpha(0.5f).background(Color.Gray).clickable(enabled = false) {}.fillMaxSize(),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically,
  ) {
    CircularProgressIndicator()
  }
}

@Composable
fun ShimmerEffect(modifier: Modifier = Modifier) {
  val transition = rememberInfiniteTransition(label = "shimmer")
  val translateAnim by
      transition.animateFloat(
          initialValue = 0f,
          targetValue = 1000f,
          animationSpec =
              infiniteRepeatable(
                  animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
                  repeatMode = RepeatMode.Restart,
              ),
          label = "shimmer",
      )

  val brush =
      Brush.linearGradient(
          colors =
              listOf(
                  Color(0xFFE0E0E0),
                  Color(0xFFF5F5F5),
                  Color(0xFFE0E0E0),
              ),
          start = Offset(translateAnim - 200f, 0f),
          end = Offset(translateAnim, 0f),
      )

  Box(modifier = modifier.background(brush))
}

@Composable
fun CommentsShimmer() {
  Row(
      modifier = Modifier.fillMaxWidth().padding(8.dp),
      verticalAlignment = Alignment.CenterVertically,
  ) {
    // Avatar circle shimmer
    ShimmerEffect(modifier = Modifier.padding(4.dp).size(32.dp).clip(CircleShape))
    Column(modifier = Modifier.padding(start = 8.dp)) {
      // Username shimmer
      ShimmerEffect(modifier = Modifier.width(100.dp).height(12.dp).clip(RoundedCornerShape(4.dp)))
      Spacer(modifier = Modifier.height(4.dp))
      // Comment text shimmer
      ShimmerEffect(modifier = Modifier.width(200.dp).height(10.dp).clip(RoundedCornerShape(4.dp)))
    }
  }
}

@Composable
fun FeedScreenShimmer() {
  Card(
      shape = RoundedCornerShape(corner = CornerSize(4.dp)),
      modifier = Modifier.fillMaxWidth().wrapContentHeight().padding(top = 4.dp, bottom = 4.dp),
  ) {
    Column {
      // Header row - avatar + username
      Row(
          modifier = Modifier.fillMaxWidth().height(50.dp),
          verticalAlignment = Alignment.CenterVertically,
      ) {
        ShimmerEffect(modifier = Modifier.padding(4.dp).size(32.dp).clip(CircleShape))
        Column(modifier = Modifier.padding(start = 8.dp)) {
          ShimmerEffect(
              modifier = Modifier.width(120.dp).height(12.dp).clip(RoundedCornerShape(4.dp))
          )
          Spacer(modifier = Modifier.height(4.dp))
          ShimmerEffect(
              modifier = Modifier.width(80.dp).height(10.dp).clip(RoundedCornerShape(4.dp))
          )
        }
      }
      // Post image shimmer
      ShimmerEffect(modifier = Modifier.fillMaxWidth().height(300.dp))

      // Likes & comments row
      Row(
          modifier = Modifier.padding(12.dp),
          verticalAlignment = Alignment.CenterVertically,
      ) {
        ShimmerEffect(modifier = Modifier.size(24.dp).clip(CircleShape))
        ShimmerEffect(
            modifier =
                Modifier.padding(start = 8.dp)
                    .width(60.dp)
                    .height(10.dp)
                    .clip(RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.weight(1f))
        ShimmerEffect(modifier = Modifier.size(24.dp).clip(CircleShape))
        ShimmerEffect(
            modifier =
                Modifier.padding(start = 4.dp)
                    .width(60.dp)
                    .height(10.dp)
                    .clip(RoundedCornerShape(4.dp))
        )
      }
    }
  }
}

@Composable
fun MyPostGridShimmer() {
  LazyColumn {
    items(10) { // 4 shimmer rows
      Row(modifier = Modifier.fillMaxWidth().height(120.dp)) {
        repeat(3) { // 3 columns per row
          ShimmerEffect(modifier = Modifier.weight(1f).fillMaxHeight().padding(1.dp))
        }
      }
    }
  }
}

@Composable
fun MyPostScreenShimmer() {
  Column(modifier = Modifier.padding(top = 20.dp)) {
    // Avatar + stats row
    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
      // Avatar circle
      ShimmerEffect(modifier = Modifier.size(80.dp).clip(CircleShape))
      // Posts / Followers / Following
      repeat(3) {
        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
          ShimmerEffect(
              modifier = Modifier.width(30.dp).height(14.dp).clip(RoundedCornerShape(4.dp))
          )
          Spacer(modifier = Modifier.height(4.dp))
          ShimmerEffect(
              modifier = Modifier.width(50.dp).height(10.dp).clip(RoundedCornerShape(4.dp))
          )
        }
      }
    }

    // Name + username + bio
    Column(modifier = Modifier.padding(18.dp)) {
      ShimmerEffect(modifier = Modifier.width(140.dp).height(14.dp).clip(RoundedCornerShape(4.dp)))
      Spacer(modifier = Modifier.height(6.dp))
      ShimmerEffect(modifier = Modifier.width(100.dp).height(12.dp).clip(RoundedCornerShape(4.dp)))
      Spacer(modifier = Modifier.height(6.dp))
      ShimmerEffect(modifier = Modifier.width(200.dp).height(10.dp).clip(RoundedCornerShape(4.dp)))
    }

    // Edit Profile button shimmer
    ShimmerEffect(
        modifier = Modifier.padding(18.dp).fillMaxWidth().height(36.dp).clip(RoundedCornerShape(10))
    )
  }
}

@Composable
fun MySearchScreenShimmer() {
  ShimmerEffect(modifier = Modifier.padding(8.dp).fillMaxWidth().height(56.dp).clip(CircleShape))
}
