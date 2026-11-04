package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Euro
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.AccountEntity
import com.example.data.ExpenseCategory

data class RecurringPreset(
    val title: String,
    val type: String, // "EXPENSE" or "INCOME"
    val defaultAmount: Double,
    val category: ExpenseCategory,
    val colorHex: Long,
    val dayOfMonth: Int = 1
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddRecurringTransactionDialog(
    accounts: List<AccountEntity>,
    initialType: String = "EXPENSE",
    onDismiss: () -> Unit,
    onSave: (
        type: String,
        title: String,
        amount: Double,
        frequency: String,
        dayOfMonth: Int,
        category: ExpenseCategory,
        colorHex: Long,
        accountId: Long,
        notes: String
    ) -> Unit
) {
    var selectedType by remember { mutableStateOf(initialType) }
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("MONTHLY") }
    var dayOfMonth by remember { mutableIntStateOf(1) }
    var selectedCategory by remember {
        mutableStateOf(if (initialType == "INCOME") ExpenseCategory.SALARY else ExpenseCategory.UTILITIES)
    }
    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id ?: 1L) }
    var selectedColor by remember {
        mutableLongStateOf(if (initialType == "INCOME") 0xFF059669L else 0xFFEF4444L)
    }
    var notes by remember { mutableStateOf("") }

    val expensePresets = listOf(
        RecurringPreset("Affitto Casa", "EXPENSE", 650.00, ExpenseCategory.UTILITIES, 0xFFEF4444L, 5),
        RecurringPreset("Rata Mutuo", "EXPENSE", 520.00, ExpenseCategory.UTILITIES, 0xFFDC2626L, 1),
        RecurringPreset("Spese Condominiali", "EXPENSE", 85.00, ExpenseCategory.UTILITIES, 0xFFF59E0BL, 10),
        RecurringPreset("Assicurazione Auto", "EXPENSE", 40.00, ExpenseCategory.TRANSPORT, 0xFF3B82F6L, 15),
        RecurringPreset("Bolletta Luce & Gas", "EXPENSE", 90.00, ExpenseCategory.UTILITIES, 0xFF8B5CF6L, 20),
        RecurringPreset("Internet Casa Fibra", "EXPENSE", 29.90, ExpenseCategory.UTILITIES, 0xFF06B6D4L, 18),
        RecurringPreset("Rata Auto", "EXPENSE", 220.00, ExpenseCategory.TRANSPORT, 0xFF6366F1L, 8),
        RecurringPreset("Palestra & Sport", "EXPENSE", 50.00, ExpenseCategory.HEALTH, 0xFF10B981L, 1)
    )

    val incomePresets = listOf(
        RecurringPreset("Stipendio Mensile Fisso", "INCOME", 1850.00, ExpenseCategory.SALARY, 0xFF059669L, 27),
        RecurringPreset("Affitto Immobile Percepito", "INCOME", 600.00, ExpenseCategory.INVESTMENTS, 0xFF10B981L, 5),
        RecurringPreset("Pensione", "INCOME", 1400.00, ExpenseCategory.SALARY, 0xFF0D9488L, 1),
        RecurringPreset("Assegno Mantenimento", "INCOME", 350.00, ExpenseCategory.GIFTS, 0xFF8B5CF6L, 10),
        RecurringPreset("Rendita Titoli / Cedola", "INCOME", 120.00, ExpenseCategory.INVESTMENTS, 0xFFF59E0BL, 15)
    )

    val currentPresets = if (selectedType == "INCOME") incomePresets else expensePresets
    val categories = if (selectedType == "INCOME") ExpenseCategory.incomeCategories() else ExpenseCategory.expenseCategories()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .navigationBarsPadding()
                .imePadding()
                .padding(vertical = 12.dp)
                .clip(RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Repeat,
                        contentDescription = null,
                        tint = if (selectedType == "INCOME") Color(0xFF059669) else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Nuova Transazione Fissa",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "Configura entrate o uscite ricorrenti periodiche",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Type Tabs
                TabRow(
                    selectedTabIndex = if (selectedType == "EXPENSE") 0 else 1,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedType == "EXPENSE",
                        onClick = {
                            selectedType = "EXPENSE"
                            selectedCategory = ExpenseCategory.UTILITIES
                            selectedColor = 0xFFEF4444L
                        },
                        text = {
                            Text(
                                "Uscita Fissa",
                                fontWeight = if (selectedType == "EXPENSE") FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                    Tab(
                        selected = selectedType == "INCOME",
                        onClick = {
                            selectedType = "INCOME"
                            selectedCategory = ExpenseCategory.SALARY
                            selectedColor = 0xFF059669L
                        },
                        text = {
                            Text(
                                "Entrata Fissa",
                                fontWeight = if (selectedType == "INCOME") FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quick Presets
                Text("Modelli Rapidi", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    currentPresets.forEach { preset ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(preset.colorHex).copy(alpha = 0.15f))
                                .clickable {
                                    title = preset.title
                                    amountText = String.format("%.2f", preset.defaultAmount)
                                    selectedCategory = preset.category
                                    selectedColor = preset.colorHex
                                    dayOfMonth = preset.dayOfMonth
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = preset.title,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(preset.colorHex)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nome Transazione (es. Affitto, Stipendio)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("recurring_title_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Amount & Day of Month
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Importo Fisso (€)") },
                        leadingIcon = {
                            Icon(Icons.Default.Euro, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("recurring_amount_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = dayOfMonth.toString(),
                        onValueChange = { dayOfMonth = it.toIntOrNull()?.coerceIn(1, 31) ?: 1 },
                        label = { Text("Giorno del mese") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(0.8f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Frequency
                Text("Frequenza Ricorrenza", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("MONTHLY" to "Mensile", "ANNUAL" to "Annuale", "WEEKLY" to "Settimanale").forEach { (fCode, fLabel) ->
                        val isSelected = frequency == fCode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { frequency = fCode }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = fLabel,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Account Selection
                if (accounts.isNotEmpty()) {
                    Text(
                        text = if (selectedType == "INCOME") "Accredita su Conto" else "Addebita su Conto",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        accounts.forEach { acc ->
                            val isSelected = acc.id == selectedAccountId
                            val bg = if (isSelected) Color(acc.colorHex) else MaterialTheme.colorScheme.surfaceVariant
                            val textColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(bg)
                                    .clickable { selectedAccountId = acc.id }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = acc.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = textColor,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Category Selection
                Text("Categoria", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = cat == selectedCategory
                        val chipBg = if (isSelected) cat.color else MaterialTheme.colorScheme.surfaceVariant
                        val contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(chipBg)
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(cat.icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(cat.labelItalian, style = MaterialTheme.typography.labelSmall, color = contentColor)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Note opzionali") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                        Text("Annulla")
                    }
                    Button(
                        onClick = {
                            val parsed = amountText.replace(",", ".").toDoubleOrNull() ?: 0.0
                            if (title.isNotBlank() && parsed > 0.0) {
                                onSave(
                                    selectedType,
                                    title,
                                    parsed,
                                    frequency,
                                    dayOfMonth,
                                    selectedCategory,
                                    selectedColor,
                                    selectedAccountId,
                                    notes
                                )
                            }
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("save_recurring_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedType == "INCOME") Color(0xFF059669) else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (selectedType == "INCOME") "Salva Entrata" else "Salva Spesa")
                    }
                }

                // Spazio extra per permettere di scorrere oltre i tasti di navigazione di sistema del telefono
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }
}
