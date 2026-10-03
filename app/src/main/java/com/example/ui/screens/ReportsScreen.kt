package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.AllowanceWeekEntity
import com.example.model.MonthlyFinancialSummary
import com.example.model.WeeklyFinancialSummary
import com.example.ui.components.ExpenseCategoryChart
import com.example.ui.theme.PrimaryTeal
import com.example.ui.theme.SecondaryAmber
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningOrange
import com.example.util.PersianDateUtil

@Composable
fun ReportsScreen(
    weeklySummary: WeeklyFinancialSummary,
    monthlySummary: MonthlyFinancialSummary,
    allWeeks: List<AllowanceWeekEntity>,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("گزارش هفتگی 📊", "گزارش ماهانه و سالانه 📅")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("reports_screen")
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "تحلیل و گزارش رفتار مالی 📈",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "دیدن روند خرج‌ها و یادگیری هوش مالی",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        TabRow(
            selectedTabIndex = selectedTab,
            divider = {},
            modifier = Modifier.fillMaxWidth().testTag("report_tabs")
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (selectedTab == 0) {
            WeeklyReportContent(
                summary = weeklySummary,
                allWeeks = allWeeks
            )
        } else {
            MonthlyReportContent(
                monthlySummary = monthlySummary,
                allWeeks = allWeeks
            )
        }
    }
}

@Composable
private fun WeeklyReportContent(
    summary: WeeklyFinancialSummary,
    allWeeks: List<AllowanceWeekEntity>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Main Weekly Summary Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("weekly_report_summary_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "عملکرد این هفته (${summary.weekLabel})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 4 Key metrics
                    ReportRowItem(
                        label = "پول توجیبی هفتگی",
                        value = PersianDateUtil.formatTomans(summary.allowanceAmount),
                        color = PrimaryTeal
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ReportRowItem(
                        label = "مجموع هزینه‌ها",
                        value = PersianDateUtil.formatTomans(summary.totalExpenses),
                        color = Color(0xFFEF4444)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ReportRowItem(
                        label = "مبلغ پس‌انداز شده",
                        value = PersianDateUtil.formatTomans(summary.totalSavings),
                        color = SuccessGreen
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ReportRowItem(
                        label = "مانده موجودی این هفته",
                        value = PersianDateUtil.formatTomans(summary.remainingBalance),
                        color = SecondaryAmber,
                        isBold = true
                    )
                }
            }
        }

        // Expense Category Chart for this week
        item {
            ExpenseCategoryChart(
                categoryExpenses = summary.categoryExpenses,
                title = "نمودار هزینه‌های این هفته بر اساس دسته‌بندی"
            )
        }

        // Section: All Weeks History
        item {
            Text(
                text = "تاریخچه هفتگی پول توجیبی",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "سوابق پرداخت هر هفته به تفکیک",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (allWeeks.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "هنوز سابقه‌ای برای هفته‌ها ثبت نشده است.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(
                items = allWeeks,
                key = { it.id }
            ) { week ->
                WeekHistoryCard(week = week)
            }
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@Composable
private fun WeekHistoryCard(week: AllowanceWeekEntity) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth().testTag("week_history_card_${week.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (week.isPaid) SuccessGreen.copy(alpha = 0.15f)
                            else WarningOrange.copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (week.isPaid) Icons.Default.CheckCircle else Icons.Default.HourglassEmpty,
                        contentDescription = null,
                        tint = if (week.isPaid) SuccessGreen else WarningOrange,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = "هفته ${PersianDateUtil.toPersianDigits(week.weekNumber.toString())}: ${PersianDateUtil.formatTomans(week.amount)}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = if (week.isPaid) "پرداخت شده ✓" else "در انتظار پرداخت",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (week.isPaid) SuccessGreen else WarningOrange
                    )
                }
            }

            if (week.note.isNotBlank()) {
                Text(
                    text = week.note,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MonthlyReportContent(
    monthlySummary: MonthlyFinancialSummary,
    allWeeks: List<AllowanceWeekEntity>
) {
    val totalYearAllowance = allWeeks.filter { it.isPaid }.sumOf { it.amount }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("monthly_report_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "خلاصه مالی ماه ${monthlySummary.monthName} ${PersianDateUtil.toPersianDigits(monthlySummary.year.toString())}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    ReportRowItem(
                        label = "مجموع پول توجیبی پرداخت‌شده",
                        value = PersianDateUtil.formatTomans(monthlySummary.totalAllowance),
                        color = PrimaryTeal
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ReportRowItem(
                        label = "مجموع کل هزینه‌ها",
                        value = PersianDateUtil.formatTomans(monthlySummary.totalExpenses),
                        color = Color(0xFFEF4444)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ReportRowItem(
                        label = "مجموع کل پس‌انداز",
                        value = PersianDateUtil.formatTomans(monthlySummary.totalSavings),
                        color = SuccessGreen
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ReportRowItem(
                        label = "میانگین هزینه روزانه",
                        value = PersianDateUtil.formatTomans(monthlySummary.dailyExpenseAverage),
                        color = SecondaryAmber
                    )

                    if (monthlySummary.topExpenseCategory != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        ReportRowItem(
                            label = "بیشترین دسته هزینه",
                            value = "${monthlySummary.topExpenseCategory.emoji} ${monthlySummary.topExpenseCategory.titlePersian} (${PersianDateUtil.formatTomans(monthlySummary.topExpenseAmount)})",
                            color = monthlySummary.topExpenseCategory.color,
                            isBold = true
                        )
                    }
                }
            }
        }

        // Annual Allowance total box
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFFCCFBF1))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(PrimaryTeal),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "مجموع پول توجیبی دریافتی امسال",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF0F766E)
                        )
                        Text(
                            text = PersianDateUtil.formatTomans(totalYearAllowance),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF115E59)
                        )
                    }
                }
            }
        }

        // Monthly Category Chart
        item {
            ExpenseCategoryChart(
                categoryExpenses = monthlySummary.categoryExpenses,
                title = "نمودار دسته‌بندی هزینه‌های ماه"
            )
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@Composable
private fun ReportRowItem(
    label: String,
    value: String,
    color: Color,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isBold) FontWeight.ExtraBold else FontWeight.SemiBold
            ),
            color = color
        )
    }
}
