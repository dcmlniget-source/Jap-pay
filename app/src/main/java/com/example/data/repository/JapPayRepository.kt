package com.example.data.repository

import com.example.data.local.JapPayDatabase
import com.example.data.model.AdminConfig
import com.example.data.model.AppNotification
import com.example.data.model.DepositRequest
import com.example.data.model.Transaction
import com.example.data.model.User
import kotlinx.coroutines.flow.Flow

class JapPayRepository(private val db: JapPayDatabase) {
    val userDao = db.userDao()
    val transactionDao = db.transactionDao()
    val depositRequestDao = db.depositRequestDao()
    val adminConfigDao = db.adminConfigDao()
    val notificationDao = db.appNotificationDao()

    fun getUserFlow(userId: String): Flow<User?> = userDao.getUserByIdFlow(userId)
    fun getAllOtherUsers(currentUserId: String): Flow<List<User>> = userDao.getAllOtherUsers(currentUserId)
    fun getAllUsers(): Flow<List<User>> = userDao.getAllUsers()
    fun getTransactionsForUser(userId: String): Flow<List<Transaction>> = transactionDao.getTransactionsForUser(userId)
    fun getAllTransactions(): Flow<List<Transaction>> = transactionDao.getAllTransactions()
    fun getDepositRequestsForUser(userId: String): Flow<List<DepositRequest>> = depositRequestDao.getDepositRequestsForUser(userId)
    fun getAllDepositRequests(): Flow<List<DepositRequest>> = depositRequestDao.getAllDepositRequests()
    fun getAdminConfigFlow(): Flow<AdminConfig?> = adminConfigDao.getAdminConfigFlow()
    fun getNotificationsForUser(userId: String): Flow<List<AppNotification>> = notificationDao.getNotificationsForUser(userId)
    fun getUnreadNotificationCount(userId: String): Flow<Int> = notificationDao.getUnreadCount(userId)

    suspend fun getUserById(userId: String) = userDao.getUserById(userId)
    suspend fun getUserByPhone(phone: String) = userDao.getUserByPhone(phone)
    suspend fun searchUsers(query: String) = userDao.searchUsers(query)
    suspend fun insertUser(user: User) = userDao.insertUser(user)
    suspend fun updateUser(user: User) = userDao.updateUser(user)

    suspend fun performTransfer(
        sender: User,
        receiverId: String,
        amount: Double,
        message: String
    ): Result<Transaction> {
        if (sender.walletBalance < amount) {
            return Result.failure(Exception("Insufficient wallet balance. You have ₹${"%.2f".format(sender.walletBalance)}"))
        }

        val receiver = userDao.getUserById(receiverId) 
            ?: userDao.getUserByPhone(receiverId.replace("@jap", ""))
            ?: return Result.failure(Exception("User $receiverId not found in Jap Pay"))

        // Deduct from sender, add to receiver
        userDao.deductWalletBalance(sender.id, amount)
        userDao.addWalletBalance(receiver.id, amount)

        val sound = if (amount > 50.0) "CRYING" else "WOW"

        val tx = Transaction(
            senderId = sender.id,
            senderName = sender.name,
            receiverId = receiver.id,
            receiverName = receiver.name,
            amount = amount,
            message = message,
            status = "SUCCESS",
            type = "TRANSFER",
            soundTriggered = sound,
            timestamp = System.currentTimeMillis()
        )
        val txId = transactionDao.insertTransaction(tx)

        // Add notifications
        notificationDao.insertNotification(
            AppNotification(
                userId = sender.id,
                title = "Payment Sent 💸",
                message = "₹${"%.2f".format(amount)} transferred to ${receiver.name} (${receiver.id})",
                type = "DEBIT",
                amount = amount
            )
        )
        notificationDao.insertNotification(
            AppNotification(
                userId = receiver.id,
                title = "Money Received! 🎉",
                message = "You received ₹${"%.2f".format(amount)} from ${sender.name}${if (message.isNotEmpty()) " with note: '$message'" else ""}",
                type = "CREDIT",
                amount = amount
            )
        )

        return Result.success(tx.copy(id = txId))
    }

    suspend fun saveToKeeper(userId: String, amount: Double) {
        userDao.transferToKeeper(userId, amount)
        transactionDao.insertTransaction(
            Transaction(
                senderId = userId,
                senderName = "My Wallet",
                receiverId = userId,
                receiverName = "My Keeper Jar",
                amount = amount,
                message = "Saved in Keeper Jar (Earn 3% interest)",
                type = "KEEPER_SAVE",
                soundTriggered = "WOW"
            )
        )
    }

    suspend fun withdrawFromKeeper(userId: String, amount: Double) {
        userDao.withdrawFromKeeper(userId, amount)
        transactionDao.insertTransaction(
            Transaction(
                senderId = userId,
                senderName = "My Keeper Jar",
                receiverId = userId,
                receiverName = "My Wallet",
                amount = amount,
                message = "Withdrawn from Keeper Jar",
                type = "KEEPER_WITHDRAW",
                soundTriggered = "WOW"
            )
        )
    }

    suspend fun submitDepositRequest(request: DepositRequest): Long {
        return depositRequestDao.insertDepositRequest(request)
    }

    suspend fun approveDeposit(requestId: Long, adminNote: String = "Verified by Admin"): Boolean {
        val req = depositRequestDao.getDepositRequestById(requestId) ?: return false
        if (req.status != "PENDING") return false

        depositRequestDao.updateStatus(requestId, "APPROVED", adminNote)
        userDao.addWalletBalance(req.userId, req.amount)

        transactionDao.insertTransaction(
            Transaction(
                senderId = "ADMIN_DEPOSIT",
                senderName = "Jap Pay Add Money",
                receiverId = req.userId,
                receiverName = req.userName,
                amount = req.amount,
                message = "Wallet deposit approved (UTR: ${req.utrNumber})",
                type = "DEPOSIT",
                soundTriggered = "WOW"
            )
        )

        notificationDao.insertNotification(
            AppNotification(
                userId = req.userId,
                title = "Deposit Approved! 💰",
                message = "Your deposit of ₹${"%.2f".format(req.amount)} (UTR: ${req.utrNumber}) has been approved and added to your wallet.",
                type = "DEPOSIT_APPROVED",
                amount = req.amount
            )
        )

        return true
    }

    suspend fun rejectDeposit(requestId: Long, reason: String = "UTR or screenshot mismatch"): Boolean {
        val req = depositRequestDao.getDepositRequestById(requestId) ?: return false
        if (req.status != "PENDING") return false

        depositRequestDao.updateStatus(requestId, "REJECTED", reason)

        notificationDao.insertNotification(
            AppNotification(
                userId = req.userId,
                title = "Deposit Rejected ⚠️",
                message = "Your deposit request of ₹${"%.2f".format(req.amount)} (UTR: ${req.utrNumber}) was rejected: $reason",
                type = "DEPOSIT_REJECTED",
                amount = req.amount
            )
        )

        return true
    }

    suspend fun broadcastNotification(title: String, message: String, targetUserId: String = "ALL") {
        notificationDao.insertNotification(
            AppNotification(
                userId = targetUserId,
                title = title,
                message = message,
                type = "ADMIN_BROADCAST"
            )
        )
    }

    suspend fun saveAdminConfig(config: AdminConfig) = adminConfigDao.saveAdminConfig(config)
}
