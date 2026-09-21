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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LeadEntity
import com.example.ui.theme.ElectricCobalt
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.TechBorder
import com.example.ui.theme.TechCardDark

@Composable
fun ExportsScreen(
    allLeads: List<LeadEntity>,
    selectedLeadIds: Set<Long>,
    onExport: (format: String, scope: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFormat by remember { mutableStateOf("CSV") }
    var selectedScope by remember { mutableStateOf("ALL") }

    val formats = listOf("CSV", "JSON")
    val scopes = listOf(
        Pair("ALL", "All Leads (${allLeads.size})"),
        Pair("HIGH_POTENTIAL", "High Potential 80+ (${allLeads.count { it.leadScore >= 80 }})"),
        Pair("NO_WEBSITE", "No Website Only (${allLeads.count { it.website.isNullOrBlank() || it.websiteStatus == "NONE" }})"),
        Pair("SELECTED", "Selected Leads (${selectedLeadIds.size})")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Data Export & Sharing",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        }
        Text(
            text = "Export verified business leads into spreadsheets or JSON format for your CRM or outreach tools.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(TechCardDark)
                .border(1.dp, TechBorder, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Format Selector
                Column {
                    Text(
                        text = "EXPORT FORMAT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        formats.forEach { fmt ->
                            val isSelected = selectedFormat == fmt
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) ElectricCobalt else TechBorder.copy(alpha = 0.3f))
                                    .border(1.dp, if (isSelected) ElectricCyan else TechBorder, RoundedCornerShape(8.dp))
                                    .clickable { selectedFormat = fmt }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (fmt == "CSV") "CSV (Excel / Sheets)" else "JSON (Developer)",
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // Scope Selector
                Column {
                    Text(
                        text = "LEADS DATA SCOPE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    scopes.forEach { (scopeKey, scopeLabel) ->
                        val isSelected = selectedScope == scopeKey
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) ElectricCobalt.copy(alpha = 0.2f) else TechBorder.copy(alpha = 0.2f))
                                .border(1.dp, if (isSelected) ElectricCyan else TechBorder, RoundedCornerShape(8.dp))
                                .clickable { selectedScope = scopeKey }
                                .padding(12.dp)
                        ) {
                            Text(
                                text = scopeLabel,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Export Button
                Button(
                    onClick = { onExport(selectedFormat, selectedScope) },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCobalt),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("export_leads_button")
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export & Share Leads", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Included Schema Details
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(TechCardDark)
                .border(1.dp, TechBorder, RoundedCornerShape(10.dp))
                .padding(14.dp)
        ) {
            Column {
                Text(
                    text = "INCLUDED EXPORT COLUMNS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElectricCyan
                )
                Spacer(modifier = Modifier.height(6.dp))
                val cols = "Business Name • Category • Phone • Email • Website • Address • City • Rating • Review Count • Lead Score (0-100) • Priority • Website Quality • Mobile Friendly • AI Summary • Recommended Pitch Services • Status"
                Text(
                    text = cols,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
