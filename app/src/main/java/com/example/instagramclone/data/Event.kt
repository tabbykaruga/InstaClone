package com.example.instagramclone.data

open class Event<out T>(private val content: T) {

  // to only be set withing the class event
  var hasBeenHandled = false
    private set

  fun getContentOrNull(): T? {
    return if (hasBeenHandled) {
      null
    } else {
      hasBeenHandled = true
      content
    }
  }
}
