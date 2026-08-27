package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.*
import com.example.ui.viewmodel.JapPayViewModel
import com.example.ui.viewmodel.Screen
import com.example.util.UrlUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun BuyCustomIdScreen(viewModel: JapPayViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val adminConfig by viewModel.adminConfig.collectAsStateWithLifecycle()

    val customIdPrice = adminConfig?.customIdPrice ?: 19.0
    var inputCustomHandle by remember { mutableStateOf("") }
    var availabilityStatus by remember { mutableStateOf<String?>(null) }
    var isAvailable by remember { mutableStateOf<Boolean?>(null) }
    var isChecking by remember { mutableStateOf(false) }
    var isPurchasing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // FamPay Gateway Sheet State for Custom ID
    var showGatewaySheet by remember { mutableStateOf(false) }
    var gatewayOrderId by remember { mutableStateOf("") }
    var gatewayPaymentLink by remember { mutableStateOf<String?>(null) }
    var gatewayQrUrl by remember { mutableStateOf<String?>(null) }
    var isVerifyingGateway by remember { mutableStateOf(false) }

    val suggestedHandles = listOf("vip", "king", "queen", "pro", "trader", "legend", "boss", "dev", "star", "fast")

    fun triggerAvailabilityCheck(handle: String) {
        val clean = handle.trim().lowercase().replace("@jap", "").replace(" ", "")
        if (clean.length < 3) {
            availabilityStatus = "Handle must be at least 3 characters"
            isAvailable = false
            return
        }
        isChecking = true
        availabilityStatus = "Checking availability..."
        viewModel.checkCustomIdAvailability(clean) { available, msg ->
            isChecking = false
            isAvailable = available
            availabilityStatus = msg
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
            .padding(top = 20.dp, bottom = 40.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.currentScreen.value = Screen.Main }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Text(
                text = "VIP Custom Jap ID",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hero VIP Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(6.dp, RoundedCornerShape(22.dp), spotColor = Color(0x30FFB300)),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF1E1035), BrandPurpleDark, Color(0xFF3F165B))
                        )
                    )
                    .padding(22.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(AccentGold.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("👑", fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Exclusive Handle", color = AccentGold, fontWeight = FontWeight.Black, fontSize = 16.sp)
                        }

                        Surface(
                            color = AccentGold,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "₹${"%.0f".format(customIdPrice)} / Year",
                                color = Color(0xFF1E1035),
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Replace your phone number with a stylish VIP handle like @king, @alex, or @trader.",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (currentUser?.isCustomIdActive == true && !currentUser?.customId.isNullOrEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.12f))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = AccentGold, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Current VIP ID: ${currentUser?.customId}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Handle Input Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(20.dp), spotColor = Color(0x15000000)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = LightSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(LightCardBorder, BrandPurpleTint)))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Choose Your Desired Jap ID",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = inputCustomHandle,
                    onValueChange = {
                        val filtered = it.lowercase().replace(" ", "").replace("@jap", "")
                        inputCustomHandle = filtered
                        triggerAvailabilityCheck(filtered)
                    },
                    label = { Text("Custom Handle") },
                    placeholder = { Text("e.g. rohit, alex, star") },
                    trailingIcon = {
                        Text(
                            "@jap",
                            color = BrandPurple,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isAvailable == true) AccentGreen else BrandPurple,
                        unfocusedBorderColor = if (isAvailable == true) AccentGreen else LightCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = LightSurface,
                        unfocusedContainerColor = LightSurface
                    ),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                if (availabilityStatus != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isAvailable == true) Icons.Default.CheckCircle else Icons.Default.Info,
                            contentDescription = null,
                            tint = if (isAvailable == true) AccentGreen else if (isChecking) BrandPurple else AccentRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = availabilityStatus!!,
                            color = if (isAvailable == true) AccentGreen else if (isChecking) BrandPurple else AccentRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("Trending Suggestions:", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(suggestedHandles) { handle ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = BrandPurpleTint,
                            modifier = Modifier.clickable {
                                inputCustomHandle = handle
                                triggerAvailabilityCheck(handle)
                            }
                        ) {
                            Text(
                                text = "$handle@jap",
                                color = BrandPurpleDark,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Benefits Checklist Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(20.dp), spotColor = Color(0x10000000)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = LightSurface)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("What you get with VIP Custom ID:", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(10.dp))

                VipBenefitRow(icon = "⚡", title = "Instant Direct Payments", subtitle = "Anyone can send money directly using your custom @jap ID")
                VipBenefitRow(icon = "👑", title = "Golden Badge on Profile & Receipts", subtitle = "Stand out with an authenticated VIP badge across all transfers")
                VipBenefitRow(icon = "📅", title = "1 Full Year Subscription", subtitle = "No hidden fees, valid for 365 days from activation")
            }
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(errorMessage!!, color = AccentRed, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }

        Spacer(modifier = Modifier.height(20.dp))

        val fullHandle = "${inputCustomHandle.trim()}@jap"
        val userBalance = currentUser?.walletBalance ?: 0.0

        // Purchase Button Option 1: Pay via Wallet
        Button(
            onClick = {
                if (inputCustomHandle.isBlank()) {
                    errorMessage = "Please enter your custom handle"
                    return@Button
                }
                if (isAvailable != true) {
                    errorMessage = "Please choose an available handle"
                    return@Button
                }

                isPurchasing = true
                viewModel.purchaseCustomId(fullHandle, paidViaWallet = true) { success, msg, _ ->
                    isPurchasing = false
                    if (success) {
                        viewModel.currentScreen.value = Screen.PaymentReceipt
                    } else {
                        errorMessage = msg
                    }
                }
            },
            enabled = !isPurchasing && isAvailable == true && userBalance >= customIdPrice,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandPurple, contentColor = Color.White)
        ) {
            if (isPurchasing) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
            } else {
                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Pay ₹${"%.0f".format(customIdPrice)} with Wallet (Bal: ₹${"%.2f".format(userBalance)})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Purchase Button Option 2: Pay via FamPay / Gateway
        OutlinedButton(
            onClick = {
                if (inputCustomHandle.isBlank()) {
                    errorMessage = "Please enter your custom handle"
                    return@OutlinedButton
                }
                if (isAvailable != true) {
                    errorMessage = "Please choose an available handle"
                    return@OutlinedButton
                }

                // If gateway mode is LINK_GATEWAY, open external browser
                val mode = adminConfig?.gatewayMode ?: "API_GATEWAY"
                if (mode == "LINK_GATEWAY" && !adminConfig?.paymentLink.isNullOrBlank()) {
                    UrlUtils.openExternalUrl(context, adminConfig?.paymentLink)
                    return@OutlinedButton
                }

                // Generate ₹19 order via FamPay
                isPurchasing = true
                viewModel.createFamPayOrder(customIdPrice) { order ->
                    isPurchasing = false
                    gatewayOrderId = order.orderId
                    gatewayPaymentLink = order.paymentLink
                    gatewayQrUrl = order.qrCodeUrl
                    showGatewaySheet = true
                }
            },
            enabled = !isPurchasing && isAvailable == true,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandPurpleDark)
        ) {
            Icon(Icons.Default.Payment, contentDescription = null, tint = BrandPurple)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Pay ₹${"%.0f".format(customIdPrice)} via FamPay / UPI Gateway",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }

    // FamPay Gateway Modal for ₹19 Custom ID
    if (showGatewaySheet) {
        AlertDialog(
            onDismissRequest = { showGatewaySheet = false },
            confirmButton = {
                Button(
                    onClick = {
                        isVerifyingGateway = true
                        viewModel.verifyFamPayOrder(gatewayOrderId, customIdPrice) { result ->
                            isVerifyingGateway = false
                            if (result.isSuccess) {
                                val fullHandle = "${inputCustomHandle.trim()}@jap"
                                viewModel.purchaseCustomId(fullHandle, paidViaWallet = false) { success, msg, _ ->
                                    showGatewaySheet = false
                                    if (success) {
                                        viewModel.currentScreen.value = Screen.PaymentReceipt
                                    } else {
                                        errorMessage = msg
                                    }
                                }
                            } else {
                                errorMessage = result.message
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPurple)
                ) {
                    if (isVerifyingGateway) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                    } else {
                        Text("I have Paid - Auto Verify", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showGatewaySheet = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            title = {
                Text("FamPay Gateway • ₹${"%.0f".format(customIdPrice)}", fontWeight = FontWeight.Bold, color = TextPrimary)
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Pay ₹${"%.0f".format(customIdPrice)} to activate VIP handle '${inputCustomHandle}@jap'",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (!gatewayPaymentLink.isNullOrBlank()) {
                        Button(
                            onClick = { UrlUtils.openExternalUrl(context, gatewayPaymentLink) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7A00)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Open in UPI App / Browser", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Order ID: $gatewayOrderId", color = TextSecondary, fontSize = 11.sp)
                }
            }
        )
    }
}

@Composable
fun VipBenefitRow(icon: String, title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 20.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(subtitle, color = TextSecondary, fontSize = 11.sp)
        }
    }
}
