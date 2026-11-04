package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AccountEntity
import com.example.data.BudgetProgress
import com.example.data.BudgetStatus
import com.example.data.MonthItem
import com.example.data.MonthSummary
import com.example.data.MonthlyStatsHelper
import com.example.data.RecurringTransactionEntity
import com.example.data.SubscriptionEntity
import com.example.data.TransactionEntity
import com.example.ui.ReceiptViewModel
import com.example.ui.components.CategorySpendingChart
import com.example.ui.components.MonthlyBudgetCard
import com.example.ui.components.SetMonthlyBudgetDialog
import com.example.ui.components.TransactionItemCard
import java.util.Locale

@Composable
fun StatisticsScreen(
    viewModel: ReceiptViewModel,
    modifier: Modifier = Modifier
) {
    val allTransactions by viewModel.allTransactions.collectAsState()
    val allRecurring by viewModel.allRecurring.collectAsState()
    val allSubscriptions by viewModel.allSubscriptions.collectAsState()
    val allAccounts by viewModel.allAccounts.collectAsState()
    val budgetProgress by viewModel.budgetProgress.collectAsState()
    val monthlyBudget by viewModel.monthlyBudget.collectAsState()

    var showBudgetDialog by remember { mutableStateOf(false) }

    // Screen state: null = list of horizontal month rectangles; non-null = detail of that month
    var selectedMonthItem by remember { mutableStateOf<MonthItem?>(null) }
    var showAllTimeStats by remember { mutableStateOf(false) }

    // Intercept back button when inside month detail or all-time stats
    if (selectedMonthItem != null || showAllTimeStats) {
        BackHandler {
            selectedMonthItem = null
            showAllTimeStats = false
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (selectedMonthItem == null && !showAllTimeStats) {
            // VIEW 1: HORIZONTAL RECTANGLES LIST FOR EVERY MONTH
            MonthsOverviewList(
                allTransactions = allTransactions,
                budgetProgress = budgetProgress,
                onOpenBudgetSettings = { showBudgetDialog = true },
                onMonthSelected = { month -> selectedMonthItem = month },
                onShowAllTimeSelected = { showAllTimeStats = true }
            )
        } else if (showAllTimeStats) {
            // VIEW 2: ALL-TIME (STORICO GLOBALE) DETAIL
            AllTimeStatsDetail(
                allTransactions = allTransactions,
                allRecurring = allRecurring,
                allSubscriptions = allSubscriptions,
                accounts = allAccounts,
                onBackClick = { showAllTimeStats = false },
                onDeleteTransaction = { tx -> viewModel.deleteTransaction(tx) }
            )
        } else {
            // VIEW 3: SINGLE MONTH STATISTICS DETAIL
            val currentSelected = selectedMonthItem!!
            val monthSummary = remember(currentSelected, allTransactions) {
                MonthlyStatsHelper.calculateSummary(currentSelected, allTransactions)
            }

            MonthDetailStatistics(
                summary = monthSummary,
                allRecurring = allRecurring,
                allSubscriptions = allSubscriptions,
                accounts = allAccounts,
                budgetProgress = budgetProgress,
                onOpenBudgetSettings = { showBudgetDialog = true },
                onBackClick = { selectedMonthItem = null },
                onPrevMonthClick = { selectedMonthItem = currentSelected.previousMonth() },
                onNextMonthClick = { selectedMonthItem = currentSelected.nextMonth() },
                onDeleteTransaction = { tx -> viewModel.deleteTransaction(tx) }
            )
        }

        if (showBudgetDialog) {
            SetMonthlyBudgetDialog(
                currentLimit = monthlyBudget?.monthlyLimit,
                onDismiss = { showBudgetDialog = false },
                onSave = { limit -> viewModel.setMonthlyBudget(limit) },
                onRemove = { viewModel.clearMonthlyBudget() }
            )
        }
    }
}

/**
 * Vista iniziale con i rettangoli orizzontali per ogni mese
 */
@Composable
private fun MonthsOverviewList(
    allTransactions: List<TransactionEntity>,
    budgetProgress: BudgetProgress?,
    onOpenBudgetSettings: () -> Unit,
    onMonthSelected: (MonthItem) -> Unit,
    onShowAllTimeSelected: () -> Unit
) {
    val monthsList = remember(allTransactions) {
        MonthlyStatsHelper.generateMonthsList(allTransactions, minPastMonths = 5)
    }

    val totalAllIncome = remember(allTransactions) {
        allTransactions.filter { it.type == "INCOME" }.sumOf { it.amount }
    }
    val totalAllExpense = remember(allTransactions) {
        allTransactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    }
    val allNetBalance = totalAllIncome - totalAllExpense

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 64.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                Text(
                    text = "Panoramica per Mese",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tocca un rettangolo mensile per consultare grafici, entrate e uscite dettagliate.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Monthly Budget Tracking Card
        item {
            MonthlyBudgetCard(
                budgetProgress = budgetProgress,
                onOpenBudgetSettings = onOpenBudgetSettings
            )
        }

        // Storico Globale Rectangle Card
        item {
            ElevatedCard(
                onClick = onShowAllTimeSelected,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_all_time_stats"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Storico Globale",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "(Tutti i mesi)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "${allTransactions.size} movimenti complessivi registrati",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${if (allNetBalance >= 0) "+" else ""}€${String.format(Locale.ITALIAN, "%.2f", allNetBalance)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (allNetBalance >= 0) Color(0xFF059669) else Color(0xFFDC2626)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = "Visualizza storico completo",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        // Section Title
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Mesi Disponibili",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Horizontal Month Rectangles
        items(monthsList, key = { it.key }) { monthItem ->
            val summary = remember(monthItem, allTransactions) {
                MonthlyStatsHelper.calculateSummary(monthItem, allTransactions)
            }

            MonthHorizontalRectangleCard(
                summary = summary,
                budgetProgress = if (monthItem.isCurrentMonth) budgetProgress else null,
                onClick = { onMonthSelected(monthItem) }
            )
        }
    }
}

/**
 * Singolo rettangolo orizzontale del mese con nome del mese, saldo e dettagli
 */
@Composable
private fun MonthHorizontalRectangleCard(
    summary: MonthSummary,
    budgetProgress: BudgetProgress? = null,
    onClick: () -> Unit
) {
    val monthItem = summary.monthItem
    val isPositive = summary.netBalance >= 0

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("month_card_${monthItem.year}_${monthItem.month}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (monthItem.isCurrentMonth) 3.dp else 1.5.dp
        ),
        border = if (monthItem.isCurrentMonth) {
            BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f))
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Badge sinistro con mese e anno
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (monthItem.isCurrentMonth) MaterialTheme.colorScheme.primaryContainer
                        else if (summary.transactionCount == 0) MaterialTheme.colorScheme.surfaceVariant
                        else if (isPositive) Color(0xFFD1FAE5)
                        else Color(0xFFFEE2E2)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = monthItem.shortMonthName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (monthItem.isCurrentMonth) MaterialTheme.colorScheme.primary
                        else if (summary.transactionCount == 0) MaterialTheme.colorScheme.onSurfaceVariant
                        else if (isPositive) Color(0xFF047857)
                        else Color(0xFFB91C1C)
                    )
                    Text(
                        text = monthItem.yearString,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Corpo centrale: Nome del mese, stato corrente e sottotitolo
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = monthItem.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    if (monthItem.isCurrentMonth) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Corrente",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (summary.transactionCount == 0) "Nessun movimento"
                        else "${summary.transactionCount} movimenti",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (summary.totalIncome > 0) {
                        Text(
                            text = "• +€${String.format(Locale.ITALIAN, "%.0f", summary.totalIncome)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF059669)
                        )
                    }

                    if (summary.totalExpense > 0) {
                        Text(
                            text = "• -€${String.format(Locale.ITALIAN, "%.0f", summary.totalExpense)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFDC2626)
                        )
                    }
                }

                if (monthItem.isCurrentMonth && budgetProgress != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    val bColor = when (budgetProgress.status) {
                        BudgetStatus.SAFE -> Color(0xFF059669)
                        BudgetStatus.WARNING -> Color(0xFFD97706)
                        BudgetStatus.EXCEEDED -> Color(0xFFDC2626)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(bColor.copy(alpha = 0.12f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Budget: ${budgetProgress.percentage}% speso (€${String.format(Locale.ITALIAN, "%.0f", budgetProgress.currentSpent)}/€${String.format(Locale.ITALIAN, "%.0f", budgetProgress.monthlyLimit)})",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = bColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Parte destra: Badge saldo netto e freccia
            Column(horizontalAlignment = Alignment.End) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (summary.transactionCount == 0) MaterialTheme.colorScheme.surfaceVariant
                            else if (isPositive) Color(0xFFD1FAE5)
                            else Color(0xFFFEE2E2)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (summary.transactionCount == 0) "€0,00"
                        else "${if (isPositive) "+" else ""}€${String.format(Locale.ITALIAN, "%.2f", summary.netBalance)}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (summary.transactionCount == 0) MaterialTheme.colorScheme.onSurfaceVariant
                        else if (isPositive) Color(0xFF065F46)
                        else Color(0xFF991B1B)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = "Apri dettagli mese",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}

/**
 * Schermata di dettaglio con tutte le statistiche del mese cliccato
 */
@Composable
private fun MonthDetailStatistics(
    summary: MonthSummary,
    allRecurring: List<RecurringTransactionEntity>,
    allSubscriptions: List<SubscriptionEntity>,
    accounts: List<AccountEntity>,
    budgetProgress: BudgetProgress? = null,
    onOpenBudgetSettings: () -> Unit = {},
    onBackClick: () -> Unit,
    onPrevMonthClick: () -> Unit,
    onNextMonthClick: () -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit
) {
    val monthItem = summary.monthItem
    val transactions = summary.transactions

    // Filtro lista movimenti nel mese
    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, EXPENSE, INCOME

    val filteredTransactions = remember(transactions, selectedFilter) {
        when (selectedFilter) {
            "EXPENSE" -> transactions.filter { it.type == "EXPENSE" }
            "INCOME" -> transactions.filter { it.type == "INCOME" }
            else -> transactions
        }
    }

    var transactionToDelete by remember { mutableStateOf<TransactionEntity?>(null) }

    // Recurring & Subscriptions
    val fixedMonthlyIncome = remember(allRecurring) {
        allRecurring.filter { it.isActive && it.type == "INCOME" }.sumOf { it.monthlyEquivalent }
    }
    val fixedMonthlyExpenses = remember(allRecurring) {
        allRecurring.filter { it.isActive && it.type == "EXPENSE" }.sumOf { it.monthlyEquivalent }
    }
    val netFixedDisposable = fixedMonthlyIncome - fixedMonthlyExpenses
    val monthlySubscriptionsTotal = remember(allSubscriptions) {
        allSubscriptions.filter { it.isActive }.sumOf { it.monthlyEquivalent }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 64.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Barra di navigazione superiore con pulsante Indietro e cambio mese
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = onBackClick,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("btn_back_to_months")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Torna all'elenco dei mesi",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tutti i Mesi", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onPrevMonthClick,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("btn_prev_month")
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Mese precedente")
                    }

                    Text(
                        text = monthItem.shortMonthName + " " + monthItem.yearString,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    IconButton(
                        onClick = onNextMonthClick,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("btn_next_month")
                    ) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Mese successivo")
                    }
                }
            }
        }

        // Intestazione con Mese esteso
        item {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Statistiche di ${monthItem.displayName}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (monthItem.isCurrentMonth) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "Mese in corso",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${summary.transactionCount} movimenti registrati in questo mese",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Budget Mensile (se mese in corso)
        if (monthItem.isCurrentMonth) {
            item {
                MonthlyBudgetCard(
                    budgetProgress = budgetProgress,
                    onOpenBudgetSettings = onOpenBudgetSettings
                )
            }
        }

        // KPI Cards: Entrate & Uscite
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatKpiCard(
                    title = "Entrate Mese",
                    amount = "+€${String.format(Locale.ITALIAN, "%.2f", summary.totalIncome)}",
                    icon = Icons.Default.ArrowDownward,
                    color = Color(0xFF059669),
                    bgColor = Color(0xFFD1FAE5),
                    subtitle = "${summary.incomeCount} accrediti",
                    modifier = Modifier.weight(1f)
                )

                StatKpiCard(
                    title = "Uscite Mese",
                    amount = "-€${String.format(Locale.ITALIAN, "%.2f", summary.totalExpense)}",
                    icon = Icons.Default.ArrowUpward,
                    color = Color(0xFFDC2626),
                    bgColor = Color(0xFFFEE2E2),
                    subtitle = "${summary.expenseCount} uscite",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Risparmio Netto / Cash Flow
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (summary.netBalance >= 0) Color(0xFF10B981).copy(alpha = 0.15f)
                                        else Color(0xFFEF4444).copy(alpha = 0.15f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (summary.netBalance >= 0) Icons.Default.Savings else Icons.AutoMirrored.Filled.TrendingUp,
                                    contentDescription = null,
                                    tint = if (summary.netBalance >= 0) Color(0xFF059669) else Color(0xFFDC2626),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Risparmio Netto Mensile",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Cash flow netto a ${monthItem.displayName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(
                            text = "${if (summary.netBalance >= 0) "+" else ""}€${String.format(Locale.ITALIAN, "%.2f", summary.netBalance)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (summary.netBalance >= 0) Color(0xFF059669) else Color(0xFFDC2626)
                        )
                    }

                    if (summary.totalIncome > 0) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Tasso di risparmio sulle entrate",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${summary.savingsRate}%",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = if (summary.savingsRate >= 20) Color(0xFF059669) else Color(0xFFD97706)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        val progress = ((summary.totalIncome - summary.totalExpense) / summary.totalIncome).toFloat().coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (summary.savingsRate >= 20) Color(0xFF10B981) else Color(0xFFF59E0B),
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
        }

        // Transazioni fisse & Ricorrenti nel mese
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Repeat,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Transazioni Fisse Ricorrenti",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${allRecurring.count { it.isActive }} voci fisse attive (stipendio, affitto, bollette)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFD1FAE5))
                                .padding(10.dp)
                        ) {
                            Text("Entrate fisse", style = MaterialTheme.typography.labelSmall, color = Color(0xFF065F46))
                            Text(
                                "+€${String.format(Locale.ITALIAN, "%.2f", fixedMonthlyIncome)}/m",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF047857)
                            )
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFEE2E2))
                                .padding(10.dp)
                        ) {
                            Text("Uscite fisse", style = MaterialTheme.typography.labelSmall, color = Color(0xFF991B1B))
                            Text(
                                "-€${String.format(Locale.ITALIAN, "%.2f", fixedMonthlyExpenses)}/m",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB91C1C)
                            )
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (netFixedDisposable >= 0) Color(0xFFE0E7FF) else Color(0xFFFFEDD5))
                                .padding(10.dp)
                        ) {
                            Text("Netto fisso", style = MaterialTheme.typography.labelSmall, color = if (netFixedDisposable >= 0) Color(0xFF3730A3) else Color(0xFF9A3412))
                            Text(
                                "${if (netFixedDisposable >= 0) "+" else ""}€${String.format(Locale.ITALIAN, "%.2f", netFixedDisposable)}/m",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (netFixedDisposable >= 0) Color(0xFF4338CA) else Color(0xFFC2410C)
                            )
                        }
                    }
                }
            }
        }

        // Incidenza abbonamenti nel mese
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF6366F1).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Subscriptions,
                            contentDescription = null,
                            tint = Color(0xFF6366F1),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Abbonamenti nel Mese",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${allSubscriptions.count { it.isActive }} servizi attivi (Netflix, Disney+, ecc.)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "€${String.format(Locale.ITALIAN, "%.2f", monthlySubscriptionsTotal)}/m",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6366F1)
                    )
                }
            }
        }

        // Category Breakdown per spese
        item {
            CategorySpendingChart(
                transactions = transactions,
                typeFilter = "EXPENSE",
                title = "Dove Hai Speso a ${monthItem.displayName}"
            )
        }

        // Category Breakdown per entrate
        if (summary.incomeCount > 0) {
            item {
                CategorySpendingChart(
                    transactions = transactions,
                    typeFilter = "INCOME",
                    title = "Origine Entrate a ${monthItem.displayName}"
                )
            }
        }

        // Sezione Movimenti del Mese
        item {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Movimenti di ${monthItem.displayName}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${filteredTransactions.size} di ${transactions.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Filtri Spese / Entrate per questo mese
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedFilter == "ALL",
                        onClick = { selectedFilter = "ALL" },
                        label = { Text("Tutti (${transactions.size})") },
                        colors = FilterChipDefaults.filterChipColors()
                    )
                    FilterChip(
                        selected = selectedFilter == "EXPENSE",
                        onClick = { selectedFilter = "EXPENSE" },
                        label = { Text("Uscite (${summary.expenseCount})") },
                        colors = FilterChipDefaults.filterChipColors()
                    )
                    FilterChip(
                        selected = selectedFilter == "INCOME",
                        onClick = { selectedFilter = "INCOME" },
                        label = { Text("Entrate (${summary.incomeCount})") },
                        colors = FilterChipDefaults.filterChipColors()
                    )
                }
            }
        }

        if (filteredTransactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (transactions.isEmpty()) "Nessun movimento registrato in questo mese."
                            else "Nessuna transazione per il filtro selezionato.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredTransactions, key = { it.id }) { tx ->
                TransactionItemCard(
                    transaction = tx,
                    accounts = accounts,
                    onDeleteClick = { transactionToDelete = tx }
                )
            }
        }
    }

    // Dialog conferma cancellazione transazione
    transactionToDelete?.let { tx ->
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = { Text("Elimina Movimento") },
            text = {
                Text("Sei sicuro di voler eliminare '${tx.title}' da €${String.format(Locale.ITALIAN, "%.2f", tx.amount)}? Il saldo del relativo conto verrà ripristinato.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteTransaction(tx)
                        transactionToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Elimina")
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) {
                    Text("Annulla")
                }
            }
        )
    }
}

