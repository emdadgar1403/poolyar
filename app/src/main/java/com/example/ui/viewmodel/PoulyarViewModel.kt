package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.AllowanceWeekEntity
import com.example.data.local.entity.GoalEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.UserSettingsEntity
import com.example.data.repository.PoulyarRepository
import com.example.model.DashboardState
import com.example.model.ExpenseCategory
import com.example.model.MonthlyFinancialSummary
import com.example.model.TransactionType
import com.example.model.WeeklyFinancialSummary
import com.example.util.NotificationHelper
import com.example.util.PersianDateUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PoulyarViewModel(
    private val repository: PoulyarRepository
) : ViewModel() {

    init {
        viewModelScope.launch {
            repository.populateSampleDataIfEmpty()
        }
    }

    val allTransactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWeeks: StateFlow<List<AllowanceWeekEntity>> = repository.allWeeks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allGoals: StateFlow<List<GoalEntity>> = repository.allGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settings: StateFlow<UserSettingsEntity> = repository.settingsFlow
        .combine(MutableStateFlow(UserSettingsEntity())) { dbSettings, fallback ->
            dbSettings ?: fallback
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserSettingsEntity())

    // UI state for Dashboard
    val dashboardState: StateFlow<DashboardState> = combine(
        allTransactions,
        allWeeks,
        allGoals,
        settings
    ) { transactions, weeks, goals, userSettings ->
        calculateDashboardState(transactions, weeks, goals, userSettings)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardState())

    // Weekly Financial Summary
    val weeklySummary: StateFlow<WeeklyFinancialSummary> = combine(
        allTransactions,
        allWeeks,
        settings
    ) { transactions, weeks, userSettings ->
        calculateWeeklySummary(transactions, weeks, userSettings)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        WeeklyFinancialSummary(
            weekNumber = 1,
            year = 1405,
            weekLabel = "",
            startDatePersian = "",
            endDatePersian = "",
            allowanceAmount = 350_000L,
            isAllowancePaid = false,
            totalExpenses = 0L,
            totalSavings = 0L,
            remainingBalance = 0L
        )
    )

    // Monthly Financial Summary
    val monthlySummary: StateFlow<MonthlyFinancialSummary> = combine(
        allTransactions,
        allWeeks,
        settings
    ) { transactions, weeks, userSettings ->
        calculateMonthlySummary(transactions, weeks, userSettings)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        MonthlyFinancialSummary(
            monthName = "مهر",
            monthNumber = 7,
            year = 1405,
            totalAllowance = 0L,
            totalExpenses = 0L,
            totalSavings = 0L,
            topExpenseCategory = null,
            topExpenseAmount = 0L,
            dailyExpenseAverage = 0L
        )
    )

    private fun calculateDashboardState(
        transactions: List<TransactionEntity>,
        weeks: List<AllowanceWeekEntity>,
        goals: List<GoalEntity>,
        userSettings: UserSettingsEntity
    ): DashboardState {
        val now = System.currentTimeMillis()
        val pDate = PersianDateUtil.getPersianDate(now)
        val currentWeek = PersianDateUtil.getPersianWeekNumber(now)
        val currentYear = pDate.year

        var availableBalance = 0L
        var totalSavings = 0L
        var weeklyExpenses = 0L
        var weeklySavings = 0L
        val categoryExpensesMap = mutableMapOf<ExpenseCategory, Long>()

        for (tx in transactions) {
            when (tx.type) {
                TransactionType.ALLOWANCE.name, TransactionType.INCOME.name -> {
                    availableBalance += tx.amount
                }
                TransactionType.EXPENSE.name -> {
                    availableBalance -= tx.amount
                    if (tx.weekNumber == currentWeek && tx.year == currentYear) {
                        weeklyExpenses += tx.amount
                        val cat = ExpenseCategory.fromName(tx.category)
                        categoryExpensesMap[cat] = (categoryExpensesMap[cat] ?: 0L) + tx.amount
                    }
                }
                TransactionType.SAVING_DEPOSIT.name -> {
                    availableBalance -= tx.amount
                    totalSavings += tx.amount
                    if (tx.weekNumber == currentWeek && tx.year == currentYear) {
                        weeklySavings += tx.amount
                    }
                }
                TransactionType.SAVING_WITHDRAW.name -> {
                    availableBalance += tx.amount
                    totalSavings -= tx.amount
                }
                TransactionType.GOAL_ALLOCATION.name -> {
                    availableBalance -= tx.amount
                }
            }
        }

        val totalGoalsSaved = goals.sumOf { it.savedAmount }
        val totalAssets = availableBalance.coerceAtLeast(0L) +
                totalSavings.coerceAtLeast(0L) +
                totalGoalsSaved

        val currentWeekEntity = weeks.find { it.weekNumber == currentWeek && it.year == currentYear }
        val isWeeklyAllowancePaid = currentWeekEntity?.isPaid ?: false

        val savingsPercent = if (userSettings.weeklyAllowance > 0) {
            ((weeklySavings.toDouble() / userSettings.weeklyAllowance.toDouble()) * 100).toInt()
        } else {
            0
        }

        val isExceeded = weeklyExpenses > userSettings.weeklyExpenseLimit

        return DashboardState(
            teenName = userSettings.teenName,
            teenAge = userSettings.teenAge,
            currentBalance = availableBalance.coerceAtLeast(0L),
            totalSavings = totalSavings.coerceAtLeast(0L),
            totalBalance = totalAssets,
            weeklyAllowance = userSettings.weeklyAllowance,
            dailyAllowance = userSettings.dailyAllowance,
            isWeeklyAllowancePaid = isWeeklyAllowancePaid,
            weeklyExpenses = weeklyExpenses,
            weeklySavings = weeklySavings,
            savingsPercentage = savingsPercent.coerceIn(0, 100),
            weeklyExpenseLimit = userSettings.weeklyExpenseLimit,
            isExpenseLimitExceeded = isExceeded,
            suggestedSavingsPercent = userSettings.suggestedSavingsPercent,
            categoryExpensesThisWeek = categoryExpensesMap,
            recentTransactions = transactions.take(6),
            currentWeekNumber = currentWeek,
            currentYear = currentYear
        )
    }

    private fun calculateWeeklySummary(
        transactions: List<TransactionEntity>,
        weeks: List<AllowanceWeekEntity>,
        userSettings: UserSettingsEntity
    ): WeeklyFinancialSummary {
        val now = System.currentTimeMillis()
        val pDate = PersianDateUtil.getPersianDate(now)
        val currentWeek = PersianDateUtil.getPersianWeekNumber(now)
        val currentYear = pDate.year

        val weekTransactions = transactions.filter { it.weekNumber == currentWeek && it.year == currentYear }
        val expenses = weekTransactions.filter { it.type == TransactionType.EXPENSE.name }.sumOf { it.amount }
        val savings = weekTransactions.filter { it.type == TransactionType.SAVING_DEPOSIT.name }.sumOf { it.amount }

        val weekEntity = weeks.find { it.weekNumber == currentWeek && it.year == currentYear }
        val isPaid = weekEntity?.isPaid ?: false
        val allowanceAmt = weekEntity?.amount ?: userSettings.weeklyAllowance
        val remaining = (allowanceAmt - expenses - savings).coerceAtLeast(0L)

        val catMap = mutableMapOf<ExpenseCategory, Long>()
        weekTransactions.filter { it.type == TransactionType.EXPENSE.name }.forEach { tx ->
            val cat = ExpenseCategory.fromName(tx.category)
            catMap[cat] = (catMap[cat] ?: 0L) + tx.amount
        }

        return WeeklyFinancialSummary(
            weekNumber = currentWeek,
            year = currentYear,
            weekLabel = PersianDateUtil.formatWeekLabel(currentWeek, currentYear),
            startDatePersian = pDate.formatShort(),
            endDatePersian = pDate.formatShort(),
            allowanceAmount = allowanceAmt,
            isAllowancePaid = isPaid,
            totalExpenses = expenses,
            totalSavings = savings,
            remainingBalance = remaining,
            categoryExpenses = catMap
        )
    }

    private fun calculateMonthlySummary(
        transactions: List<TransactionEntity>,
        weeks: List<AllowanceWeekEntity>,
        userSettings: UserSettingsEntity
    ): MonthlyFinancialSummary {
        val now = System.currentTimeMillis()
        val pDate = PersianDateUtil.getPersianDate(now)

        // Filter this Persian year
        val yearTransactions = transactions.filter { it.year == pDate.year }
        val totalAllowance = weeks.filter { it.year == pDate.year && it.isPaid }.sumOf { it.amount }
        val totalExpenses = yearTransactions.filter { it.type == TransactionType.EXPENSE.name }.sumOf { it.amount }
        val totalSavings = yearTransactions.filter { it.type == TransactionType.SAVING_DEPOSIT.name }.sumOf { it.amount }

        val catMap = mutableMapOf<ExpenseCategory, Long>()
        yearTransactions.filter { it.type == TransactionType.EXPENSE.name }.forEach { tx ->
            val cat = ExpenseCategory.fromName(tx.category)
            catMap[cat] = (catMap[cat] ?: 0L) + tx.amount
        }

        val topCategory = catMap.maxByOrNull { it.value }
        val daysInMonth = if (pDate.month <= 6) 31 else 30
        val dailyAvg = if (daysInMonth > 0) totalExpenses / daysInMonth else 0L

        return MonthlyFinancialSummary(
            monthName = pDate.monthName,
            monthNumber = pDate.month,
            year = pDate.year,
            totalAllowance = totalAllowance,
            totalExpenses = totalExpenses,
            totalSavings = totalSavings,
            topExpenseCategory = topCategory?.key,
            topExpenseAmount = topCategory?.value ?: 0L,
            dailyExpenseAverage = dailyAvg,
            categoryExpenses = catMap
        )
    }

    // Actions
    fun recordExpense(title: String, amount: Long, category: ExpenseCategory, note: String = "") {
        viewModelScope.launch {
            repository.recordExpense(title, amount, category, note)
        }
    }

    fun recordAllowancePayment(
        weekNumber: Int = dashboardState.value.currentWeekNumber,
        year: Int = dashboardState.value.currentYear,
        amount: Long = dashboardState.value.weeklyAllowance,
        note: String = "دریافت پول توجیبی"
    ) {
        viewModelScope.launch {
            repository.recordAllowancePayment(weekNumber, year, amount, note)
        }
    }

    fun transferToSavings(amount: Long, note: String = "انتقال به قلک پس‌انداز") {
        viewModelScope.launch {
            repository.transferToSavings(amount, note)
        }
    }

    fun withdrawFromSavings(amount: Long, note: String = "برداشت از پس‌انداز") {
        viewModelScope.launch {
            repository.withdrawFromSavings(amount, note)
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
        }
    }

    fun addGoal(title: String, targetAmount: Long, initialSaved: Long = 0L, iconEmoji: String = "🎯") {
        viewModelScope.launch {
            repository.addGoal(title, targetAmount, initialSaved, iconEmoji)
        }
    }

    fun addFundsToGoal(goalId: Long, amount: Long) {
        viewModelScope.launch {
            repository.addFundsToGoal(goalId, amount)
        }
    }

    fun deleteGoal(goalId: Long) {
        viewModelScope.launch {
            repository.deleteGoal(goalId)
        }
    }

    fun updateSettings(newSettings: UserSettingsEntity) {
        viewModelScope.launch {
            repository.updateSettings(newSettings)
        }
    }

    fun sendTestNotification(context: Context): Boolean {
        return NotificationHelper.sendAllowanceReminder(
            context = context,
            amountFormatted = PersianDateUtil.formatTomans(dashboardState.value.weeklyAllowance)
        )
    }

    fun verifyParentPin(enteredPin: String): Boolean {
        return settings.value.parentPin == enteredPin
    }

    fun updateParentPin(newPin: String) {
        viewModelScope.launch {
            repository.updateSettings(settings.value.copy(parentPin = newPin))
        }
    }

    fun resetToSampleData() {
        viewModelScope.launch {
            repository.resetToSampleData()
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
        }
    }
}

class PoulyarViewModelFactory(
    private val repository: PoulyarRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PoulyarViewModel::class.java)) {
            return PoulyarViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
