package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.UserSettingsEntity
import com.example.ui.components.dialogs.ParentPinDialog
import com.example.ui.theme.PrimaryTeal
import com.example.ui.theme.SecondaryAmber
import com.example.util.PersianDateUtil

@Composable
fun SettingsScreen(
    settings: UserSettingsEntity,
    onSaveSettings: (UserSettingsEntity) -> Unit,
    onSendTestNotification: () -> Boolean,
    onResetSampleData: () -> Unit,
    onClearAllData: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isParentUnlocked by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }

    var teenName by remember(settings) { mutableStateOf(settings.teenName) }
    var dailyAllowanceText by remember(settings) { mutableStateOf(settings.dailyAllowance.toString()) }
    var weeklyAllowanceText by remember(settings) { mutableStateOf(settings.weeklyAllowance.toString()) }
    var payoutDay by remember(settings) { mutableStateOf(settings.payoutDay) }
    var currency by remember(settings) { mutableStateOf(settings.currency) }
    var weeklyLimitText by remember(settings) { mutableStateOf(settings.weeklyExpenseLimit.toString()) }
    var savingsPercentText by remember(settings) { mutableStateOf(settings.suggestedSavingsPercent.toString()) }
    var notificationsEnabled by remember(settings) { mutableStateOf(settings.notificationsEnabled) }

    var newPinText by remember { mutableStateOf("") }
    var showChangePinSection by remember { mutableStateOf(false) }

    if (showPinDialog) {
        ParentPinDialog(
            correctPin = settings.parentPin,
            onDismiss = { showPinDialog = false },
            onSuccess = {
                isParentUnlocked = true
                Toast.makeText(context, "بخش والدین باز شد", Toast.LENGTH_SHORT).show()
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("settings_screen")
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Title
        Text(
            text = "تنظیمات والدین و برنامه ⚙️",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "مدیریت مبالغ پول توجیبی، سقف بودجه و امنیت",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Parent Lock Status Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isParentUnlocked) Color(0xFFF0FDF4) else Color(0xFFFFFBEB)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth().testTag("parent_lock_status_card")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (isParentUnlocked) PrimaryTeal else SecondaryAmber),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isParentUnlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = if (isParentUnlocked) "بخش والدین باز است" else "بخش والدین قفل است 🔒",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isParentUnlocked) "می‌توانید مقادیر را تغییر دهید" else "برای ویرایش ابتدا رمز را وارد کنید",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (!isParentUnlocked) {
                    Button(
                        onClick = { showPinDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SecondaryAmber),
                        modifier = Modifier.testTag("unlock_parent_settings_button")
                    ) {
                        Text("ورود با PIN", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                } else {
                    OutlinedButton(
                        onClick = { isParentUnlocked = false },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("قفل کردن")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Settings Fields
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "مشخصات و پول توجیبی",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Teen Name
                OutlinedTextField(
                    value = teenName,
                    onValueChange = { if (isParentUnlocked) teenName = it },
                    label = { Text("نام نوجوان") },
                    enabled = isParentUnlocked,
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("teen_name_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Daily Allowance
                OutlinedTextField(
                    value = dailyAllowanceText,
                    onValueChange = { input ->
                        if (isParentUnlocked) dailyAllowanceText = input.filter { it.isDigit() }
                    },
                    label = { Text("مبلغ پول توجیبی روزانه (تومان)") },
                    enabled = isParentUnlocked,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("daily_allowance_input"),
                    supportingText = {
                        val parsed = dailyAllowanceText.toLongOrNull() ?: 0L
                        Text(PersianDateUtil.formatTomans(parsed))
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Weekly Allowance
                OutlinedTextField(
                    value = weeklyAllowanceText,
                    onValueChange = { input ->
                        if (isParentUnlocked) weeklyAllowanceText = input.filter { it.isDigit() }
                    },
                    label = { Text("مبلغ پول توجیبی هفتگی (تومان)") },
                    enabled = isParentUnlocked,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("weekly_allowance_input"),
                    supportingText = {
                        val parsed = weeklyAllowanceText.toLongOrNull() ?: 0L
                        Text(PersianDateUtil.formatTomans(parsed))
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Payout Day
                OutlinedTextField(
                    value = payoutDay,
                    onValueChange = { if (isParentUnlocked) payoutDay = it },
                    label = { Text("روز پرداخت پول توجیبی (مثلاً: شنبه)") },
                    enabled = isParentUnlocked,
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Currency
                OutlinedTextField(
                    value = currency,
                    onValueChange = { if (isParentUnlocked) currency = it },
                    label = { Text("واحد پول") },
                    enabled = isParentUnlocked,
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Weekly Expense Limit
                OutlinedTextField(
                    value = weeklyLimitText,
                    onValueChange = { input ->
                        if (isParentUnlocked) weeklyLimitText = input.filter { it.isDigit() }
                    },
                    label = { Text("سقف هزینه هفتگی (هشدار بودجه - تومان)") },
                    enabled = isParentUnlocked,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("weekly_limit_input"),
                    supportingText = {
                        val parsed = weeklyLimitText.toLongOrNull() ?: 0L
                        Text(PersianDateUtil.formatTomans(parsed))
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Suggested Savings Percent
                OutlinedTextField(
                    value = savingsPercentText,
                    onValueChange = { input ->
                        if (isParentUnlocked) savingsPercentText = input.filter { it.isDigit() }
                    },
                    label = { Text("درصد پیشنهادی پس‌انداز (٪)") },
                    enabled = isParentUnlocked,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("savings_percent_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Save Button
                if (isParentUnlocked) {
                    Button(
                        onClick = {
                            val daily = dailyAllowanceText.toLongOrNull() ?: 50_000L
                            val weekly = weeklyAllowanceText.toLongOrNull() ?: 350_000L
                            val limit = weeklyLimitText.toLongOrNull() ?: 300_000L
                            val percent = (savingsPercentText.toIntOrNull() ?: 20).coerceIn(0, 100)

                            onSaveSettings(
                                settings.copy(
                                    teenName = teenName.trim(),
                                    dailyAllowance = daily,
                                    weeklyAllowance = weekly,
                                    payoutDay = payoutDay.trim(),
                                    currency = currency.trim(),
                                    weeklyExpenseLimit = limit,
                                    suggestedSavingsPercent = percent,
                                    notificationsEnabled = notificationsEnabled
                                )
                            )
                            Toast.makeText(context, "تنظیمات با موفقیت ذخیره شد ✓", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                        modifier = Modifier.fillMaxWidth().testTag("save_settings_button")
                    ) {
                        Text("ذخیره تغییرات", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Notifications & Reminders Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = PrimaryTeal,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "اعلان یادآوری پول توجیبی",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = {
                            if (isParentUnlocked) {
                                notificationsEnabled = it
                                onSaveSettings(settings.copy(notificationsEnabled = it))
                            } else {
                                Toast.makeText(context, "ابتدا بخش والدین را باز کنید", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.testTag("notification_switch")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "ارسال اعلان محلی در روز تعیین‌شده: «وقت دریافت پول توجیبی این هفته رسیده است.»",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        val sent = onSendTestNotification()
                        if (sent) {
                            Toast.makeText(context, "اعلان آزمایشی ارسال شد 🔔", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "لطفاً مجوز اعلان را در دستگاه فعال کنید", Toast.LENGTH_LONG).show()
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("send_test_notification_button")
                ) {
                    Text("ارسال یک اعلان آزمایشی به نوجوان 🔔")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Change Parent PIN Card
        if (isParentUnlocked) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = SecondaryAmber,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تغییر رمز عبور والدین",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = newPinText,
                        onValueChange = { if (it.length <= 4 && it.all { ch -> ch.isDigit() }) newPinText = it },
                        label = { Text("رمز ۴ رقمی جدید") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().testTag("change_pin_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            if (newPinText.length == 4) {
                                onSaveSettings(settings.copy(parentPin = newPinText))
                                newPinText = ""
                                Toast.makeText(context, "رمز عبور با موفقیت تغییر یافت ✓", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "رمز باید دقیقاً ۴ رقم باشد", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SecondaryAmber),
                        modifier = Modifier.fillMaxWidth().testTag("save_pin_button")
                    ) {
                        Text("ثبت رمز عبور جدید", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Reset / Reload Sample Data
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "مدیریت داده‌های برنامه",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "می‌توانید داده‌های نمونه اولیه را دوباره بارگذاری کنید یا تمام اطلاعات را پاک نمایید.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                onResetSampleData()
                                Toast.makeText(context, "داده‌های نمونه بارگذاری شدند", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).testTag("reset_sample_data_button")
                        ) {
                            Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("داده‌های نمونه")
                        }

                        Button(
                            onClick = {
                                onClearAllData()
                                Toast.makeText(context, "تمام داده‌ها پاک شدند", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            modifier = Modifier.weight(1f).testTag("clear_all_data_button")
                        ) {
                            Text("پاکسازی کل داده‌ها", color = Color.White)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}
