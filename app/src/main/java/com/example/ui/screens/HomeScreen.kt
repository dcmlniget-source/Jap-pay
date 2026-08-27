package com.example.ui.screens

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Transaction
import com.example.ui.components.JapAvatar
import com.example.ui.components.JapCameraPreview
import com.example.ui.components.QrCodeDisplay
import com.example.ui.theme.*
import com.example.ui.viewmodel.JapPayViewModel
import com.example.ui.viewmodel.Screen
import com.example.util.QrCodeUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: JapPayViewModel) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val transactions by viewModel.userTransactions.collectAsStateWithLifecycle()
    val otherUsers by viewModel.otherUsers.collectAsStateWithLifecycle()
    val unreadCount by viewModel.unreadNotificationsCount.collectAsStateWithLifecycle()
    val adminConfig by viewModel.adminConfig.collectAsStateWithLifecycle()

    var showCameraView by remember { mutableStateOf(false) }
    var isTorchOn by remember { mutableStateOf(false) }
    var showQrModal by remember { mutableStateOf(false) }
    var showBalanceModal by remember { mutableStateOf(false) }

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

    if (showQrModal && currentUser != null) {
        ModalBottomSheet(
            onDismissRequest = { showQrModal = false },
            containerColor = LightSurface,
            dragHandle = { BottomSheetDefaults.DragHandle(color = LightCardBorder) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                JapAvatar(name = currentUser!!.name, colorIndex = currentUser!!.avatarColorIndex, size = 64)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = currentUser!!.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = currentUser!!.id,
                    style = MaterialTheme.typography.bodyMedium,
                    color = BrandPurple,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(20.dp))

                QrCodeDisplay(
                    content = "jappay://pay?id=${currentUser!!.id}&name=${currentUser!!.name}",
                    size = 230
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Scan with any Jap Pay app to send money instantly",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showBalanceModal && currentUser != null) {
        ModalBottomSheet(
            onDismissRequest = { showBalanceModal = false },
            containerColor = LightSurface,
            dragHandle = { BottomSheetDefaults.DragHandle(color = LightCardBorder) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Total Available Balance",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "₹${"%.2f".format(currentUser!!.walletBalance)}",
                    style = MaterialTheme.typography.displayMedium,
                    color = BrandPurple,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = LightCardElevated),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(LightCardBorder, LightCardBorder))),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(AccentGreenBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Savings, contentDescription = null, tint = AccentGreen)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Keeper Savings Jar", color = TextPrimary, fontWeight = FontWeight.Bold)
                            Text("Earn 3% per year", color = TextSecondary, fontSize = 12.sp)
                        }
                        Text(
                            text = "₹${"%.2f".format(currentUser!!.keeperBalance)}",
                            color = AccentGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        showBalanceModal = false
                        viewModel.currentScreen.value = Screen.AddMoney
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPurple, contentColor = Color.White)
                ) {
                    Text("+ Add Money to Wallet", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp)
    ) {
        // 1. PhonePe Top App Bar (Avatar, Jap ID, Scan & Notifications)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                JapAvatar(
                    name = currentUser?.name ?: "User",
                    colorIndex = currentUser?.avatarColorIndex ?: 0,
                    size = 44,
                    modifier = Modifier.clickable { showQrModal = true }
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showQrModal = true }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = currentUser?.name ?: "User",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.Verified, contentDescription = null, tint = BrandPurple, modifier = Modifier.size(16.dp))
                    }
                    Text(
                        text = currentUser?.id ?: "@jap",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }

                // QR Icon
                IconButton(
                    onClick = { showQrModal = true },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(BrandPurpleTint)
                ) {
                    Icon(
                        Icons.Default.QrCode2,
                        contentDescription = "My QR",
                        tint = BrandPurple,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Notification Icon with Badge
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(BrandPurpleTint)
                        .clickable { viewModel.currentScreen.value = Screen.Notifications },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Notifications,
                        contentDescription = "Notifications",
                        tint = BrandPurple,
                        modifier = Modifier.size(20.dp)
                    )
                    if (unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(AccentRed)
                                .align(Alignment.TopEnd)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // 2. Search & Pay Bar
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { viewModel.currentScreen.value = Screen.SendMoney }
                    .border(1.dp, LightCardBorder, RoundedCornerShape(16.dp)),
                color = LightSurface,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Search by name, phone or @jap ID...",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(Icons.Default.ContactPhone, contentDescription = null, tint = BrandPurple, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // 3. PhonePe-style "Transfer Money" 4-tile Clean White Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(3.dp, RoundedCornerShape(20.dp), spotColor = Color(0x15000000)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = LightSurface),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(LightCardBorder, LightCardBorder)))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Transfer Money",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TransferActionTile(
                            icon = Icons.Default.Person,
                            label = "To Mobile / @jap",
                            onClick = { viewModel.currentScreen.value = Screen.SendMoney }
                        )
                        TransferActionTile(
                            icon = Icons.Default.AccountBalance,
                            label = "To Bank / Self",
                            onClick = { viewModel.currentScreen.value = Screen.SendMoney }
                        )
                        TransferActionTile(
                            icon = Icons.Default.AddCard,
                            label = "Add Money",
                            onClick = { viewModel.currentScreen.value = Screen.AddMoney }
                        )
                        TransferActionTile(
                            icon = Icons.Default.AccountBalanceWallet,
                            label = "Check Balance",
                            onClick = { showBalanceModal = true }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = LightDivider)
                    Spacer(modifier = Modifier.height(12.dp))

                    // UPI ID Badge / Copy Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(BrandPurpleTint.copy(alpha = 0.5f))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "My Jap UPI ID: ",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = currentUser?.id ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = BrandPurple,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        IconButton(
                            onClick = {
                                currentUser?.id?.let {
                                    clipboardManager.setText(AnnotatedString(it))
                                    viewModel.showToast("UPI ID copied: $it")
                                }
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = BrandPurple, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // 4. Live Camera / Scan & Pay Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .shadow(3.dp, RoundedCornerShape(20.dp), spotColor = Color(0x15000000)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = LightSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(listOf(LightCardBorder, BrandPurple.copy(alpha = 0.4f)))
                )
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (showCameraView) {
                        JapCameraPreview(
                            modifier = Modifier.fillMaxSize(),
                            isFlashOn = isTorchOn
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.radialGradient(
                                        listOf(BrandPurpleTint.copy(alpha = 0.4f), LightSurface),
                                        radius = 400f
                                    )
                                ),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(BrandPurpleTint)
                                    .border(1.5.dp, BrandPurple, RoundedCornerShape(20.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.QrCodeScanner,
                                    contentDescription = null,
                                    tint = BrandPurple,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Scan & Pay Any QR Code",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Real-time scanner with Jap Pay logo verification",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }

                    // Floating Controls Overlay
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(14.dp),
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
                            onClick = { showCameraView = !showCameraView },
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (showCameraView) BrandPurple else BrandPurpleDark,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                if (showCameraView) Icons.Default.Videocam else Icons.Default.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (showCameraView) "Camera Active" else "Open Camera", fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
                                tint = if (isTorchOn) BrandPurple else TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }

        // 5. Quick Pay Contacts / People
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quick Send",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "View All",
                    color = BrandPurple,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { viewModel.currentScreen.value = Screen.SendMoney }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(otherUsers) { user ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.prepareTransfer(user.id, user.name) }
                            .padding(horizontal = 2.dp)
                    ) {
                        JapAvatar(name = user.name, colorIndex = user.avatarColorIndex, size = 52)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = user.name.substringBefore(" "),
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                        Text(
                            text = "₹ pay",
                            color = BrandPurple,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // 6. Recharge & Bill Payments Section (PhonePe style grid)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(20.dp), spotColor = Color(0x12000000)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = LightSurface),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(LightCardBorder, LightCardBorder)))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Recharge & Pay Bills",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        BillServiceTile(icon = Icons.Default.PhoneAndroid, label = "Mobile\nRecharge", color = Color(0xFF1E88E5)) {
                            viewModel.showToast("Mobile Recharge service")
                        }
                        BillServiceTile(icon = Icons.Default.Tv, label = "DTH\nTV", color = Color(0xFFFB8C00)) {
                            viewModel.showToast("DTH recharge service")
                        }
                        BillServiceTile(icon = Icons.Default.ElectricBolt, label = "Electricity\nBill", color = Color(0xFFE53935)) {
                            viewModel.showToast("Electricity bill payment")
                        }
                        BillServiceTile(icon = Icons.Default.DirectionsCar, label = "FASTag\nRecharge", color = Color(0xFF43A047)) {
                            viewModel.showToast("FASTag recharge")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // 7. Recent Transactions Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "History",
                    color = BrandPurple,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { viewModel.currentScreen.value = Screen.TransactionHistory }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        if (transactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = LightSurface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(LightCardBorder, LightCardBorder)))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No transactions yet. Start sending money!", color = TextSecondary, fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(transactions.take(5)) { tx ->
                TransactionItemRow(tx = tx, currentUserId = currentUser?.id ?: "")
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun TransferActionTile(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(72.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(BrandPurpleTint),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = BrandPurple, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            color = TextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 14.sp
        )
    }
}

@Composable
fun BillServiceTile(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(72.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            color = TextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 14.sp
        )
    }
}

@Composable
fun TransactionItemRow(tx: Transaction, currentUserId: String) {
    val isCredit = tx.receiverId == currentUserId
    val isKeeper = tx.type == "KEEPER_SAVE" || tx.type == "KEEPER_WITHDRAW"
    val dateStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(tx.timestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(16.dp), spotColor = Color(0x0F000000)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(LightCardBorder, LightCardBorder)))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (isCredit) AccentGreenBg else if (isKeeper) BrandPurpleTint else AccentRedBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        isKeeper -> Icons.Default.Savings
                        isCredit -> Icons.Default.ArrowDownward
                        else -> Icons.Default.ArrowUpward
                    },
                    contentDescription = null,
                    tint = if (isCredit) AccentGreen else if (isKeeper) BrandPurple else AccentRed,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isCredit) tx.senderName else tx.receiverName,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = if (tx.message.isNotEmpty()) tx.message else dateStr,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (isCredit) "+" else "-"}₹${"%.2f".format(tx.amount)}",
                    color = if (isCredit) AccentGreen else TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = if (isCredit) "Received" else "Paid",
                    color = if (isCredit) AccentGreen else TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

