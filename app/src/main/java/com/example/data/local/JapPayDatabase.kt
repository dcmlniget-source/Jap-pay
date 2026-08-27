package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AdminConfig
import com.example.data.model.AppNotification
import com.example.data.model.DepositRequest
import com.example.data.model.Transaction
import com.example.data.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        User::class,
        Transaction::class,
        DepositRequest::class,
        AdminConfig::class,
        AppNotification::class
    ],
    version = 1,
    exportSchema = false
)
abstract class JapPayDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun transactionDao(): TransactionDao
    abstract fun depositRequestDao(): DepositRequestDao
    abstract fun adminConfigDao(): AdminConfigDao
    abstract fun appNotificationDao(): AppNotificationDao

    companion object {
        @Volatile
        private var INSTANCE: JapPayDatabase? = null

        fun getDatabase(context: Context): JapPayDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JapPayDatabase::class.java,
                    "jappay_database"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDatabase(database)
                    }
                }
            }
        }

        suspend fun populateDatabase(database: JapPayDatabase) {
            val userDao = database.userDao()
            val adminConfigDao = database.adminConfigDao()
            val transactionDao = database.transactionDao()
            val notificationDao = database.appNotificationDao()

            // 1. Admin Config
            adminConfigDao.saveAdminConfig(
                AdminConfig(
                    id = 1,
                    adminPhone = "+918791738300",
                    adminPassword = "9876543211",
                    adminUpiId = "8791738300@jap",
                    adminQrData = "jappay://pay?id=8791738300@jap&name=JapPay+Admin",
                    depositInstructions = "1. Scan the Jap Pay QR code or send to UPI ID 8791738300@jap\n2. Enter 12-digit UTR and upload payment receipt\n3. Balance credited instantly on verification!",
                    noticeMessage = "Welcome to Jap Pay! 0% transaction fee peer-to-peer virtual money network."
                )
            )

            // 2. Preloaded Community Users
            val users = listOf(
                User(
                    id = "8791738300@jap",
                    name = "Devansh",
                    phone = "8791738300",
                    countryCode = "+91",
                    password = "password123",
                    address = "Sector 62, Noida, Uttar Pradesh, India",
                    aadhaarVerified = true,
                    aadhaarNumberMasked = "XXXX-XXXX-8300",
                    walletBalance = 1500.0,
                    keeperBalance = 350.0,
                    totalRewardsEarned = 18.50,
                    isAdmin = false,
                    avatarColorIndex = 0
                ),
                User(
                    id = "varun@jap",
                    name = "Varun Rathore",
                    phone = "9812345670",
                    countryCode = "+91",
                    password = "password123",
                    address = "Connaught Place, New Delhi",
                    aadhaarVerified = true,
                    aadhaarNumberMasked = "XXXX-XXXX-5670",
                    walletBalance = 850.0,
                    avatarColorIndex = 1
                ),
                User(
                    id = "anshika@jap",
                    name = "Anshika Sharma",
                    phone = "9876543201",
                    countryCode = "+91",
                    password = "password123",
                    address = "Bandra West, Mumbai, Maharashtra",
                    aadhaarVerified = true,
                    aadhaarNumberMasked = "XXXX-XXXX-3201",
                    walletBalance = 1200.0,
                    avatarColorIndex = 2
                ),
                User(
                    id = "anay@jap",
                    name = "Anay Gupta",
                    phone = "9711223344",
                    countryCode = "+91",
                    password = "password123",
                    address = "Indiranagar, Bengaluru, Karnataka",
                    aadhaarVerified = true,
                    aadhaarNumberMasked = "XXXX-XXXX-3344",
                    walletBalance = 920.0,
                    avatarColorIndex = 3
                ),
                User(
                    id = "sarvesh@jap",
                    name = "Sarvesh Verma",
                    phone = "9988776655",
                    countryCode = "+91",
                    password = "password123",
                    address = "Gomti Nagar, Lucknow, Uttar Pradesh",
                    aadhaarVerified = true,
                    aadhaarNumberMasked = "XXXX-XXXX-6655",
                    walletBalance = 640.0,
                    avatarColorIndex = 4
                ),
                User(
                    id = "suresh@jap",
                    name = "Suresh Kumar",
                    phone = "9123456780",
                    countryCode = "+91",
                    password = "password123",
                    address = "Park Street, Kolkata, West Bengal",
                    aadhaarVerified = true,
                    aadhaarNumberMasked = "XXXX-XXXX-6780",
                    walletBalance = 430.0,
                    avatarColorIndex = 5
                )
            )

            for (user in users) {
                userDao.insertUser(user)
            }

            // 3. Initial Sample Transactions
            val initialTransactions = listOf(
                Transaction(
                    senderId = "varun@jap",
                    senderName = "Varun Rathore",
                    receiverId = "8791738300@jap",
                    receiverName = "Devansh",
                    amount = 120.0,
                    message = "Dinner split 🍕",
                    status = "SUCCESS",
                    type = "TRANSFER",
                    soundTriggered = "WOW",
                    timestamp = System.currentTimeMillis() - 3600000 * 2
                ),
                Transaction(
                    senderId = "8791738300@jap",
                    senderName = "Devansh",
                    receiverId = "anshika@jap",
                    receiverName = "Anshika Sharma",
                    amount = 60.0,
                    message = "Coffee on me ☕",
                    status = "SUCCESS",
                    type = "TRANSFER",
                    soundTriggered = "CRYING",
                    timestamp = System.currentTimeMillis() - 3600000 * 5
                ),
                Transaction(
                    senderId = "ADMIN",
                    senderName = "Jap Pay Cashback",
                    receiverId = "8791738300@jap",
                    receiverName = "Devansh",
                    amount = 20.0,
                    message = "Welcome Reward Bonus 🎉",
                    status = "SUCCESS",
                    type = "REWARD",
                    soundTriggered = "WOW",
                    timestamp = System.currentTimeMillis() - 3600000 * 24
                )
            )

            for (tx in initialTransactions) {
                transactionDao.insertTransaction(tx)
            }

            // 4. Initial Notifications
            val initialNotifications = listOf(
                AppNotification(
                    userId = "8791738300@jap",
                    title = "Money Received! 🎉",
                    message = "Varun Rathore sent you ₹120.00 with note: 'Dinner split 🍕'",
                    type = "CREDIT",
                    amount = 120.0,
                    timestamp = System.currentTimeMillis() - 3600000 * 2
                ),
                AppNotification(
                    userId = "ALL",
                    title = "Welcome to Jap Pay ⚡",
                    message = "Send money to any @jap ID or scan QR code instantly with 0% fees.",
                    type = "ADMIN_BROADCAST",
                    timestamp = System.currentTimeMillis() - 3600000 * 12
                )
            )

            for (notif in initialNotifications) {
                notificationDao.insertNotification(notif)
            }
        }
    }
}
