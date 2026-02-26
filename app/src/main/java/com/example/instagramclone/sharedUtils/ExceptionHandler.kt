package com.example.instagramclone.sharedUtils

import androidx.compose.runtime.mutableStateOf
import com.example.instagramclone.data.Event

fun handleException(exception: Exception? = null, customMessage: String = "") {
  val popupNotification = mutableStateOf<Event<String>?>(null)
  exception?.printStackTrace()

  val errorMsg = exception?.localizedMessage ?: ""
  val message = if (customMessage.isEmpty()) errorMsg else "$customMessage: $errorMsg"
  popupNotification.value = Event(message)
}
