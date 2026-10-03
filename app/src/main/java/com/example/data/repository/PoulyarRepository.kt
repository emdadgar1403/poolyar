package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entity.AllowanceWeekEntity
import com.example.data.local.entity.GoalEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.UserSettingsEntity
import com.example.model.ExpenseCategory
import com.example.model.TransactionType
import com.example.util.PersianDateUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class PoulyarRepository(private val database: AppDatabase) {

    private val transactionDao = database.transactionDao()
    private val allowanceDao = database.allowanceDao()
    private val goalDao = database.goalDao()
    private val settingsDao = database.settingsDao()

    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val recentTransactions: Flow<List<TransactionEntity>> = transactionDao.getRecentTransactions(15)
    val allWeeks: Flow<List<AllowanceWeekEntity>> = allowanceDao.getAllWeeks()
    val allGoals: Flow<List<GoalEntity>> = goalDao.getAllGoals()
    val settingsFlow: Flow<UserSettingsEntity?> = settingsDao.getSettings()

    fun getTransactionsForWeek(weekNumber: Int, year: Int): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsForWeek(weekNumber, year)
    }

    fun getWeekFlow(weekNumber: Int, year: Int): Flow<AllowanceWeekEntity?> {
        return allowanceDao.getWeekFlow(weekNumber, year)
    }

    suspend fun getSettings(): UserSettingsEntity {
        return settingsDao.getSettingsOnce() ?: UserSettingsEntity().also {
            settingsDao.insertOrUpdate(it)
        }
    }

    suspend fun updateSettings(settings: UserSettingsEntity) {
        settingsDao.insertOrUpdate(settings)
    }

    /**
     * Record payment of weekly allowance.
     */
    suspend fun recordAllowancePayment(
        weekNumber: Int,
        year: Int,
        amount: Long,
        note: String = "پول توجیبی هفتگی"
    ) {
        val now = System.currentTimeMillis()
        val persianDate = PersianDateUtil.getPersianDate(now)

        // Check if week entity already exists
        val existingWeek = allowanceDao.getWeekByNumber(weekNumber, year)
        if (existingWeek != null) {
            allowanceDao.update(
                existingWeek.copy(
                    amount = amount,
                    isPaid = true,
                    paidTimestamp = now,
                    note = note
                )
            )
        } else {
            allowanceDao.insertOrUpdate(
                AllowanceWeekEntity(
                    weekNumber = weekNumber,
                    year = year,
                    amount = amount,
                    isPaid = true,
                    paidTimestamp = now,
                    startDatePersian = persianDate.formatShort(),
                    endDatePersian = persianDate.formatShort(),
                    note = note
                )
            )
        }

        // Add corresponding transaction
        val weekLabel = PersianDateUtil.formatWeekLabel(weekNumber, year)
        transactionDao.insert(
            TransactionEntity(
                title = "دریافت پول توجیبی ($weekLabel)",
                amount = amount,
                type = TransactionType.ALLOWANCE.name,
                category = ExpenseCategory.OTHER.name,
                timestamp = now,
                persianDate = persianDate.formatReadable(),
                weekNumber = weekNumber,
                year = year,
                note = note
            )
        )
    }

    /**
     * Add an expense.
     */
    suspend fun recordExpense(
        title: String,
        amount: Long,
        category: ExpenseCategory,
        note: String = "",
        customTimestamp: Long = System.currentTimeMillis()
    ) {
        val persianDate = PersianDateUtil.getPersianDate(customTimestamp)
        val weekNumber = PersianDateUtil.getPersianWeekNumber(customTimestamp)

        transactionDao.insert(
            TransactionEntity(
                title = title.trim(),
                amount = amount,
                type = TransactionType.EXPENSE.name,
                category = category.name,
                timestamp = customTimestamp,
                persianDate = persianDate.formatReadable(),
                weekNumber = weekNumber,
                year = persianDate.year,
                note = note.trim()
            )
        )
    }

    /**
     * Transfer money to savings.
     */
    suspend fun transferToSavings(amount: Long, note: String = "انتقال به قلک پس‌انداز") {
        val now = System.currentTimeMillis()
        val persianDate = PersianDateUtil.getPersianDate(now)
        val weekNumber = PersianDateUtil.getPersianWeekNumber(now)

        transactionDao.insert(
            TransactionEntity(
                title = "انتقال به پس‌انداز 🪙",
                amount = amount,
                type = TransactionType.SAVING_DEPOSIT.name,
                category = ExpenseCategory.OTHER.name,
                timestamp = now,
                persianDate = persianDate.formatReadable(),
                weekNumber = weekNumber,
                year = persianDate.year,
                note = note
            )
        )
    }

    /**
     * Withdraw from savings back to available balance.
     */
    suspend fun withdrawFromSavings(amount: Long, note: String = "برداشت از پس‌انداز") {
        val now = System.currentTimeMillis()
        val persianDate = PersianDateUtil.getPersianDate(now)
        val weekNumber = PersianDateUtil.getPersianWeekNumber(now)

        transactionDao.insert(
            TransactionEntity(
                title = "برداشت از پس‌انداز",
                amount = amount,
                type = TransactionType.SAVING_WITHDRAW.name,
                category = ExpenseCategory.OTHER.name,
                timestamp = now,
                persianDate = persianDate.formatReadable(),
                weekNumber = weekNumber,
                year = persianDate.year,
                note = note
            )
        )
    }

    /**
     * Delete a transaction.
     */
    suspend fun deleteTransaction(id: Long) {
        transactionDao.deleteById(id)
    }

    /**
     * Financial Goals
     */
    suspend fun addGoal(
        title: String,
        targetAmount: Long,
        initialSaved: Long = 0L,
        iconEmoji: String = "🎯"
    ): Long {
        val id = goalDao.insert(
            GoalEntity(
                title = title.trim(),
                targetAmount = targetAmount,
                savedAmount = initialSaved,
                iconEmoji = iconEmoji,
                isCompleted = initialSaved >= targetAmount
            )
        )

        if (initialSaved > 0) {
            val now = System.currentTimeMillis()
            val persianDate = PersianDateUtil.getPersianDate(now)
            val weekNumber = PersianDateUtil.getPersianWeekNumber(now)
            transactionDao.insert(
                TransactionEntity(
                    title = "اختصاص به هدف «$title»",
                    amount = initialSaved,
                    type = TransactionType.GOAL_ALLOCATION.name,
                    category = ExpenseCategory.OTHER.name,
                    timestamp = now,
                    persianDate = persianDate.formatReadable(),
                    weekNumber = weekNumber,
                    year = persianDate.year,
                    relatedGoalId = id
                )
            )
        }
        return id
    }

    suspend fun addFundsToGoal(goalId: Long, amount: Long) {
        val goal = goalDao.getGoalById(goalId) ?: return
        val newSaved = goal.savedAmount + amount
        val isCompleted = newSaved >= goal.targetAmount
        goalDao.update(
            goal.copy(
                savedAmount = newSaved,
                isCompleted = isCompleted
            )
        )

        val now = System.currentTimeMillis()
        val persianDate = PersianDateUtil.getPersianDate(now)
        val weekNumber = PersianDateUtil.getPersianWeekNumber(now)

        transactionDao.insert(
            TransactionEntity(
                title = "افزایش پس‌انداز هدف «${goal.title}»",
                amount = amount,
                type = TransactionType.GOAL_ALLOCATION.name,
                category = ExpenseCategory.OTHER.name,
                timestamp = now,
                persianDate = persianDate.formatReadable(),
                weekNumber = weekNumber,
                year = persianDate.year,
                relatedGoalId = goalId
            )
        )
    }

    suspend fun deleteGoal(goalId: Long) {
        goalDao.deleteById(goalId)
    }

    /**
     * Pre-populates sample realistic data for a 13-year-old teen on first app launch.
     */
    suspend fun populateSampleDataIfEmpty() {
        val existingSettings = settingsDao.getSettingsOnce()
        if (existingSettings != null && existingSettings.isInitialized) {
            return
        }

        val now = System.currentTimeMillis()
        val pDate = PersianDateUtil.getPersianDate(now)
        val currentWeek = PersianDateUtil.getPersianWeekNumber(now)
        val currentYear = pDate.year

        // Save default settings
        val settings = existingSettings?.copy(isInitialized = true)
            ?: UserSettingsEntity(
                teenName = "امیرعلی",
                teenAge = 13,
                dailyAllowance = 50_000L,
                weeklyAllowance = 350_000L,
                payoutDay = "شنبه",
                currency = "تومان",
                weeklyExpenseLimit = 300_000L,
                suggestedSavingsPercent = 20,
                notificationsEnabled = true,
                parentPin = "1234",
                isInitialized = true
            )
        settingsDao.insertOrUpdate(settings)

        // Add current week allowance as paid
        allowanceDao.insertOrUpdate(
            AllowanceWeekEntity(
                weekNumber = currentWeek,
                year = currentYear,
                amount = 350_000L,
                isPaid = true,
                paidTimestamp = now - 86400000L * 3,
                startDatePersian = pDate.formatShort(),
                endDatePersian = pDate.formatShort(),
                note = "پول توجیبی هفتگی منظم"
            )
        )

        // Previous week allowance as well for historical view
        if (currentWeek > 1) {
            allowanceDao.insertOrUpdate(
                AllowanceWeekEntity(
                    weekNumber = currentWeek - 1,
                    year = currentYear,
                    amount = 350_000L,
                    isPaid = true,
                    paidTimestamp = now - 86400000L * 10,
                    startDatePersian = pDate.formatShort(),
                    endDatePersian = pDate.formatShort(),
                    note = "پول توجیبی هفته قبل"
                )
            )
        }

        // Transactions
        val sampleTransactions = listOf(
            TransactionEntity(
                title = "دریافت پول توجیبی هفتگی",
                amount = 350_000L,
                type = TransactionType.ALLOWANCE.name,
                category = ExpenseCategory.OTHER.name,
                timestamp = now - 86400000L * 3,
                persianDate = PersianDateUtil.getPersianDate(now - 86400000L * 3).formatReadable(),
                weekNumber = currentWeek,
                year = currentYear,
                note = "پرداخت توسط والدین"
            ),
            TransactionEntity(
                title = "خرید ساندویچ و آبمیوه مدرسه",
                amount = 80_000L,
                type = TransactionType.EXPENSE.name,
                category = ExpenseCategory.FOOD.name,
                timestamp = now - 86400000L * 2,
                persianDate = PersianDateUtil.getPersianDate(now - 86400000L * 2).formatReadable(),
                weekNumber = currentWeek,
                year = currentYear,
                note = "زنگ تفریح دوم"
            ),
            TransactionEntity(
                title = "کارت بازی با بچه‌ها",
                amount = 50_000L,
                type = TransactionType.EXPENSE.name,
                category = ExpenseCategory.FUN.name,
                timestamp = now - 86400000L * 2 + 3600000L,
                persianDate = PersianDateUtil.getPersianDate(now - 86400000L * 2).formatReadable(),
                weekNumber = currentWeek,
                year = currentYear,
                note = "تفریح عصرگاهی"
            ),
            TransactionEntity(
                title = "کتاب داستان و کمیک",
                amount = 30_000L,
                type = TransactionType.EXPENSE.name,
                category = ExpenseCategory.BOOK.name,
                timestamp = now - 86400000L * 1,
                persianDate = PersianDateUtil.getPersianDate(now - 86400000L * 1).formatReadable(),
                weekNumber = currentWeek,
                year = currentYear,
                note = "کتابفروشی محله"
            ),
            TransactionEntity(
                title = "کرایه اسنپ تا کلاس زبان",
                amount = 10_000L,
                type = TransactionType.EXPENSE.name,
                category = ExpenseCategory.TRANSPORT.name,
                timestamp = now - 3600000L * 4,
                persianDate = PersianDateUtil.getPersianDate(now - 3600000L * 4).formatReadable(),
                weekNumber = currentWeek,
                year = currentYear,
                note = "برگشت از کلاس"
            ),
            TransactionEntity(
                title = "انتقال به قلک پس‌انداز 🪙",
                amount = 70_000L,
                type = TransactionType.SAVING_DEPOSIT.name,
                category = ExpenseCategory.OTHER.name,
                timestamp = now - 3600000L * 2,
                persianDate = PersianDateUtil.getPersianDate(now - 3600000L * 2).formatReadable(),
                weekNumber = currentWeek,
                year = currentYear,
                note = "برای هدفون جدید"
            )
        )
        transactionDao.insertAll(sampleTransactions)

        // Sample financial goal
        goalDao.insert(
            GoalEntity(
                title = "خرید هدفون گیمینگ بلوتوثی",
                targetAmount = 2_000_000L,
                savedAmount = 500_000L,
                iconEmoji = "🎧",
                isCompleted = false,
                targetDatePersian = "پایان پاییز"
            )
        )
        goalDao.insert(
            GoalEntity(
                title = "خرید توپ فوتبال اصل",
                targetAmount = 600_000L,
                savedAmount = 200_000L,
                iconEmoji = "⚽",
                isCompleted = false,
                targetDatePersian = "ماه آینده"
            )
        )
    }

    /**
     * Clear all user data.
     */
    suspend fun clearAllData() {
        transactionDao.deleteAll()
        allowanceDao.deleteAll()
        goalDao.deleteAll()
        val s = settingsDao.getSettingsOnce()
        if (s != null) {
            settingsDao.update(s.copy(isInitialized = false))
        }
    }

    /**
     * Reset and re-populate sample data.
     */
    suspend fun resetToSampleData() {
        transactionDao.deleteAll()
        allowanceDao.deleteAll()
        goalDao.deleteAll()
        val s = settingsDao.getSettingsOnce()
        if (s != null) {
            settingsDao.update(s.copy(isInitialized = false))
        }
        populateSampleDataIfEmpty()
    }
}
