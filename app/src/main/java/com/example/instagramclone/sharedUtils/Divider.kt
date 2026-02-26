package com.example.instagramclone.sharedUtils

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun Divider() {
  HorizontalDivider(
      modifier = Modifier.alpha(0.3f).padding(top = 8.dp, bottom = 8.dp),
      DividerDefaults.Thickness,
      color = Color.LightGray,
  )
}
