package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class FinanceRepository(private val db: AppDatabase) {

    val wallets: Flow<List<Wallet>> = db.walletDao().getAllWallets()
    val transactions: Flow<List<Transaction>> = db.transactionDao().getAllTransactions()
    val categories: Flow<List<Category>> = db.categoryDao().getAllCategories()
    val debts: Flow<List<Debt>> = db.debtDao().getAllDebts()
    val savingsGoals: Flow<List<SavingsGoal>> = db.savingsGoalDao().getAllSavingsGoals()
    val recurringTransactions: Flow<List<RecurringTransaction>> = db.recurringTransactionDao().getAllRecurringTransactions()
    val settings: Flow<CurrencySetting> = db.currencySettingDao().getSettingsFlow()
        .map { it ?: CurrencySetting() }

    suspend fun initializeDefaults() {
        // Initialize settings if empty
        val currentSettings = db.currencySettingDao().getSettings()
        if (currentSettings == null) {
            db.currencySettingDao().insertOrUpdateSettings(CurrencySetting())
        }

        // Initialize categories if empty
        val currentCats = db.categoryDao().getAllCategories().firstOrNull() ?: emptyList()
        if (currentCats.isEmpty()) {
            val defaultCategories = listOf(
                Category(name = "الطعام", iconName = "restaurant", colorValue = 0xFFE57373.toInt(), isCustom = false),
                Category(name = "المواصلات", iconName = "directions_transit", colorValue = 0xFF64B5F6.toInt(), isCustom = false),
                Category(name = "السكن", iconName = "home", colorValue = 0xFFFFB74D.toInt(), isCustom = false),
                Category(name = "الفواتير", iconName = "receipt_long", colorValue = 0xFFBA68C8.toInt(), isCustom = false),
                Category(name = "التسوق", iconName = "shopping_bag", colorValue = 0xFF4DB6AC.toInt(), isCustom = false),
                Category(name = "الصحة", iconName = "medical_services", colorValue = 0xFF81C784.toInt(), isCustom = false),
                Category(name = "التعليم", iconName = "school", colorValue = 0xFFAED581.toInt(), isCustom = false),
                Category(name = "العمل", iconName = "work", colorValue = 0xFF90A4AE.toInt(), isCustom = false),
                Category(name = "الرواتب", iconName = "payments", colorValue = 0xFF81C784.toInt(), isCustom = false),
                Category(name = "أخرى", iconName = "more_horiz", colorValue = 0xFFE0E0E0.toInt(), isCustom = false)
            )
            for (cat in defaultCategories) {
                db.categoryDao().insertCategory(cat)
            }
        }

        // Initialize wallets if empty
        val currentWallets = db.walletDao().getAllWallets().firstOrNull() ?: emptyList()
        if (currentWallets.isEmpty()) {
            db.walletDao().insertWallet(Wallet(name = "محفظة الدولار", color = 0xFF2196F3.toInt(), iconName = "wallet", balance = 1000.0, currency = "USD"))
            db.walletDao().insertWallet(Wallet(name = "محفظة التركي", color = 0xFF4CAF50.toInt(), iconName = "wallet", balance = 5000.0, currency = "TRY"))
            db.walletDao().insertWallet(Wallet(name = "محفظة السوري", color = 0xFFFF9800.toInt(), iconName = "wallet", balance = 100000.0, currency = "SYP"))
        }
    }

    // Wallets
    suspend fun getWalletById(id: Int): Wallet? = db.walletDao().getWalletById(id)
    suspend fun insertWallet(wallet: Wallet) = db.walletDao().insertWallet(wallet)
    suspend fun updateWallet(wallet: Wallet) = db.walletDao().updateWallet(wallet)
    suspend fun deleteWallet(wallet: Wallet) = db.walletDao().deleteWallet(wallet)

    // Transactions with balance modification
    suspend fun insertTransaction(transaction: Transaction) {
        db.transactionDao().insertTransaction(transaction)
        
        // Adjust wallet balances
        when (transaction.type) {
            "INCOME" -> {
                val wallet = db.walletDao().getWalletById(transaction.walletId)
                if (wallet != null) {
                    db.walletDao().updateWallet(wallet.copy(balance = wallet.balance + transaction.amount))
                }
            }
            "EXPENSE" -> {
                val wallet = db.walletDao().getWalletById(transaction.walletId)
                if (wallet != null) {
                    db.walletDao().updateWallet(wallet.copy(balance = wallet.balance - transaction.amount))
                }
            }
            "TRANSFER" -> {
                val fromWallet = db.walletDao().getWalletById(transaction.walletId)
                val toWallet = transaction.toWalletId?.let { db.walletDao().getWalletById(it) }
                if (fromWallet != null && toWallet != null) {
                    db.walletDao().updateWallet(fromWallet.copy(balance = fromWallet.balance - transaction.amount))
                    db.walletDao().updateWallet(toWallet.copy(balance = toWallet.balance + (transaction.amount * transaction.exchangeRate)))
                }
            }
        }
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        db.transactionDao().deleteTransaction(transaction)
        
        // Revert wallet balances
        when (transaction.type) {
            "INCOME" -> {
                val wallet = db.walletDao().getWalletById(transaction.walletId)
                if (wallet != null) {
                    db.walletDao().updateWallet(wallet.copy(balance = wallet.balance - transaction.amount))
                }
            }
            "EXPENSE" -> {
                val wallet = db.walletDao().getWalletById(transaction.walletId)
                if (wallet != null) {
                    db.walletDao().updateWallet(wallet.copy(balance = wallet.balance + transaction.amount))
                }
            }
            "TRANSFER" -> {
                val fromWallet = db.walletDao().getWalletById(transaction.walletId)
                val toWallet = transaction.toWalletId?.let { db.walletDao().getWalletById(it) }
                if (fromWallet != null && toWallet != null) {
                    db.walletDao().updateWallet(fromWallet.copy(balance = fromWallet.balance + transaction.amount))
                    db.walletDao().updateWallet(toWallet.copy(balance = toWallet.balance - (transaction.amount * transaction.exchangeRate)))
                }
            }
        }
    }

    // Categories
    suspend fun insertCategory(category: Category) = db.categoryDao().insertCategory(category)
    suspend fun deleteCategory(category: Category) = db.categoryDao().deleteCategory(category)

    // Debts
    suspend fun insertDebt(debt: Debt) = db.debtDao().insertDebt(debt)
    suspend fun updateDebt(debt: Debt) = db.debtDao().updateDebt(debt)
    suspend fun deleteDebt(debt: Debt) = db.debtDao().deleteDebt(debt)

    // Savings Goals
    suspend fun insertSavingsGoal(savingsGoal: SavingsGoal) = db.savingsGoalDao().insertSavingsGoal(savingsGoal)
    suspend fun updateSavingsGoal(savingsGoal: SavingsGoal) = db.savingsGoalDao().updateSavingsGoal(savingsGoal)
    suspend fun deleteSavingsGoal(savingsGoal: SavingsGoal) = db.savingsGoalDao().deleteSavingsGoal(savingsGoal)

    // Recurring
    suspend fun insertRecurring(recurring: RecurringTransaction) = db.recurringTransactionDao().insertRecurring(recurring)
    suspend fun deleteRecurring(recurring: RecurringTransaction) = db.recurringTransactionDao().deleteRecurring(recurring)

    // Settings
    suspend fun updateSettings(settings: CurrencySetting) = db.currencySettingDao().insertOrUpdateSettings(settings)
}
