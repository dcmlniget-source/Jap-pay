package com.example.ui.screens

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.QrCodeDisplay
import com.example.ui.theme.*
import com.example.ui.viewmodel.JapPayViewModel
import com.example.ui.viewmodel.Screen

@Composable
fun WalletScreen(viewModel: JapPayViewModel) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    var isBalanceHidden by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
            .padding(top = 20.dp, bottom = 90.dp)
    ) {
        Text(
            text = "Wallet",
            style = MaterialTheme.typography.displaySmall,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Check Balance Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(20.dp), spotColor = Color(0x15000000)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = LightSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(LightCardBorder, BrandPurpleTint)))
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Available Balance", color = TextSecondary, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = if (isBalanceHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle Balance",
                            tint = BrandPurple,
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { isBalanceHidden = !isBalanceHidden }
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isBalanceHidden) "₹ ••••••" else "₹${"%.2f".format(currentUser?.walletBalance ?: 0.0)}",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = { viewModel.currentScreen.value = Screen.AddMoney },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandPurple,
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Money", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Virtual JapPay RuPay Gold Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .shadow(4.dp, RoundedCornerShape(22.dp), spotColor = BrandPurpleDark),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            listOf(BrandPurpleDark, BrandPurple, Color(0xFF7B1FA2))
                        )
                    )
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Bolt, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Jap Pay", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                        }
                        Text("PREPAID VIRTUAL", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp, 26.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFFFD54F))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(Icons.Default.Contactless, contentDescription = null, tint = Color.White.copy(alpha = 0.8f))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(currentUser?.name?.uppercase() ?: "USER", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(currentUser?.id ?: "user@jap", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                        }
                        Text("RuPay", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Personal QR Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(20.dp), spotColor = Color(0x15000000)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = LightSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(LightCardBorder, LightCardBorder)))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Your Personal QR", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(currentUser?.id ?: "user@jap", color = BrandPurple, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(14.dp))

                QrCodeDisplay(
                    content = "jappay://pay?id=${currentUser?.id}&name=${currentUser?.name}",
                    size = 180
                )

                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.showToast("Copied ${currentUser?.id} to clipboard!") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = BrandPurple, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy ID", color = BrandPurple, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { viewModel.showToast("Sharing Jap Pay QR Code...") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPurple, contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share QR", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun KeeperScreen(viewModel: JapPayViewModel) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    var saveAmount by remember { mutableStateOf("") }
    var showSaveDialog by remember { mutableStateOf(false) }
    var showWithdrawDialog by remember { mutableStateOf(false) }

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            containerColor = LightSurface,
            title = { Text("Save to Keeper Jar", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Earn 3.0% interest per year on your virtual funds.", color = TextSecondary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = saveAmount,
                        onValueChange = { saveAmount = it },
                        placeholder = { Text("Amount (₹)", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandPurple,
                            unfocusedBorderColor = LightCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = LightSurface,
                            unfocusedContainerColor = LightSurface
                        ),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = saveAmount.toDoubleOrNull() ?: 0.0
                        if (amt > 0) {
                            viewModel.saveToKeeper(amt)
                            showSaveDialog = false
                            saveAmount = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPurple, contentColor = Color.White)
                ) {
                    Text("Deposit", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    if (showWithdrawDialog) {
        AlertDialog(
            onDismissRequest = { showWithdrawDialog = false },
            containerColor = LightSurface,
            title = { Text("Withdraw to Wallet", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Move savings back into your main spending wallet.", color = TextSecondary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = saveAmount,
                        onValueChange = { saveAmount = it },
                        placeholder = { Text("Amount (₹)", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandPurple,
                            unfocusedBorderColor = LightCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = LightSurface,
                            unfocusedContainerColor = LightSurface
                        ),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = saveAmount.toDoubleOrNull() ?: 0.0
                        if (amt > 0) {
                            viewModel.withdrawFromKeeper(amt)
                            showWithdrawDialog = false
                            saveAmount = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPurple, contentColor = Color.White)
                ) {
                    Text("Withdraw", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWithdrawDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
            .padding(top = 20.dp, bottom = 90.dp)
    ) {
        Text(
            text = "Keeper",
            style = MaterialTheme.typography.displaySmall,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "A saving jar for your future",
            color = TextSecondary,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Large Jar Graphic Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(24.dp), spotColor = Color(0x15000000)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = LightSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(LightCardBorder, AccentGreen.copy(alpha = 0.4f))))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Jar icon container
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(AccentGreenBg)
                        .border(2.dp, AccentGreen, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Savings, contentDescription = null, tint = AccentGreen, modifier = Modifier.size(54.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "₹${"%.2f".format(currentUser?.keeperBalance ?: 0.0)}",
                    style = MaterialTheme.typography.displaySmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Total Rewards: ₹${"%.2f".format(currentUser?.totalRewardsEarned ?: 18.50)}",
                    color = AccentGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { showWithdrawDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Withdraw", color = TextPrimary, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { showSaveDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGreen, contentColor = Color.White)
                    ) {
                        Text("Save (Earn 3%)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Autosave info card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(1.dp, RoundedCornerShape(16.dp), spotColor = Color(0x10000000)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = LightSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(LightCardBorder, LightCardBorder)))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Autorenew, contentDescription = null, tint = BrandPurple)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Autosave Spare Change", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Round up every Jap payment to the nearest ₹10 and auto-save.", color = TextSecondary, fontSize = 12.sp)
                }
                Switch(
                    checked = true,
                    onCheckedChange = { viewModel.showToast("Autosave toggled!") },
                    colors = SwitchDefaults.colors(checkedThumbColor = BrandPurple, checkedTrackColor = BrandPurpleTint)
                )
            }
        }
    }
}

@Composable
fun RewardsScreen(viewModel: JapPayViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
            .padding(top = 20.dp, bottom = 90.dp)
    ) {
        Text(
            text = "Rewards",
            style = MaterialTheme.typography.displaySmall,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Text("Scratch cards, cashback coupons and gifts", color = TextSecondary, fontSize = 14.sp)

        Spacer(modifier = Modifier.height(20.dp))

        // Rakhi Gift / Surprise Box Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(20.dp), spotColor = Color(0x15000000))
                .clickable { viewModel.showToast("Gift Box Claimed! ₹25 added to rewards.") },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = BrandPurpleTint),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(BrandPurple, BrandPurpleLight)))
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🎁", fontSize = 36.sp)
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Special Mystery Gift Box", color = BrandPurpleDark, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Send money to 3 friends this week to unlock ₹100 cashback!", color = TextSecondary, fontSize = 12.sp)
                }
                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = BrandPurple)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Your Scratch Cards", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ScratchCardItem(title = "Flat ₹50 Jap Cashback", subtitle = "Unlocked on next transfer", color = AccentBlue) {
                viewModel.showToast("Scratch card unlocked! Keep transacting.")
            }
            ScratchCardItem(title = "100% Free Transfer", subtitle = "Zero virtual fee pass", color = BrandPurple) {
                viewModel.showToast("Fee pass active!")
            }
        }
    }
}

