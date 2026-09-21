package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LeadEntity
import com.example.ui.theme.CoralRed
import com.example.ui.theme.ElectricCobalt
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.TechBorder
import com.example.ui.theme.TechCardDark
import com.example.ui.theme.VividAmber

@Composable
fun CategoryDistributionChart(leads: List<LeadEntity>, modifier: Modifier = Modifier) {
    val categoryCounts = leads.groupBy { it.category }
        .mapValues { it.value.size }
        .toList()
        .sortedByDescending { it.second }
        .take(5)

    val maxCount = categoryCounts.maxOfOrNull { it.second } ?: 1

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(TechCardDark)
            .border(1.dp, TechBorder, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = "Leads by Category",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (categoryCounts.isEmpty()) {
                Text(
                    "No leads recorded yet.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                categoryCounts.forEachIndexed { index, (category, count) ->
                    val progress = (count.toFloat() / maxCount).coerceIn(0.05f, 1f)
                    val color = when (index % 4) {
                        0 -> ElectricCobalt
                        1 -> ElectricCyan
                        2 -> VividAmber
                        else -> EmeraldGreen
                    }

                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = category,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$count leads",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(TechBorder)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progress)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(color)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScoreDistributionChart(leads: List<LeadEntity>, modifier: Modifier = Modifier) {
    val high = leads.count { it.leadScore >= 80 }
    val medium = leads.count { it.leadScore in 60..79 }
    val low = leads.count { it.leadScore < 60 }
    val total = (high + medium + low).coerceAtLeast(1)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(TechCardDark)
            .border(1.dp, TechBorder, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = "Lead Score Distribution",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Spacer(modifier = Modifier.height(14.dp))

            // Multi-segment horizontal bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(TechBorder)
            ) {
                if (high > 0) {
                    Box(
                        modifier = Modifier
                            .weight(high.toFloat() / total)
                            .fillMaxHeight()
                            .background(EmeraldGreen)
                    )
                }
                if (medium > 0) {
                    Box(
                        modifier = Modifier
                            .weight(medium.toFloat() / total)
                            .fillMaxHeight()
                            .background(VividAmber)
                    )
                }
                if (low > 0) {
                    Box(
                        modifier = Modifier
                            .weight(low.toFloat() / total)
                            .fillMaxHeight()
                            .background(Color(0xFF64748B))
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LegendItem(label = "High (80-100)", count = high, color = EmeraldGreen)
                LegendItem(label = "Medium (60-79)", count = medium, color = VividAmber)
                LegendItem(label = "Low (0-59)", count = low, color = Color(0xFF64748B))
            }
        }
    }
}

@Composable
fun WebsiteStatusOverview(leads: List<LeadEntity>, modifier: Modifier = Modifier) {
    val noWeb = leads.count { it.website.isNullOrBlank() || it.websiteStatus == "NONE" }
    val poorWeb = leads.count { !it.website.isNullOrBlank() && it.websiteQuality.equals("POOR", ignoreCase = true) }
    val avgWeb = leads.count { !it.website.isNullOrBlank() && it.websiteQuality.equals("AVERAGE", ignoreCase = true) }
    val goodWeb = leads.count { !it.website.isNullOrBlank() && it.websiteQuality.equals("GOOD", ignoreCase = true) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(TechCardDark)
            .border(1.dp, TechBorder, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = "Website Availability & Quality",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                WebStatusMetric(label = "No Website", count = noWeb, color = CoralRed, modifier = Modifier.weight(1f))
                WebStatusMetric(label = "Poor Quality", count = poorWeb, color = VividAmber, modifier = Modifier.weight(1f))
                WebStatusMetric(label = "Average", count = avgWeb, color = ElectricCobalt, modifier = Modifier.weight(1f))
                WebStatusMetric(label = "Good", count = goodWeb, color = EmeraldGreen, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun WebStatusMetric(label: String, count: Int, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$count",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = color
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LegendItem(label: String, count: Int, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = "$label: $count",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
