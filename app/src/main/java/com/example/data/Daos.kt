package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {
    @Query("SELECT * FROM wallets")
    fun getAllWallets(): Flow<List<Wallet>>

    @Query("SELECT * FROM wallets WHERE id = :id")
    suspend fun getWalletById(id: Int): Wallet?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallet(wallet: Wallet): Long

    @Update
    suspend fun updateWallet(wallet: Wallet)

    @Delete
    suspend fun deleteWallet(wallet: Wallet)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE walletId = :walletId OR toWalletId = :walletId ORDER BY date DESC")
    fun getTransactionsByWallet(walletId: Int): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction): Long

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<Category>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category): Long

    @Delete
    suspend fun deleteCategory(category: Category)
}

@Dao
interface DebtDao {
    @Query("SELECT * FROM debts ORDER BY dueDate ASC")
    fun getAllDebts(): Flow<List<Debt>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebt(debt: Debt): Long

    @Update
    suspend fun updateDebt(debt: Debt)

    @Delete
    suspend fun deleteDebt(debt: Debt)
}

@Dao
interface SavingsGoalDao {
    @Query("SELECT * FROM savings_goals")
    fun getAllSavingsGoals(): Flow<List<SavingsGoal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavingsGoal(savingsGoal: SavingsGoal): Long

    @Update
    suspend fun updateSavingsGoal(savingsGoal: SavingsGoal)

    @Delete
    suspend fun deleteSavingsGoal(savingsGoal: SavingsGoal)
}

@Dao
interface RecurringTransactionDao {
    @Query("SELECT * FROM recurring_transactions")
    fun getAllRecurringTransactions(): Flow<List<RecurringTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurring(recurring: RecurringTransaction): Long

    @Delete
    suspend fun deleteRecurring(recurring: RecurringTransaction)
}

@Dao
interface CurrencySettingDao {
    @Query("SELECT * FROM currency_settings WHERE id = 1")
    fun getSettingsFlow(): Flow<CurrencySetting?>

    @Query("SELECT * FROM currency_settings WHERE id = 1")
    suspend fun getSettings(): CurrencySetting?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSettings(settings: CurrencySetting)
}