@Composable
fun ScratchCardItem(title: String, subtitle: String, color: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .clickable(onClick = onClick)
            .height(140.dp)
            .shadow(2.dp, RoundedCornerShape(16.dp), spotColor = Color(0x10000000)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(LightCardBorder, LightCardBorder)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.CardGiftcard, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }

            Column {
                Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(subtitle, color = TextSecondary, fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun BankScreen(viewModel: JapPayViewModel) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
            .padding(top = 20.dp, bottom = 90.dp)
    ) {
        Text(
            text = "Bank & Ledger",
            style = MaterialTheme.typography.displaySmall,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Text("In-app internal virtual currency ledger", color = TextSecondary, fontSize = 14.sp)

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(18.dp), spotColor = Color(0x10000000)),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = LightSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(LightCardBorder, LightCardBorder)))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.AccountBalance, contentDescription = null, tint = BrandPurple)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Jap Pay Virtual Bank Account", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text("Account Holder: ${currentUser?.name ?: "User"}", color = TextSecondary, fontSize = 13.sp)
                Text("VPA / UPI ID: ${currentUser?.id ?: "user@jap"}", color = BrandPurple, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text("IFSC Code: JAPP0000889 (Virtual)", color = TextSecondary, fontSize = 13.sp)
                Text("Daily Transfer Limit: ₹50,000.00", color = TextSecondary, fontSize = 13.sp)
            }
        }
    }
}

