package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Category
import com.example.data.Transaction
import com.example.data.Wallet
import com.example.ui.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(viewModel: FinanceViewModel) {
    val transactions by viewModel.filteredTransactions.collectAsState()
    val wallets by viewModel.wallets.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filterWalletId by viewModel.filterWalletId.collectAsState()
    val filterCategory by viewModel.filterCategory.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf("ALL") } // ALL, INCOME, EXPENSE, TRANSFER

    val displayedTransactions = remember(transactions, selectedTab) {
        if (selectedTab == "ALL") transactions
        else transactions.filter { it.type == selectedTab }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "إضافة عملية")
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Screen Title
            Text(
                text = "العمليات المالية",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Right
            )

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("بحث عن عملية...") },
                trailingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "بحث") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                textStyle = TextStyle(textAlign = TextAlign.Right)
            )

            // Dynamic Filter chips row
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Wallet filter chip
                item {
                    val activeWallet = wallets.find { it.id == filterWalletId }
                    FilterChip(
                        selected = filterWalletId != null,
                        onClick = {
                            if (filterWalletId != null) viewModel.setFilterWalletId(null)
                            else if (wallets.isNotEmpty()) viewModel.setFilterWalletId(wallets.first().id)
                        },
                        label = { Text(activeWallet?.name ?: "كل المحافظ") }
                    )
                }

                // Category filter chip
                item {
                    FilterChip(
                        selected = filterCategory != null,
                        onClick = {
                            if (filterCategory != null) viewModel.setFilterCategory(null)
                            else if (categories.isNotEmpty()) viewModel.setFilterCategory(categories.first().name)
                        },
                        label = { Text(filterCategory ?: "كل التصنيفات") }
                    )
                }
            }

            // Quick Filters for active wallet drop down if clicked
            if (filterWalletId != null) {
                Text(
                    text = "تصفية حسب المحفظة المحددة. اضغط للتبديل:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(wallets) { wallet ->
                        InputChip(
                            selected = wallet.id == filterWalletId,
                            onClick = { viewModel.setFilterWalletId(wallet.id) },
                            label = { Text(wallet.name) }
                        )
                    }
                }
            }

            // Tabs for Income vs Expense
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(4.dp)
            ) {
                val tabs = listOf(
                    "TRANSFER" to "تحويل",
                    "EXPENSE" to "مصروفات",
                    "INCOME" to "إيرادات",
                    "ALL" to "الكل"
                )
                tabs.forEach { (key, label) ->
                    val isSelected = selectedTab == key
                    Box(
                        modifier = Modifier
                            .weight(1.0f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else Color.Transparent
                            )
                            .clickable { selectedTab = key }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Transactions list
            if (displayedTransactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.0f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = "Empty",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("لا توجد عمليات تطابق البحث حالياً", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.0f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(displayedTransactions) { tx ->
                        val dismissState = remember { mutableStateOf(false) }
                        // Swipe to delete simulation or simple click delete!
                        Box(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TransactionRowItemWithActions(
                                tx = tx,
                                viewModel = viewModel,
                                onDelete = { viewModel.deleteTransaction(tx) }
                            )
                        }
                    }
                }
            }
        }

        // Add Transaction Dialog
        if (showAddDialog) {
            AddTransactionDialog(
                viewModel = viewModel,
                onDismiss = { showAddDialog = false }
            )
        }
    }
}

