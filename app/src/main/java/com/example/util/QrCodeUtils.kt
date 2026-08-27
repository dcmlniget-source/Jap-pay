package com.example.util

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import com.google.zxing.qrcode.QRCodeWriter

object QrCodeUtils {
    fun generateQrBitmap(content: String, size: Int = 512): Bitmap? {
        return try {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size)
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            for (x in 0 until size) {
                for (y in 0 until size) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            bitmap
        } catch (_: Exception) {
            null
        }
    }

    fun decodeQrFromBitmap(bitmap: Bitmap): String? {
        return try {
            val width = bitmap.width
            val height = bitmap.height
            val pixels = IntArray(width * height)
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

            val source = RGBLuminanceSource(width, height, pixels)
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
            val reader = QRCodeReader()
            val result = reader.decode(binaryBitmap)
            result.text
        } catch (_: Exception) {
            null
        }
    }

    fun parseJapPayUri(data: String): Pair<String, String>? {
        // e.g. "jappay://pay?id=8791738300@jap&name=Devansh" or "8791738300@jap" or standard UPI
        val clean = data.trim()
        if (clean.startsWith("jappay://pay")) {
            val id = clean.substringAfter("id=").substringBefore("&")
            val name = clean.substringAfter("name=", "").substringBefore("&").replace("+", " ")
            return Pair(id, if (name.isNotEmpty()) name else id)
        } else if (clean.startsWith("upi://pay")) {
            val pa = clean.substringAfter("pa=").substringBefore("&")
            val pn = clean.substringAfter("pn=", "").substringBefore("&").replace("+", " ")
            val handle = if (pa.endsWith("@jap")) pa else "$pa@jap"
            return Pair(handle, if (pn.isNotEmpty()) pn else handle)
        } else if (clean.contains("@jap") || clean.all { it.isDigit() }) {
            val handle = if (clean.endsWith("@jap")) clean else "$clean@jap"
            return Pair(handle, handle)
        }
        return null
    }
}
