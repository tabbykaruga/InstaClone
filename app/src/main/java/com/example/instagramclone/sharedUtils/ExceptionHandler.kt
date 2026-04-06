package com.example.instagramclone.sharedUtils

import androidx.compose.runtime.MutableState
import com.example.instagramclone.data.Event
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException

fun handleException(
    popupNotification: MutableState<Event<String>?>,
    exception: Exception? = null,
    customMessage: String = "",
) {
  exception?.printStackTrace()

  val errorMsg =
      when (exception) {
        // Firebase Auth errors
          is FirebaseAuthException -> when {
              exception.errorCode == "ERROR_INVALID_EMAIL" -> "The email address is not valid"
              exception.errorCode == "ERROR_USER_NOT_FOUND" -> "No account found with this email"
              exception.errorCode == "ERROR_WRONG_PASSWORD" -> "Incorrect password, please try again"
              exception.errorCode == "ERROR_USER_DISABLED" -> "This account has been disabled"
              exception.errorCode == "ERROR_EMAIL_ALREADY_IN_USE" -> "An account with this email already exists"
              exception.errorCode == "ERROR_WEAK_PASSWORD" -> "Password must be at least 6 characters"
              exception.errorCode == "ERROR_TOO_MANY_REQUESTS" -> "Too many attempts, please try again later"
              exception.errorCode.contains("CREDENTIAL") ||
                      exception.errorCode.contains("LOGIN") ||
                      exception.message?.contains("credential", ignoreCase = true) == true -> "Invalid email or password"
              else -> "Authentication failed, please try again"
          }

        // Network errors
        is FirebaseNetworkException -> "No internet connection, please check your network"

        // Fallback
        else -> exception?.localizedMessage ?: ""
      }

  val message =
      when {
        customMessage.isNotEmpty() && errorMsg.isNotEmpty() -> "$customMessage: $errorMsg"
        customMessage.isNotEmpty() -> customMessage
        else -> errorMsg
      }

  popupNotification.value = Event(message)
}