@Composable
fun TransactionRowItemWithActions(
    tx: Transaction,
    viewModel: FinanceViewModel,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
    val dateStr = sdf.format(Date(tx.date))

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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDeleteConfirm = true }
    ) {
        val wallets by viewModel.wallets.collectAsState()
        val fromWallet = wallets.find { it.id == tx.walletId }
        val toWallet = tx.toWalletId?.let { rxId -> wallets.find { it.id == rxId } }
        val fromCurrency = fromWallet?.currency ?: "USD"
        val toCurrency = toWallet?.currency ?: "USD"

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Value & Action trigger
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

            // Right: Content Details
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = tx.description,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
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
                    if (tx.imagePath != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Attachment, contentDescription = "Attachment", modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("مرفق الفاتورة", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
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

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("حذف", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("إلغاء")
                }
            },
            title = { Text("حذف العملية المالية", textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth()) },
            text = { Text("هل أنت متأكد من رغبتك في حذف هذه العملية؟ سيتم تعديل أرصدة المحافظ تلقائياً.", textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth()) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    viewModel: FinanceViewModel,
    onDismiss: () -> Unit
) {
    val wallets by viewModel.wallets.collectAsState()
    val categories by viewModel.categories.collectAsState()

    var type by remember { mutableStateOf("EXPENSE") } // INCOME, EXPENSE, TRANSFER
    var amountStr by remember { mutableStateOf("") }
    var selectedWalletId by remember { mutableStateOf(wallets.firstOrNull()?.id ?: 0) }
    var selectedToWalletId by remember { mutableStateOf(wallets.getOrNull(1)?.id ?: wallets.firstOrNull()?.id ?: 0) }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("الطعام") }
    var hasAttachment by remember { mutableStateOf(false) }
    var exchangeRateStr by remember { mutableStateOf("1.0") }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    var selectedDate by remember { mutableStateOf(calendar.timeInMillis) }
    val dateString = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date(selectedDate))

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val cal = Calendar.getInstance()
            cal.set(year, month, dayOfMonth)
            selectedDate = cal.timeInMillis
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val activeWallet = wallets.find { it.id == selectedWalletId }
    val activeToWallet = wallets.find { it.id == selectedToWalletId }

    LaunchedEffect(selectedWalletId, selectedToWalletId) {
        val fW = wallets.find { it.id == selectedWalletId }
        val tW = wallets.find { it.id == selectedToWalletId }
        if (fW != null && tW != null) {
            exchangeRateStr = when {
                fW.currency == "USD" && tW.currency == "TRY" -> "33.0"
                fW.currency == "USD" && tW.currency == "SYP" -> "15000.0"
                fW.currency == "TRY" && tW.currency == "USD" -> "0.03"
                fW.currency == "TRY" && tW.currency == "SYP" -> "454.0"
                fW.currency == "SYP" && tW.currency == "USD" -> "0.000067"
                fW.currency == "SYP" && tW.currency == "TRY" -> "0.0022"
                else -> "1.0"
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountStr.toDoubleOrNull() ?: 0.0
                    val rate = exchangeRateStr.toDoubleOrNull() ?: 1.0
                    if (amount > 0 && selectedWalletId > 0) {
                        viewModel.addTransaction(
                            type = type,
                            amount = amount,
                            walletId = selectedWalletId,
                            toWalletId = if (type == "TRANSFER") selectedToWalletId else null,
                            description = description.ifEmpty { "عملية جديدة" },
                            category = if (type == "TRANSFER") "تحويل" else selectedCategory,
                            imagePath = if (hasAttachment) "mock_bill_uri" else null,
                            exchangeRate = if (type == "TRANSFER") rate else 1.0
                        )
                        onDismiss()
                    }
                }
            ) {
                Text("حفظ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        },
        title = { Text("إضافة عملية جديدة", textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth()) },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Operation Type Toggle Segment
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(2.dp)
                    ) {
                        val types = listOf(
                            "TRANSFER" to "تحويل",
                            "INCOME" to "دخل",
                            "EXPENSE" to "مصروف"
                        )
                        types.forEach { (key, label) ->
                            val isSelected = type == key
                            Box(
                                modifier = Modifier
                                    .weight(1.0f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (isSelected) {
                                            when (key) {
                                                "EXPENSE" -> Color(0xFFC62828)
                                                "INCOME" -> Color(0xFF2E7D32)
                                                else -> Color(0xFF1565C0)
                                            }
                                        } else Color.Transparent
                                    )
                                    .clickable { type = key }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Amount Field (labelled with wallet currency)
                item {
                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it },
                        label = { Text("المبلغ (${activeWallet?.currency ?: "USD"})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(textAlign = TextAlign.Right)
                    )
                }

                // Description Field
                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("الوصف / الملاحظات") },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(textAlign = TextAlign.Right)
                    )
                }

                // Source Wallet Selection
                item {
                    Text("من محفظة", style = MaterialTheme.typography.labelMedium, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Right)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(wallets) { wallet ->
                            InputChip(
                                selected = wallet.id == selectedWalletId,
                                onClick = { selectedWalletId = wallet.id },
                                label = { Text("${wallet.name} (${wallet.currency})") }
                            )
                        }
                    }
                }

                // Destination Wallet (only if TRANSFER)
                if (type == "TRANSFER") {
                    item {
                        Text("إلى محفظة", style = MaterialTheme.typography.labelMedium, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Right)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(wallets.filter { it.id != selectedWalletId }) { wallet ->
                                InputChip(
                                    selected = wallet.id == selectedToWalletId,
                                    onClick = { selectedToWalletId = wallet.id },
                                    label = { Text("${wallet.name} (${wallet.currency})") }
                                )
                            }
                        }
                    }

                    if (activeWallet != null && activeToWallet != null && activeWallet.currency != activeToWallet.currency) {
                        item {
                            OutlinedTextField(
                                value = exchangeRateStr,
                                onValueChange = { exchangeRateStr = it },
                                label = { Text("سعر الصرف (1 ${activeWallet.currency} = ? ${activeToWallet.currency})") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = TextStyle(textAlign = TextAlign.Right)
                            )
                        }

                        val amount = amountStr.toDoubleOrNull() ?: 0.0
                        val rate = exchangeRateStr.toDoubleOrNull() ?: 1.0
                        if (amount > 0) {
                            item {
                                Text(
                                    text = "المبلغ المستلم التقريبي: ${String.format(Locale.US, "%,.2f", amount * rate)} ${activeToWallet.currency}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Right
                                )
                            }
                        }
                    }
                }

                // Category Selection (only if NOT TRANSFER)
                if (type != "TRANSFER") {
                    item {
                        Text("التصنيف", style = MaterialTheme.typography.labelMedium, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Right)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(categories) { cat ->
                                InputChip(
                                    selected = cat.name == selectedCategory,
                                    onClick = { selectedCategory = cat.name },
                                    label = { Text(cat.name) }
                                )
                            }
                        }
                    }
                }

                // Date Picker Button
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { datePickerDialog.show() }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = dateString, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                        Text(text = "تاريخ العملية", style = MaterialTheme.typography.labelMedium)
                    }
                }

                // Attachment (Mock Image capture/attachment)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(checked = hasAttachment, onCheckedChange = { hasAttachment = it })
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("إرفاق صورة الفاتورة (مرفق)", style = MaterialTheme.typography.labelMedium)
                            Icon(imageVector = Icons.Default.CameraAlt, contentDescription = "Camera")
                        }
                    }
                }
            }
        }
    )
}
