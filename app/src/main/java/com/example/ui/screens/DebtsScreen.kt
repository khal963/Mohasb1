package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.Debt
import com.example.ui.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtsScreen(viewModel: FinanceViewModel) {
    val debts by viewModel.debts.collectAsState()
    val rate = viewModel.settings.collectAsState().value
    val displayCurrency by viewModel.displayCurrency.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf("TO_ME") } // TO_ME (ديون لي), BY_ME (ديون علي)
    var debtToPay by remember { mutableStateOf<Debt?>(null) }

    // Filtered lists
    val filteredDebts = remember(debts, selectedTab) {
        debts.filter { it.type == selectedTab }
    }

    // Convert all debts in active tab to base (USD) or display currency for totals
    val totalDebtAmountUSD = remember(filteredDebts, rate) {
        filteredDebts.sumOf { debt ->
            val unpaid = debt.amount - debt.paidAmount
            when (debt.currency) {
                "USD" -> unpaid
                "TRY" -> if (rate.usdToTry > 0) unpaid / rate.usdToTry else unpaid
                "SYP" -> if (rate.usdToSyp > 0) unpaid / rate.usdToSyp else unpaid
                else -> unpaid
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "إضافة دين")
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Text(
                text = "إدارة الديون",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Right
            )

            // Tabs for TO_ME vs BY_ME
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(4.dp)
            ) {
                listOf(
                    "BY_ME" to "ديون علي (المطالبات)",
                    "TO_ME" to "ديون لي (المستحقات)"
                ).forEach { (key, label) ->
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
                            .padding(vertical = 10.dp),
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

            // Summary Info Box
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = viewModel.formatAmount(totalDebtAmountUSD),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (selectedTab == "TO_ME") "إجمالي مستحقاتي لدى الآخرين" else "إجمالي المطالبات المستحقة عليّ",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // List of Debts
            if (filteredDebts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.0f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Handshake,
                            contentDescription = "Empty debts",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (selectedTab == "TO_ME") "لا توجد ديون مستحقة لك حالياً" else "لا توجد ديون مستحقة عليك حالياً",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.0f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredDebts) { debt ->
                        DebtItemRow(
                            debt = debt,
                            viewModel = viewModel,
                            onPayClick = { debtToPay = debt },
                            onDeleteClick = { viewModel.deleteDebt(debt) }
                        )
                    }
                }
            }
        }

        // Add Debt Dialog
        if (showAddDialog) {
            AddDebtDialog(
                viewModel = viewModel,
                selectedTab = selectedTab,
                onDismiss = { showAddDialog = false }
            )
        }

        // Pay Debt Dialog
        if (debtToPay != null) {
            PayDebtDialog(
                debt = debtToPay!!,
                onDismiss = { debtToPay = null },
                onConfirm = { amount ->
                    viewModel.payDebtPartially(debtToPay!!, amount)
                    debtToPay = null
                }
            )
        }
    }
}

@Composable
fun DebtItemRow(
    debt: Debt,
    viewModel: FinanceViewModel,
    onPayClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
    val dueDateStr = sdf.format(Date(debt.dueDate))

    val isOverdue = System.currentTimeMillis() > debt.dueDate && !debt.isPaid
    val dueColor = if (isOverdue) Color(0xFFC62828) else MaterialTheme.colorScheme.primary

    val progress = if (debt.amount > 0) (debt.paidAmount / debt.amount).toFloat() else 0.0f

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (debt.isPaid) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Actions (Pay, Delete)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                        )
                    }

                    if (!debt.isPaid) {
                        Button(
                            onClick = onPayClick,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("تسديد", style = MaterialTheme.typography.labelMedium)
                        }
                    } else {
                        AssistChip(
                            onClick = {},
                            label = { Text("تم السدادكامل") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Paid",
                                    modifier = Modifier.size(16.dp),
                                    tint = Color(0xFF2E7D32)
                                )
                            }
                        )
                    }
                }

                // Name and Amount Details
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = debt.personName,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${debt.amount} ${debt.currency}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (debt.isPaid) Color.Gray else MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Progress bar
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (debt.isPaid) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "المدفوع: ${debt.paidAmount} ${debt.currency}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "المتبقي: ${debt.amount - debt.paidAmount} ${debt.currency}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isOverdue) Color(0xFFC62828) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Due Date & Notes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (debt.notes.isNotEmpty()) {
                    Text(
                        text = debt.notes,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "تاريخ الاستحقاق: $dueDateStr",
                        style = MaterialTheme.typography.labelSmall,
                        color = dueColor,
                        fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Normal
                    )
                    Icon(
                        imageVector = if (isOverdue) Icons.Default.NotificationImportant else Icons.Default.CalendarToday,
                        contentDescription = "Due Date Icon",
                        tint = dueColor,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddDebtDialog(
    viewModel: FinanceViewModel,
    selectedTab: String,
    onDismiss: () -> Unit
) {
    var personName by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var selectedCurrency by remember { mutableStateOf("USD") }
    var notes by remember { mutableStateOf("") }

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

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountStr.toDoubleOrNull() ?: 0.0
                    if (personName.isNotEmpty() && amount > 0) {
                        viewModel.addDebt(
                            personName = personName,
                            amount = amount,
                            currency = selectedCurrency,
                            dueDate = selectedDate,
                            notes = notes,
                            type = selectedTab
                        )
                        onDismiss()
                    }
                }
            ) {
                Text("إضافة")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        },
        title = {
            Text(
                text = if (selectedTab == "TO_ME") "إضافة مستحق (دين لي)" else "إضافة مطالبة (دين علي)",
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = personName,
                    onValueChange = { personName = it },
                    label = { Text("اسم الشخص") },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(textAlign = TextAlign.Right)
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("المبلغ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(textAlign = TextAlign.Right)
                )

                // Currency selector
                Text("العملة", style = MaterialTheme.typography.labelMedium, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Right)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    listOf("USD", "TRY", "SYP").forEach { curr ->
                        FilterChip(
                            selected = selectedCurrency == curr,
                            onClick = { selectedCurrency = curr },
                            label = { Text(curr) }
                        )
                    }
                }

                // Date Picker trigger
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { datePickerDialog.show() }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = dateString, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                    Text(text = "تاريخ الاستحقاق", style = MaterialTheme.typography.labelMedium)
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات إضافية") },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(textAlign = TextAlign.Right)
                )
            }
        }
    )
}

@Composable
fun PayDebtDialog(
    debt: Debt,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double) -> Unit
) {
    var payAmountStr by remember { mutableStateOf("") }
    val maxPay = debt.amount - debt.paidAmount

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val amt = payAmountStr.toDoubleOrNull() ?: 0.0
                    if (amt > 0) {
                        onConfirm(amt)
                    }
                }
            ) {
                Text("تسديد")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        },
        title = { Text("سداد جزء أو كامل الدين", textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth()) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "الحد الأقصى للسداد: $maxPay ${debt.currency}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right
                )
                OutlinedTextField(
                    value = payAmountStr,
                    onValueChange = { payAmountStr = it },
                    label = { Text("مبلغ السداد الحالي") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(textAlign = TextAlign.Right)
                )
            }
        }
    )
}
