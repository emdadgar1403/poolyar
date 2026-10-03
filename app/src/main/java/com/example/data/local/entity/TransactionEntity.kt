package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.ExpenseCategory
import com.example.model.TransactionType

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Long,
    val type: String = TransactionType.EXPENSE.name,
    val category: String = ExpenseCategory.OTHER.name,
    val timestamp: Long = System.currentTimeMillis(),
    val persianDate: String = "",
    val weekNumber: Int = 1,
    val year: Int = 1405,
    val note: String = "",
    val relatedGoalId: Long? = null
)
