package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Country
import com.example.data.model.CountryData
import com.example.ui.components.CountryCodeModal
import com.example.ui.theme.*
import com.example.ui.viewmodel.JapPayViewModel
import com.example.ui.viewmodel.Screen
import com.example.util.BiometricHelper

@Composable
fun LoginScreen(viewModel: JapPayViewModel) {
    val context = LocalContext.current
    var selectedCountry by remember { mutableStateOf(CountryData.countries.first { it.dialCode == "+91" }) }
    var showCountryPicker by remember { mutableStateOf(false) }

    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var showOtpField by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isSendingOtp by viewModel.isSendingOtp.collectAsStateWithLifecycle()
    val isVerifyingOtp by viewModel.isVerifyingOtp.collectAsStateWithLifecycle()
    val otpSentMessage by viewModel.otpSentMessage.collectAsStateWithLifecycle()

    if (showCountryPicker) {
        CountryCodeModal(
            selectedCountry = selectedCountry,
            onCountrySelected = { selectedCountry = it },
            onDismiss = { showCountryPicker = false }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
            .padding(horizontal = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // App Logo
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .shadow(8.dp, CircleShape, spotColor = BrandPurple)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(BrandPurple, BrandPurpleDark))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Bolt,
                    contentDescription = "Jap Pay",
                    tint = Color.White,
                    modifier = Modifier.size(46.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Jap Pay",
                style = MaterialTheme.typography.displaySmall,
                color = BrandPurple,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )

            Text(
                text = "Instant virtual currency peer-to-peer payments",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Country & Phone Input
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Country Code Button
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { showCountryPicker = true }
                        .border(1.dp, LightCardBorder, RoundedCornerShape(14.dp)),
                    color = LightSurface,
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 15.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(selectedCountry.flag, fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(selectedCountry.dialCode, color = TextPrimary, fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Mobile Number Field
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    placeholder = { Text("Mobile Number", color = TextSecondary) },
                    modifier = Modifier.weight(1f),
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
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Password Input
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("Password", color = TextSecondary) },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = TextSecondary
                        )
                    }
                },
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

            if (showOtpField) {
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = otpCode,
                    onValueChange = { if (it.length <= 6) otpCode = it },
                    placeholder = { Text("Enter 6-digit SMS OTP", color = TextSecondary) },
                    leadingIcon = { Icon(Icons.Default.Sms, contentDescription = null, tint = BrandPurple) },
                    trailingIcon = {
                        if (isSendingOtp) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = BrandPurple)
                        } else {
                            TextButton(onClick = { viewModel.requestOtp(selectedCountry, phone) }) {
                                Text("Resend", color = BrandPurple, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    },
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
            }

            if (otpSentMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = AccentGreenBg,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        otpSentMessage!!,
                        color = AccentGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(8.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(errorMessage!!, color = AccentRed, fontSize = 13.sp, textAlign = TextAlign.Center)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Login Button
            Button(
                onClick = {
                    errorMessage = null
                    viewModel.loginWithCredentials(
                        country = selectedCountry,
                        phone = phone,
                        pass = password,
                        otpCode = if (showOtpField) otpCode else null,
                        onSuccess = {},
                        onError = { errorMessage = it }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandPurple,
                    contentColor = Color.White
                )
            ) {
                Text("Login to Jap Pay", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // SMS8 OTP Option
            OutlinedButton(
                onClick = {
                    showOtpField = true
                    viewModel.requestOtp(selectedCountry, phone)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandPurple),
                border = ButtonDefaults.outlinedButtonBorder().copy(brush = Brush.horizontalGradient(listOf(BrandPurple, BrandPurpleLight)))
            ) {
                Icon(Icons.Default.Pin, contentDescription = null, tint = BrandPurple)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Login via SMS OTP", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Fingerprint Quick Auth Button
            if (context is FragmentActivity && BiometricHelper.canAuthenticate(context)) {
                IconButton(
                    onClick = {
                        BiometricHelper.promptBiometric(
                            activity = context,
                            onSuccess = {
                                viewModel.currentUserId.value = "8791738300@jap"
                                viewModel.currentScreen.value = Screen.Main
                            },
                            onError = { errorMessage = it }
                        )
                    },
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(BrandPurpleTint)
                ) {
                    Icon(
                        Icons.Default.Fingerprint,
                        contentDescription = "Fingerprint Login",
                        tint = BrandPurple,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Tap for Fingerprint Login", color = TextSecondary, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Switch to Sign Up
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { viewModel.currentScreen.value = Screen.SignUp }
            ) {
                Text("Don't have an account? ", color = TextSecondary, fontSize = 14.sp)
                Text("Sign Up with AI KYC", color = BrandPurple, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SignUpScreen(viewModel: JapPayViewModel) {
    val context = LocalContext.current
    var selectedCountry by remember { mutableStateOf(CountryData.countries.first { it.dialCode == "+91" }) }
    var showCountryPicker by remember { mutableStateOf(false) }

    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isSendingOtp by viewModel.isSendingOtp.collectAsStateWithLifecycle()
    val isVerifyingOtp by viewModel.isVerifyingOtp.collectAsStateWithLifecycle()
    val aadhaarBitmap by viewModel.aadhaarBitmap.collectAsStateWithLifecycle()
    val isVerifyingAadhaar by viewModel.isVerifyingAadhaar.collectAsStateWithLifecycle()
    val aadhaarResult by viewModel.aadhaarAiResult.collectAsStateWithLifecycle()
    val otpVerified by viewModel.otpVerified.collectAsStateWithLifecycle()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                val stream = context.contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(stream)
                if (bitmap != null) {
                    viewModel.verifyAadhaarWithAi(bitmap)
                }
            } catch (_: Exception) {}
        }
    }

    if (showCountryPicker) {
        CountryCodeModal(
            selectedCountry = selectedCountry,
            onCountrySelected = { selectedCountry = it },
            onDismiss = { showCountryPicker = false }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
            .padding(horizontal = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(28.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.currentScreen.value = Screen.Login }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Text(
                    text = "Create Jap Pay Account",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Form Fields
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full Name") },
                placeholder = { Text("e.g. Devansh") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = BrandPurple) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandPurple,
                    unfocusedBorderColor = LightCardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = LightSurface,
                    unfocusedContainerColor = LightSurface
                ),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Phone with country code
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { showCountryPicker = true }
                        .border(1.dp, LightCardBorder, RoundedCornerShape(14.dp)),
                    color = LightSurface,
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 15.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(selectedCountry.flag, fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(selectedCountry.dialCode, color = TextPrimary, fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Mobile Number") },
                    placeholder = { Text("10 digits") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandPurple,
                        unfocusedBorderColor = LightCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = LightSurface,
                        unfocusedContainerColor = LightSurface
                    ),
                    shape = RoundedCornerShape(14.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Create Password") },
                visualTransformation = PasswordVisualTransformation(),
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = BrandPurple) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandPurple,
                    unfocusedBorderColor = LightCardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = LightSurface,
                    unfocusedContainerColor = LightSurface
                ),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Address / City") },
                placeholder = { Text("e.g. Sector 62, Noida") },
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = BrandPurple) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandPurple,
                    unfocusedBorderColor = LightCardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = LightSurface,
                    unfocusedContainerColor = LightSurface
                ),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // AI Aadhaar Verification Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(16.dp), spotColor = Color(0x12000000)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = LightSurface),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(LightCardBorder, BrandPurpleTint)))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.VerifiedUser, contentDescription = null, tint = BrandPurple)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "AI Aadhaar Verification",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Upload or take a photo of your Aadhaar card. Gemini AI verifies authenticity and creates your verified @jap handle.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (aadhaarBitmap != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(LightCardElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                bitmap = aadhaarBitmap!!.asImageBitmap(),
                                contentDescription = "Aadhaar Card",
                                modifier = Modifier.fillMaxSize()
                            )

                            if (isVerifyingAadhaar) {
                                Surface(
                                    color = Color.Black.copy(alpha = 0.7f),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        CircularProgressIndicator(color = BrandPurple)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("AI analyzing document...", color = Color.White, fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        if (aadhaarResult != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = if (aadhaarResult!!.isAuthentic) AccentGreenBg else AccentRedBg,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        if (aadhaarResult!!.isAuthentic) Icons.Default.CheckCircle else Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = if (aadhaarResult!!.isAuthentic) AccentGreen else AccentRed
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = aadhaarResult!!.statusText,
                                            color = if (aadhaarResult!!.isAuthentic) AccentGreen else AccentRed,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "${aadhaarResult!!.maskedNumber} • ${aadhaarResult!!.details}",
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Button(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandPurpleTint),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = BrandPurple)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Select Aadhaar Photo / Scan", color = BrandPurple, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SMS8 OTP Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = otpCode,
                    onValueChange = { if (it.length <= 6) otpCode = it },
                    placeholder = { Text("SMS OTP", color = TextSecondary) },
                    modifier = Modifier.weight(1f),
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

                Spacer(modifier = Modifier.width(10.dp))

                Button(
                    onClick = {
                        if (phone.isNotBlank()) {
                            viewModel.requestOtp(selectedCountry, phone)
                        } else {
                            errorMessage = "Enter mobile number first"
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPurpleTint)
                ) {
                    if (isSendingOtp) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = BrandPurple)
                    } else {
                        Text("Get OTP", color = BrandPurple, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(errorMessage!!, color = AccentRed, fontSize = 13.sp, textAlign = TextAlign.Center)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sign Up Submit
            Button(
                onClick = {
                    errorMessage = null
                    viewModel.registerUser(
                        name = fullName,
                        country = selectedCountry,
                        phone = phone,
                        password = password,
                        address = address,
                        onSuccess = {},
                        onError = { errorMessage = it }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandPurple,
                    contentColor = Color.White
                )
            ) {
                Text("Complete Registration (Get ₹100 Bonus)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

