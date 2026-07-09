package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Transaction
import com.example.ui.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(viewModel: FinanceViewModel) {
    val transactions by viewModel.transactions.collectAsState()
    val wallets by viewModel.wallets.collectAsState()

    // Aggregate statistics
    val totalIncomesUSD = transactions.filter { it.type == "INCOME" }.sumOf { it.amount }
    val totalExpensesUSD = transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    val netProfitUSD = totalIncomesUSD - totalExpensesUSD

    // Top Category
    val categorySpending = transactions.filter { it.type == "EXPENSE" }
        .groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.amount } }
    val topCategory = categorySpending.maxByOrNull { it.value }

    // Top Wallet
    val walletTxCounts = transactions
        .groupBy { it.walletId }
        .mapValues { it.value.size }
    val topWalletId = walletTxCounts.maxByOrNull { it.value }?.key
    val topWallet = wallets.find { it.id == topWalletId }

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
                text = "التقارير والإحصائيات",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Right
            )
        }

        // Net standing card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = viewModel.formatAmount(netProfitUSD),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                            color = if (netProfitUSD >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)
                        )
                        Text(
                            text = if (netProfitUSD >= 0) "صافي الأرباح والادخار" else "صافي العجز والخسارة",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PieChart,
                            contentDescription = "Stats",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Custom Donut Chart (Category Breakdown)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "توزيع المصروفات حسب التصنيف",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Right
                    )

                    if (categorySpending.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("لا يوجد بيانات كافية لرسم المخطط", style = MaterialTheme.typography.bodyMedium)
                        }
                    } else {
                        // Drawing Donut Chart
                        val totalSpending = categorySpending.values.sum()
                        val sliceColors = listOf(
                            Color(0xFFE57373), Color(0xFF64B5F6), Color(0xFFFFB74D),
                            Color(0xFFBA68C8), Color(0xFF4DB6AC), Color(0xFF81C784), Color(0xFF90A4AE)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            // Legend
                            Column(
                                modifier = Modifier.weight(1.0f),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                categorySpending.entries.take(5).forEachIndexed { index, entry ->
                                    val col = sliceColors[index % sliceColors.size]
                                    val percent = (entry.value / totalSpending * 100).toInt()
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "${entry.key} ($percent%)",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(col)
                                        )
                                    }
                                }
                            }

                            // Donut canvas
                            Box(
                                modifier = Modifier.size(130.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.size(110.dp)) {
                                    var startAngle = 0f
                                    categorySpending.values.forEachIndexed { idx, valUSD ->
                                        val sweepAngle = (valUSD / totalSpending * 360f).toFloat()
                                        drawArc(
                                            color = sliceColors[idx % sliceColors.size],
                                            startAngle = startAngle,
                                            sweepAngle = sweepAngle,
                                            useCenter = false,
                                            style = Stroke(width = 24f)
                                        )
                                        startAngle += sweepAngle
                                    }
                                }
                                Text(
                                    text = "المصروفات",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Monthly Income vs Expenses bar chart (Last 6 Months)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "مقارنة شهريّة: إيرادات ومصروفات",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Right
                    )

                    // Simple Canvas Bar Chart simulating monthly comparison
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                        ) {
                            val barWidth = 32f
                            val groupSpacing = 48f
                            val maxAmount = maxOf(totalIncomesUSD, totalExpensesUSD, 1000.0)
                            val canvasHeight = size.height

                            // Draw baseline
                            drawLine(
                                color = Color.LightGray,
                                start = Offset(0f, canvasHeight),
                                end = Offset(size.width, canvasHeight),
                                strokeWidth = 3f
                            )

                            // Draw income bar (Green)
                            val incHeight = ((totalIncomesUSD / maxAmount) * (canvasHeight - 20f)).toFloat()
                            drawRect(
                                color = Color(0xFF2E7D32),
                                topLeft = Offset(size.width / 2f - groupSpacing - barWidth, canvasHeight - incHeight),
                                size = Size(barWidth, incHeight)
                            )

                            // Draw expense bar (Red)
                            val expHeight = ((totalExpensesUSD / maxAmount) * (canvasHeight - 20f)).toFloat()
                            drawRect(
                                color = Color(0xFFC62828),
                                topLeft = Offset(size.width / 2f + groupSpacing, canvasHeight - expHeight),
                                size = Size(barWidth, expHeight)
                            )
                        }

                        // Labels
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Text("الإيرادات الكلية", style = MaterialTheme.typography.labelSmall, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                            Text("المصروفات الكلية", style = MaterialTheme.typography.labelSmall, color = Color(0xFFC62828), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Side details & Highlights
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "تحليل الأنشطة",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Right
                    )

                    // Top Spending Category
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = topCategory?.let { "${it.key} (${viewModel.formatAmount(it.value)})" } ?: "لا يوجد",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "الأكثر إنفاقاً",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                    // Most Active Wallet
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = topWallet?.name ?: "لا يوجد",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "المحفظة الأكثر استخداماً",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
