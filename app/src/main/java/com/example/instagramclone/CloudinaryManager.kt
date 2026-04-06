package com.example.instagramclone

import android.content.Context
import com.cloudinary.android.MediaManager

object CloudinaryManager {

  fun init(context: Context) {

    val config =
        mapOf(
            "cloud_name" to "dzbmizgxl",
            "api_key" to "483591785932245",
            "api_secret" to "gvVwD-iuDm8v-b4KaM3lPP-bmdo",
        )

    MediaManager.init(context, config)
  }
}
