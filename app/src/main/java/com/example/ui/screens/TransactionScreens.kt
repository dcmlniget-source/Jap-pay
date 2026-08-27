package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AppNotification
import com.example.data.model.Transaction
import com.example.ui.components.JapAvatar
import com.example.ui.components.QrCodeDisplay
import com.example.ui.theme.*
import com.example.ui.viewmodel.JapPayViewModel
import com.example.ui.viewmodel.Screen
import com.example.util.BiometricHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SendMoneyScreen(viewModel: JapPayViewModel) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val otherUsers by viewModel.otherUsers.collectAsStateWithLifecycle()

    var targetId by remember { mutableStateOf(viewModel.transferTargetId.value) }
    var targetName by remember { mutableStateOf(viewModel.transferTargetName.value) }
    var amountText by remember { mutableStateOf(viewModel.transferAmount.value) }
    var messageText by remember { mutableStateOf(viewModel.transferMessage.value) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }
    var successMsg by remember { mutableStateOf("") }
    val isTransferring by viewModel.isTransferring.collectAsStateWithLifecycle()

    val parsedAmount = amountText.toDoubleOrNull() ?: 0.0
    val isGreaterThan50 = parsedAmount > 50.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
            .padding(top = 20.dp, bottom = 40.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.currentScreen.value = Screen.Main }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Text(
                text = "Send Money",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isSuccess) {
            // Success Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(20.dp), spotColor = Color(0x15000000)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = LightSurface),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(AccentGreen, BrandPurple)))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(AccentGreenBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AccentGreen, modifier = Modifier.size(48.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Payment Successful!", style = MaterialTheme.typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("₹${"%.2f".format(parsedAmount)}", style = MaterialTheme.typography.displayMedium, color = BrandPurple, fontWeight = FontWeight.Black)
                    Text("Sent to $targetName ($targetId)", color = TextSecondary, fontSize = 14.sp)
                    if (isGreaterThan50) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("😭 Crying sound effect played (> ₹50)!", color = AccentRed, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { viewModel.currentScreen.value = Screen.Main },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPurple, contentColor = Color.White)
                    ) {
                        Text("Done", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // Recipient Selector
            Text("Select Recipient", color = TextSecondary, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = targetId,
                onValueChange = { targetId = it },
                placeholder = { Text("Enter Mobile or @jap ID", color = TextSecondary) },
                leadingIcon = { Icon(Icons.Default.AlternateEmail, contentDescription = null, tint = BrandPurple) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandPurple,
                    unfocusedBorderColor = LightCardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = LightSurface,
                    unfocusedContainerColor = LightSurface
                ),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Quick select recent users
            Text("Or Choose Contact:", color = TextSecondary, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                otherUsers.take(3).forEach { user ->
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable {
                                targetId = user.id
                                targetName = user.name
                            }
                            .border(1.dp, if (targetId == user.id) BrandPurple else LightCardBorder, RoundedCornerShape(20.dp)),
                        color = LightSurface,
                        shadowElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            JapAvatar(name = user.name, colorIndex = user.avatarColorIndex, size = 24)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(user.name.substringBefore(" "), color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Amount Input
            Text("Enter Amount", color = TextSecondary, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                placeholder = { Text("0.00", color = TextSecondary, fontSize = 24.sp) },
                leadingIcon = { Text("₹", color = BrandPurple, fontSize = 28.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandPurple,
                    unfocusedBorderColor = LightCardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = LightSurface,
                    unfocusedContainerColor = LightSurface
                ),
                textStyle = MaterialTheme.typography.titleLarge.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            // Dynamic funny Crying notice if > ₹50
            if (isGreaterThan50) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = AccentRedBg,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("😭", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Note: Amount is > ₹50! Jap Pay will play a crying sound effect on transfer.",
                            color = AccentRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Optional Message
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                placeholder = { Text("Add a message (optional)", color = TextSecondary) },
                leadingIcon = { Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, tint = TextSecondary) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandPurple,
                    unfocusedBorderColor = LightCardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = LightSurface,
                    unfocusedContainerColor = LightSurface
                ),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))
            Text("Available balance: ₹${"%.2f".format(currentUser?.walletBalance ?: 0.0)}", color = TextSecondary, fontSize = 12.sp)

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(errorMessage!!, color = AccentRed, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Pay Button
            Button(
                onClick = {
                    errorMessage = null
                    if (targetId.isBlank() || parsedAmount <= 0) {
                        errorMessage = "Please enter valid recipient and amount"
                        return@Button
                    }
                    if (parsedAmount > (currentUser?.walletBalance ?: 0.0)) {
                        errorMessage = "Insufficient balance! Please add money first."
                        return@Button
                    }

                    // Biometric Authentication before paying
                    if (context is FragmentActivity && BiometricHelper.canAuthenticate(context)) {
                        BiometricHelper.promptBiometric(
                            activity = context,
                            title = "Authorize Jap Pay Transfer",
                            subtitle = "Paying ₹${"%.2f".format(parsedAmount)} to $targetId",
                            onSuccess = {
                                viewModel.executeTransfer(
                                    targetId = targetId,
                                    amount = parsedAmount,
                                    message = messageText
                                ) { success, msg, _ ->
                                    if (success) {
                                        viewModel.currentScreen.value = Screen.PaymentReceipt
                                    } else {
                                        errorMessage = msg
                                    }
                                }
                            },
                            onError = {
                                // Fallback to direct transfer
                                viewModel.executeTransfer(
                                    targetId = targetId,
                                    amount = parsedAmount,
                                    message = messageText
                                ) { success, msg, _ ->
                                    if (success) {
                                        viewModel.currentScreen.value = Screen.PaymentReceipt
                                    } else {
                                        errorMessage = msg
                                    }
                                }
                            }
                        )
                    } else {
                        viewModel.executeTransfer(
                            targetId = targetId,
                            amount = parsedAmount,
                            message = messageText
                        ) { success, msg, _ ->
                            if (success) {
                                viewModel.currentScreen.value = Screen.PaymentReceipt
                            } else {
                                errorMessage = msg
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandPurple, contentColor = Color.White),
                enabled = !isTransferring
            ) {
                if (isTransferring) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                } else {
                    Icon(Icons.Filled.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Pay ₹${if (parsedAmount > 0) "%.2f".format(parsedAmount) else ""}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun NotificationsScreen(viewModel: JapPayViewModel) {
    val notifications by viewModel.userNotifications.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp, bottom = 40.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.currentScreen.value = Screen.Main }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Text(
                text = "Notifications",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No notifications yet", color = TextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(notifications) { notif ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(1.dp, RoundedCornerShape(14.dp), spotColor = Color(0x10000000)),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = LightSurface),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(listOf(LightCardBorder, if (notif.isRead) LightCardBorder else BrandPurpleTint))
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (notif.type) {
                                            "CREDIT" -> AccentGreenBg
                                            "DEBIT" -> AccentRedBg
                                            else -> BrandPurpleTint
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (notif.type) {
                                        "CREDIT" -> Icons.Default.ArrowDownward
                                        "DEBIT" -> Icons.Default.ArrowUpward
                                        else -> Icons.Default.Notifications
                                    },
                                    contentDescription = null,
                                    tint = when (notif.type) {
                                        "CREDIT" -> AccentGreen
                                        "DEBIT" -> AccentRed
                                        else -> BrandPurple
                                    },
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(notif.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(notif.message, color = TextSecondary, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionHistoryScreen(viewModel: JapPayViewModel) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val transactions by viewModel.userTransactions.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp, bottom = 40.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.currentScreen.value = Screen.Main }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Text(
                text = "Transaction Statement",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No transaction history", color = TextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(transactions) { tx ->
                    TransactionItemRow(tx = tx, currentUserId = currentUser?.id ?: "")
                }
            }
        }
    }
}

