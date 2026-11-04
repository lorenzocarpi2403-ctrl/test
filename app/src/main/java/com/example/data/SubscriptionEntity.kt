package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val cost: Double,
    val currency: String = "€",
    val billingCycle: String = "MONTHLY", // MONTHLY or ANNUAL
    val nextRenewalDate: Long, // Epoch millis
    val category: String = "Streaming",
    val colorHex: Long = 0xFF6366F1, // Default indigo
    val accountId: Long = 1,
    val isActive: Boolean = true
) {
    val monthlyEquivalent: Double
        get() = if (billingCycle == "ANNUAL") cost / 12.0 else cost
}
