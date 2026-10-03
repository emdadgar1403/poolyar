package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "financial_goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val targetAmount: Long,
    val savedAmount: Long = 0L,
    val iconEmoji: String = "🎯",
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val targetDatePersian: String = ""
)
