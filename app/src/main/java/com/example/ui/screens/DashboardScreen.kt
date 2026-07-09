package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Transaction
import com.example.data.Wallet
import com.example.ui.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: FinanceViewModel,
    onNavigateToWallets: () -> Unit,
    onNavigateToTransactions: () -> Unit,
    onAddTransactionClick: () -> Unit
) {
    val wallets by viewModel.wallets.collectAsState()
    val transactions by viewModel.transactions.collectAsState()

    val usdTotal = wallets.filter { it.currency == "USD" }.sumOf { it.balance }
    val tryTotal = wallets.filter { it.currency == "TRY" }.sumOf { it.balance }
    val sypTotal = wallets.filter { it.currency == "SYP" }.sumOf { it.balance }

    val walletsMap = wallets.associateBy { it.id }

    val usdIncome = transactions.filter { it.type == "INCOME" && (walletsMap[it.walletId]?.currency ?: "USD") == "USD" }.sumOf { it.amount }
    val tryIncome = transactions.filter { it.type == "INCOME" && (walletsMap[it.walletId]?.currency ?: "USD") == "TRY" }.sumOf { it.amount }
    val sypIncome = transactions.filter { it.type == "INCOME" && (walletsMap[it.walletId]?.currency ?: "USD") == "SYP" }.sumOf { it.amount }

    val usdExpense = transactions.filter { it.type == "EXPENSE" && (walletsMap[it.walletId]?.currency ?: "USD") == "USD" }.sumOf { it.amount }
    val tryExpense = transactions.filter { it.type == "EXPENSE" && (walletsMap[it.walletId]?.currency ?: "USD") == "TRY" }.sumOf { it.amount }
    val sypExpense = transactions.filter { it.type == "EXPENSE" && (walletsMap[it.walletId]?.currency ?: "USD") == "SYP" }.sumOf { it.amount }

    val recentTransactions = transactions.take(5)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Top Space
        item { Spacer(modifier = Modifier.height(8.dp)) }

        // Header Greeting
        item {
            Column {
                Text(
                    text = "أهلاً بك في محاسبتي 👋",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "إليك ملخصك المالي لليوم",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Total Balance Hero Card (Dynamic multi-currency summary)
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.tertiary
                                )
                             )
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "إجمالي أرصدة المحافظ",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("دولار", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
                                Text(viewModel.formatAmount(usdTotal, "USD"), color = Color.White, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("تركي", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
                                Text(viewModel.formatAmount(tryTotal, "TRY"), color = Color.White, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("سوري", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
                                Text(viewModel.formatAmount(sypTotal, "SYP"), color = Color.White, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }
            }
        }

        // Quick Stats: Income vs Expense Cards Row (Multi-currency lists)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Income Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.weight(1.0f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE8F5E9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = "دخل",
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "الإيرادات",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (usdIncome > 0 || (tryIncome == 0.0 && sypIncome == 0.0)) {
                                Text(
                                    text = viewModel.formatAmount(usdIncome, "USD"),
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF2E7D32)
                                )
                            }
                            if (tryIncome > 0) {
                                Text(
                                    text = viewModel.formatAmount(tryIncome, "TRY"),
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF2E7D32)
                                )
                            }
                            if (sypIncome > 0) {
                                Text(
                                    text = viewModel.formatAmount(sypIncome, "SYP"),
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }
                    }
                }

                // Expense Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.weight(1.0f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFEBEE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingDown,
                                contentDescription = "مصروف",
                                tint = Color(0xFFC62828),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "المصروفات",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (usdExpense > 0 || (tryExpense == 0.0 && sypExpense == 0.0)) {
                                Text(
                                    text = viewModel.formatAmount(usdExpense, "USD"),
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFC62828)
                                )
                            }
                            if (tryExpense > 0) {
                                Text(
                                    text = viewModel.formatAmount(tryExpense, "TRY"),
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFC62828)
                                )
                            }
                            if (sypExpense > 0) {
                                Text(
                                    text = viewModel.formatAmount(sypExpense, "SYP"),
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFC62828)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Low Balance Warnings (dynamic currency limits)
        val lowBalanceWallets = wallets.filter { wallet ->
            val limit = when (wallet.currency) {
                "USD" -> 10.0
                "TRY" -> 100.0
                "SYP" -> 5000.0
                else -> 10.0
            }
            wallet.balance < limit
        }
        if (lowBalanceWallets.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    lowBalanceWallets.forEach { wallet ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Warning",
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = "تنبيه: رصيد محفظة '${wallet.name}' منخفض للغاية (${viewModel.formatAmount(wallet.balance, wallet.currency)})",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }
            }
        }

        // Wallets Sub-header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onNavigateToWallets) {
                    Text("عرض الكل", style = MaterialTheme.typography.labelLarge)
                }
                Text(
                    text = "محافظي المخصصة",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        // Horizontal Wallets Row
        item {
            if (wallets.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("لا يوجد محافظ حالياً", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(wallets) { wallet ->
                        WalletDashboardItem(wallet = wallet, viewModel = viewModel)
                    }
                }
            }
        }

        // Transactions Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onNavigateToTransactions) {
                    Text("عرض الكل", style = MaterialTheme.typography.labelLarge)
                }
                Text(
                    text = "آخر العمليات المالية",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        // Recent Transactions List
        if (recentTransactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Receipt,
                                contentDescription = "لا يوجد عمليات",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("لم يتم تسجيل أي عملية مالية بعد", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        } else {
            items(recentTransactions) { tx ->
                TransactionItemRow(tx = tx, viewModel = viewModel)
            }
        }
    }
}

