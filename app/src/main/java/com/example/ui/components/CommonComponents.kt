package com.example.ui.components

import android.graphics.Bitmap
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.data.model.Country
import com.example.data.model.CountryData
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainTab
import com.example.util.QrCodeUtils

val avatarColors = listOf(
    Color(0xFF5F259F), // PhonePe Purple
    Color(0xFF1E88E5), // Blue
    Color(0xFF00897B), // Teal
    Color(0xFFFB8C00), // Orange
    Color(0xFFD81B60), // Pink
    Color(0xFF43A047)  // Green
)

@Composable
fun JapAvatar(
    name: String,
    modifier: Modifier = Modifier,
    colorIndex: Int = 0,
    size: Int = 42
) {
    val initial = name.firstOrNull()?.uppercase() ?: "J"
    val color = avatarColors[colorIndex % avatarColors.size]

    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = (size * 0.44).sp
        )
    }
}

@Composable
fun QrCodeDisplay(
    content: String,
    modifier: Modifier = Modifier,
    size: Int = 220,
    customImageUrl: String? = null
) {
    val qrBitmap = remember(content) {
        QrCodeUtils.generateQrBitmap(content, size = 512)
    }

    Box(
        modifier = modifier
            .size(size.dp)
            .shadow(4.dp, RoundedCornerShape(20.dp), spotColor = Color(0x1A000000))
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .border(1.dp, LightCardBorder, RoundedCornerShape(20.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        if (!customImageUrl.isNullOrBlank()) {
            AsyncImage(
                model = customImageUrl,
                contentDescription = "Custom Payment QR",
                modifier = Modifier.fillMaxSize()
            )
        } else if (qrBitmap != null) {
            Image(
                bitmap = qrBitmap.asImageBitmap(),
                contentDescription = "Jap Pay QR Code",
                modifier = Modifier.fillMaxSize()
            )
        } else {
            CircularProgressIndicator(color = BrandPurple)
        }
    }
}

@Composable
fun JapCameraPreview(
    modifier: Modifier = Modifier,
    isFlashOn: Boolean = false
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                val provider = cameraProviderFuture.get()
                cameraProvider = provider
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                try {
                    provider.unbindAll()
                    val camera = provider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
                    camera.cameraControl.enableTorch(isFlashOn)
                } catch (_: Exception) {}
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryCodeModal(
    selectedCountry: Country,
    onCountrySelected: (Country) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = remember(searchQuery) {
        if (searchQuery.isBlank()) CountryData.countries
        else CountryData.countries.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.dialCode.contains(searchQuery) ||
            it.code.contains(searchQuery, ignoreCase = true)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = LightSurface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = LightCardBorder) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Select Country Code",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search country or code...", color = TextSecondary) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = BrandPurple) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandPurple,
                    unfocusedBorderColor = LightCardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 350.dp)
            ) {
                items(filtered) { country ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                onCountrySelected(country)
                                onDismiss()
                            }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = country.flag, fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = country.name,
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = country.dialCode,
                            style = MaterialTheme.typography.titleMedium,
                            color = BrandPurple,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    HorizontalDivider(color = LightDivider)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun JapBottomNavigationBar(
    currentTab: MainTab,
    onTabSelected: (MainTab) -> Unit
) {
    NavigationBar(
        containerColor = LightSurface,
        tonalElevation = 6.dp,
        modifier = Modifier
            .border(width = 0.5.dp, color = LightCardBorder, shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
    ) {
        NavigationBarItem(
            selected = currentTab == MainTab.BANK,
            onClick = { onTabSelected(MainTab.BANK) },
            icon = { Icon(Icons.Outlined.AccountBalance, contentDescription = "Bank") },
            label = { Text("Bank", fontSize = 11.sp, fontWeight = if (currentTab == MainTab.BANK) FontWeight.Bold else FontWeight.Normal) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = BrandPurple,
                selectedTextColor = BrandPurple,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary,
                indicatorColor = BrandPurpleTint
            )
        )
        NavigationBarItem(
            selected = currentTab == MainTab.WALLET,
            onClick = { onTabSelected(MainTab.WALLET) },
            icon = { Icon(Icons.Outlined.AccountBalanceWallet, contentDescription = "Wallet") },
            label = { Text("Wallet", fontSize = 11.sp, fontWeight = if (currentTab == MainTab.WALLET) FontWeight.Bold else FontWeight.Normal) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = BrandPurple,
                selectedTextColor = BrandPurple,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary,
                indicatorColor = BrandPurpleTint
            )
        )
        NavigationBarItem(
            selected = currentTab == MainTab.HOME,
            onClick = { onTabSelected(MainTab.HOME) },
            icon = {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (currentTab == MainTab.HOME) BrandPurple else BrandPurpleTint),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Bolt,
                        contentDescription = "Home",
                        tint = if (currentTab == MainTab.HOME) Color.White else BrandPurple
                    )
                }
            },
            label = { Text("Home", fontSize = 11.sp, fontWeight = if (currentTab == MainTab.HOME) FontWeight.Bold else FontWeight.Normal) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = BrandPurple,
                selectedTextColor = BrandPurple,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = currentTab == MainTab.REWARDS,
            onClick = { onTabSelected(MainTab.REWARDS) },
            icon = { Icon(Icons.Outlined.EmojiEvents, contentDescription = "Rewards") },
            label = { Text("Rewards", fontSize = 11.sp, fontWeight = if (currentTab == MainTab.REWARDS) FontWeight.Bold else FontWeight.Normal) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = BrandPurple,
                selectedTextColor = BrandPurple,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary,
                indicatorColor = BrandPurpleTint
            )
        )
        NavigationBarItem(
            selected = currentTab == MainTab.KEEPER,
            onClick = { onTabSelected(MainTab.KEEPER) },
            icon = { Icon(Icons.Outlined.Savings, contentDescription = "Keeper") },
            label = { Text("Keeper", fontSize = 11.sp, fontWeight = if (currentTab == MainTab.KEEPER) FontWeight.Bold else FontWeight.Normal) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = BrandPurple,
                selectedTextColor = BrandPurple,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary,
                indicatorColor = BrandPurpleTint
            )
        )
    }
}

