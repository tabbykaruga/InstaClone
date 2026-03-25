package com.example.instagramclone.sharedUtils

import android.net.Uri
import com.cloudinary.android.MediaManager

object ImageUploader {

  fun uploadImage(imageUri: Uri, onSuccess: (String) -> Unit, onError: (String) -> Unit) {

    MediaManager.get()
        .upload(imageUri)
        .option("eager", "c_fill,w_1080,h_1080,g_center") // ← no import needed, just a string
        .option("eager_async", false)
        .callback(
            object : com.cloudinary.android.callback.UploadCallback {

              override fun onStart(requestId: String) {}

              override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

              override fun onSuccess(requestId: String, resultData: Map<*, *>) {

                val url = resultData["secure_url"] as String
                onSuccess(url)
              }

              override fun onError(
                  requestId: String,
                  error: com.cloudinary.android.callback.ErrorInfo,
              ) {

                onError(error.description)
              }

              override fun onReschedule(
                  requestId: String,
                  error: com.cloudinary.android.callback.ErrorInfo,
              ) {}
            }
        )
        .dispatch()
  }
}
