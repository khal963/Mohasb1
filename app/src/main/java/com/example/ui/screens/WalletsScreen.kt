package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Wallet
import com.example.ui.FinanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletsScreen(viewModel: FinanceViewModel) {
    val wallets by viewModel.wallets.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var walletToEdit by remember { mutableStateOf<Wallet?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "إضافة محفظة")
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (wallets.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = "Empty wallets",
                        modifier = Modifier.size(72.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "لا توجد محافظ مخصصة حتى الآن",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "اضغط على الزر بالأسفل لإضافة أول محفظة لك",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = "المحافظ المتاحة",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        textAlign = TextAlign.Right
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(wallets) { wallet ->
                            WalletCardItem(
                                wallet = wallet,
                                viewModel = viewModel,
                                onEditClick = { walletToEdit = wallet },
                                onDeleteClick = { viewModel.deleteWallet(wallet) }
                            )
                        }
                    }
                }
            }
        }

        // Add Wallet Dialog
        if (showAddDialog) {
            WalletDialog(
                title = "إنشاء محفظة جديدة",
                onDismiss = { showAddDialog = false },
                onConfirm = { name, color, icon, initialBalance, currency ->
                    viewModel.addWallet(name, color, icon, initialBalance, currency)
                    showAddDialog = false
                }
            )
        }

        // Edit Wallet Dialog
        if (walletToEdit != null) {
            val wallet = walletToEdit!!
            WalletDialog(
                title = "تعديل المحفظة",
                initialName = wallet.name,
                initialColor = wallet.color,
                initialIcon = wallet.iconName,
                initialBalance = wallet.balance,
                initialCurrency = wallet.currency,
                isEdit = true,
                onDismiss = { walletToEdit = null },
                onConfirm = { name, color, icon, balance, currency ->
                    viewModel.updateWallet(wallet.copy(name = name, color = color, iconName = icon, balance = balance, currency = currency))
                    walletToEdit = null
                }
            )
        }
    }
}

@Composable
fun WalletCardItem(
    wallet: Wallet,
    viewModel: FinanceViewModel,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val walletIcon = when (wallet.iconName) {
        "wallet" -> Icons.Default.Wallet
        "home" -> Icons.Default.Home
        "car" -> Icons.Default.DirectionsCar
        "savings" -> Icons.Default.Savings
        "work" -> Icons.Default.Work
        else -> Icons.Default.Wallet
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(wallet.color)),
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
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
                IconButton(onClick = onEditClick, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = walletIcon,
                        contentDescription = wallet.name,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = wallet.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    textAlign = TextAlign.Right
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = viewModel.formatAmount(wallet.balance, wallet.currency),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                    color = Color.White,
                    textAlign = TextAlign.Right
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletDialog(
    title: String,
    initialName: String = "",
    initialColor: Int = 0xFF2196F3.toInt(),
    initialIcon: String = "wallet",
    initialBalance: Double = 0.0,
    initialCurrency: String = "USD",
    isEdit: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (name: String, color: Int, iconName: String, balance: Double, currency: String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var selectedColor by remember { mutableStateOf(initialColor) }
    var selectedIcon by remember { mutableStateOf(initialIcon) }
    var balanceStr by remember { mutableStateOf(if (initialBalance == 0.0) "" else initialBalance.toString()) }
    var selectedCurrency by remember { mutableStateOf(initialCurrency) }

    val colorsList = listOf(
        0xFF2196F3.toInt(), // Blue
        0xFF4CAF50.toInt(), // Green
        0xFFFF9800.toInt(), // Orange
        0xFFE91E63.toInt(), // Pink/Red
        0xFF9C27B0.toInt(), // Purple
        0xFF009688.toInt(), // Teal
        0xFF607D8B.toInt()  // Blue Grey
    )

    val iconsMap = listOf("wallet", "home", "car", "savings", "work")

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotEmpty()) {
                        val bal = balanceStr.toDoubleOrNull() ?: 0.0
                        onConfirm(name, selectedColor, selectedIcon, bal, selectedCurrency)
                    }
                }
            ) {
                Text("تأكيد")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.End
            ) {
                // Name Field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم المحفظة") },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(textAlign = TextAlign.Right)
                )

                // Initial Balance Field (only if not editing)
                if (!isEdit) {
                    OutlinedTextField(
                        value = balanceStr,
                        onValueChange = { balanceStr = it },
                        label = { Text("الرصيد الأولي") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(textAlign = TextAlign.Right)
                    )
                }

                // Currency Selector
                Text(
                    text = "عملة المحفظة",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    listOf("USD" to "دولار ($)", "TRY" to "تركي (₺)", "SYP" to "سوري (ل.س)").forEach { (curCode, curLabel) ->
                        FilterChip(
                            selected = selectedCurrency == curCode,
                            onClick = { selectedCurrency = curCode },
                            label = { Text(curLabel) }
                        )
                    }
                }

                // Color Picker
                Text(
                    text = "اختر لوناً مميزاً",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    colorsList.forEach { col ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(col))
                                .clickable { selectedColor = col }
                                .padding(2.dp)
                        ) {
                            if (selectedColor == col) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.5f))
                                )
                            }
                        }
                    }
                }

                // Icon Picker
                Text(
                    text = "اختر أيقونة المحفظة",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    iconsMap.forEach { iconName ->
                        val icon = when (iconName) {
                            "wallet" -> Icons.Default.Wallet
                            "home" -> Icons.Default.Home
                            "car" -> Icons.Default.DirectionsCar
                            "savings" -> Icons.Default.Savings
                            "work" -> Icons.Default.Work
                            else -> Icons.Default.Wallet
                        }
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (selectedIcon == iconName) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { selectedIcon = iconName },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = iconName,
                                tint = if (selectedIcon == iconName) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    )
}
