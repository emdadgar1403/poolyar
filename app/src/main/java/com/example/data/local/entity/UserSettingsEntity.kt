package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val teenName: String = "امیرعلی",
    val teenAge: Int = 13,
    val dailyAllowance: Long = 50_000L,
    val weeklyAllowance: Long = 350_000L,
    val payoutDay: String = "شنبه",
    val currency: String = "تومان",
    val weeklyExpenseLimit: Long = 300_000L,
    val suggestedSavingsPercent: Int = 20,
    val notificationsEnabled: Boolean = true,
    val parentPin: String = "1234",
    val isInitialized: Boolean = false
)
