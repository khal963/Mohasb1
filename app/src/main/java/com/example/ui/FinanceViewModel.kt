package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

class FinanceViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = FinanceRepository(db)

    // Data streams
    val wallets = repository.wallets.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val transactions = repository.transactions.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val categories = repository.categories.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val debts = repository.debts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val savingsGoals = repository.savingsGoals.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val recurringTransactions = repository.recurringTransactions.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val settings = repository.settings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CurrencySetting())

    // Language state & Dark/Light mode overrides
    private val _isDarkMode = MutableStateFlow<Boolean?>(null) // null means system default
    val isDarkMode = _isDarkMode.asStateFlow()

    fun setDarkMode(dark: Boolean?) {
        _isDarkMode.value = dark
    }

    // Active screen currency for displaying amounts
    private val _displayCurrency = MutableStateFlow("USD")
    val displayCurrency = _displayCurrency.asStateFlow()

    fun setDisplayCurrency(currency: String) {
        _displayCurrency.value = currency
    }

    // Undo action cache
    private var lastDeletedTransaction: Transaction? = null
    private val _showUndoSnackbar = MutableStateFlow(false)
    val showUndoSnackbar = _showUndoSnackbar.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initializeDefaults()
            // Sync setting with display currency
            settings.collectLatest {
                _displayCurrency.value = it.baseCurrency
            }
        }
    }

    // Currency Formatting (Using the specified currency directly)
    fun formatAmount(amount: Double, currency: String): String {
        val symbol = when (currency) {
            "USD" -> "$"
            "TRY" -> "₺"
            "SYP" -> "ل.س"
            else -> currency
        }
        return String.format(Locale.US, "%,.2f %s", amount, symbol)
    }

    // Overload for general display currency (e.g., active preference)
    fun formatAmount(amount: Double): String {
        return formatAmount(amount, _displayCurrency.value)
    }

    // Search and Filters State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _filterWalletId = MutableStateFlow<Int?>(null)
    val filterWalletId = _filterWalletId.asStateFlow()

    private val _filterCategory = MutableStateFlow<String?>(null)
    val filterCategory = _filterCategory.asStateFlow()

    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun setFilterWalletId(id: Int?) { _filterWalletId.value = id }
    fun setFilterCategory(cat: String?) { _filterCategory.value = cat }

    val filteredTransactions = combine(
        transactions,
        _searchQuery,
        _filterWalletId,
        _filterCategory
    ) { txs, query, walletId, cat ->
        txs.filter { tx ->
            val matchesQuery = query.isEmpty() || 
                    tx.description.contains(query, ignoreCase = true) || 
                    tx.category.contains(query, ignoreCase = true)
            val matchesWallet = walletId == null || tx.walletId == walletId || tx.toWalletId == walletId
            val matchesCat = cat == null || tx.category == cat
            matchesQuery && matchesWallet && matchesCat
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Wallets Actions
    fun addWallet(name: String, color: Int, iconName: String, balance: Double, currency: String) {
        viewModelScope.launch {
            repository.insertWallet(Wallet(name = name, color = color, iconName = iconName, balance = balance, currency = currency))
        }
    }

    fun updateWallet(wallet: Wallet) {
        viewModelScope.launch {
            repository.updateWallet(wallet)
        }
    }

    fun deleteWallet(wallet: Wallet) {
        viewModelScope.launch {
            repository.deleteWallet(wallet)
        }
    }

    // Transactions Actions
    fun addTransaction(
        type: String,
        amount: Double,
        walletId: Int,
        toWalletId: Int?,
        description: String,
        category: String,
        imagePath: String? = null,
        exchangeRate: Double = 1.0
    ) {
        viewModelScope.launch {
            val tx = Transaction(
                type = type,
                amount = amount,
                walletId = walletId,
                toWalletId = toWalletId,
                description = description,
                category = category,
                imagePath = imagePath,
                exchangeRate = exchangeRate
            )
            repository.insertTransaction(tx)
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            lastDeletedTransaction = transaction
            _showUndoSnackbar.value = true
            repository.deleteTransaction(transaction)
        }
    }

    fun undoDeleteTransaction() {
        val tx = lastDeletedTransaction
        if (tx != null) {
            viewModelScope.launch {
                repository.insertTransaction(tx)
                lastDeletedTransaction = null
                _showUndoSnackbar.value = false
            }
        }
    }

    fun dismissUndo() {
        _showUndoSnackbar.value = false
    }

    // Categories Actions
    fun addCategory(name: String, iconName: String, colorValue: Int) {
        viewModelScope.launch {
            repository.insertCategory(Category(name = name, iconName = iconName, colorValue = colorValue, isCustom = true))
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }

    // Debts Actions
    fun addDebt(personName: String, amount: Double, currency: String, dueDate: Long, notes: String, type: String) {
        viewModelScope.launch {
            repository.insertDebt(
                Debt(
                    personName = personName,
                    amount = amount,
                    currency = currency,
                    dueDate = dueDate,
                    notes = notes,
                    type = type
                )
            )
        }
    }

    fun payDebtPartially(debt: Debt, payAmount: Double) {
        viewModelScope.launch {
            val updatedPaid = debt.paidAmount + payAmount
            val isFullyPaid = updatedPaid >= debt.amount
            val updatedDebt = debt.copy(
                paidAmount = if (isFullyPaid) debt.amount else updatedPaid,
                isPaid = isFullyPaid
            )
            repository.updateDebt(updatedDebt)
            
            val wallet = wallets.value.find { it.currency == debt.currency } ?: wallets.value.firstOrNull()
            if (wallet != null) {
                val desc = "سداد دين: ${debt.personName}"
                val txType = if (debt.type == "TO_ME") "INCOME" else "EXPENSE"
                repository.insertTransaction(
                    Transaction(
                        type = txType,
                        amount = payAmount,
                        walletId = wallet.id,
                        description = desc,
                        category = "أخرى"
                    )
                )
            }
        }
    }

    fun deleteDebt(debt: Debt) {
        viewModelScope.launch {
            repository.deleteDebt(debt)
        }
    }

    // Savings Goals
    fun addSavingsGoal(name: String, targetAmount: Double, walletId: Int?) {
        viewModelScope.launch {
            repository.insertSavingsGoal(SavingsGoal(name = name, targetAmount = targetAmount, walletId = walletId))
        }
    }

    fun contributeToSavingsGoal(goal: SavingsGoal, amount: Double) {
        viewModelScope.launch {
            val updatedCurrent = goal.currentAmount + amount
            val updatedGoal = goal.copy(currentAmount = updatedCurrent)
            repository.updateSavingsGoal(updatedGoal)

            // Subtract from active wallet if matched
            if (goal.walletId != null) {
                val wallet = repository.getWalletById(goal.walletId)
                if (wallet != null) {
                    repository.updateWallet(wallet.copy(balance = wallet.balance - amount))
                    // Log as a special savings transaction
                    repository.insertTransaction(
                        Transaction(
                            type = "EXPENSE",
                            amount = amount,
                            walletId = goal.walletId,
                            description = "مساهمة في هدف الادخار: ${goal.name}",
                            category = "الادخار"
                        )
                    )
                }
            }
        }
    }

    fun deleteSavingsGoal(goal: SavingsGoal) {
        viewModelScope.launch {
            repository.deleteSavingsGoal(goal)
        }
    }

    // Recurring Transactions
    fun addRecurringTransaction(type: String, amount: Double, walletId: Int, category: String, description: String, frequency: String) {
        viewModelScope.launch {
            val nextRun = System.currentTimeMillis() + when (frequency) {
                "DAILY" -> 24 * 60 * 60 * 1000L
                "WEEKLY" -> 7 * 24 * 60 * 60 * 1000L
                "MONTHLY" -> 30 * 24 * 60 * 60 * 1000L
                else -> 30 * 24 * 60 * 60 * 1000L
            }
            repository.insertRecurring(
                RecurringTransaction(
                    type = type,
                    amount = amount,
                    walletId = walletId,
                    category = category,
                    description = description,
                    frequency = frequency,
                    nextRunDate = nextRun
                )
            )
        }
    }

    fun deleteRecurring(recurring: RecurringTransaction) {
        viewModelScope.launch {
            repository.deleteRecurring(recurring)
        }
    }

    // Settings actions
    fun updateCurrencySettings(baseCur: String, tryRate: Double, sypRate: Double) {
        viewModelScope.launch {
            val current = settings.value
            repository.updateSettings(
                current.copy(
                    baseCurrency = baseCur,
                    usdToTry = tryRate,
                    usdToSyp = sypRate
                )
            )
            _displayCurrency.value = baseCur
        }
    }

    fun setSecurityPin(pin: String) {
        viewModelScope.launch {
            val current = settings.value
            repository.updateSettings(
                current.copy(
                    isAppLocked = pin.isNotEmpty(),
                    appPin = pin
                )
            )
        }
    }

    // Export Reports to CSV
    fun exportReport(context: Context, periodType: String): Uri? {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val csvHeader = "ID,Type,Amount,Currency,Wallet,Description,Category,Date\n"
        val csvBuilder = StringBuilder(csvHeader)

        val txList = transactions.value
        val walletMap = wallets.value.associateBy { it.id }

        for (tx in txList) {
            val wallet = walletMap[tx.walletId]
            val walletName = wallet?.name ?: "Unknown"
            val currency = wallet?.currency ?: "USD"
            val dateStr = sdf.format(Date(tx.date))
            csvBuilder.append("${tx.id},${tx.type},${tx.amount},\"$currency\",\"$walletName\",\"${tx.description}\",\"${tx.category}\",$dateStr\n")
        }

        return try {
            val cachePath = File(context.cacheDir, "reports")
            cachePath.mkdirs()
            val file = File(cachePath, "mohasabaty_report_${periodType}_${System.currentTimeMillis()}.csv")
            val writer = FileWriter(file)
            writer.write(csvBuilder.toString())
            writer.flush()
            writer.close()

            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // JSON Local Backup
    fun backupData(context: Context): Uri? {
        val rootObj = JSONObject()
        try {
            // Wallets
            val walletArray = JSONArray()
            wallets.value.forEach {
                val obj = JSONObject()
                obj.put("name", it.name)
                obj.put("color", it.color)
                obj.put("iconName", it.iconName)
                obj.put("balance", it.balance)
                obj.put("currency", it.currency)
                walletArray.put(obj)
            }
            rootObj.put("wallets", walletArray)

            // Transactions
            val txArray = JSONArray()
            transactions.value.forEach {
                val obj = JSONObject()
                obj.put("type", it.type)
                obj.put("amount", it.amount)
                obj.put("walletId", it.walletId)
                obj.put("toWalletId", it.toWalletId ?: -1)
                obj.put("description", it.description)
                obj.put("category", it.category)
                obj.put("date", it.date)
                obj.put("exchangeRate", it.exchangeRate)
                txArray.put(obj)
            }
            rootObj.put("transactions", txArray)

            // Debts
            val debtArray = JSONArray()
            debts.value.forEach {
                val obj = JSONObject()
                obj.put("personName", it.personName)
                obj.put("amount", it.amount)
                obj.put("currency", it.currency)
                obj.put("dueDate", it.dueDate)
                obj.put("notes", it.notes)
                obj.put("isPaid", it.isPaid)
                obj.put("type", it.type)
                obj.put("paidAmount", it.paidAmount)
                debtArray.put(obj)
            }
            rootObj.put("debts", debtArray)

            val cachePath = File(context.cacheDir, "backups")
            cachePath.mkdirs()
            val file = File(cachePath, "mohasabaty_backup.json")
            val writer = FileWriter(file)
            writer.write(rootObj.toString(4))
            writer.flush()
            writer.close()

            return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    // JSON Restore
    fun restoreData(jsonString: String): Boolean {
        return try {
            val rootObj = JSONObject(jsonString)
            viewModelScope.launch {
                // Clear and Restore Wallets
                val walletArray = rootObj.optJSONArray("wallets")
                if (walletArray != null) {
                    for (i in 0 until walletArray.length()) {
                        val obj = walletArray.getJSONObject(i)
                        repository.insertWallet(
                            Wallet(
                                name = obj.getString("name"),
                                color = obj.getInt("color"),
                                iconName = obj.getString("iconName"),
                                balance = obj.getDouble("balance"),
                                currency = obj.optString("currency", "USD")
                            )
                        )
                    }
                }

                // Clear and Restore Transactions
                val txArray = rootObj.optJSONArray("transactions")
                if (txArray != null) {
                    for (i in 0 until txArray.length()) {
                        val obj = txArray.getJSONObject(i)
                        val toId = obj.optInt("toWalletId", -1)
                        repository.insertTransaction(
                            Transaction(
                                type = obj.getString("type"),
                                amount = obj.getDouble("amount"),
                                walletId = obj.getInt("walletId"),
                                toWalletId = if (toId == -1) null else toId,
                                description = obj.getString("description"),
                                category = obj.getString("category"),
                                date = obj.getLong("date"),
                                exchangeRate = obj.optDouble("exchangeRate", 1.0)
                            )
                        )
                    }
                }

                // Restore Debts
                val debtArray = rootObj.optJSONArray("debts")
                if (debtArray != null) {
                    for (i in 0 until debtArray.length()) {
                        val obj = debtArray.getJSONObject(i)
                        repository.insertDebt(
                            Debt(
                                personName = obj.getString("personName"),
                                amount = obj.getDouble("amount"),
                                currency = obj.getString("currency"),
                                dueDate = obj.getLong("dueDate"),
                                notes = obj.getString("notes"),
                                isPaid = obj.getBoolean("isPaid"),
                                type = obj.getString("type"),
                                paidAmount = obj.getDouble("paidAmount")
                            )
                        )
                    }
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
