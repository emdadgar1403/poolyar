package com.example.model

import androidx.compose.ui.graphics.Color

enum class ExpenseCategory(
    val titlePersian: String,
    val emoji: String,
    val color: Color
) {
    FOOD("خوراکی", "🍔", Color(0xFFF59E0B)),
    FUN("تفریح", "🎮", Color(0xFF8B5CF6)),
    BOOK("کتاب و آموزش", "📚", Color(0xFF3B82F6)),
    PERSONAL("وسایل شخصی", "🧸", Color(0xFFEC4899)),
    GIFT("هدیه", "🎁", Color(0xFF10B981)),
    TRANSPORT("رفت‌وآمد", "🚕", Color(0xFFEAB308)),
    OTHER("سایر", "💰", Color(0xFF64748B));

    companion object {
        fun fromName(name: String?): ExpenseCategory {
            return entries.find { it.name == name } ?: OTHER
        }
    }
}

enum class TransactionType(
    val titlePersian: String
) {
    EXPENSE("هزینه"),
    ALLOWANCE("پول توجیبی"),
    INCOME("درآمد دیگر"),
    SAVING_DEPOSIT("انتقال به پس‌انداز"),
    SAVING_WITHDRAW("برداشت از پس‌انداز"),
    GOAL_ALLOCATION("اختصاص به هدف");

    companion object {
        fun fromName(name: String?): TransactionType {
            return entries.find { it.name == name } ?: EXPENSE
        }
    }
}
