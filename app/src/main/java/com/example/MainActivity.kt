package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.JapBottomNavigationBar
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.JapPayViewModel
import com.example.ui.viewmodel.MainTab
import com.example.ui.viewmodel.Screen
import kotlinx.coroutines.delay

class MainActivity : FragmentActivity() {
    private val viewModel: JapPayViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        requestAppPermissions()

        setContent {
            JapPayTheme {
                JapPayApp(viewModel)
            }
        }
    }

    private fun requestAppPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        val needed = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (needed.isNotEmpty()) {
            permissionLauncher.launch(needed.toTypedArray())
        }
    }
}

@Composable
fun JapPayApp(viewModel: JapPayViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val toastMsg by viewModel.toastMessage.collectAsStateWithLifecycle()

    LaunchedEffect(toastMsg) {
        if (toastMsg != null) {
            delay(3000)
            viewModel.clearToast()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
    ) {
        Scaffold(
            bottomBar = {
                if (currentScreen == Screen.Main) {
                    JapBottomNavigationBar(
                        currentTab = currentTab,
                        onTabSelected = { viewModel.currentTab.value = it }
                    )
                }
            },
            containerColor = LightBackground
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = innerPadding.calculateTopPadding(),
                        bottom = if (currentScreen == Screen.Main) 0.dp else innerPadding.calculateBottomPadding()
                    )
            ) {
                when (currentScreen) {
                    Screen.Splash -> SplashScreen(viewModel)
                    Screen.Login -> LoginScreen(viewModel)
                    Screen.SignUp -> SignUpScreen(viewModel)
                    Screen.Admin -> AdminPanelScreen(viewModel)
                    Screen.SendMoney -> SendMoneyScreen(viewModel)
                    Screen.AddMoney -> AddMoneyScreen(viewModel)
                    Screen.BuyCustomId -> BuyCustomIdScreen(viewModel)
                    Screen.PaymentReceipt -> PaymentReceiptScreen(viewModel)
                    Screen.Notifications -> NotificationsScreen(viewModel)
                    Screen.TransactionHistory -> TransactionHistoryScreen(viewModel)
                    Screen.Scanner -> FullScreenScannerView(viewModel, onClose = { viewModel.currentScreen.value = Screen.Main })
                    Screen.Main -> {
                        when (currentTab) {
                            MainTab.HOME -> HomeScreen(viewModel)
                            MainTab.WALLET -> WalletScreen(viewModel)
                            MainTab.KEEPER -> KeeperScreen(viewModel)
                            MainTab.REWARDS -> RewardsScreen(viewModel)
                            MainTab.BANK -> BankScreen(viewModel)
                        }
                    }
                }
            }
        }

        // Animated In-App Toast Banner
        AnimatedVisibility(
            visible = toastMsg != null,
            enter = slideInVertically(initialOffsetY = { -it }),
            exit = slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            toastMsg?.let { msg ->
                Surface(
                    color = LightSurface,
                    shape = RoundedCornerShape(16.dp),
                    tonalElevation = 6.dp,
                    shadowElevation = 8.dp,
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(LightCardBorder)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(BrandPurple)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = msg,
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

