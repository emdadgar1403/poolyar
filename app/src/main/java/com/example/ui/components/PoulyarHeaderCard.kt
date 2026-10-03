package com.example.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DashboardState
import com.example.util.PersianDateUtil

@Composable
fun PoulyarHeaderCard(
    state: DashboardState,
    modifier: Modifier = Modifier
) {
    val persianToday = PersianDateUtil.getPersianDate()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("dashboard_header_card"),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF0F766E),
                            Color(0xFF115E59),
                            Color(0xFF064E3B)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                // Teen Greeting Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "سلام ${state.teenName} عزیز! 👋",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                ),
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = persianToday.formatReadable(),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFCCFBF1)
                        )
                    }

                    // Iranian Poulyar Logo Badge
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF14B8A6).copy(alpha = 0.35f))
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.res.painterResource(
                                id = com.example.R.drawable.poulyar_iranian_logo_1790997755553
                            ),
                            contentDescription = "نشان پولیار نوجوان",
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Available Balance (موجودی قابل خرج)
                Text(
                    text = "موجودی قابل خرج من",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFFA7F3D0)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = PersianDateUtil.formatTomans(state.currentBalance),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 28.sp
                    ),
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Secondary metrics: Total savings & Total wealth
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.12f))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Savings
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Savings,
                            contentDescription = null,
                            tint = Color(0xFFFDE68A),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "قلک پس‌انداز",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFE2E8F0)
                            )
                            Text(
                                text = PersianDateUtil.formatTomans(state.totalSavings),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White
                            )
                        }
                    }

                    // Divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(28.dp)
                            .background(Color.White.copy(alpha = 0.25f))
                    )

                    // Total Balance
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = Color(0xFFA7F3D0),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "مجموع دارایی",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFE2E8F0)
                            )
                            Text(
                                text = PersianDateUtil.formatTomans(state.totalBalance),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
