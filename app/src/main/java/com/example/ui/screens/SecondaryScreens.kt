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
            .background(DarkBackground)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
            .padding(top = 20.dp, bottom = 90.dp)
    ) {
        Text(
            text = "Wallet",
            style = MaterialTheme.typography.displayMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Check Balance Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCardElevated),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(DarkCardBorder, JapYellowPrimary.copy(alpha = 0.5f))))
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Check balance", color = TextSecondary, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = if (isBalanceHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle Balance",
                            tint = JapYellowPrimary,
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
                        containerColor = JapYellowPrimary,
                        contentColor = DarkBackground
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
                .height(190.dp),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(JapYellowPrimary, JapYellowDark)))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF2E2405), Color(0xFF151410), Color(0xFF382C06))
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
                            Icon(Icons.Filled.Bolt, contentDescription = null, tint = JapYellowPrimary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("jap pay", color = JapYellowPrimary, fontWeight = FontWeight.Black, fontSize = 18.sp)
                        }
                        Text("PREPAID VIRTUAL", color = JapYellowLight, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp, 26.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(JapGold)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(Icons.Default.Contactless, contentDescription = null, tint = JapYellowLight)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(currentUser?.name?.uppercase() ?: "DEVANSH", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(currentUser?.id ?: "8791738300@jap", color = JapYellowPrimary, fontSize = 12.sp)
                        }
                        Text("RuPay", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Personal QR Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(DarkCardBorder, DarkCardBorder)))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Your Personal QR", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(currentUser?.id ?: "user@jap", color = JapYellowPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
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
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = JapYellowPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy ID", color = TextPrimary, fontSize = 12.sp)
                    }

                    Button(
                        onClick = { viewModel.showToast("Sharing Jap Pay QR Code...") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = JapYellowPrimary, contentColor = DarkBackground),
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
            containerColor = DarkSurface,
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
                            focusedBorderColor = JapYellowPrimary,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
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
                    colors = ButtonDefaults.buttonColors(containerColor = JapYellowPrimary, contentColor = DarkBackground)
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
            containerColor = DarkSurface,
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
                            focusedBorderColor = JapYellowPrimary,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
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
                    colors = ButtonDefaults.buttonColors(containerColor = JapYellowPrimary, contentColor = DarkBackground)
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
            .background(DarkBackground)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
            .padding(top = 20.dp, bottom = 90.dp)
    ) {
        Text(
            text = "Keeper",
            style = MaterialTheme.typography.displayMedium,
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
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(DarkCardBorder, AccentGreen.copy(alpha = 0.4f))))
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
                    style = MaterialTheme.typography.displayMedium,
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
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGreen, contentColor = Color.Black)
                    ) {
                        Text("Save (Earn 3%)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Autosave info card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCardElevated)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Autorenew, contentDescription = null, tint = JapYellowPrimary)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Autosave Spare Change", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Round up every Jap payment to the nearest ₹10 and auto-save.", color = TextSecondary, fontSize = 12.sp)
                }
                Switch(
                    checked = true,
                    onCheckedChange = { viewModel.showToast("Autosave toggled!") },
                    colors = SwitchDefaults.colors(checkedThumbColor = JapYellowPrimary, checkedTrackColor = DarkCard)
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
            .background(DarkBackground)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
            .padding(top = 20.dp, bottom = 90.dp)
    ) {
        Text(
            text = "Rewards",
            style = MaterialTheme.typography.displayMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Text("Scratch cards, cashback coupons and gifts", color = TextSecondary, fontSize = 14.sp)

        Spacer(modifier = Modifier.height(20.dp))

        // Rakhi Gift / Surprise Box Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.showToast("Gift Box Claimed! ₹25 added to rewards.") },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2A153A)),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(AccentPurple, JapYellowPrimary)))
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🎁", fontSize = 36.sp)
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Special Mystery Gift Box", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Send money to 3 friends this week to unlock ₹100 cashback!", color = TextSecondary, fontSize = 12.sp)
                }
                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = JapYellowPrimary)
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
            ScratchCardItem(title = "100% Free Transfer", subtitle = "Zero virtual fee pass", color = JapGold) {
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
            .height(140.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
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
                    .background(color.copy(alpha = 0.2f)),
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
            .background(DarkBackground)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
            .padding(top = 20.dp, bottom = 90.dp)
    ) {
        Text(
            text = "Bank & Ledger",
            style = MaterialTheme.typography.displayMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Text("In-app internal virtual currency ledger", color = TextSecondary, fontSize = 14.sp)

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCardElevated)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.AccountBalance, contentDescription = null, tint = JapYellowPrimary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Jap Pay Virtual Bank Account", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text("Account Holder: ${currentUser?.name ?: "Devansh"}", color = TextSecondary, fontSize = 13.sp)
                Text("VPA / UPI ID: ${currentUser?.id ?: "8791738300@jap"}", color = JapYellowPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text("IFSC Code: JAPP0000889 (Virtual)", color = TextSecondary, fontSize = 13.sp)
                Text("Daily Transfer Limit: ₹50,000.00", color = TextSecondary, fontSize = 13.sp)
            }
        }
    }
}
