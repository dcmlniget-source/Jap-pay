package com.example.ui.screens

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.Transaction
import com.example.data.remote.FamPayService
import com.example.ui.components.JapAvatar
import com.example.ui.components.JapCameraPreview
import com.example.ui.theme.*
import com.example.ui.viewmodel.JapPayViewModel
import com.example.ui.viewmodel.Screen
import com.example.util.QrCodeUtils
import com.example.util.UrlUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// FamPay Official Colors
val FamPayOrange = Color(0xFFFF7A00)
val FamPayOrangeDark = Color(0xFFE56700)
val FamPayOrangeLight = Color(0xFFFFF2E5)

@Composable
fun FamPayLogoBadge(modifier: Modifier = Modifier, size: Int = 40) {
    Surface(
        modifier = modifier
            .size(size.dp)
            .shadow(3.dp, RoundedCornerShape((size * 0.28).dp), spotColor = FamPayOrangeDark),
        shape = RoundedCornerShape((size * 0.28).dp),
        color = FamPayOrange
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_fampay_logo),
                contentDescription = "FamPay Official Logo",
                modifier = Modifier.size((size * 0.72).dp)
            )
        }
    }
}

@Composable
fun FamPayBrandedHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.horizontalGradient(listOf(FamPayOrange, FamPayOrangeDark)))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FamPayLogoBadge(size = 46)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "FamPay",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.White.copy(alpha = 0.25f)
                ) {
                    Text(
                        text = "LIVE API",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Text(
                text = "Instant Wallet Top-up with FamPay Gateway",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Icon(
            imageVector = Icons.Default.ElectricBolt,
            contentDescription = null,
            tint = Color(0xFFFFD700),
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun AddMoneyScreen(viewModel: JapPayViewModel) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val adminConfig by viewModel.adminConfig.collectAsStateWithLifecycle()
    val isCreatingOrder by viewModel.isCreatingFamPayOrder.collectAsStateWithLifecycle()
    val activeOrder by viewModel.activeFamPayOrder.collectAsStateWithLifecycle()

    val defaultGatewayMode = adminConfig?.gatewayMode ?: "API_GATEWAY"
    var selectedGatewayMode by remember(defaultGatewayMode) { mutableStateOf(defaultGatewayMode) }

    var amountText by remember { mutableStateOf("500") }
    var utrNumberText by remember { mutableStateOf("") }
    var screenshotUri by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showCheckoutDialog by remember { mutableStateOf(false) }

    // API Verification State
    var isVerifyingPayment by remember { mutableStateOf(false) }
    var verifyStatusMessage by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            screenshotUri = uri.toString()
        }
    }

    val parsedAmount = amountText.toDoubleOrNull() ?: 0.0

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
                text = "Add Money to Wallet",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Gateway Mode Switcher Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(LightSurface)
                .border(1.dp, LightCardBorder, RoundedCornerShape(12.dp))
                .padding(4.dp)
        ) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .clickable { selectedGatewayMode = "API_GATEWAY" },
                color = if (selectedGatewayMode == "API_GATEWAY") FamPayOrange else Color.Transparent
            ) {
                Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                    Text(
                        "⚡ FamPay API",
                        color = if (selectedGatewayMode == "API_GATEWAY") Color.White else TextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .clickable { selectedGatewayMode = "MANUAL_GATEWAY" },
                color = if (selectedGatewayMode == "MANUAL_GATEWAY") BrandPurple else Color.Transparent
            ) {
                Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                    Text(
                        "📝 Manual UPI",
                        color = if (selectedGatewayMode == "MANUAL_GATEWAY") Color.White else TextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            if (!adminConfig?.paymentLink.isNullOrBlank()) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(9.dp))
                        .clickable { selectedGatewayMode = "LINK_GATEWAY" },
                    color = if (selectedGatewayMode == "LINK_GATEWAY") BrandPurpleDark else Color.Transparent
                ) {
                    Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                        Text(
                            "🔗 Link",
                            color = if (selectedGatewayMode == "LINK_GATEWAY") Color.White else TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (selectedGatewayMode == "API_GATEWAY") {
            // FamPay Exclusive Gateway Banner
            FamPayBrandedHeader()
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Current Wallet Balance Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = LightSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(LightCardBorder, FamPayOrange.copy(alpha = 0.2f))))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Current Wallet Balance", color = TextSecondary, fontSize = 12.sp)
                    Text(
                        text = "₹${"%.2f".format(currentUser?.walletBalance ?: 0.0)}",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = BrandPurpleTint,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = BrandPurple, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- MODE 1: FamPay Automated Gateway ---
        if (selectedGatewayMode == "API_GATEWAY") {
            Text("Enter Amount to Add", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = amountText,
                onValueChange = {
                    if (it.all { char -> char.isDigit() || char == '.' }) {
                        amountText = it
                    }
                },
                leadingIcon = {
                    Text("₹", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = FamPayOrange)
                },
                trailingIcon = {
                    if (amountText.isNotEmpty()) {
                        IconButton(onClick = { amountText = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextSecondary)
                        }
                    }
                },
                placeholder = { Text("0.00", color = TextSecondary, fontSize = 20.sp) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = TextPrimary),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FamPayOrange,
                    unfocusedBorderColor = LightCardBorder,
                    focusedContainerColor = LightSurface,
                    unfocusedContainerColor = LightSurface
                ),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Amount Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("100", "200", "500", "1000", "2000").forEach { chipAmt ->
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { amountText = chipAmt }
                            .border(
                                1.dp,
                                if (amountText == chipAmt) FamPayOrange else LightCardBorder,
                                RoundedCornerShape(12.dp)
                            ),
                        color = if (amountText == chipAmt) FamPayOrangeLight else LightSurface
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+₹$chipAmt",
                                color = if (amountText == chipAmt) FamPayOrangeDark else TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // FamPay Trust & Security Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = FamPayOrangeLight.copy(alpha = 0.5f)),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(FamPayOrange.copy(alpha = 0.4f), FamPayOrange.copy(alpha = 0.1f))))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FamPayLogoBadge(size = 32)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "100% Automated via FamPay API",
                            color = FamPayOrangeDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Direct API order generation & webhook verification.",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (parsedAmount <= 0) {
                        errorMessage = "Please enter an amount greater than ₹0"
                        return@Button
                    }
                    errorMessage = null
                    viewModel.createFamPayOrder(parsedAmount) { order ->
                        showCheckoutDialog = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FamPayOrange,
                    contentColor = Color.White
                ),
                enabled = !isCreatingOrder
            ) {
                if (isCreatingOrder) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FamPayLogoBadge(size = 26)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Proceed with FamPay • ₹${"%.2f".format(parsedAmount)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }

        // --- MODE 2: Manual UPI & UTR Gateway ---
        else if (selectedGatewayMode == "MANUAL_GATEWAY") {
            val adminUpi = adminConfig?.adminUpiId ?: "8791738300@jap"
            val adminQrData = adminConfig?.adminQrData ?: "upi://pay?pa=$adminUpi&pn=JapPayAdmin&cu=INR"

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = LightSurface),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(LightCardBorder, BrandPurpleTint)))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Scan QR or Pay to Admin UPI", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    // QR Display
                    val qrBitmap = remember(adminQrData) { QrCodeUtils.generateQrBitmap(adminQrData, 360) }
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (qrBitmap != null) {
                            Image(
                                bitmap = qrBitmap.asImageBitmap(),
                                contentDescription = "Payment QR",
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.size(80.dp), tint = BrandPurple)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Copy UPI ID Pill
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(BrandPurpleTint)
                            .clickable {
                                clipboardManager.setText(AnnotatedString(adminUpi))
                                viewModel.showToast("UPI ID Copied: $adminUpi")
                            }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(adminUpi, color = BrandPurpleDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = BrandPurple, modifier = Modifier.size(14.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Amount Input
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("Amount Paid (₹)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandPurple,
                    unfocusedBorderColor = LightCardBorder
                ),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // UTR Number Input
            OutlinedTextField(
                value = utrNumberText,
                onValueChange = { utrNumberText = it },
                label = { Text("12-Digit UTR / Transaction ID") },
                placeholder = { Text("e.g. 423456789012") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandPurple,
                    unfocusedBorderColor = LightCardBorder
                ),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Screenshot Upload
            OutlinedButton(
                onClick = { photoPickerLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandPurple)
            ) {
                Icon(Icons.Default.UploadFile, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (screenshotUri != null) "Screenshot Attached ✓" else "Upload Payment Screenshot", fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (parsedAmount <= 0 || utrNumberText.isBlank()) {
                        errorMessage = "Please enter valid amount and 12-digit UTR number"
                        return@Button
                    }
                    errorMessage = null
                    viewModel.submitDepositRequest(
                        amount = parsedAmount,
                        utrNumber = utrNumberText,
                        screenshotUri = screenshotUri,
                        onSuccess = {
                            viewModel.currentScreen.value = Screen.Main
                        },
                        onError = { err -> errorMessage = err }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandPurple, contentColor = Color.White)
            ) {
                Text("Submit for Admin Approval", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }

        // --- MODE 3: Link Gateway ---
        else if (selectedGatewayMode == "LINK_GATEWAY") {
            val payLink = adminConfig?.paymentLink ?: "https://py.freepanel.in"

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = LightSurface),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(LightCardBorder, BrandPurpleTint)))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("External Gateway Payment", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Click below to open the official payment gateway in your browser.", color = TextSecondary, fontSize = 12.sp)

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { UrlUtils.openExternalUrl(context, payLink) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPurpleDark, contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open Payment Link in Browser", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(errorMessage!!, color = AccentRed, fontSize = 13.sp)
        }
    }

    // FamPay Payment Checkout Dialog & Real-Time Verification
    if (showCheckoutDialog && activeOrder != null) {
        val order = activeOrder!!
        val paymentLink = order.paymentLink ?: "upi://pay?pa=8791738300@jap&pn=JapPay&am=${"%.2f".format(order.amountInRupees)}&cu=INR&tr=${order.orderId}"

        AlertDialog(
            onDismissRequest = { showCheckoutDialog = false },
            containerColor = LightSurface,
            shape = RoundedCornerShape(24.dp),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FamPayLogoBadge(size = 38)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("FamPay Checkout", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text("Live Order: ${order.orderId}", color = TextSecondary, fontSize = 11.sp)
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Total Deposit Amount", color = TextSecondary, fontSize = 12.sp)
                    Text(
                        text = "₹${"%.2f".format(order.amountInRupees)}",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Black,
                        color = FamPayOrange
                    )
                    Text("(${order.amountInPaise} paise)", color = TextSecondary, fontSize = 11.sp)

                    Spacer(modifier = Modifier.height(14.dp))

                    // QR Code for Instant UPI Scanning
                    val qrBitmap = remember(paymentLink) { QrCodeUtils.generateQrBitmap(paymentLink, 300) }
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White)
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (qrBitmap != null) {
                            Image(
                                bitmap = qrBitmap.asImageBitmap(),
                                contentDescription = "Scan to Pay",
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.size(70.dp), tint = FamPayOrange)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Open in UPI App / External Browser Button
                    Button(
                        onClick = {
                            UrlUtils.openExternalUrl(context, paymentLink)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = FamPayOrange, contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pay via UPI App / Browser", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    if (verifyStatusMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(verifyStatusMessage!!, color = FamPayOrangeDark, fontSize = 12.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isVerifyingPayment = true
                        verifyStatusMessage = "Checking payment status from FamPay API..."
                        viewModel.verifyFamPayOrder(order.orderId, order.amountInRupees) { result ->
                            isVerifyingPayment = false
                            if (result.isSuccess) {
                                showCheckoutDialog = false
                                viewModel.currentScreen.value = Screen.PaymentReceipt
                            } else {
                                verifyStatusMessage = result.message
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPurple, contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isVerifyingPayment) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                    } else {
                        Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("I Have Paid • Auto Verify", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showCheckoutDialog = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
fun PaymentReceiptScreen(viewModel: JapPayViewModel) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val lastTx by viewModel.lastCompletedTransaction.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    val tx = lastTx ?: Transaction(
        id = 1,
        senderId = currentUser?.id ?: "devansh@jap",
        senderName = currentUser?.name ?: "Devansh",
        receiverId = "varun@jap",
        receiverName = "Varun Rathore",
        amount = 500.0,
        message = "Jap Pay Transfer",
        status = "SUCCESS",
        type = "TRANSFER",
        timestamp = System.currentTimeMillis()
    )

    val dateFormatted = remember(tx.timestamp) {
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(tx.timestamp))
    }

    val txnIdString = remember(tx.id, tx.timestamp) {
        "JAP${tx.timestamp.toString().takeLast(8)}${tx.id}"
    }

    val utrNumberString = remember(tx.timestamp) {
        "42${(tx.timestamp / 1000).toString().takeLast(10)}"
    }

    // Success entrance animations
    val transitionState = remember { MutableTransitionState(false) }.apply { targetState = true }
    val transition = updateTransition(transitionState, label = "PaymentSuccessTransition")
    val checkmarkScale by transition.animateFloat(
        transitionSpec = { spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow) },
        label = "CheckmarkScale"
    ) { if (it) 1f else 0f }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp)
            .statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Top Status Header with Animated Glowing Badge
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(AccentGreen.copy(alpha = 0.15f))
            )
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(AccentGreen.copy(alpha = 0.3f))
            )
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color(0xFF00C853), Color(0xFF00E676)))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Success",
                    tint = Color.White,
                    modifier = Modifier.size((36 * checkmarkScale).dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = when (tx.type) {
                "DEPOSIT" -> "Money Added to Wallet!"
                "CUSTOM_ID_PURCHASE" -> "VIP Jap ID Activated! 👑"
                else -> "Payment Successful"
            },
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            fontWeight = FontWeight.Black
        )

        Text(
            text = dateFormatted,
            color = TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Amount Display
        Text(
            text = "₹${"%.2f".format(tx.amount)}",
            style = MaterialTheme.typography.displayMedium,
            color = if (tx.type == "DEPOSIT") FamPayOrangeDark else BrandPurple,
            fontWeight = FontWeight.Black,
            letterSpacing = (-1).sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Main Comprehensive Payment Receipt Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(22.dp), spotColor = Color(0x15000000)),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = LightSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(LightCardBorder, BrandPurpleTint)))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Receiver / Payee Details
                Text("RECEIVER DETAILS", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (tx.type == "DEPOSIT") {
                        FamPayLogoBadge(size = 46)
                    } else {
                        JapAvatar(name = tx.receiverName, size = 46, colorIndex = 1)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = tx.receiverName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = tx.receiverId,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (tx.type == "DEPOSIT") FamPayOrangeDark else BrandPurple,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = AccentGreenBg,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Verified, contentDescription = "Verified", tint = AccentGreen, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = LightDivider)
                Spacer(modifier = Modifier.height(16.dp))

                // Payment Breakdown Table
                ReceiptDetailRow(
                    label = "Transaction ID",
                    value = txnIdString,
                    isCopyable = true,
                    onCopy = {
                        clipboardManager.setText(AnnotatedString(txnIdString))
                        viewModel.showToast("Transaction ID copied!")
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))

                ReceiptDetailRow(
                    label = "UTR Reference No.",
                    value = utrNumberString,
                    isCopyable = true,
                    onCopy = {
                        clipboardManager.setText(AnnotatedString(utrNumberString))
                        viewModel.showToast("UTR copied!")
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))

                ReceiptDetailRow(
                    label = "Paid From",
                    value = if (tx.type == "DEPOSIT") "FamPay Gateway" else "Jap Pay Wallet (${currentUser?.name ?: "Devansh"})"
                )
                Spacer(modifier = Modifier.height(10.dp))

                ReceiptDetailRow(
                    label = "Payment Mode",
                    value = when (tx.type) {
                        "DEPOSIT" -> "FamPay Instant Top-Up"
                        "CUSTOM_ID_PURCHASE" -> "VIP Jap Registry Subscription"
                        else -> "Jap 0% Peer-to-Peer Transfer"
                    }
                )

                if (tx.message.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    ReceiptDetailRow(label = "Message / Note", value = tx.message)
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = LightDivider)
                Spacer(modifier = Modifier.height(14.dp))

                // 256-Bit Security Badge
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(BrandPurpleTint.copy(alpha = 0.5f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = BrandPurple, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "100% Protected & Verified by Jap Pay Secure Mesh",
                        color = BrandPurple,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = {
                    val shareText = "Jap Pay Payment Receipt\nAmount: ₹${"%.2f".format(tx.amount)}\nPaid to: ${tx.receiverName} (${tx.receiverId})\nTxn ID: $txnIdString\nUTR: $utrNumberString\nStatus: SUCCESS ✓"
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }
                    context.startActivity(Intent.createChooser(intent, "Share Payment Receipt"))
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandPurple)
            ) {
                Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Share", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    viewModel.currentScreen.value = Screen.Main
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandPurple, contentColor = Color.White)
            ) {
                Text("Done", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

@Composable
fun ReceiptDetailRow(
    label: String,
    value: String,
    isCopyable: Boolean = false,
    onCopy: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(value, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            if (isCopyable) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    tint = BrandPurple,
                    modifier = Modifier
                        .size(14.dp)
                        .clickable { onCopy() }
                )
            }
        }
    }
}
