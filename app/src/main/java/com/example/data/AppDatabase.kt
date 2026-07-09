package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        Wallet::class,
        Transaction::class,
        Category::class,
        Debt::class,
        SavingsGoal::class,
        RecurringTransaction::class,
        CurrencySetting::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun walletDao(): WalletDao
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun debtDao(): DebtDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    abstract fun recurringTransactionDao(): RecurringTransactionDao
    abstract fun currencySettingDao(): CurrencySettingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mohasabaty_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
