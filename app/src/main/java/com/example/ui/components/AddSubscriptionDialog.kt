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
import androidx.compose.material.icons.filled.Subscriptions
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

data class SubscriptionPreset(
    val name: String,
    val defaultCost: Double,
    val category: String,
    val colorHex: Long,
    val cycle: String = "MONTHLY"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddSubscriptionDialog(
    accounts: List<AccountEntity>,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        cost: Double,
        billingCycle: String,
        nextRenewalDate: Long,
        category: String,
        colorHex: Long,
        accountId: Long
    ) -> Unit
) {
    val presets = listOf(
        SubscriptionPreset("Netflix", 13.99, "Streaming", 0xFFE50914L),
        SubscriptionPreset("Spotify", 10.99, "Musica", 0xFF1DB954L),
        SubscriptionPreset("Disney+", 9.99, "Streaming", 0xFF113CCFL),
        SubscriptionPreset("Amazon Prime", 4.99, "Servizi", 0xFFFF9900L),
        SubscriptionPreset("iCloud+", 2.99, "Cloud", 0xFF007AFFL),
        SubscriptionPreset("YouTube Premium", 11.99, "Video", 0xFFFF0000L),
        SubscriptionPreset("DAZN", 34.99, "Sport", 0xFFF7FF14L),
        SubscriptionPreset("Palestra / Fitness", 45.00, "Fitness", 0xFF10B981L),
        SubscriptionPreset("ChatGPT Plus", 20.00, "Produttività", 0xFF10A37FL)
    )

    var name by remember { mutableStateOf("") }
    var costText by remember { mutableStateOf("") }
    var billingCycle by remember { mutableStateOf("MONTHLY") }
    var category by remember { mutableStateOf("Streaming") }
    var selectedColor by remember { mutableLongStateOf(0xFF6366F1L) }
    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id ?: 1L) }
    var renewalDaysAhead by remember { mutableStateOf("15") }

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
                        imageVector = Icons.Default.Subscriptions,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Aggiungi Abbonamento",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "Tieni sotto controllo i servizi ricorrenti e le scadenze",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Presets Quick Chips
                Text("Preimpostati Popolari", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presets.forEach { preset ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(preset.colorHex).copy(alpha = 0.15f))
                                .clickable {
                                    name = preset.name
                                    costText = String.format("%.2f", preset.defaultCost)
                                    category = preset.category
                                    selectedColor = preset.colorHex
                                    billingCycle = preset.cycle
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = preset.name,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(preset.colorHex)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome Servizio (es. Netflix)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("subscription_name_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Cost & Cycle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = costText,
                        onValueChange = { costText = it },
                        label = { Text("Costo (€)") },
                        leadingIcon = {
                            Icon(Icons.Default.Euro, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("subscription_cost_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = renewalDaysAhead,
                        onValueChange = { renewalDaysAhead = it },
                        label = { Text("Giorni al rinnovo") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Billing Cycle Tabs
                Text("Frequenza Rinnovo", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                TabRow(
                    selectedTabIndex = if (billingCycle == "MONTHLY") 0 else 1,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = billingCycle == "MONTHLY",
                        onClick = { billingCycle = "MONTHLY" },
                        text = { Text("Mensile") }
                    )
                    Tab(
                        selected = billingCycle == "ANNUAL",
                        onClick = { billingCycle = "ANNUAL" },
                        text = { Text("Annuale") }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Account Selection
                if (accounts.isNotEmpty()) {
                    Text("Conto di Addebito", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
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

                // Category
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Categoria (es. Streaming, Musica, Cloud)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
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
                            val parsedCost = costText.replace(",", ".").toDoubleOrNull() ?: 0.0
                            val days = renewalDaysAhead.toIntOrNull() ?: 15
                            val nextDate = System.currentTimeMillis() + (days * 86_400_000L)
                            if (name.isNotBlank() && parsedCost > 0.0) {
                                onSave(
                                    name,
                                    parsedCost,
                                    billingCycle,
                                    nextDate,
                                    category.ifBlank { "Altro" },
                                    selectedColor,
                                    selectedAccountId
                                )
                            }
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("save_subscription_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Aggiungi")
                    }
                }

                // Spazio extra per permettere di scorrere oltre i tasti di navigazione di sistema del telefono
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }
}
