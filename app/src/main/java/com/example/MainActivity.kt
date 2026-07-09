package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.FinanceViewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: FinanceViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settings by viewModel.settings.collectAsState()
            val overrideDarkMode by viewModel.isDarkMode.collectAsState()
            val systemDark = isSystemInDarkTheme()

            // Determine dark theme
            val darkTheme = overrideDarkMode ?: systemDark

            MyApplicationTheme(darkTheme = darkTheme) {
                var isUnlocked by remember { mutableStateOf(!settings.isAppLocked) }

                // Reset lock state if setting changes to locked
                LaunchedEffect(settings.isAppLocked) {
                    if (settings.isAppLocked) {
                        isUnlocked = false
                    }
                }

                if (settings.isAppLocked && !isUnlocked) {
                    LockScreen(
                        correctPin = settings.appPin,
                        onUnlocked = { isUnlocked = true }
                    )
                } else {
                    MainScreenContent(viewModel = viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenContent(viewModel: FinanceViewModel) {
    val displayCurrency by viewModel.displayCurrency.collectAsState()
    val showUndo by viewModel.showUndoSnackbar.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var currentScreen by remember { mutableStateOf("DASHBOARD") }

    // Navigation Items
    val navigationItems = listOf(
        NavigationItem("SETTINGS", "الإعدادات", Icons.Default.Settings),
        NavigationItem("STATS", "الإحصائيات", Icons.Default.BarChart),
        NavigationItem("DEBTS", "الديون", Icons.Default.Handshake),
        NavigationItem("TRANSACTIONS", "العمليات", Icons.Default.ReceiptLong),
        NavigationItem("WALLETS", "المحافظ", Icons.Default.AccountBalanceWallet),
        NavigationItem("DASHBOARD", "الرئيسية", Icons.Default.Home)
    )

    // Sync snackbar trigger with ViewModel state
    LaunchedEffect(showUndo) {
        if (showUndo) {
            val result = snackbarHostState.showSnackbar(
                message = "تم حذف العملية بنجاح",
                actionLabel = "تراجع",
                duration = SnackbarDuration.Long
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.undoDeleteTransaction()
            } else {
                viewModel.dismissUndo()
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "محاسبتي",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                actions = {
                    // Currency quick switcher
                    Row(
                        modifier = Modifier.padding(end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "العملة:",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        var expanded by remember { mutableStateOf(false) }
                        AssistChip(
                            onClick = { expanded = true },
                            label = { Text(displayCurrency) },
                            trailingIcon = { Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Dropdown") }
                        )
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            listOf("USD", "TRY", "SYP").forEach { cur ->
                                DropdownMenuItem(
                                    text = { Text(cur) },
                                    onClick = {
                                        viewModel.setDisplayCurrency(cur)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.background,
                tonalElevation = 8.dp
            ) {
                navigationItems.forEach { item ->
                    val isSelected = currentScreen == item.key
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentScreen = item.key },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        label = {
                            Text(
                                text = item.title,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                "DASHBOARD" -> DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToWallets = { currentScreen = "WALLETS" },
                    onNavigateToTransactions = { currentScreen = "TRANSACTIONS" },
                    onAddTransactionClick = { currentScreen = "TRANSACTIONS" }
                )
                "WALLETS" -> WalletsScreen(viewModel = viewModel)
                "TRANSACTIONS" -> TransactionsScreen(viewModel = viewModel)
                "DEBTS" -> DebtsScreen(viewModel = viewModel)
                "STATS" -> StatsScreen(viewModel = viewModel)
                "SETTINGS" -> SettingsScreen(viewModel = viewModel)
            }
        }
    }
}

data class NavigationItem(
    val key: String,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
