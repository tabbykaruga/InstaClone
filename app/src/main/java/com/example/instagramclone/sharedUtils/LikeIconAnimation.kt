package com.example.instagramclone.sharedUtils

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private enum class LikeIconSize {
  SMALL,
  LARGE,
}

@Composable
fun LikeAnimation(like: Boolean = true) {
  var sizeState by remember { mutableStateOf(LikeIconSize.SMALL) }
  val transition = updateTransition(targetState = sizeState, label = "")
  val size by
      transition.animateDp(
          label = "",
          transitionSpec = {
            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
          },
      ) { state ->
        when (state) {
          LikeIconSize.SMALL -> 0.dp
          LikeIconSize.LARGE -> 150.dp
        }
      }
  Icon(
      imageVector = if (like) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
      contentDescription = "Like",
      tint = if (like) Color.Red else Color.Black,
      modifier = Modifier.size(size),
  )
  sizeState = LikeIconSize.LARGE
}
