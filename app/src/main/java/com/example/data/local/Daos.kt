package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AdminConfig
import com.example.data.model.AppNotification
import com.example.data.model.DepositRequest
import com.example.data.model.Transaction
import com.example.data.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getUserByIdFlow(id: String): Flow<User?>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): User?

    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    suspend fun getUserByPhone(phone: String): User?

    @Query("SELECT * FROM users WHERE phone = :phone OR phone = :phoneWithoutCode OR id = :id LIMIT 1")
    suspend fun findUserForLogin(phone: String, phoneWithoutCode: String, id: String): User?

    @Query("SELECT * FROM users WHERE id LIKE '%' || :query || '%' OR name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%'")
    suspend fun searchUsers(query: String): List<User>

    @Query("SELECT * FROM users WHERE id != :currentUserId ORDER BY name ASC")
    fun getAllOtherUsers(currentUserId: String): Flow<List<User>>

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsers(): Flow<List<User>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Query("SELECT * FROM users WHERE customId = :customId LIMIT 1")
    suspend fun getUserByCustomId(customId: String): User?

    @Query("SELECT * FROM users WHERE id = :targetId OR phone = :targetId OR customId = :targetId LIMIT 1")
    suspend fun findUserByIdOrPhoneOrCustomId(targetId: String): User?

    @Query("UPDATE users SET customId = :customId, isCustomIdActive = 1, customIdExpiresAt = :expiresAt WHERE id = :userId")
    suspend fun updateCustomId(userId: String, customId: String, expiresAt: Long)

    @Query("UPDATE users SET walletBalance = walletBalance + :amount WHERE id = :userId")
    suspend fun addWalletBalance(userId: String, amount: Double)

    @Query("UPDATE users SET walletBalance = walletBalance - :amount WHERE id = :userId")
    suspend fun deductWalletBalance(userId: String, amount: Double)

    @Query("UPDATE users SET keeperBalance = keeperBalance + :amount, walletBalance = walletBalance - :amount WHERE id = :userId")
    suspend fun transferToKeeper(userId: String, amount: Double)

    @Query("UPDATE users SET keeperBalance = keeperBalance - :amount, walletBalance = walletBalance + :amount WHERE id = :userId")
    suspend fun withdrawFromKeeper(userId: String, amount: Double)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE senderId = :userId OR receiverId = :userId ORDER BY timestamp DESC")
    fun getTransactionsForUser(userId: String): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction): Long

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: Long): Transaction?
}

@Dao
interface DepositRequestDao {
    @Query("SELECT * FROM deposit_requests WHERE userId = :userId ORDER BY timestamp DESC")
    fun getDepositRequestsForUser(userId: String): Flow<List<DepositRequest>>

    @Query("SELECT * FROM deposit_requests ORDER BY timestamp DESC")
    fun getAllDepositRequests(): Flow<List<DepositRequest>>

    @Query("SELECT * FROM deposit_requests WHERE status = 'PENDING' ORDER BY timestamp DESC")
    fun getPendingDepositRequests(): Flow<List<DepositRequest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDepositRequest(request: DepositRequest): Long

    @Update
    suspend fun updateDepositRequest(request: DepositRequest)

    @Query("UPDATE deposit_requests SET status = :status, adminNote = :note WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String, note: String)

    @Query("SELECT * FROM deposit_requests WHERE id = :id LIMIT 1")
    suspend fun getDepositRequestById(id: Long): DepositRequest?
}

@Dao
interface AdminConfigDao {
    @Query("SELECT * FROM admin_config WHERE id = 1 LIMIT 1")
    fun getAdminConfigFlow(): Flow<AdminConfig?>

    @Query("SELECT * FROM admin_config WHERE id = 1 LIMIT 1")
    suspend fun getAdminConfig(): AdminConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAdminConfig(config: AdminConfig)
}

@Dao
interface AppNotificationDao {
    @Query("SELECT * FROM app_notifications WHERE userId = :userId OR userId = 'ALL' ORDER BY timestamp DESC")
    fun getNotificationsForUser(userId: String): Flow<List<AppNotification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: AppNotification): Long

    @Query("UPDATE app_notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("UPDATE app_notifications SET isRead = 1 WHERE userId = :userId OR userId = 'ALL'")
    suspend fun markAllAsRead(userId: String)

    @Query("SELECT COUNT(*) FROM app_notifications WHERE (userId = :userId OR userId = 'ALL') AND isRead = 0")
    fun getUnreadCount(userId: String): Flow<Int>
}
