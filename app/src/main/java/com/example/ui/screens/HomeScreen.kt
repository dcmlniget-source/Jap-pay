package com.example.ui.screens

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Transaction
import com.example.data.model.User
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
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val transactions by viewModel.userTransactions.collectAsStateWithLifecycle()
    val otherUsers by viewModel.otherUsers.collectAsStateWithLifecycle()
    val unreadCount by viewModel.unreadNotificationsCount.collectAsStateWithLifecycle()

    var showCameraView by remember { mutableStateOf(false) }
    var isTorchOn by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
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
                        viewModel.showToast("No QR code found in photo")
                    }
                }
            } catch (_: Exception) {}
        }
    }

    if (showQrModal && currentUser != null) {
        ModalBottomSheet(
            onDismissRequest = { showQrModal = false },
            containerColor = DarkSurface,
            dragHandle = { BottomSheetDefaults.DragHandle(color = DarkCardBorder) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = currentUser!!.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = currentUser!!.id,
                    style = MaterialTheme.typography.bodyMedium,
                    color = JapYellowPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(20.dp))

                QrCodeDisplay(content = "jappay://pay?id=${currentUser!!.id}&name=${currentUser!!.name}", size = 220)

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Scan with any Jap Pay app to receive virtual money",
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
            containerColor = DarkSurface,
            dragHandle = { BottomSheetDefaults.DragHandle(color = DarkCardBorder) }
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
                    color = JapYellowPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCardElevated),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Savings, contentDescription = null, tint = AccentGreen)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Keeper Savings Jar", color = TextPrimary, fontWeight = FontWeight.SemiBold)
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
                    colors = ButtonDefaults.buttonColors(containerColor = JapYellowPrimary, contentColor = DarkBackground)
                ) {
                    Text("+ Add Money to Wallet", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp)
    ) {
        // 1. Top Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                JapAvatar(
                    name = currentUser?.name ?: "Devansh",
                    colorIndex = currentUser?.avatarColorIndex ?: 0,
                    size = 40,
                    modifier = Modifier.clickable { showQrModal = true }
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Search / Pay Contact Bar
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .clickable { viewModel.currentScreen.value = Screen.SendMoney }
                        .border(1.dp, DarkCardBorder, RoundedCornerShape(24.dp)),
                    color = DarkSurface
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pay any contact or @jap",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Notification Icon with Badge
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(DarkSurface)
                        .clickable { viewModel.currentScreen.value = Screen.Notifications },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Send,
                        contentDescription = "Notifications",
                        tint = JapYellowPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    if (unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(AccentRed)
                                .align(Alignment.TopEnd)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }

        // 2. Large Interactive "Scan & Pay" Camera Viewfinder Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(270.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(listOf(DarkCardBorder, JapYellowPrimary.copy(alpha = 0.6f)))
                )
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (showCameraView) {
                        JapCameraPreview(
                            modifier = Modifier.fillMaxSize(),
                            isFlashOn = isTorchOn
                        )
                    } else {
                        // Graphic Stylized QR Viewfinder
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.radialGradient(
                                        listOf(DarkCardElevated, DarkCard),
                                        radius = 400f
                                    )
                                ),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(130.dp)
                                    .border(2.dp, JapYellowPrimary, RoundedCornerShape(16.dp))
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.QrCodeScanner,
                                    contentDescription = null,
                                    tint = JapYellowPrimary,
                                    modifier = Modifier.size(64.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Scan & Pay Any QR",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Instant zero-fee Jap transfer",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }

                    // Floating Controls Overlay (Camera toggle, Flash, Gallery)
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Gallery Button
                        IconButton(
                            onClick = { qrGalleryLauncher.launch("image/*") },
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.6f))
                        ) {
                            Icon(Icons.Default.Image, contentDescription = "Scan from Gallery", tint = Color.White)
                        }

                        // Toggle Live Camera / Graphic Mode
                        Button(
                            onClick = { showCameraView = !showCameraView },
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (showCameraView) JapYellowPrimary else Color.Black.copy(alpha = 0.6f),
                                contentColor = if (showCameraView) DarkBackground else Color.White
                            )
                        ) {
                            Icon(
                                if (showCameraView) Icons.Default.Videocam else Icons.Default.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (showCameraView) "Camera Active" else "Open Camera", fontWeight = FontWeight.Bold)
                        }

                        // Torch Button
                        IconButton(
                            onClick = { isTorchOn = !isTorchOn },
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.6f))
                        ) {
                            Icon(
                                if (isTorchOn) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                                contentDescription = "Flash",
                                tint = if (isTorchOn) JapYellowPrimary else Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }

        // 3. Quick Action Buttons Grid (Your QR, Check Balance, History, Add Money, etc.)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                QuickActionButton(
                    icon = Icons.Default.QrCode,
                    label = "Your QR",
                    onClick = { showQrModal = true }
                )
                QuickActionButton(
                    icon = Icons.Default.AccountBalanceWallet,
                    label = "Check Balance",
                    onClick = { showBalanceModal = true }
                )
                QuickActionButton(
                    icon = Icons.Default.History,
                    label = "History",
                    onClick = { viewModel.currentScreen.value = Screen.TransactionHistory }
                )
                QuickActionButton(
                    icon = Icons.Default.AddCircle,
                    label = "Add Money",
                    onClick = { viewModel.currentScreen.value = Screen.AddMoney }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sound Effects & Demo testing cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.simulateReceiveMoney(amount = 75.0) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCardElevated)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🎉", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Receive ₹75", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Plays 'Wow!' sound", color = AccentGreen, fontSize = 11.sp)
                        }
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.prepareTransfer("anshika@jap", "Anshika Sharma") },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCardElevated)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("😭", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Send > ₹50", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Plays Crying sound", color = AccentRed, fontSize = 11.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // 4. Recommended People / Contacts
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recommended",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "View All",
                    color = JapYellowPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { viewModel.currentScreen.value = Screen.SendMoney }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(otherUsers) { user ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.prepareTransfer(user.id, user.name) }
                            .padding(4.dp)
                    ) {
                        JapAvatar(name = user.name, colorIndex = user.avatarColorIndex, size = 52)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = user.name.substringBefore(" "),
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                        Text(
                            text = "@jap",
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // 5. Recent Activity / Transactions
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
                    text = "Statement",
                    color = JapYellowPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { viewModel.currentScreen.value = Screen.TransactionHistory }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        if (transactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No transactions yet. Start sending money!", color = TextSecondary, fontSize = 13.sp)
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
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(DarkCardElevated)
                .border(1.dp, DarkCardBorder, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = JapYellowPrimary, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            color = TextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun TransactionItemRow(tx: Transaction, currentUserId: String) {
    val isCredit = tx.receiverId == currentUserId
    val isKeeper = tx.type == "KEEPER_SAVE" || tx.type == "KEEPER_WITHDRAW"
    val dateStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(tx.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(DarkCardBorder, DarkCardBorder.copy(alpha = 0.2f))))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (isCredit) AccentGreenBg else if (isKeeper) DarkCardElevated else AccentRedBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        isKeeper -> Icons.Default.Savings
                        isCredit -> Icons.Default.ArrowDownward
                        else -> Icons.Default.ArrowUpward
                    },
                    contentDescription = null,
                    tint = if (isCredit) AccentGreen else if (isKeeper) JapYellowPrimary else AccentRed,
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
                if (tx.soundTriggered == "CRYING") {
                    Text("😭 Crying sound", color = AccentRed, fontSize = 10.sp)
                } else if (tx.soundTriggered == "WOW") {
                    Text("🎉 Wow!", color = AccentGreen, fontSize = 10.sp)
                }
            }
        }
    }
}
