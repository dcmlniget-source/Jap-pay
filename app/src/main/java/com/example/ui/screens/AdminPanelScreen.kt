package com.example.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.ui.components.JapAvatar
import com.example.ui.theme.*
import com.example.ui.viewmodel.JapPayViewModel
import com.example.ui.viewmodel.Screen
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AdminTab {
    DEPOSITS, QR_SETTINGS, BROADCAST, USERS, AUDIT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(viewModel: JapPayViewModel) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedAdminTab by remember { mutableStateOf(AdminTab.DEPOSITS) }

    val depositRequests by viewModel.allDepositRequests.collectAsStateWithLifecycle()
    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val adminConfig by viewModel.adminConfig.collectAsStateWithLifecycle()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = LightSurface,
                modifier = Modifier.width(290.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(BrandPurpleTint),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.AdminPanelSettings, contentDescription = null, tint = BrandPurple, modifier = Modifier.size(26.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Jap Pay Admin", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Master Control Panel", color = BrandPurple, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    HorizontalDivider(color = LightDivider, modifier = Modifier.padding(vertical = 8.dp))

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.HourglassTop, contentDescription = null) },
                        label = { Text("Deposit Approvals (${depositRequests.count { it.status == "PENDING" }})") },
                        selected = selectedAdminTab == AdminTab.DEPOSITS,
                        onClick = {
                            selectedAdminTab = AdminTab.DEPOSITS
                            scope.launch { drawerState.close() }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = BrandPurpleTint,
                            selectedIconColor = BrandPurple,
                            selectedTextColor = BrandPurple,
                            unselectedTextColor = TextSecondary
                        )
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.QrCode, contentDescription = null) },
                        label = { Text("Payment QR & Links") },
                        selected = selectedAdminTab == AdminTab.QR_SETTINGS,
                        onClick = {
                            selectedAdminTab = AdminTab.QR_SETTINGS
                            scope.launch { drawerState.close() }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = BrandPurpleTint,
                            selectedIconColor = BrandPurple,
                            selectedTextColor = BrandPurple,
                            unselectedTextColor = TextSecondary
                        )
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Campaign, contentDescription = null) },
                        label = { Text("Send Push Notifications") },
                        selected = selectedAdminTab == AdminTab.BROADCAST,
                        onClick = {
                            selectedAdminTab = AdminTab.BROADCAST
                            scope.launch { drawerState.close() }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = BrandPurpleTint,
                            selectedIconColor = BrandPurple,
                            selectedTextColor = BrandPurple,
                            unselectedTextColor = TextSecondary
                        )
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.People, contentDescription = null) },
                        label = { Text("Users & Balances") },
                        selected = selectedAdminTab == AdminTab.USERS,
                        onClick = {
                            selectedAdminTab = AdminTab.USERS
                            scope.launch { drawerState.close() }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = BrandPurpleTint,
                            selectedIconColor = BrandPurple,
                            selectedTextColor = BrandPurple,
                            unselectedTextColor = TextSecondary
                        )
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.ReceiptLong, contentDescription = null) },
                        label = { Text("Transactions Audit") },
                        selected = selectedAdminTab == AdminTab.AUDIT,
                        onClick = {
                            selectedAdminTab = AdminTab.AUDIT
                            scope.launch { drawerState.close() }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = BrandPurpleTint,
                            selectedIconColor = BrandPurple,
                            selectedTextColor = BrandPurple,
                            unselectedTextColor = TextSecondary
                        )
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = {
                            viewModel.currentUserId.value = "8791738300@jap"
                            viewModel.currentScreen.value = Screen.Main
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPurpleTint),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = null, tint = BrandPurple)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Switch to User App", color = BrandPurple, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            when (selectedAdminTab) {
                                AdminTab.DEPOSITS -> "Deposit Requests Review"
                                AdminTab.QR_SETTINGS -> "Payment QR & Links Setup"
                                AdminTab.BROADCAST -> "Broadcast Notification"
                                AdminTab.USERS -> "Registered Users"
                                AdminTab.AUDIT -> "Audit Logs"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = TextPrimary
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = BrandPurple)
                        }
                    },
                    actions = {
                        TextButton(onClick = { viewModel.currentScreen.value = Screen.Main }) {
                            Text("User App", color = BrandPurple, fontWeight = FontWeight.Bold)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = LightSurface)
                )
            },
            containerColor = LightBackground
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                when (selectedAdminTab) {
                    AdminTab.DEPOSITS -> {
                        val pending = depositRequests
                        if (pending.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No deposit requests available.", color = TextSecondary)
                            }
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(pending) { req ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .shadow(2.dp, RoundedCornerShape(16.dp), spotColor = Color(0x12000000)),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = LightSurface),
                                        border = CardDefaults.outlinedCardBorder().copy(
                                            brush = Brush.horizontalGradient(
                                                listOf(
                                                    LightCardBorder,
                                                    when (req.status) {
                                                        "APPROVED" -> AccentGreen
                                                        "REJECTED" -> AccentRed
                                                        else -> BrandPurple
                                                    }
                                                )
                                            )
                                        )
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(req.userName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                                    Text(req.userId, color = BrandPurple, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                                }
                                                Text(
                                                    "₹${"%.2f".format(req.amount)}",
                                                    color = TextPrimary,
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 18.sp
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(10.dp))
                                            Text("UTR: ${req.utrNumber}", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                            Text(
                                                "Status: ${req.status}",
                                                color = when (req.status) {
                                                    "APPROVED" -> AccentGreen
                                                    "REJECTED" -> AccentRed
                                                    else -> BrandPurple
                                                },
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )

                                            if (req.status == "PENDING") {
                                                Spacer(modifier = Modifier.height(14.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    OutlinedButton(
                                                        onClick = { viewModel.adminRejectDeposit(req.id, "Invalid UTR / Payment Not Received") },
                                                        modifier = Modifier.weight(1f),
                                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentRed),
                                                        shape = RoundedCornerShape(10.dp)
                                                    ) {
                                                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("Reject")
                                                    }

                                                    Button(
                                                        onClick = { viewModel.adminApproveDeposit(req.id) },
                                                        modifier = Modifier.weight(1f),
                                                        colors = ButtonDefaults.buttonColors(containerColor = AccentGreen, contentColor = Color.White),
                                                        shape = RoundedCornerShape(10.dp)
                                                    ) {
                                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("Verify & Credit", fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    AdminTab.QR_SETTINGS -> {
                        var upiId by remember { mutableStateOf(adminConfig?.adminUpiId ?: "8791738300@jap") }
                        var qrImageUrl by remember { mutableStateOf(adminConfig?.adminQrImageUrl ?: "") }
                        var paymentLink by remember { mutableStateOf(adminConfig?.paymentLink ?: "") }
                        var instructions by remember { mutableStateOf(adminConfig?.depositInstructions ?: "") }
                        var notice by remember { mutableStateOf(adminConfig?.noticeMessage ?: "") }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text("Configure Payment QR & UPI Gateway", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(14.dp))

                            OutlinedTextField(
                                value = upiId,
                                onValueChange = { upiId = it },
                                label = { Text("Admin Receiving UPI ID") },
                                placeholder = { Text("e.g. 8791738300@jap or devansh@upi") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BrandPurple,
                                    unfocusedBorderColor = LightCardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = qrImageUrl,
                                onValueChange = { qrImageUrl = it },
                                label = { Text("Custom Payment QR Image URL") },
                                placeholder = { Text("https://example.com/payment_qr.png") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BrandPurple,
                                    unfocusedBorderColor = LightCardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )

                            if (qrImageUrl.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .size(140.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(1.dp, LightCardBorder, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = qrImageUrl,
                                        contentDescription = "Preview QR Image",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = paymentLink,
                                onValueChange = { paymentLink = it },
                                label = { Text("Payment Link (Optional)") },
                                placeholder = { Text("https://pay.example.com/instant") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BrandPurple,
                                    unfocusedBorderColor = LightCardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = instructions,
                                onValueChange = { instructions = it },
                                label = { Text("Deposit Instructions for Users") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BrandPurple,
                                    unfocusedBorderColor = LightCardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = notice,
                                onValueChange = { notice = it },
                                label = { Text("System Announcement Banner") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BrandPurple,
                                    unfocusedBorderColor = LightCardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = {
                                    viewModel.adminUpdateConfig(upiId, qrImageUrl, paymentLink, instructions, notice)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandPurple, contentColor = Color.White),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Save Settings", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                            Spacer(modifier = Modifier.height(30.dp))
                        }
                    }

                    AdminTab.BROADCAST -> {
                        var title by remember { mutableStateOf("") }
                        var message by remember { mutableStateOf("") }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text("Broadcast Push Notification to Users", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = title,
                                onValueChange = { title = it },
                                placeholder = { Text("Notification Title (e.g. Cashback Weekend ⚡)") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BrandPurple,
                                    unfocusedBorderColor = LightCardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = message,
                                onValueChange = { message = it },
                                placeholder = { Text("Message Body...") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 4,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BrandPurple,
                                    unfocusedBorderColor = LightCardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = {
                                    if (title.isNotBlank() && message.isNotBlank()) {
                                        viewModel.adminBroadcastNotification(title, message, "ALL")
                                        title = ""
                                        message = ""
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandPurple, contentColor = Color.White),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Send Push Broadcast", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    AdminTab.USERS -> {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(allUsers) { user ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .shadow(1.dp, RoundedCornerShape(14.dp), spotColor = Color(0x10000000)),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = LightSurface),
                                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(LightCardBorder, LightCardBorder)))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        JapAvatar(name = user.name, colorIndex = user.avatarColorIndex, size = 42)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(user.name, color = TextPrimary, fontWeight = FontWeight.Bold)
                                            Text(user.id, color = BrandPurple, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                            Text("Aadhaar: ${user.aadhaarNumberMasked} (${if (user.aadhaarVerified) "Verified ✓" else "Pending"})", color = TextSecondary, fontSize = 11.sp)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("₹${"%.2f".format(user.walletBalance)}", color = AccentGreen, fontWeight = FontWeight.Bold)
                                            Text("Keeper: ₹${"%.2f".format(user.keeperBalance)}", color = TextSecondary, fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    AdminTab.AUDIT -> {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(allTransactions) { tx ->
                                val dateStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(tx.timestamp))
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = LightSurface),
                                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(LightCardBorder, LightCardBorder)))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("${tx.senderName} ➔ ${tx.receiverName}", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                            Text("${tx.senderId} to ${tx.receiverId} • $dateStr", color = TextSecondary, fontSize = 11.sp)
                                        }
                                        Text("₹${"%.2f".format(tx.amount)}", color = BrandPurple, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

