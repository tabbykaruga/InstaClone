package com.example.instagramclone.sharedUtils

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.instagramclone.viewModel.AuthViewModel

@Composable
fun NotificationMessage(vm: AuthViewModel) {
  val notificationState = vm.popupNotification.value
  val notificationMsg = notificationState?.getContentOrNull()
  if (notificationMsg != null) {
    Toast.makeText(LocalContext.current, notificationMsg, Toast.LENGTH_LONG).show()
  }
}
