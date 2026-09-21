package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.data.model.LeadEntity
import com.example.ui.components.LeadCard
import com.example.ui.theme.CoralRed
import com.example.ui.theme.ElectricCobalt
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.TechBorder
import com.example.ui.theme.TechCardDark

@Composable
fun AllLeadsScreen(
    leads: List<LeadEntity>,
    selectedLeadIds: Set<Long>,
    selectedCategoryFilter: String?,
    selectedStatusFilter: String?,
    selectedWebsiteFilter: String?,
    onSetCategoryFilter: (String?) -> Unit,
    onSetStatusFilter: (String?) -> Unit,
    onSetWebsiteFilter: (String?) -> Unit,
    onToggleSelect: (Long) -> Unit,
    onSelectAll: (Boolean) -> Unit,
    onDeleteSelected: () -> Unit,
    onExportSelected: () -> Unit,
    onSelectLead: (Long) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onGenerateOutreach: (LeadEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf("ALL") + leads.map { it.category }.distinct()
    val websiteFilters = listOf("ALL", "NO_WEBSITE", "POOR", "AVERAGE", "GOOD")
    val statusFilters = listOf("ALL", "NEW", "CONTACTED", "INTERESTED", "FOLLOW_UP", "CONVERTED")

    val isAllSelected = leads.isNotEmpty() && selectedLeadIds.size == leads.size

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Screen Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Prospect Directory",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Text(
                    text = "${leads.size} leads found matching criteria",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Horizontal Filter Chips Row: Website status
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            websiteFilters.forEach { filter ->
                val isSelected = (filter == "ALL" && selectedWebsiteFilter == null) || (filter == selectedWebsiteFilter)
                val label = when (filter) {
                    "NO_WEBSITE" -> "No Website"
                    "POOR" -> "Poor Quality"
                    "AVERAGE" -> "Average"
                    "GOOD" -> "Good"
                    else -> "All Websites"
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) ElectricCobalt else TechCardDark)
                        .border(1.dp, if (isSelected) ElectricCyan else TechBorder, RoundedCornerShape(16.dp))
                        .clickable { onSetWebsiteFilter(if (filter == "ALL") null else filter) }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Bulk Selection Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(TechCardDark)
                .border(1.dp, TechBorder, RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isAllSelected,
                        onCheckedChange = { onSelectAll(it) },
                        colors = CheckboxDefaults.colors(checkedColor = ElectricCobalt),
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("select_all_checkbox")
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (selectedLeadIds.isEmpty()) "Select All" else "${selectedLeadIds.size} Selected",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                if (selectedLeadIds.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(
                            onClick = onExportSelected,
                            modifier = Modifier.height(30.dp)
                        ) {
                            Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = onDeleteSelected,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralRed),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Delete", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Leads Lazy List
        if (leads.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No leads found",
                        style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Try clearing filters or discover new businesses from the Extract tab.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(leads, key = { it.id }) { lead ->
                    val isSelected = selectedLeadIds.contains(lead.id)
                    LeadCard(
                        lead = lead,
                        isSelected = isSelected,
                        onToggleSelect = { onToggleSelect(lead.id) },
                        onClick = { onSelectLead(lead.id) },
                        onToggleFavorite = { onToggleFavorite(lead.id) },
                        onGenerateOutreach = { onGenerateOutreach(lead) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