@Composable
fun WalletDashboardItem(wallet: Wallet, viewModel: FinanceViewModel) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(wallet.color)),
        modifier = Modifier
            .width(160.dp)
            .height(110.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val icon = when (wallet.iconName) {
                    "wallet" -> Icons.Default.Wallet
                    "home" -> Icons.Default.Home
                    "car" -> Icons.Default.DirectionsCar
                    "savings" -> Icons.Default.Savings
                    "work" -> Icons.Default.Work
                    else -> Icons.Default.Wallet
                }
                Icon(
                    imageVector = icon,
                    contentDescription = wallet.name,
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(24.dp)
                )
            }
            Column {
                Text(
                    text = wallet.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    text = viewModel.formatAmount(wallet.balance, wallet.currency),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
fun TransactionItemRow(tx: Transaction, viewModel: FinanceViewModel) {
    val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
    val dateStr = sdf.format(Date(tx.date))
    val wallets by viewModel.wallets.collectAsState()

    val fromWallet = wallets.find { it.id == tx.walletId }
    val toWallet = tx.toWalletId?.let { rxId -> wallets.find { it.id == rxId } }
    val fromCurrency = fromWallet?.currency ?: "USD"
    val toCurrency = toWallet?.currency ?: "USD"

    val color = when (tx.type) {
        "INCOME" -> Color(0xFF2E7D32)
        "EXPENSE" -> Color(0xFFC62828)
        "TRANSFER" -> Color(0xFF1565C0)
        else -> MaterialTheme.colorScheme.onSurface
    }

    val icon = when (tx.category) {
        "الطعام" -> Icons.Default.Restaurant
        "المواصلات" -> Icons.Default.DirectionsTransit
        "السكن" -> Icons.Default.Home
        "الفواتير" -> Icons.Default.ReceiptLong
        "التسوق" -> Icons.Default.ShoppingBag
        "الصحة" -> Icons.Default.MedicalServices
        "التعليم" -> Icons.Default.School
        "العمل" -> Icons.Default.Work
        "الرواتب" -> Icons.Default.Payments
        else -> Icons.Default.Receipt
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Price & Type
            Column(horizontalAlignment = Alignment.Start) {
                val amountText = when (tx.type) {
                    "TRANSFER" -> {
                        "${viewModel.formatAmount(tx.amount, fromCurrency)} ← ${viewModel.formatAmount(tx.amount * tx.exchangeRate, toCurrency)}"
                    }
                    "EXPENSE" -> "-${viewModel.formatAmount(tx.amount, fromCurrency)}"
                    else -> "+${viewModel.formatAmount(tx.amount, fromCurrency)}"
                }
                Text(
                    text = amountText,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = color
                )
                Text(
                    text = when (tx.type) {
                        "INCOME" -> "إيداع"
                        "EXPENSE" -> "مصروف"
                        "TRANSFER" -> "تحويل"
                        else -> tx.type
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            // Right: Icon & Details
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = tx.description,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = tx.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "• $dateStr",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(color.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = tx.category,
                        tint = color
                    )
                }
            }
        }
    }
}
