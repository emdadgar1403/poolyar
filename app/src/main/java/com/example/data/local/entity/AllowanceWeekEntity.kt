package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "allowance_weeks")
data class AllowanceWeekEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val weekNumber: Int,
    val year: Int,
    val amount: Long,
    val isPaid: Boolean = false,
    val paidTimestamp: Long? = null,
    val startDatePersian: String = "",
    val endDatePersian: String = "",
    val note: String = ""
)
