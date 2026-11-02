package com.example.temolink.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

object ImageUtils {
    /**
     * Decodes a Base64 string (optionally with data URI prefix) into an ImageBitmap.
     */
    fun decodeBase64ToBitmap(base64String: String): ImageBitmap? {
        return try {
            val pureBase64 = if (base64String.contains(",")) {
                base64String.substringAfter(",")
            } else {
                base64String
            }
            val imageBytes = Base64.decode(pureBase64, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            bitmap?.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }
}

@Composable
fun rememberBase64Image(base64String: String): ImageBitmap? {
    return remember(base64String) {
        if (base64String.isEmpty()) null
        else ImageUtils.decodeBase64ToBitmap(base64String)
    }
}
