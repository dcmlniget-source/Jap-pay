package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String, // e.g. "8791738300@jap"
    val name: String,
    val phone: String, // e.g. "8791738300"
    val countryCode: String = "+91",
    val password: String,
    val address: String = "",
    val aadhaarImageUri: String? = null,
    val aadhaarVerified: Boolean = true,
    val aadhaarNumberMasked: String = "XXXX-XXXX-1234",
    val aadhaarVerificationNotes: String = "AI Verified Authentic",
    val walletBalance: Double = 0.0,
    val keeperBalance: Double = 0.0,
    val totalRewardsEarned: Double = 0.0,
    val isBiometricEnabled: Boolean = true,
    val isAdmin: Boolean = false,
    val avatarColorIndex: Int = 0,
    val customId: String? = null, // e.g. "alex@jap"
    val isCustomIdActive: Boolean = false,
    val customIdExpiresAt: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val senderId: String,
    val senderName: String,
    val receiverId: String,
    val receiverName: String,
    val amount: Double,
    val message: String = "",
    val status: String = "SUCCESS", // "SUCCESS", "FAILED", "PENDING"
    val type: String = "TRANSFER", // "TRANSFER", "DEPOSIT", "REWARD", "KEEPER_SAVE", "KEEPER_WITHDRAW", "CUSTOM_ID_PURCHASE"
    val soundTriggered: String = "NONE", // "CRYING", "WOW", "NONE"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "deposit_requests")
data class DepositRequest(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val userName: String,
    val userPhone: String,
    val amount: Double,
    val utrNumber: String,
    val screenshotUri: String? = null,
    val status: String = "PENDING", // "PENDING", "APPROVED", "REJECTED"
    val adminNote: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "admin_config")
data class AdminConfig(
    @PrimaryKey val id: Int = 1,
    val adminPhone: String = "+918791738300",
    val adminPassword: String = "9876543211",
    val adminUpiId: String = "8791738300@jap",
    val adminQrData: String = "upi://pay?pa=8791738300@jap&pn=JapPayAdmin&cu=INR",
    val adminQrImageUrl: String = "",
    val paymentLink: String = "",
    val gatewayMode: String = "API_GATEWAY", // "API_GATEWAY", "MANUAL_GATEWAY", "LINK_GATEWAY"
    val famPayApiKey: String = "FAM_LIVE_sk_vZJ4iRe9T2Ouw1mAXzwT7OLVgsCj08xX",
    val customIdPrice: Double = 19.0,
    val depositInstructions: String = "Scan the QR or pay via active gateway. Balance credited instantly on verification!",
    val noticeMessage: String = "Welcome to Jap Pay! Instant peer-to-peer virtual transactions."
)

@Entity(tableName = "app_notifications")
data class AppNotification(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String, // "ALL" or specific user ID
    val title: String,
    val message: String,
    val type: String = "GENERAL", // "CREDIT", "DEBIT", "ADMIN_BROADCAST", "DEPOSIT_APPROVED", "DEPOSIT_REJECTED"
    val amount: Double = 0.0,
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

data class Country(
    val code: String,
    val name: String,
    val flag: String,
    val dialCode: String
)

object CountryData {
    val countries = listOf(
        Country("IN", "India", "🇮🇳", "+91"),
        Country("US", "United States", "🇺🇸", "+1"),
        Country("GB", "United Kingdom", "🇬🇧", "+44"),
        Country("AE", "United Arab Emirates", "🇦🇪", "+971"),
        Country("CA", "Canada", "🇨🇦", "+1"),
        Country("AU", "Australia", "🇦🇺", "+61"),
        Country("SG", "Singapore", "🇸🇬", "+65"),
        Country("SA", "Saudi Arabia", "🇸🇦", "+966"),
        Country("JP", "Japan", "🇯🇵", "+81"),
        Country("DE", "Germany", "🇩🇪", "+49"),
        Country("FR", "France", "🇫🇷", "+33"),
        Country("BD", "Bangladesh", "🇧🇩", "+880"),
        Country("NP", "Nepal", "🇳🇵", "+977"),
        Country("LK", "Sri Lanka", "🇱🇰", "+94"),
        Country("PK", "Pakistan", "🇵🇰", "+92"),
        Country("MY", "Malaysia", "🇲🇾", "+60"),
        Country("ID", "Indonesia", "🇮🇩", "+62"),
        Country("BR", "Brazil", "🇧🇷", "+55"),
        Country("ZA", "South Africa", "🇿🇦", "+27"),
        Country("RU", "Russia", "🇷🇺", "+7"),
        Country("IT", "Italy", "🇮🇹", "+39"),
        Country("ES", "Spain", "🇪🇸", "+34"),
        Country("NL", "Netherlands", "🇳🇱", "+31"),
        Country("NZ", "New Zealand", "🇳🇿", "+64"),
        Country("TH", "Thailand", "🇹🇭", "+66"),
        Country("VN", "Vietnam", "🇻🇳", "+84"),
        Country("PH", "Philippines", "🇵🇭", "+63"),
        Country("KW", "Kuwait", "🇰🇼", "+965"),
        Country("QA", "Qatar", "🇶🇦", "+974"),
        Country("OM", "Oman", "🇴🇲", "+968")
    )
}
