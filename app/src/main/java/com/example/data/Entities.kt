package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wallets")
data class Wallet(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val color: Int, // Hex integer or ARGB integer
    val iconName: String,
    val balance: Double,
    val currency: String = "USD"
)

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "INCOME", "EXPENSE", "TRANSFER"
    val amount: Double,
    val walletId: Int,
    val toWalletId: Int? = null, // Only used when type is "TRANSFER"
    val date: Long = System.currentTimeMillis(),
    val description: String,
    val category: String,
    val imagePath: String? = null,
    val exchangeRate: Double = 1.0
)

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val iconName: String,
    val colorValue: Int,
    val isCustom: Boolean = false
)

@Entity(tableName = "debts")
data class Debt(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val personName: String,
    val amount: Double,
    val currency: String, // "USD", "TRY", "SYP"
    val dueDate: Long,
    val notes: String,
    val isPaid: Boolean = false,
    val type: String, // "TO_ME" (ديون لي), "BY_ME" (ديون علي)
    val paidAmount: Double = 0.0
)

@Entity(tableName = "savings_goals")
data class SavingsGoal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val walletId: Int? = null
)

@Entity(tableName = "recurring_transactions")
data class RecurringTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "INCOME", "EXPENSE"
    val amount: Double,
    val walletId: Int,
    val category: String,
    val description: String,
    val frequency: String, // "DAILY", "WEEKLY", "MONTHLY"
    val nextRunDate: Long
)

@Entity(tableName = "currency_settings")
data class CurrencySetting(
    @PrimaryKey val id: Int = 1,
    val baseCurrency: String = "USD",
    val usdToTry: Double = 33.0,
    val usdToSyp: Double = 15000.0,
    val isAppLocked: Boolean = false,
    val appPin: String = "",
    val isBiometricEnabled: Boolean = false,
    val language: String = "AR" // "AR" or "EN"
)
