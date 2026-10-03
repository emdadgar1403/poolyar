package com.example.util

import java.text.DecimalFormat
import java.util.Calendar
import java.util.Date

/**
 * Persian (Jalali / Solar Hijri) date and number utilities.
 * Handles Iran standard calendar arithmetic, week calculation (Saturday to Friday),
 * Persian digit localization and Tomans currency formatting.
 */
object PersianDateUtil {

    private val PERSIAN_DIGITS = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

    val MONTH_NAMES = listOf(
        "فروردین", "اردیبهشت", "خرداد",
        "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر",
        "دی", "بهمن", "اسفند"
    )

    val WEEKDAY_NAMES = listOf(
        "شنبه", "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنج‌شنبه", "جمعه"
    )

    data class PersianDate(
        val year: Int,
        val month: Int,
        val day: Int,
        val dayOfWeek: String,
        val monthName: String
    ) {
        fun formatShort(): String {
            val m = if (month < 10) "۰$month" else toPersianDigits(month.toString())
            val d = if (day < 10) "۰$day" else toPersianDigits(day.toString())
            val y = toPersianDigits(year.toString())
            return "$y/$m/$d"
        }

        fun formatReadable(): String {
            return "$dayOfWeek، ${toPersianDigits(day.toString())} $monthName ${toPersianDigits(year.toString())}"
        }
    }

    /**
     * Converts English digits 0-9 to Persian digits ۰-۹
     */
    fun toPersianDigits(text: String): String {
        val sb = StringBuilder()
        for (ch in text) {
            if (ch in '0'..'9') {
                sb.append(PERSIAN_DIGITS[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    /**
     * Formats a number with thousands separators and Persian digits.
     * Example: 350000 -> ۳۵۰٬۰۰۰
     */
    fun formatNumber(number: Long): String {
        val df = DecimalFormat("#,###")
        val formatted = df.format(number).replace(',', '٬')
        return toPersianDigits(formatted)
    }

    /**
     * Formats amount in Tomans.
     * Example: 350000 -> ۳۵۰٬۰۰۰ تومان
     */
    fun formatTomans(amount: Long): String {
        return "${formatNumber(amount)} تومان"
    }

    /**
     * Converts a Gregorian calendar/timestamp to Persian Jalali Date.
     */
    fun getPersianDate(timestamp: Long = System.currentTimeMillis()): PersianDate {
        val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
        val gYear = calendar.get(Calendar.YEAR)
        val gMonth = calendar.get(Calendar.MONTH) + 1
        val gDay = calendar.get(Calendar.DAY_OF_MONTH)
        val calDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        // Mapping Calendar.DAY_OF_WEEK (SUNDAY=1, MONDAY=2, ... SATURDAY=7)
        // In Persian: Saturday is first day
        val dayOfWeekName = when (calDayOfWeek) {
            Calendar.SATURDAY -> "شنبه"
            Calendar.SUNDAY -> "یکشنبه"
            Calendar.MONDAY -> "دوشنبه"
            Calendar.TUESDAY -> "سه‌شنبه"
            Calendar.WEDNESDAY -> "چهارشنبه"
            Calendar.THURSDAY -> "پنج‌شنبه"
            Calendar.FRIDAY -> "جمعه"
            else -> "شنبه"
        }

        val (jYear, jMonth, jDay) = gregorianToJalali(gYear, gMonth, gDay)
        val monthName = MONTH_NAMES.getOrElse(jMonth - 1) { "" }

        return PersianDate(jYear, jMonth, jDay, dayOfWeekName, monthName)
    }

    /**
     * Calculate Persian week of year (1 to 52).
     */
    fun getPersianWeekNumber(timestamp: Long = System.currentTimeMillis()): Int {
        val pDate = getPersianDate(timestamp)
        var daysPast = 0
        for (m in 1 until pDate.month) {
            daysPast += if (m <= 6) 31 else 30
        }
        daysPast += pDate.day
        val week = (daysPast - 1) / 7 + 1
        return week.coerceIn(1, 52)
    }

    /**
     * Formats current or given week label (e.g. «هفته سوم - مهر ۱۴۰۵»)
     */
    fun formatWeekLabel(weekNumber: Int, year: Int): String {
        val persianNum = toPersianDigits(weekNumber.toString())
        return "هفته $persianNum سال ${toPersianDigits(year.toString())}"
    }

    /**
     * Pure Kotlin standard Gregorian to Jalali converter algorithm.
     */
    private fun gregorianToJalali(gy: Int, gm: Int, gd: Int): Triple<Int, Int, Int> {
        val gDaysInMonth = intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        val jDaysInMonth = intArrayOf(31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29)

        var gy2 = gy - 1600
        var gm2 = gm - 1
        val gd2 = gd - 1

        var gDayNo = 365 * gy2 + (gy2 + 3) / 4 - (gy2 + 99) / 100 + (gy2 + 399) / 400

        for (i in 0 until gm2) {
            gDayNo += gDaysInMonth[i]
        }
        if (gm2 > 1 && ((gy2 % 4 == 0 && gy2 % 100 != 0) || (gy2 % 400 == 0))) {
            gDayNo++
        }
        gDayNo += gd2

        var jDayNo = gDayNo - 79
        val jNp = jDayNo / 12053
        jDayNo %= 12053

        var jy = 979 + 33 * jNp + 4 * (jDayNo / 1461)
        jDayNo %= 1461

        if (jDayNo >= 366) {
            jy += (jDayNo - 1) / 365
            jDayNo = (jDayNo - 1) % 365
        }

        var jm = 0
        for (i in 0..11) {
            if (jDayNo < jDaysInMonth[i]) {
                jm = i + 1
                break
            }
            jDayNo -= jDaysInMonth[i]
        }
        val jd = jDayNo + 1

        return Triple(jy, jm, jd)
    }
}
