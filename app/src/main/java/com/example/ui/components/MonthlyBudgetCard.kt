package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BudgetProgress
import com.example.data.BudgetStatus
import com.example.data.MonthItem
import java.util.Locale

@Composable
fun MonthlyBudgetCard(
    budgetProgress: BudgetProgress?,
    onOpenBudgetSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentMonthName = MonthItem.current().displayName

    if (budgetProgress == null) {
        // Empty State: Prompt user to set a budget
        Card(
            modifier = modifier
                .fillMaxWidth()
                .testTag("budget_card_empty"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.TrackChanges,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Budget Mensile ($currentMonthName)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Imposta un limite di spesa per monitorare le uscite in tempo reale.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onOpenBudgetSettings,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("set_budget_button")
                ) {
                    Text("Imposta", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    } else {
        // Active Budget Tracking State
        val status = budgetProgress.status

        val (statusColor, containerBg, statusLabel, statusIcon) = when (status) {
            BudgetStatus.SAFE -> {
                Quadruple(
                    Color(0xFF059669), // Emerald Green
                    Color(0xFFECFDF5),
                    "Entro il limite",
                    Icons.Default.CheckCircle
                )
            }
            BudgetStatus.WARNING -> {
                Quadruple(
                    Color(0xFFD97706), // Amber Warning
                    Color(0xFFFFFBEB),
                    "Attenzione (quasi al limite)",
                    Icons.Default.WarningAmber
                )
            }
            BudgetStatus.EXCEEDED -> {
                Quadruple(
                    Color(0xFFDC2626), // Danger Red
                    Color(0xFFFEF2F2),
                    "Budget superato!",
                    Icons.Default.ErrorOutline
                )
            }
        }

        val animatedProgress by animateFloatAsState(
            targetValue = budgetProgress.progressFraction,
            label = "budget_progress"
        )

        Card(
            modifier = modifier
                .fillMaxWidth()
                .testTag("budget_card")
                .clickable { onOpenBudgetSettings() },
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = BorderStroke(
                1.5.dp,
                if (status == BudgetStatus.SAFE) {
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                } else {
                    statusColor.copy(alpha = 0.6f)
                }
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Header: Month Title, Status Tag, and Edit Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(statusColor.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = statusIcon,
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = "Budget $currentMonthName",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = statusColor.copy(alpha = 0.15f),
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Text(
                                    text = statusLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onOpenBudgetSettings,
                        modifier = Modifier.testTag("set_budget_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Modifica Budget",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Spending Numbers (Spent vs Limit)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "Speso questo mese",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "€${String.format(Locale.ITALIAN, "%.2f", budgetProgress.currentSpent)}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (status == BudgetStatus.EXCEEDED) statusColor else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = " / €${String.format(Locale.ITALIAN, "%.2f", budgetProgress.monthlyLimit)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 2.dp, start = 4.dp)
                            )
                        }
                    }

                    // Remaining or Overspent badge
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (status == BudgetStatus.EXCEEDED) {
                            statusColor.copy(alpha = 0.12f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    ) {
                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (status == BudgetStatus.EXCEEDED) "Sforato di" else "Rimanenti",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (status == BudgetStatus.EXCEEDED) statusColor else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (status == BudgetStatus.EXCEEDED) {
                                    "+€${String.format(Locale.ITALIAN, "%.2f", budgetProgress.overspentAmount)}"
                                } else {
                                    "€${String.format(Locale.ITALIAN, "%.2f", budgetProgress.remaining)}"
                                },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (status == BudgetStatus.EXCEEDED) statusColor else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Progress Bar with Percentage Tag
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp),
                            color = statusColor,
                            trackColor = Color.Transparent
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "${budgetProgress.percentage}%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Daily Advice / Status Tip Box
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (status == BudgetStatus.EXCEEDED) {
                                Icons.Default.ErrorOutline
                            } else {
                                Icons.Default.Savings
                            },
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when {
                                status == BudgetStatus.EXCEEDED -> {
                                    "Hai superato il budget di €${String.format(Locale.ITALIAN, "%.2f", budgetProgress.overspentAmount)}. Limita le spese non essenziali."
                                }
                                budgetProgress.dailySafeAllowance > 0.0 -> {
                                    "Puoi spendere circa €${String.format(Locale.ITALIAN, "%.2f", budgetProgress.dailySafeAllowance)}/giorno per i restanti ${budgetProgress.daysRemainingInMonth} giorni."
                                }
                                else -> {
                                    "Budget quasi esaurito per questo mese (${budgetProgress.daysRemainingInMonth} giorni rimanenti)."
                                }
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
