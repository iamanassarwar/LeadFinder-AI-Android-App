package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.SearchHistoryEntity
import com.example.service.ExtractionProgress
import com.example.ui.theme.CoralRed
import com.example.ui.theme.ElectricCobalt
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.TechBorder
import com.example.ui.theme.TechCardDark
import com.example.ui.theme.VividAmber

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExtractLeadsScreen(
    progress: ExtractionProgress,
    recentSearches: List<SearchHistoryEntity>,
    onStartExtraction: (category: String, location: String, radiusKm: Int, limit: Int, noWebOnly: Boolean, phoneOnly: Boolean) -> Unit,
    onStopExtraction: () -> Unit,
    modifier: Modifier = Modifier
) {
    var category by remember { mutableStateOf("Restaurants") }
    var location by remember { mutableStateOf("Austin, TX") }
    var radiusKm by remember { mutableFloatStateOf(10f) }
    var resultLimit by remember { mutableIntStateOf(10) }
    var requireNoWebsite by remember { mutableStateOf(true) }
    var requirePhone by remember { mutableStateOf(false) }

    val popularCategories = listOf(
        "Restaurants", "Dentists", "Plumbers", "Auto Repair", "Gyms", "Spas & Salons", "Roofers", "Lawyers"
    )

    val popularLocations = listOf(
        "Austin, TX", "Miami, FL", "Denver, CO", "Chicago, IL", "Seattle, WA", "Toronto, ON"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Title and description
        Text(
            text = "Lead Prospecting Discovery",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )
        Text(
            text = "Target high-intent local businesses by location and category to discover web design sales opportunities.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Live Extraction Monitor (if running or completed)
        if (progress.isRunning || progress.stage.isNotBlank() || progress.error != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(TechCardDark)
                    .border(
                        1.dp,
                        if (progress.isRunning) ElectricCyan else if (progress.error != null) CoralRed else EmeraldGreen,
                        RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (progress.isRunning) {
                                CircularProgressIndicator(
                                    color = ElectricCyan,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else if (progress.error != null) {
                                Icon(imageVector = Icons.Default.Stop, contentDescription = null, tint = CoralRed, modifier = Modifier.size(18.dp))
                            } else {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (progress.isRunning) "Extracting Leads Live..." else if (progress.error != null) "Search Error" else "Extraction Completed",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        }

                        if (progress.isRunning) {
                            OutlinedButton(
                                onClick = onStopExtraction,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralRed),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("Stop", fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = progress.error ?: progress.stage,
                        fontSize = 12.sp,
                        color = if (progress.error != null) CoralRed else MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (progress.isRunning && progress.total > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        val frac = (progress.current.toFloat() / progress.total).coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { frac },
                            color = ElectricCyan,
                            trackColor = TechBorder,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("Found: ${progress.foundCount}", fontSize = 11.sp, color = ElectricCyan, fontWeight = FontWeight.Bold)
                        Text("Saved: ${progress.savedCount}", fontSize = 11.sp, color = EmeraldGreen, fontWeight = FontWeight.Bold)
                        Text("Duplicates: ${progress.duplicateCount}", fontSize = 11.sp, color = VividAmber, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Form Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(TechCardDark)
                .border(1.dp, TechBorder, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Category Input
                Column {
                    Text(
                        text = "BUSINESS CATEGORY / NICHE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Category, contentDescription = null, tint = ElectricCobalt)
                        },
                        placeholder = { Text("e.g. Restaurants, Dentists, Plumbers") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("category_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricCobalt,
                            unfocusedBorderColor = TechBorder
                        )
                    )

                    // Popular category chips
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        popularCategories.forEach { cat ->
                            val isSelected = category.equals(cat, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isSelected) ElectricCobalt else TechBorder.copy(alpha = 0.4f))
                                    .clickable { category = cat }
                                    .padding(horizontal = 9.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = cat,
                                    fontSize = 11.sp,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                // Location Input
                Column {
                    Text(
                        text = "TARGET LOCATION / CITY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = VividAmber)
                        },
                        placeholder = { Text("e.g. Austin, TX or Chicago, IL") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("location_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricCobalt,
                            unfocusedBorderColor = TechBorder
                        )
                    )

                    // Popular location chips
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        popularLocations.forEach { loc ->
                            val isSelected = location.equals(loc, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isSelected) ElectricCobalt else TechBorder.copy(alpha = 0.4f))
                                    .clickable { location = loc }
                                    .padding(horizontal = 9.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = loc,
                                    fontSize = 11.sp,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                // Radius Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "SEARCH RADIUS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${radiusKm.toInt()} km",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan
                        )
                    }
                    Slider(
                        value = radiusKm,
                        onValueChange = { radiusKm = it },
                        valueRange = 1f..50f,
                        colors = SliderDefaults.colors(
                            thumbColor = ElectricCyan,
                            activeTrackColor = ElectricCobalt,
                            inactiveTrackColor = TechBorder
                        )
                    )
                }

                // Result Limit Selector
                Column {
                    Text(
                        text = "MAX RESULTS TO EXTRACT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(5, 10, 20, 50).forEach { limitVal ->
                            val isSelected = resultLimit == limitVal
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) ElectricCobalt else TechBorder.copy(alpha = 0.3f))
                                    .border(1.dp, if (isSelected) ElectricCyan else TechBorder, RoundedCornerShape(8.dp))
                                    .clickable { resultLimit = limitVal }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$limitVal leads",
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // Filter Toggles
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = requireNoWebsite,
                            onCheckedChange = { requireNoWebsite = it },
                            colors = CheckboxDefaults.colors(checkedColor = ElectricCobalt)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "Prioritize businesses with NO website",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Text(
                                text = "Best targets for full turnkey web development",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = requirePhone,
                            onCheckedChange = { requirePhone = it },
                            colors = CheckboxDefaults.colors(checkedColor = ElectricCobalt)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "Must have verified telephone number",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Text(
                                text = "Enables immediate cold call or WhatsApp sales prospecting",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Submit Discovery Action
                Button(
                    onClick = {
                        onStartExtraction(category, location, radiusKm.toInt(), resultLimit, requireNoWebsite, requirePhone)
                    },
                    enabled = !progress.isRunning && category.isNotBlank() && location.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCobalt),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("start_extraction_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Discover & Analyze Leads",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Recent Searches
        if (recentSearches.isNotEmpty()) {
            Spacer(modifier = Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.History, contentDescription = null, tint = VividAmber, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Recent Searches",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            recentSearches.take(5).forEach { search ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(TechCardDark)
                        .border(1.dp, TechBorder, RoundedCornerShape(8.dp))
                        .clickable {
                            category = search.keyword
                            location = search.location
                            radiusKm = search.radiusKm.toFloat()
                        }
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${search.keyword} in ${search.location}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Radius: ${search.radiusKm} km • ${search.resultCount} results found",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "Rerun",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
