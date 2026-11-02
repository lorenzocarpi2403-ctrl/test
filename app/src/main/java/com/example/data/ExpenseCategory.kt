package com.example.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class ExpenseCategory(
    val labelItalian: String,
    val icon: ImageVector,
    val color: Color
) {
    GROCERIES(
        labelItalian = "Alimentari & Spesa",
        icon = Icons.Default.ShoppingCart,
        color = Color(0xFF10B981) // Green
    ),
    RESTAURANT(
        labelItalian = "Ristorante & Bar",
        icon = Icons.Default.Fastfood,
        color = Color(0xFFF59E0B) // Amber
    ),
    TRANSPORT(
        labelItalian = "Trasporti & Carburante",
        icon = Icons.Default.DirectionsCar,
        color = Color(0xFF3B82F6) // Blue
    ),
    UTILITIES(
        labelItalian = "Bollette & Casa",
        icon = Icons.Default.Home,
        color = Color(0xFF8B5CF6) // Purple
    ),
    SHOPPING(
        labelItalian = "Shopping",
        icon = Icons.Default.Receipt,
        color = Color(0xFFEC4899) // Pink
    ),
    HEALTH(
        labelItalian = "Salute & Farmacia",
        icon = Icons.Default.LocalPharmacy,
        color = Color(0xFFEF4444) // Red
    ),
    ENTERTAINMENT(
        labelItalian = "Svago & Tempo Libero",
        icon = Icons.Default.Movie,
        color = Color(0xFF06B6D4) // Cyan
    ),
    OTHER(
        labelItalian = "Altro",
        icon = Icons.Default.MoreHoriz,
        color = Color(0xFF6B7280) // Gray
    );

    companion object {
        fun fromString(value: String?): ExpenseCategory {
            if (value == null) return OTHER
            val cleaned = value.trim().uppercase()
            return entries.firstOrNull { it.name == cleaned }
                ?: when {
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
    }
}