/**
 * Schermata Statistiche Storico Globale (Tutti i Mesi)
 */
@Composable
private fun AllTimeStatsDetail(
    allTransactions: List<TransactionEntity>,
    allRecurring: List<RecurringTransactionEntity>,
    allSubscriptions: List<SubscriptionEntity>,
    accounts: List<AccountEntity>,
    onBackClick: () -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit
) {
    val totalIncome = remember(allTransactions) {
        allTransactions.filter { it.type == "INCOME" }.sumOf { it.amount }
    }
    val totalExpense = remember(allTransactions) {
        allTransactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    }
    val netSavings = totalIncome - totalExpense
    val savingsRate = if (totalIncome > 0) ((netSavings / totalIncome) * 100).coerceIn(-100.0, 100.0).toInt() else 0

    var transactionToDelete by remember { mutableStateOf<TransactionEntity?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 64.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Back Button
        item {
            OutlinedButton(
                onClick = onBackClick,
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("btn_back_to_months")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Torna all'elenco dei mesi",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Tutti i Mesi", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        item {
            Column {
                Text(
                    text = "Storico Globale (Tutti i Mesi)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Statistiche aggregate su tutte le transazioni finora registrate",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Summary KPI Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatKpiCard(
                    title = "Entrate Totali",
                    amount = "+€${String.format(Locale.ITALIAN, "%.2f", totalIncome)}",
                    icon = Icons.Default.ArrowDownward,
                    color = Color(0xFF059669),
                    bgColor = Color(0xFFD1FAE5),
                    subtitle = "${allTransactions.count { it.type == "INCOME" }} accrediti",
                    modifier = Modifier.weight(1f)
                )

                StatKpiCard(
                    title = "Spese Totali",
                    amount = "-€${String.format(Locale.ITALIAN, "%.2f", totalExpense)}",
                    icon = Icons.Default.ArrowUpward,
                    color = Color(0xFFDC2626),
                    bgColor = Color(0xFFFEE2E2),
                    subtitle = "${allTransactions.count { it.type == "EXPENSE" }} uscite",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Net Savings Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (netSavings >= 0) Color(0xFF10B981).copy(alpha = 0.15f)
                                        else Color(0xFFEF4444).copy(alpha = 0.15f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (netSavings >= 0) Icons.Default.Savings else Icons.AutoMirrored.Filled.TrendingUp,
                                    contentDescription = null,
                                    tint = if (netSavings >= 0) Color(0xFF059669) else Color(0xFFDC2626),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Risparmio Netto Totale",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Totale complessivo",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(
                            text = "${if (netSavings >= 0) "+" else ""}€${String.format(Locale.ITALIAN, "%.2f", netSavings)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (netSavings >= 0) Color(0xFF059669) else Color(0xFFDC2626)
                        )
                    }

                    if (totalIncome > 0) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Tasso di risparmio storico",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$savingsRate%",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = if (savingsRate >= 20) Color(0xFF059669) else Color(0xFFD97706)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        val progress = ((totalIncome - totalExpense) / totalIncome).toFloat().coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (savingsRate >= 20) Color(0xFF10B981) else Color(0xFFF59E0B),
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
        }

        // Category Breakdown for Expenses
        item {
            CategorySpendingChart(
                transactions = allTransactions,
                typeFilter = "EXPENSE",
                title = "Dove Hai Speso (Storico Categorie)"
            )
        }

        // Category Breakdown for Income
        if (allTransactions.any { it.type == "INCOME" }) {
            item {
                CategorySpendingChart(
                    transactions = allTransactions,
                    typeFilter = "INCOME",
                    title = "Origine delle Entrate (Storico)"
                )
            }
        }
    }

    // Delete dialog
    transactionToDelete?.let { tx ->
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = { Text("Elimina Movimento") },
            text = {
                Text("Sei sicuro di voler eliminare '${tx.title}' da €${String.format(Locale.ITALIAN, "%.2f", tx.amount)}?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteTransaction(tx)
                        transactionToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Elimina")
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) {
                    Text("Annulla")
                }
            }
        )
    }
}

@Composable
fun StatKpiCard(
    title: String,
    amount: String,
    icon: ImageVector,
    color: Color,
    bgColor: Color,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = amount,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}
