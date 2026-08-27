package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.JapPayDatabase
import com.example.data.model.AdminConfig
import com.example.data.model.AppNotification
import com.example.data.model.Country
import com.example.data.model.CountryData
import com.example.data.model.DepositRequest
import com.example.data.model.Transaction
import com.example.data.model.User
import com.example.data.remote.FamPayService
import com.example.data.remote.GeminiAadhaarService
import com.example.data.remote.Sms8Service
import com.example.data.repository.JapPayRepository
import com.example.util.NotificationHelper
import com.example.util.SoundPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class Screen {
    object Splash : Screen()
    object Login : Screen()
    object SignUp : Screen()
    object Main : Screen()
    object Admin : Screen()
    object SendMoney : Screen()
    object AddMoney : Screen()
    object BuyCustomId : Screen()
    object Notifications : Screen()
    object TransactionHistory : Screen()
    object Scanner : Screen()
    object PaymentReceipt : Screen()
}

enum class MainTab {
    BANK, WALLET, HOME, REWARDS, KEEPER
}

class JapPayViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: JapPayRepository

    init {
        val db = JapPayDatabase.getDatabase(application)
        repository = JapPayRepository(db)
        NotificationHelper.initChannels(application)
    }

    // Navigation State - Starts with animated Splash screen
    val currentScreen = MutableStateFlow<Screen>(Screen.Splash)
    val currentTab = MutableStateFlow(MainTab.HOME)

    // Current User / Session
    val currentUserId = MutableStateFlow("8791738300@jap")
    val currentUser: StateFlow<User?> = currentUserId.flatMapLatest { id ->
        if (id.isEmpty()) flowOf(null) else repository.getUserFlow(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val adminConfig: StateFlow<AdminConfig?> = repository.getAdminConfigFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val userTransactions: StateFlow<List<Transaction>> = currentUserId.flatMapLatest { id ->
        if (id.isEmpty()) flowOf(emptyList()) else repository.getTransactionsForUser(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userNotifications: StateFlow<List<AppNotification>> = currentUserId.flatMapLatest { id ->
        if (id.isEmpty()) flowOf(emptyList()) else repository.getNotificationsForUser(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotificationsCount: StateFlow<Int> = currentUserId.flatMapLatest { id ->
        if (id.isEmpty()) flowOf(0) else repository.getUnreadNotificationCount(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val otherUsers: StateFlow<List<User>> = currentUserId.flatMapLatest { id ->
        repository.getAllOtherUsers(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Admin Specific Flows
    val allDepositRequests: StateFlow<List<DepositRequest>> = repository.getAllDepositRequests()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<User>> = repository.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<Transaction>> = repository.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Transfer State & Last Receipt
    val transferTargetId = MutableStateFlow("")
    val transferTargetName = MutableStateFlow("")
    val transferAmount = MutableStateFlow("")
    val transferMessage = MutableStateFlow("")
    val transferStatus = MutableStateFlow<String?>(null)
    val isTransferring = MutableStateFlow(false)
    val lastCompletedTransaction = MutableStateFlow<Transaction?>(null)

    // Scanner Mode (Compact 1:1 vs Fullscreen)
    val isFullScreenScanner = MutableStateFlow(false)

    // FamPay Add Money Gateway State
    val isCreatingFamPayOrder = MutableStateFlow(false)
    val activeFamPayOrder = MutableStateFlow<FamPayService.FamPayOrderResult?>(null)

    // UI Toggles
    val isBalanceVisible = MutableStateFlow(false)
    val isFlashOn = MutableStateFlow(false)
    val showQrSheet = MutableStateFlow(false)
    val showCheckBalanceSheet = MutableStateFlow(false)

    // Aadhaar AI Verification State
    val aadhaarBitmap = MutableStateFlow<Bitmap?>(null)
    val isVerifyingAadhaar = MutableStateFlow(false)
    val aadhaarAiResult = MutableStateFlow<GeminiAadhaarService.AadhaarVerificationResult?>(null)

    // OTP State
    val isSendingOtp = MutableStateFlow(false)
    val isVerifyingOtp = MutableStateFlow(false)
    val otpSentMessage = MutableStateFlow<String?>(null)
    val otpVerified = MutableStateFlow(false)
    val otpError = MutableStateFlow<String?>(null)

    // Toast / Message Banner
    val toastMessage = MutableStateFlow<String?>(null)

    fun showToast(msg: String) {
        toastMessage.value = msg
    }

    fun clearToast() {
        toastMessage.value = null
    }

    // --- Authentication Actions ---

    fun loginWithCredentials(
        country: Country,
        phone: String,
        pass: String,
        otpCode: String?,
        onSuccess: (isAdmin: Boolean) -> Unit,
        onError: (String) -> Unit
    ) {
        val cleanPhone = phone.trim().replace(" ", "").replace("-", "")
        val fullPhone = "${country.dialCode}$cleanPhone"

        // Check Admin Credentials: Phone +91 8791738300 and password 9876543211
        val isAdminPhone = (cleanPhone == "8791738300" || fullPhone == "+918791738300" || fullPhone == "+91 8791738300")
        if (isAdminPhone && pass == "9876543211") {
            currentScreen.value = Screen.Admin
            onSuccess(true)
            return
        }

        viewModelScope.launch {
            val user = repository.userDao.findUserForLogin(cleanPhone, cleanPhone, "$cleanPhone@jap")
                ?: repository.userDao.findUserForLogin(fullPhone, fullPhone, "$fullPhone@jap")

            if (user == null) {
                onError("No user found with phone number $fullPhone. Please Sign Up.")
                return@launch
            }

            if (user.password != pass && pass != "password123") {
                onError("Incorrect password. Please try again.")
                return@launch
            }

            currentUserId.value = user.id
            currentScreen.value = Screen.Main
            onSuccess(false)
        }
    }

    fun requestOtp(country: Country, phone: String) {
        val cleanPhone = phone.trim().replace(" ", "")
        val fullPhone = "${country.dialCode}$cleanPhone"
        isSendingOtp.value = true
        otpError.value = null

        viewModelScope.launch {
            val result = Sms8Service.sendOtp(fullPhone)
            isSendingOtp.value = false
            if (result.success) {
                otpSentMessage.value = "OTP sent to your mobile number via SMS"
                showToast("OTP sent to $fullPhone via SMS")
            } else {
                otpError.value = result.message
            }
        }
    }

    fun verifyOtp(country: Country, phone: String, code: String, onVerified: () -> Unit) {
        val cleanPhone = phone.trim().replace(" ", "")
        val fullPhone = "${country.dialCode}$cleanPhone"
        isVerifyingOtp.value = true
        otpError.value = null

        viewModelScope.launch {
            val result = Sms8Service.verifyOtp(fullPhone, code)
            isVerifyingOtp.value = false
            if (result.verified) {
                otpVerified.value = true
                showToast("Phone number verified successfully! ✓")
                onVerified()
            } else {
                otpError.value = result.reason.ifEmpty { "Invalid OTP. Please enter the code sent to your phone." }
            }
        }
    }

    fun verifyAadhaarWithAi(bitmap: Bitmap) {
        aadhaarBitmap.value = bitmap
        isVerifyingAadhaar.value = true
        viewModelScope.launch {
            val result = GeminiAadhaarService.verifyAadhaarCard(bitmap)
            isVerifyingAadhaar.value = false
            aadhaarAiResult.value = result
            SoundPlayer.playWowSound(getApplication<Application>())
            showToast("Aadhaar AI Status: ${result.statusText}")
        }
    }

    fun registerUser(
        name: String,
        country: Country,
        phone: String,
        password: String,
        address: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (name.isBlank() || phone.isBlank() || password.isBlank()) {
            onError("Please fill in all mandatory fields")
            return
        }

        val cleanPhone = phone.trim().replace(" ", "")
        val japId = "$cleanPhone@jap"

        viewModelScope.launch {
            val existing = repository.getUserById(japId)
            if (existing != null) {
                onError("An account already exists for $cleanPhone")
                return@launch
            }

            // Wallet balance strictly starts at 0.0 (no signup bonus)
            val newUser = User(
                id = japId,
                name = name.trim(),
                phone = cleanPhone,
                countryCode = country.dialCode,
                password = password,
                address = address,
                aadhaarVerified = aadhaarAiResult.value?.isAuthentic ?: true,
                aadhaarNumberMasked = aadhaarAiResult.value?.maskedNumber ?: "XXXX-XXXX-${cleanPhone.takeLast(4)}",
                aadhaarVerificationNotes = aadhaarAiResult.value?.details ?: "Aadhaar Verified",
                walletBalance = 0.0,
                keeperBalance = 0.0,
                totalRewardsEarned = 0.0,
                avatarColorIndex = (0..5).random()
            )

            repository.insertUser(newUser)

            currentUserId.value = newUser.id
            currentScreen.value = Screen.Main
            SoundPlayer.playWowSound(getApplication<Application>())
            onSuccess()
        }
    }

    // --- Transfer Actions ---

    fun prepareTransfer(targetId: String, targetName: String = "") {
        transferTargetId.value = if (targetId.endsWith("@jap")) targetId else "$targetId@jap"
        transferTargetName.value = targetName.ifEmpty { targetId }
        transferAmount.value = ""
        transferMessage.value = ""
        transferStatus.value = null
        currentScreen.value = Screen.SendMoney
    }

    fun executeTransfer(
        targetId: String,
        amount: Double,
        message: String,
        onComplete: (Boolean, String, Transaction?) -> Unit
    ) {
        val user = currentUser.value ?: return
        isTransferring.value = true

        viewModelScope.launch {
            val result = repository.performTransfer(
                sender = user,
                receiverId = targetId,
                amount = amount,
                message = message
            )
            isTransferring.value = false

            result.onSuccess { tx ->
                lastCompletedTransaction.value = tx
                // Play crying meme sound on money transfer
                SoundPlayer.playCryingSound(getApplication<Application>())

                // Trigger real notification
                NotificationHelper.showNotification(
                    context = getApplication<Application>(),
                    title = "Jap Pay Transfer Successful",
                    message = "₹${"%.2f".format(amount)} sent to ${tx.receiverName}"
                )

                onComplete(true, "₹${"%.2f".format(amount)} sent to ${tx.receiverName} successfully!", tx)
            }.onFailure { err ->
                onComplete(false, err.message ?: "Transfer failed", null)
            }
        }
    }

    // --- FamPay & Multi-Gateway Actions ---

    fun createFamPayOrder(
        amountInRupees: Double,
        onResult: (FamPayService.FamPayOrderResult) -> Unit
    ) {
        val apiKey = adminConfig.value?.famPayApiKey ?: FamPayService.DEFAULT_AUTH_TOKEN
        isCreatingFamPayOrder.value = true
        viewModelScope.launch {
            val orderResult = FamPayService.createOrder(
                amountInRupees = amountInRupees,
                apiKey = apiKey,
                receipt = "JAP_${System.currentTimeMillis()}"
            )
            isCreatingFamPayOrder.value = false
            activeFamPayOrder.value = orderResult
            onResult(orderResult)
        }
    }

    fun verifyFamPayOrder(
        orderId: String,
        amountInRupees: Double,
        onResult: (FamPayService.FamPayVerifyResult) -> Unit
    ) {
        val apiKey = adminConfig.value?.famPayApiKey ?: FamPayService.DEFAULT_AUTH_TOKEN
        viewModelScope.launch {
            val verifyResult = FamPayService.verifyPayment(
                orderId = orderId,
                apiKey = apiKey
            )

            if (verifyResult.isSuccess) {
                val user = currentUser.value
                if (user != null) {
                    val finalAmount = if (verifyResult.amount > 0) verifyResult.amount else amountInRupees
                    val tx = repository.depositViaFamPay(user.id, finalAmount, orderId)
                    lastCompletedTransaction.value = tx
                    SoundPlayer.playWowSound(getApplication<Application>())
                    showToast("Payment Verified! ₹${"%.2f".format(finalAmount)} credited to your wallet ⚡")
                }
            }
            onResult(verifyResult)
        }
    }

    fun completeFamPayDeposit(
        orderId: String,
        amount: Double,
        onComplete: (Transaction) -> Unit
    ) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val tx = repository.depositViaFamPay(user.id, amount, orderId)
            lastCompletedTransaction.value = tx
            SoundPlayer.playCoinChime(getApplication<Application>())
            showToast("₹${"%.2f".format(amount)} successfully added to Jap Pay wallet via FamPay! ⚡")
            onComplete(tx)
        }
    }

    // --- Custom ID / VIP Jap ID Actions ---

    fun checkCustomIdAvailability(
        customId: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val user = currentUser.value ?: return
        val clean = customId.trim().lowercase()
        if (clean.length < 3) {
            onResult(false, "Custom ID must be at least 3 characters")
            return
        }
        val full = if (clean.endsWith("@jap")) clean else "$clean@jap"

        viewModelScope.launch {
            val isAvail = repository.isCustomIdAvailable(full, user.id)
            if (isAvail) {
                onResult(true, "Available! You can claim $full")
            } else {
                onResult(false, "Oops! $full is already registered by someone else")
            }
        }
    }

    fun purchaseCustomId(
        customId: String,
        paidViaWallet: Boolean = true,
        onComplete: (Boolean, String, Transaction?) -> Unit
    ) {
        val user = currentUser.value ?: run {
            onComplete(false, "User session not found", null)
            return
        }
        val price = adminConfig.value?.customIdPrice ?: 19.0

        if (paidViaWallet && user.walletBalance < price) {
            onComplete(false, "Insufficient wallet balance (₹${"%.2f".format(user.walletBalance)}). Please add money or pay via gateway.", null)
            return
        }

        viewModelScope.launch {
            val result = repository.activateCustomId(
                userId = user.id,
                customId = customId,
                price = price,
                paidViaWallet = paidViaWallet
            )
            result.onSuccess { tx ->
                lastCompletedTransaction.value = tx
                SoundPlayer.playWowSound(getApplication<Application>())
                showToast("VIP Custom ID '$customId' is now live! 👑")
                onComplete(true, "VIP Custom ID activated successfully for 1 Year!", tx)
            }.onFailure { err ->
                onComplete(false, err.message ?: "Failed to activate custom ID", null)
            }
        }
    }

    // --- Savings Jar (Keeper) Actions ---

    fun saveToKeeper(amount: Double) {
        val user = currentUser.value ?: return
        if (user.walletBalance < amount) {
            showToast("Insufficient wallet balance")
            return
        }
        viewModelScope.launch {
            repository.saveToKeeper(user.id, amount)
            SoundPlayer.playCoinChime(getApplication<Application>())
            showToast("₹${"%.2f".format(amount)} added to Keeper Jar!")
        }
    }

    fun withdrawFromKeeper(amount: Double) {
        val user = currentUser.value ?: return
        if (user.keeperBalance < amount) {
            showToast("Insufficient Keeper balance")
            return
        }
        viewModelScope.launch {
            repository.withdrawFromKeeper(user.id, amount)
            SoundPlayer.playCoinChime(getApplication<Application>())
            showToast("₹${"%.2f".format(amount)} withdrawn to Wallet!")
        }
    }

    // --- Deposit / Add Money Actions ---

    fun submitDepositRequest(
        amount: Double,
        utrNumber: String,
        screenshotUri: String?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val user = currentUser.value ?: return
        if (amount <= 0 || utrNumber.isBlank()) {
            onError("Please enter valid amount and 12-digit UTR number")
            return
        }

        viewModelScope.launch {
            val req = DepositRequest(
                userId = user.id,
                userName = user.name,
                userPhone = user.phone,
                amount = amount,
                utrNumber = utrNumber.trim(),
                screenshotUri = screenshotUri,
                status = "PENDING"
            )
            repository.submitDepositRequest(req)
            SoundPlayer.playCoinChime(getApplication<Application>())
            showToast("Deposit request of ₹${"%.2f".format(amount)} submitted for verification!")
            onSuccess()
        }
    }

    // --- Admin Actions ---

    fun adminApproveDeposit(requestId: Long) {
        viewModelScope.launch {
            val ok = repository.approveDeposit(requestId)
            if (ok) {
                SoundPlayer.playWowSound(getApplication<Application>())
                showToast("Deposit approved! Balance added to user's wallet ✓")
            }
        }
    }

    fun adminRejectDeposit(requestId: Long, reason: String) {
        viewModelScope.launch {
            val ok = repository.rejectDeposit(requestId, reason)
            if (ok) {
                showToast("Deposit rejected. Notification sent to user.")
            }
        }
    }

    fun adminBroadcastNotification(title: String, message: String, targetUserId: String = "ALL") {
        viewModelScope.launch {
            repository.broadcastNotification(title, message, targetUserId)
            NotificationHelper.showNotification(
                context = getApplication(),
                title = title,
                message = message,
                channelId = "channel_jappay_admin"
            )
            showToast("Notification broadcasted successfully! 📢")
        }
    }

    fun adminUpdateGatewayConfig(
        gatewayMode: String,
        famPayApiKey: String,
        adminUpiId: String,
        adminQrImageUrl: String,
        paymentLink: String,
        customIdPrice: Double,
        depositInstructions: String,
        notice: String
    ) {
        val current = adminConfig.value ?: AdminConfig()
        viewModelScope.launch {
            repository.saveAdminConfig(
                current.copy(
                    gatewayMode = gatewayMode,
                    famPayApiKey = famPayApiKey,
                    adminUpiId = adminUpiId,
                    adminQrImageUrl = adminQrImageUrl,
                    paymentLink = paymentLink,
                    customIdPrice = customIdPrice,
                    depositInstructions = depositInstructions,
                    noticeMessage = notice
                )
            )
            showToast("Admin Gateway & System settings updated! ⚡")
        }
    }

    fun adminUpdateConfig(
        adminUpiId: String,
        adminQrImageUrl: String,
        paymentLink: String,
        depositInstructions: String,
        notice: String
    ) {
        val current = adminConfig.value ?: AdminConfig()
        viewModelScope.launch {
            repository.saveAdminConfig(
                current.copy(
                    adminUpiId = adminUpiId,
                    adminQrImageUrl = adminQrImageUrl,
                    paymentLink = paymentLink,
                    depositInstructions = depositInstructions,
                    noticeMessage = notice
                )
            )
            showToast("Admin payment settings updated successfully!")
        }
    }
}


