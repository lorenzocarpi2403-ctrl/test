package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AccountEntity
import com.example.data.ExpenseCategory
import com.example.data.RecurringTransactionEntity
import com.example.ui.ReceiptViewModel
import com.example.ui.components.AddRecurringTransactionDialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RecurringTransactionsScreen(
    viewModel: ReceiptViewModel,
    modifier: Modifier = Modifier
) {
    val recurringList by viewModel.allRecurring.collectAsState()
    val accounts by viewModel.allAccounts.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Uscite Fisse, 1: Entrate Fisse
    var showAddDialog by remember { mutableStateOf(false) }

    val activeList = remember(recurringList) { recurringList.filter { it.isActive } }

    val totalMonthlyExpenses = remember(activeList) {
        activeList.filter { it.type == "EXPENSE" }.sumOf { it.monthlyEquivalent }
    }
    val totalMonthlyIncome = remember(activeList) {
        activeList.filter { it.type == "INCOME" }.sumOf { it.monthlyEquivalent }
    }
    val netMonthlyFixed = remember(totalMonthlyIncome, totalMonthlyExpenses) {
        totalMonthlyIncome - totalMonthlyExpenses
    }

    val displayedItems = remember(recurringList, selectedTab) {
        val targetType = if (selectedTab == 0) "EXPENSE" else "INCOME"
        recurringList.filter { it.type == targetType }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = if (selectedTab == 0) MaterialTheme.colorScheme.primary else Color(0xFF059669),
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = {
                    Text(
                        if (selectedTab == 0) "Nuova Spesa Fissa" else "Nuova Entrata Fissa",
                        fontWeight = FontWeight.Bold
                    )
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("add_recurring_fab")
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Fixed Budget Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        Color(0xFF0F172A),
                                        Color(0xFF1E293B),
                                        Color(0xFF334155)
                                    )
                                )
                            )
                            .padding(22.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Bilancio Fisso Mensile",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${activeList.size} ricorrenti attive",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "${if (netMonthlyFixed >= 0) "+" else ""}€${String.format(Locale.ITALIAN, "%.2f", netMonthlyFixed)} / mese",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (netMonthlyFixed >= 0) Color(0xFF34D399) else Color(0xFFF87171)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Entrate Fisse
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(Color(0xFF059669).copy(alpha = 0.25f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(14.dp))
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text("Entrate Fisse", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                                        Text("+€${String.format(Locale.ITALIAN, "%.2f", totalMonthlyIncome)}/m", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }

                                // Spese Fisse
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(Color(0xFFDC2626).copy(alpha = 0.25f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = Color(0xFFF87171), modifier = Modifier.size(14.dp))
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text("Spese Fisse", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                                        Text("-€${String.format(Locale.ITALIAN, "%.2f", totalMonthlyExpenses)}/m", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Tab Selector: Uscite Fisse vs Entrate Fisse
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clip(RoundedCornerShape(14.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                "Uscite Fisse (${recurringList.count { it.type == "EXPENSE" }})",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                "Entrate Fisse (${recurringList.count { it.type == "INCOME" }})",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // Empty State
            if (displayedItems.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.EventRepeat,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (selectedTab == 0) "Nessuna spesa fissa configurata" else "Nessuna entrata fissa configurata",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (selectedTab == 0)
                                    "Aggiungi affitto, mutuo, bollette ricorrenti o abbonamenti per tenerne traccia automatica."
                                else
                                    "Aggiungi stipendio mensile fisso, rendite o entrate ricorrenti.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(displayedItems, key = { it.id }) { item ->
                    RecurringItemCard(
                        item = item,
                        accounts = accounts,
                        onToggle = { viewModel.toggleRecurringActive(item) },
                        onApplyNow = { viewModel.applyRecurringNow(item) },
                        onDelete = { viewModel.deleteRecurring(item) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddRecurringTransactionDialog(
            accounts = accounts,
            initialType = if (selectedTab == 0) "EXPENSE" else "INCOME",
            onDismiss = { showAddDialog = false },
            onSave = { type, title, amount, frequency, dayOfMonth, category, colorHex, accountId, notes ->
                viewModel.addRecurring(type, title, amount, frequency, dayOfMonth, category, colorHex, accountId, notes)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun RecurringItemCard(
    item: RecurringTransactionEntity,
    accounts: List<AccountEntity>,
    onToggle: () -> Unit,
    onApplyNow: () -> Unit,
    onDelete: () -> Unit
) {
    val category = remember(item.category) {
        ExpenseCategory.fromString(item.category)
    }

    val account = remember(item.accountId, accounts) {
        accounts.firstOrNull { it.id == item.accountId }
    }

    val isIncome = item.type == "INCOME"

    val freqLabel = when (item.frequency) {
        "ANNUAL" -> "Ogni anno (giorno ${item.dayOfMonth})"
        "WEEKLY" -> "Ogni settimana"
        else -> "Ogni mese (giorno ${item.dayOfMonth})"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("recurring_card_${item.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isActive) MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (item.isActive) 2.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category/Brand Icon
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(item.colorHex).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = null,
                        tint = Color(item.colorHex),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$freqLabel • ${category.labelItalian}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (account != null) {
                        Box(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(account.colorHex).copy(alpha = 0.12f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = account.name,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(account.colorHex)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${if (isIncome) "+" else "-"}€${String.format(Locale.ITALIAN, "%.2f", item.amount)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isIncome) Color(0xFF059669) else MaterialTheme.colorScheme.onSurface
                    )

                    Switch(
                        checked = item.isActive,
                        onCheckedChange = { onToggle() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = if (isIncome) Color(0xFF059669) else MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action row: "Registra nel conto ora" button + Delete icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onApplyNow,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isIncome) Color(0xFF059669).copy(alpha = 0.15f) else MaterialTheme.colorScheme.primaryContainer,
                        contentColor = if (isIncome) Color(0xFF059669) else MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Registra nel conto ora", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Elimina",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
