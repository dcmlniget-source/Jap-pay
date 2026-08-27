package com.example.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.EncodeHintType
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

object QrCodeUtils {
    fun generateQrBitmap(content: String, size: Int = 512): Bitmap? {
        return try {
            val hints = mapOf(
                EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H,
                EncodeHintType.MARGIN to 1
            )
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints)
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            for (x in 0 until size) {
                for (y in 0 until size) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.parseColor("#1E293B") else Color.WHITE)
                }
            }

            // Draw Jap Pay Logo in the center of the QR code
            val canvas = Canvas(bitmap)
            val logoSize = size * 0.22f
            val logoLeft = (size - logoSize) / 2f
            val logoTop = (size - logoSize) / 2f
            val logoRect = RectF(logoLeft, logoTop, logoLeft + logoSize, logoTop + logoSize)

            // Outer white padding ring for clean scanning contrast
            val whitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                style = Paint.Style.FILL
            }
            val cornerRadius = logoSize * 0.28f
            canvas.drawRoundRect(logoRect, cornerRadius, cornerRadius, whitePaint)

            // Inner Brand purple background
            val innerPadding = logoSize * 0.08f
            val innerRect = RectF(
                logoLeft + innerPadding,
                logoTop + innerPadding,
                logoLeft + logoSize - innerPadding,
                logoTop + logoSize - innerPadding
            )
            val brandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#5F259F") // PhonePe / Jap purple
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(innerRect, cornerRadius * 0.85f, cornerRadius * 0.85f, brandPaint)

            // Draw clean Lightning Bolt symbol inside logo
            val boltPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#FFD600") // Electric Yellow
                style = Paint.Style.FILL
            }
            val cx = size / 2f
            val cy = size / 2f
            val boltW = logoSize * 0.35f
            val boltH = logoSize * 0.52f

            val path = Path().apply {
                moveTo(cx + boltW * 0.1f, cy - boltH * 0.5f)
                lineTo(cx - boltW * 0.45f, cy + boltH * 0.05f)
                lineTo(cx - boltW * 0.05f, cy + boltH * 0.05f)
                lineTo(cx - boltW * 0.2f, cy + boltH * 0.5f)
                lineTo(cx + boltW * 0.45f, cy - boltH * 0.05f)
                lineTo(cx + boltW * 0.05f, cy - boltH * 0.05f)
                close()
            }
            canvas.drawPath(path, boltPaint)

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

