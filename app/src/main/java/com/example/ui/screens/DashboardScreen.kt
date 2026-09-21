package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActivityLogEntity
import com.example.data.model.LeadEntity
import com.example.ui.components.CategoryDistributionChart
import com.example.ui.components.LeadCard
import com.example.ui.components.ScoreDistributionChart
import com.example.ui.components.StatCard
import com.example.ui.components.WebsiteStatusOverview
import com.example.ui.theme.CoralRed
import com.example.ui.theme.ElectricCobalt
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.TechBorder
import com.example.ui.theme.TechCardDark
import com.example.ui.theme.VividAmber
import com.example.ui.viewmodel.AppScreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    leads: List<LeadEntity>,
    highPotentialLeads: List<LeadEntity>,
    recentLogs: List<ActivityLogEntity>,
    onNavigate: (AppScreen) -> Unit,
    onSelectLead: (Long) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onGenerateOutreach: (LeadEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalLeads = leads.size
    val highPotCount = highPotentialLeads.size
    val noWebsiteCount = leads.count { it.website.isNullOrBlank() || it.websiteStatus == "NONE" }
    val convertedCount = leads.count { it.status.equals("CONVERTED", ignoreCase = true) || it.status.equals("CONTACTED", ignoreCase = true) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Action Banner: Start New Lead Prospecting
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        androidx.compose.ui.graphics.Brush.horizontalGradient(
                            listOf(ElectricCobalt, Color(0xFF1E3A8A))
                        )
                    )
                    .padding(18.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Find High-Value Local Clients",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Extract Google Maps businesses, audit missing websites, and craft AI-tailored proposals in seconds.",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { onNavigate(AppScreen.EXTRACT_LEADS) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("extract_leads_banner_button")
                    ) {
                        Text(
                            text = "Start Lead Discovery",
                            color = ElectricCobalt,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = ElectricCobalt,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Metrics Grid (2x2)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard(
                        title = "TOTAL LEADS",
                        value = "$totalLeads",
                        subtitle = "Prospects in database",
                        icon = Icons.Default.People,
                        iconTint = ElectricCobalt,
                        onClick = { onNavigate(AppScreen.ALL_LEADS) },
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "HIGH POTENTIAL",
                        value = "$highPotCount",
                        subtitle = "Scores >= 80/100",
                        icon = Icons.Default.Star,
                        iconTint = EmeraldGreen,
                        onClick = { onNavigate(AppScreen.HIGH_POTENTIAL) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard(
                        title = "NO WEBSITE",
                        value = "$noWebsiteCount",
                        subtitle = "Prime web design targets",
                        icon = Icons.Default.Language,
                        iconTint = CoralRed,
                        onClick = { onNavigate(AppScreen.ALL_LEADS) },
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "ACTIVE OUTREACH",
                        value = "$convertedCount",
                        subtitle = "Contacted / Converted",
                        icon = Icons.Default.CheckCircle,
                        iconTint = VividAmber,
                        onClick = { onNavigate(AppScreen.ALL_LEADS) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Visual Data Analytics Section
        item {
            ScoreDistributionChart(leads = leads)
        }

        item {
            WebsiteStatusOverview(leads = leads)
        }

        item {
            CategoryDistributionChart(leads = leads)
        }

        // High Potential Leads Spotlight Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Top High-Potential Prospects",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Text(
                    text = "View All (${highPotentialLeads.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ElectricCyan,
                    modifier = Modifier.clickable { onNavigate(AppScreen.HIGH_POTENTIAL) }
                )
            }
        }

        // Spotlight Leads
        val topSpotlight = highPotentialLeads.take(3)
        if (topSpotlight.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(TechCardDark)
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No high-potential leads yet. Start by extracting local businesses!",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(topSpotlight, key = { it.id }) { lead ->
                LeadCard(
                    lead = lead,
                    isSelected = false,
                    onToggleSelect = { onSelectLead(lead.id) },
                    onClick = { onSelectLead(lead.id) },
                    onToggleFavorite = { onToggleFavorite(lead.id) },
                    onGenerateOutreach = { onGenerateOutreach(lead) }
                )
            }
        }

        // Recent System Activity Log
        item {
            Text(
                text = "Recent Prospecting Activity",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        }

        val displayLogs = recentLogs.take(5)
        if (displayLogs.isEmpty()) {
            item {
                Text(
                    text = "No recent operations recorded.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(displayLogs, key = { it.id }) { log ->
                val timeFormatted = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(log.createdAt))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(TechCardDark)
                        .border(1.dp, TechBorder, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(ElectricCyan)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = log.details,
                                fontSize = 12.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = timeFormatted,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
