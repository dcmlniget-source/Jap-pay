package com.example.ui.screens

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.JapCameraPreview
import com.example.ui.theme.*
import com.example.ui.viewmodel.JapPayViewModel
import com.example.ui.viewmodel.Screen
import com.example.util.QrCodeUtils

@Composable
fun CompactQrScannerCard(
    viewModel: JapPayViewModel,
    onExpandToFullScreen: () -> Unit
) {
    val context = LocalContext.current
    var isCameraActive by remember { mutableStateOf(false) }
    var isTorchOn by remember { mutableStateOf(false) }

    val qrGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                val stream = context.contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(stream)
                if (bitmap != null) {
                    val decoded = QrCodeUtils.decodeQrFromBitmap(bitmap)
                    if (decoded != null) {
                        val parsed = QrCodeUtils.parseJapPayUri(decoded)
                        if (parsed != null) {
                            viewModel.prepareTransfer(parsed.first, parsed.second)
                        } else {
                            viewModel.prepareTransfer(decoded, decoded)
                        }
                    } else {
                        viewModel.showToast("No QR code found in selected image")
                    }
                }
            } catch (_: Exception) {}
        }
    }

    // 1:1 Aspect Ratio Card
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.0f)
            .shadow(4.dp, RoundedCornerShape(24.dp), spotColor = Color(0x18000000))
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { onExpandToFullScreen() }
                )
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(listOf(BrandPurple.copy(alpha = 0.6f), AccentGold.copy(alpha = 0.6f)))
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (isCameraActive) {
                JapCameraPreview(
                    modifier = Modifier.fillMaxSize(),
                    isFlashOn = isTorchOn
                )
                // Scanner Line Animation Overlay
                ScannerLaserOverlay(modifier = Modifier.fillMaxSize())
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                listOf(BrandPurpleTint.copy(alpha = 0.6f), LightSurface),
                                radius = 500f
                            )
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(BrandPurpleTint)
                            .border(2.dp, BrandPurple, RoundedCornerShape(22.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = BrandPurple,
                            modifier = Modifier.size(54.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Scan & Pay (1:1 Mode)",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Double-tap to open Full-Screen Scanner",
                        style = MaterialTheme.typography.bodySmall,
                        color = BrandPurple,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Top-right Expand button badge
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .clip(CircleShape)
                    .clickable { onExpandToFullScreen() },
                color = Color.Black.copy(alpha = 0.6f),
                shape = CircleShape
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Fullscreen, contentDescription = "Full Screen", tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Full Screen", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Floating Controls at Bottom
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { qrGalleryLauncher.launch("image/*") },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.9f))
                        .shadow(2.dp, CircleShape)
                ) {
                    Icon(Icons.Default.Image, contentDescription = "Gallery", tint = BrandPurple, modifier = Modifier.size(20.dp))
                }

                Button(
                    onClick = { isCameraActive = !isCameraActive },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCameraActive) BrandPurple else BrandPurpleDark,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        if (isCameraActive) Icons.Default.Videocam else Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isCameraActive) "Live View" else "Start Scan", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                IconButton(
                    onClick = { isTorchOn = !isTorchOn },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.9f))
                        .shadow(2.dp, CircleShape)
                ) {
                    Icon(
                        if (isTorchOn) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                        contentDescription = "Flash",
                        tint = if (isTorchOn) AccentGold else TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FullScreenScannerView(
    viewModel: JapPayViewModel,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    var isTorchOn by remember { mutableStateOf(false) }

    val qrGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                val stream = context.contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(stream)
                if (bitmap != null) {
                    val decoded = QrCodeUtils.decodeQrFromBitmap(bitmap)
                    if (decoded != null) {
                        val parsed = QrCodeUtils.parseJapPayUri(decoded)
                        if (parsed != null) {
                            viewModel.prepareTransfer(parsed.first, parsed.second)
                        } else {
                            viewModel.prepareTransfer(decoded, decoded)
                        }
                    } else {
                        viewModel.showToast("No QR code found in selected photo")
                    }
                }
            } catch (_: Exception) {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { onClose() }
                )
            }
    ) {
        // Live Full-screen camera preview
        JapCameraPreview(
            modifier = Modifier.fillMaxSize(),
            isFlashOn = isTorchOn
        )

        // Darkened Scrim Overlay with Viewfinder cutout
        FullScreenScannerHUD()

        // Top App Bar Controls
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onClose() },
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f))
            ) {
                Icon(Icons.Default.FullscreenExit, contentDescription = "Minimize", tint = Color.White)
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Black.copy(alpha = 0.6f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(AccentGreen)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Jap Pay Ultra Scan", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = { isTorchOn = !isTorchOn },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                ) {
                    Icon(
                        if (isTorchOn) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                        contentDescription = "Torch",
                        tint = if (isTorchOn) AccentGold else Color.White
                    )
                }

                IconButton(
                    onClick = { qrGalleryLauncher.launch("image/*") },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                ) {
                    Icon(Icons.Default.Image, contentDescription = "Gallery", tint = Color.White)
                }
            }
        }

        // Bottom Hints
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.Black.copy(alpha = 0.7f)
            ) {
                Text(
                    text = "Point camera at any Jap Pay or UPI QR Code\nDouble-tap anywhere to return to 1:1 view",
                    color = Color.White,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun FullScreenScannerHUD() {
    val infiniteTransition = rememberInfiniteTransition(label = "LaserAnimation")
    val laserPosition by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LaserPosition"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val boxSize = w * 0.72f
        val left = (w - boxSize) / 2f
        val top = (h - boxSize) / 2f
        val right = left + boxSize
        val bottom = top + boxSize
        val cornerLength = 36.dp.toPx()
        val strokeWidth = 4.dp.toPx()

        // Top corner brackets (Gold & Purple)
        // Top-left
        drawLine(Color(0xFFFFD700), Offset(left, top), Offset(left + cornerLength, top), strokeWidth)
        drawLine(Color(0xFFFFD700), Offset(left, top), Offset(left, top + cornerLength), strokeWidth)

        // Top-right
        drawLine(Color(0xFFFFD700), Offset(right, top), Offset(right - cornerLength, top), strokeWidth)
        drawLine(Color(0xFFFFD700), Offset(right, top), Offset(right, top + cornerLength), strokeWidth)

        // Bottom-left
        drawLine(Color(0xFF7C4DFF), Offset(left, bottom), Offset(left + cornerLength, bottom), strokeWidth)
        drawLine(Color(0xFF7C4DFF), Offset(left, bottom), Offset(left, bottom - cornerLength), strokeWidth)

        // Bottom-right
        drawLine(Color(0xFF7C4DFF), Offset(right, bottom), Offset(right - cornerLength, bottom), strokeWidth)
        drawLine(Color(0xFF7C4DFF), Offset(right, bottom), Offset(right, bottom - cornerLength), strokeWidth)

        // Animated Horizontal Laser Beam
        val laserY = top + (boxSize * laserPosition)
        drawLine(
            brush = Brush.horizontalGradient(
                listOf(Color.Transparent, Color(0xFFFFD700), Color(0xFF7C4DFF), Color(0xFFFFD700), Color.Transparent),
                startX = left,
                endX = right
            ),
            start = Offset(left, laserY),
            end = Offset(right, laserY),
            strokeWidth = 3.dp.toPx()
        )
    }
}

@Composable
fun ScannerLaserOverlay(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "CompactLaserAnimation")
    val laserPosition by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "CompactLaserPos"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val laserY = h * laserPosition

        drawLine(
            brush = Brush.horizontalGradient(
                listOf(Color.Transparent, Color(0xFF7C4DFF), Color(0xFFFFD700), Color(0xFF7C4DFF), Color.Transparent)
            ),
            start = Offset(0f, laserY),
            end = Offset(w, laserY),
            strokeWidth = 3.dp.toPx()
        )
    }
}
