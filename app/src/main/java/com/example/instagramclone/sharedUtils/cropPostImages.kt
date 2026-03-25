package com.example.instagramclone.sharedUtils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream

suspend fun cropAndResizeImage(context: Context, imageUri: Uri): Uri {
  val bitmap =
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, imageUri))
      } else {
        @Suppress("DEPRECATION")
        MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
      }

  // Target size — square crop
  val targetSize = 1080

  // Crop to square from center first
  val srcWidth = bitmap.width
  val srcHeight = bitmap.height
  val cropSize = minOf(srcWidth, srcHeight)
  val xOffset = (srcWidth - cropSize) / 2
  val yOffset = (srcHeight - cropSize) / 2

  val croppedBitmap = Bitmap.createBitmap(bitmap, xOffset, yOffset, cropSize, cropSize)

  // Scale down to target size
  val scaledBitmap = Bitmap.createScaledBitmap(croppedBitmap, targetSize, targetSize, true)

  // Save to temp file
  val tempFile = File(context.cacheDir, "cropped_${System.currentTimeMillis()}.jpg")
  FileOutputStream(tempFile).use { out ->
    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
  }

  return Uri.fromFile(tempFile)
}
