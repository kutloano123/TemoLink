package com.example.temolink

import android.app.Application
import android.util.Log
import com.cloudinary.android.MediaManager

class TemoLinkApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Cloudinary with your specific credentials
        // API Key is used to identify the environment; API Secret is NEVER included in the app.
        val config = mapOf(
            "cloud_name" to "fpnybxkt",
            "api_key" to "472163979416742",
            "secure" to true
        )
        
        try {
            MediaManager.init(this, config)
            Log.d("TemoLinkApp", "Cloudinary initialized successfully")
        } catch (e: Exception) {
            Log.e("TemoLinkApp", "Cloudinary initialization error: ${e.message}")
        }
    }
}
