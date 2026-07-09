package com.example.ui.screens

import android.widget.Toast
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
import com.example.data.RecurringTransaction
import com.example.data.SavingsGoal
import com.example.ui.FinanceViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: FinanceViewModel) {
    val settings by viewModel.settings.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val savingsGoals by viewModel.savingsGoals.collectAsState()
    val recurringTxs by viewModel.recurringTransactions.collectAsState()
    val wallets by viewModel.wallets.collectAsState()

    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showGoalDialog by remember { mutableStateOf(false) }
    var showRecurringDialog by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Title
        item {
            Text(
                text = "الإعدادات والتحكم",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Right
            )
        }

        // 1. Theme Configuration
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "مظهر التطبيق",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Right
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = isDarkMode ?: false,
                            onCheckedChange = { viewModel.setDarkMode(it) }
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("تفعيل الوضع الداكن (Dark Mode)")
                            Icon(imageVector = Icons.Default.DarkMode, contentDescription = "Dark Mode")
                        }
                    }
                }
            }
        }



        // 3. Savings Goals
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { showGoalDialog = true }) {
                            Icon(imageVector = Icons.Default.AddCircle, contentDescription = "Add Goal", tint = MaterialTheme.colorScheme.primary)
                        }
                        Text(
                            text = "أهداف الادخار وتتبع الإنجاز",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    if (savingsGoals.isEmpty()) {
                        Text(
                            text = "لا توجد أهداف ادخارية مضافة حالياً.",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Right
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            savingsGoals.forEach { goal ->
                                var contribAmountStr by remember { mutableStateOf("") }
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        IconButton(onClick = { viewModel.deleteSavingsGoal(goal) }) {
                                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Goal", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                        }
                                        Text(text = goal.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                    }

                                    val progress = if (goal.targetAmount > 0) (goal.currentAmount / goal.targetAmount).toFloat() else 0f
                                    val percent = (progress * 100).toInt()

                                    LinearProgressIndicator(
                                        progress = { progress.coerceIn(0f, 1f) },
                                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
                                    )

                                    val goalWallet = wallets.find { it.id == goal.walletId }
                                    val goalCurrency = goalWallet?.currency ?: "USD"
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("${percent}%", style = MaterialTheme.typography.labelSmall)
                                        Text(
                                            text = "${viewModel.formatAmount(goal.currentAmount, goalCurrency)} من ${viewModel.formatAmount(goal.targetAmount, goalCurrency)}",
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }

                                    // Contribute inputs
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Button(
                                            onClick = {
                                                val amt = contribAmountStr.toDoubleOrNull() ?: 0.0
                                                if (amt > 0) {
                                                    viewModel.contributeToSavingsGoal(goal, amt)
                                                    contribAmountStr = ""
                                                }
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Text("ادخار", style = MaterialTheme.typography.labelSmall)
                                        }
                                        OutlinedTextField(
                                            value = contribAmountStr,
                                            onValueChange = { contribAmountStr = it },
                                            placeholder = { Text("مبلغ المساهمة") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.weight(1.0f).height(48.dp),
                                            textStyle = TextStyle(textAlign = TextAlign.Right, fontSize = 12.sp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Recurring Auto Transactions
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { showRecurringDialog = true }) {
                            Icon(imageVector = Icons.Default.AddCircle, contentDescription = "Add Recurring", tint = MaterialTheme.colorScheme.primary)
                        }
                        Text(
                            text = "العمليات المالية المتكررة تلقائياً",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    if (recurringTxs.isEmpty()) {
                        Text(
                            text = "لا توجد أي جدولة للعمليات المتكررة.",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Right
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            recurringTxs.forEach { rec ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(onClick = { viewModel.deleteRecurring(rec) }) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(rec.description, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                        Text(
                                            text = "${viewModel.formatAmount(rec.amount)} • تكرار: ${when(rec.frequency){"DAILY"->"يومي" "WEEKLY"->"أسبوعي" else -> "شهري"}}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. App Lock PIN configuration
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "الأمان وقفل التطبيق",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Right
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(onClick = { showPinDialog = true }) {
                            Text(if (settings.isAppLocked) "تغيير أو إيقاف الرمز" else "إعداد الرمز السري PIN")
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "رمز القفل ببصمة الإصبع أو PIN",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = if (settings.isAppLocked) "القفل مفعّل حالياً 🔒" else "الحماية غير مفعلة 🔓",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (settings.isAppLocked) Color(0xFF2E7D32) else Color.Gray
                            )
                        }
                    }
                }
            }
        }

        // 6. Backup & Restore Data
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "النسخ الاحتياطي وتصدير التقارير",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Right
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Backup JSON
                        Button(
                            onClick = {
                                val uri = viewModel.backupData(context)
                                if (uri != null) {
                                    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                        type = "application/json"
                                        putExtra(android.content.Intent.EXTRA_STREAM, uri)
                                        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(android.content.Intent.createChooser(intent, "حفظ النسخة الاحتياطية"))
                                } else {
                                    Toast.makeText(context, "فشل إنشاء النسخة الاحتياطية", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("نسخة احتياطية JSON", fontSize = 11.sp)
                        }

                        // Export Excel/CSV
                        Button(
                            onClick = {
                                val uri = viewModel.exportReport(context, "MONTH")
                                if (uri != null) {
                                    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                        type = "text/csv"
                                        putExtra(android.content.Intent.EXTRA_STREAM, uri)
                                        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(android.content.Intent.createChooser(intent, "تصدير التقرير كـ Excel CSV"))
                                } else {
                                    Toast.makeText(context, "فشل تصدير التقرير", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("تصدير Excel", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }



    // PIN configuration Dialog
    if (showPinDialog) {
        var pinVal by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setSecurityPin(pinVal)
                        showPinDialog = false
                        Toast.makeText(context, "تم تحديث إعدادات القفل بنجاح", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("تأكيد")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) {
                    Text("إلغاء")
                }
            },
            title = { Text("إعداد القفل السري PIN", textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth()) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("أدخل رمز PIN الرقمي المكون من 4 أرقام لتأمين التطبيق، أو اتركه فارغاً لإلغاء القفل.", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Right)
                    OutlinedTextField(
                        value = pinVal,
                        onValueChange = { if (it.length <= 4) pinVal = it },
                        label = { Text("رمز PIN الجديد") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(textAlign = TextAlign.Right)
                    )
                }
            }
        )
    }

    // Savings goal dialog
    if (showGoalDialog) {
        var goalName by remember { mutableStateOf("") }
        var targetStr by remember { mutableStateOf("") }
        var selectedWalletId by remember { mutableStateOf(wallets.firstOrNull()?.id) }

        AlertDialog(
            onDismissRequest = { showGoalDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        val tar = targetStr.toDoubleOrNull() ?: 0.0
                        if (goalName.isNotEmpty() && tar > 0) {
                            viewModel.addSavingsGoal(goalName, tar, selectedWalletId)
                            showGoalDialog = false
                        }
                    }
                ) {
                    Text("إضافة الهدف")
                }
            },
            dismissButton = {
                TextButton(onClick = { showGoalDialog = false }) {
                    Text("إلغاء")
                }
            },
            title = { Text("إضافة هدف ادخار جديد", textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth()) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = goalName,
                        onValueChange = { goalName = it },
                        label = { Text("اسم هدف الادخار (مثال: محفظة المنزل الجديد)") },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(textAlign = TextAlign.Right)
                    )

                    val selectedWallet = wallets.find { it.id == selectedWalletId }
                    val goalCurrency = selectedWallet?.currency ?: "USD"
                    OutlinedTextField(
                        value = targetStr,
                        onValueChange = { targetStr = it },
                        label = { Text("المبلغ المستهدف ($goalCurrency)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(textAlign = TextAlign.Right)
                    )

                    Text("ربط مع محفظة الدفع المخصصة:", style = MaterialTheme.typography.labelSmall)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        wallets.forEach { w ->
                            FilterChip(
                                selected = selectedWalletId == w.id,
                                onClick = { selectedWalletId = w.id },
                                label = { Text(w.name) }
                            )
                        }
                    }
                }
            }
        )
    }

    // Recurring Transaction Dialog
    if (showRecurringDialog) {
        var recDesc by remember { mutableStateOf("") }
        var recAmountStr by remember { mutableStateOf("") }
        var recType by remember { mutableStateOf("EXPENSE") }
        var recFreq by remember { mutableStateOf("MONTHLY") }
        var selectedWalletId by remember { mutableStateOf(wallets.firstOrNull()?.id ?: 0) }

        AlertDialog(
            onDismissRequest = { showRecurringDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = recAmountStr.toDoubleOrNull() ?: 0.0
                        if (recDesc.isNotEmpty() && amt > 0 && selectedWalletId > 0) {
                            viewModel.addRecurringTransaction(
                                type = recType,
                                amount = amt,
                                walletId = selectedWalletId,
                                category = "أخرى",
                                description = recDesc,
                                frequency = recFreq
                            )
                            showRecurringDialog = false
                        }
                    }
                ) {
                    Text("جدولة")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRecurringDialog = false }) {
                    Text("إلغاء")
                }
            },
            title = { Text("جدولة عملية متكررة جديدة", textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth()) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(selected = recType == "INCOME", onClick = { recType = "INCOME" }, label = { Text("دخل متكرر") })
                        FilterChip(selected = recType == "EXPENSE", onClick = { recType = "EXPENSE" }, label = { Text("مصروف متكرر") })
                    }

                    OutlinedTextField(
                        value = recDesc,
                        onValueChange = { recDesc = it },
                        label = { Text("الوصف الجدولة (مثال: اشتراك نت شهري)") },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(textAlign = TextAlign.Right)
                    )

                    val selectedWallet = wallets.find { it.id == selectedWalletId }
                    val recCurrency = selectedWallet?.currency ?: "USD"
                    OutlinedTextField(
                        value = recAmountStr,
                        onValueChange = { recAmountStr = it },
                        label = { Text("المبلغ ($recCurrency)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(textAlign = TextAlign.Right)
                    )

                    Text("المحفظة المرتبطة:", style = MaterialTheme.typography.labelSmall)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        wallets.forEach { w ->
                            FilterChip(
                                selected = selectedWalletId == w.id,
                                onClick = { selectedWalletId = w.id },
                                label = { Text("${w.name} (${w.currency})") }
                            )
                        }
                    }

                    Text("تكرار الجدولة:", style = MaterialTheme.typography.labelSmall)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("DAILY" to "يومي", "WEEKLY" to "أسبوعي", "MONTHLY" to "شهري").forEach { (f, label) ->
                            FilterChip(
                                selected = recFreq == f,
                                onClick = { recFreq = f },
                                label = { Text(label) }
                            )
                        }
                    }
                }
            }
        )
    }
}
