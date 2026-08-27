package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object UrlUtils {
    /**
     * Safely opens any web URL or custom payment link in an external browser / app.
     */
    fun openExternalUrl(context: Context, url: String?) {
        if (url.isNullOrBlank()) {
            Toast.makeText(context, "No URL provided", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            var formattedUrl = url.trim()
            if (!formattedUrl.startsWith("http://") &&
                !formattedUrl.startsWith("https://") &&
                !formattedUrl.startsWith("upi://") &&
                !formattedUrl.startsWith("mailto:") &&
                !formattedUrl.startsWith("tel:")
            ) {
                formattedUrl = "https://$formattedUrl"
            }

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(formattedUrl)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                // Fallback attempt with explicit browser intent if default handler throws
                val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(fallbackIntent)
            } catch (ex: Exception) {
                Toast.makeText(context, "Could not open link: ${ex.localizedMessage ?: "No browser found"}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
