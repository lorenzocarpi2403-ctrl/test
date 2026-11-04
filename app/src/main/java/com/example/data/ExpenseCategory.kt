package com.example.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class ExpenseCategory(
    val labelItalian: String,
    val icon: ImageVector,
    val color: Color,
    val isIncome: Boolean = false
) {
    // Spese
    GROCERIES("Spesa & Alimentari", Icons.Default.ShoppingCart, Color(0xFF10B981)),
    RESTAURANT("Ristorante & Bar", Icons.Default.Fastfood, Color(0xFFF59E0B)),
    TRANSPORT("Trasporti & Auto", Icons.Default.DirectionsCar, Color(0xFF3B82F6)),
    UTILITIES("Bollette & Casa", Icons.Default.Home, Color(0xFF8B5CF6)),
    SHOPPING("Shopping & Vestiti", Icons.Default.Receipt, Color(0xFFEC4899)),
    HEALTH("Salute & Cura", Icons.Default.LocalPharmacy, Color(0xFFEF4444)),
    ENTERTAINMENT("Svago & Tempo Libero", Icons.Default.Movie, Color(0xFF06B6D4)),
    OTHER("Altra Spesa", Icons.Default.MoreHoriz, Color(0xFF6B7280)),

    // Entrate
    SALARY("Stipendio", Icons.Default.AccountBalance, Color(0xFF059669), true),
    FREELANCE("Freelance & Extra", Icons.Default.Laptop, Color(0xFF10B981), true),
    INVESTMENTS("Investimenti & Rendite", Icons.Default.TrendingUp, Color(0xFF0D9488), true),
    GIFTS("Regali & Rimborsi", Icons.Default.CardGiftcard, Color(0xFF8B5CF6), true),
    SAVINGS_RETURN("Risparmi & Interessi", Icons.Default.Savings, Color(0xFFF59E0B), true),
    OTHER_INCOME("Altra Entrata", Icons.Default.AttachMoney, Color(0xFF3B82F6), true);

    companion object {
        fun fromString(value: String?): ExpenseCategory {
            if (value == null) return OTHER
            val cleaned = value.trim().uppercase()
            return entries.firstOrNull { it.name == cleaned }
                ?: when {
                    cleaned.contains("STIPEND") -> SALARY
                    cleaned.contains("FREELANCE") || cleaned.contains("LAVORO") -> FREELANCE
                    cleaned.contains("INVEST") || cleaned.contains("DIVIDEND") -> INVESTMENTS
                    cleaned.contains("REGAL") || cleaned.contains("RIMBORS") -> GIFTS
                    cleaned.contains("SPESA") || cleaned.contains("SUPERMERCATO") || cleaned.contains("ALIMENT") -> GROCERIES
                    cleaned.contains("BAR") || cleaned.contains("RISTORANTE") || cleaned.contains("PIZZA") || cleaned.contains("CAFFE") -> RESTAURANT
                    cleaned.contains("BENZINA") || cleaned.contains("CARBURANTE") || cleaned.contains("TAXI") || cleaned.contains("TRENO") -> TRANSPORT
                    cleaned.contains("LUCE") || cleaned.contains("GAS") || cleaned.contains("BOLLETTA") || cleaned.contains("CASA") -> UTILITIES
                    cleaned.contains("ABBIGLIAMENTO") || cleaned.contains("VESTITI") || cleaned.contains("ZARA") -> SHOPPING
                    cleaned.contains("FARMACIA") || cleaned.contains("MEDIC") || cleaned.contains("DOTTORE") -> HEALTH
                    cleaned.contains("CINEMA") || cleaned.contains("TEATRO") || cleaned.contains("CONCERTO") -> ENTERTAINMENT
                    else -> OTHER
                }
        }

        fun expenseCategories(): List<ExpenseCategory> = entries.filter { !it.isIncome }
        fun incomeCategories(): List<ExpenseCategory> = entries.filter { it.isIncome }
    }
}
