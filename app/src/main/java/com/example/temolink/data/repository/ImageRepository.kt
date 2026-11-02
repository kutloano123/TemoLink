package com.example.temolink.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.cloudinary.android.MediaManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

class ImageRepository(private val context: Context) {
    private val TAG = "ImageRepository"
    private val CLOUD_NAME = "fpnybxkt"
    private val API_KEY = "472163979416742"
    private val PRESET = "temolink_produce_upload"

    /**
     * Uploads an image directly to Cloudinary using the underlying Uploader.
     * This bypasses the background scheduler to prevent timeouts and ensures 
     * the request is processed immediately.
     */
    suspend fun uploadImage(uri: Uri, folder: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                // Set a generous 2-minute timeout for the network request
                withTimeout(120000) {
                    // 1. Convert Uri to ByteArray for reliable data access
                    val inputStream = context.contentResolver.openInputStream(uri)
                        ?: return@withTimeout Result.failure(Exception("Could not open selected image"))
                    val bytes = inputStream.use { it.readBytes() }

                    Log.d(TAG, "Starting direct unsigned upload to Cloudinary...")

                    // 2. Perform direct unsigned upload. 
                    // We include credentials and 'unsigned' flag to satisfy SDK requirements.
                    val options = mapOf(
                        "upload_preset" to PRESET,
                        "unsigned" to true,
                        "cloud_name" to CLOUD_NAME,
                        "api_key" to API_KEY,
                        "folder" to "temolink/$folder"
                    )

                    // This is a blocking network call executed on Dispatchers.IO
                    val result = MediaManager.get().cloudinary.uploader().upload(bytes, options)

                    // 3. Extract the permanent HTTPS secure_url
                    val secureUrl = result["secure_url"] as? String
                    if (secureUrl != null) {
                        Log.d(TAG, "Upload successful: $secureUrl")
                        Result.success(secureUrl)
                    } else {
                        Result.failure(Exception("Cloudinary response missing secure_url"))
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Direct upload failed: ${e.message}", e)
                Result.failure(e)
            }
        }
    }
}
