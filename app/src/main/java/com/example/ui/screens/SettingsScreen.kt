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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.AppSettings
import com.example.service.GeminiService
import com.example.ui.theme.CoralRed
import com.example.ui.theme.ElectricCobalt
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.TechBorder
import com.example.ui.theme.TechCardDark
import com.example.ui.theme.VividAmber
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    settings: AppSettings,
    geminiService: GeminiService,
    onUpdateSettings: (geminiKey: String?, model: String?, mapsKey: String?, radius: Int?, limit: Int?, threshold: Int?, demo: Boolean?) -> Unit,
    onClearData: () -> Unit,
    onResetDemoData: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    var geminiKey by remember(settings.geminiApiKey) { mutableStateOf(settings.geminiApiKey) }
    var selectedModel by remember(settings.geminiModel) { mutableStateOf(settings.geminiModel) }
    var mapsKey by remember(settings.googleMapsApiKey) { mutableStateOf(settings.googleMapsApiKey) }
    var showGeminiKey by remember { mutableStateOf(false) }

    var testStatus by remember { mutableStateOf<String?>(null) }
    var isTestingConnection by remember { mutableStateOf(false) }

    val models = listOf(
        Pair("gemini-2.5-flash", "Gemini 2.5 Flash (Recommended - Fast & Smart)"),
        Pair("gemini-3.1-pro-preview", "Gemini 3.1 Pro (Deep Complex Reasoning)")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Title Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Platform Settings & API",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        }
        Text(
            text = "Configure your AI models, API keys, prospecting parameters, and local data storage.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Gemini AI Configuration Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(TechCardDark)
                .border(1.dp, TechBorder, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "GEMINI AI ENGINE",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }

                // API Key Field
                OutlinedTextField(
                    value = geminiKey,
                    onValueChange = {
                        geminiKey = it
                        onUpdateSettings(it, null, null, null, null, null, null)
                    },
                    label = { Text("Gemini API Key", fontSize = 12.sp) },
                    placeholder = { Text("AIzaSy...") },
                    singleLine = true,
                    visualTransformation = if (showGeminiKey) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showGeminiKey = !showGeminiKey }) {
                            Icon(
                                imageVector = if (showGeminiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle key visibility"
                            )
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCobalt,
                        unfocusedBorderColor = TechBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("gemini_api_key_input")
                )

                // Model Selector
                Text(
                    text = "AI MODEL SELECTION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                models.forEach { (modelKey, modelLabel) ->
                    val isSelected = selectedModel == modelKey
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) ElectricCobalt.copy(alpha = 0.2f) else TechBorder.copy(alpha = 0.2f))
                            .border(1.dp, if (isSelected) ElectricCyan else TechBorder, RoundedCornerShape(8.dp))
                            .clickable {
                                selectedModel = modelKey
                                onUpdateSettings(null, modelKey, null, null, null, null, null)
                            }
                            .padding(10.dp)
                    ) {
                        Text(
                            text = modelLabel,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Test Connection Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                isTestingConnection = true
                                testStatus = null
                                val result = geminiService.testConnection(geminiKey, selectedModel)
                                testStatus = if (result.isSuccess) {
                                    "Connected: ${result.getOrNull()}"
                                } else {
                                    "Failed: ${result.exceptionOrNull()?.localizedMessage}"
                                }
                                isTestingConnection = false
                            }
                        },
                        enabled = !isTestingConnection,
                        modifier = Modifier.testTag("test_gemini_connection_button")
                    ) {
                        if (isTestingConnection) {
                            CircularProgressIndicator(color = ElectricCyan, strokeWidth = 2.dp, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Testing...", fontSize = 11.sp)
                        } else {
                            Text("Test Gemini API", fontSize = 11.sp)
                        }
                    }

                    if (testStatus != null) {
                        Text(
                            text = testStatus.orEmpty(),
                            fontSize = 11.sp,
                            color = if (testStatus?.startsWith("Connected") == true) EmeraldGreen else CoralRed,
                            maxLines = 2,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Demo Mode & Google Maps Settings
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(TechCardDark)
                .border(1.dp, TechBorder, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "DEMO & VERIFIED DATA MODE",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Enables full offline workflow with rich real-world local business leads even without personal API keys.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = settings.isDemoMode,
                        onCheckedChange = { onUpdateSettings(null, null, null, null, null, null, it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = ElectricCyan, checkedTrackColor = ElectricCobalt)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Optional Google Maps Key
                OutlinedTextField(
                    value = mapsKey,
                    onValueChange = {
                        mapsKey = it
                        onUpdateSettings(null, null, it, null, null, null, null)
                    },
                    label = { Text("Google Places API Key (Optional)", fontSize = 12.sp) },
                    placeholder = { Text("Optional - fallback to Gemini Intelligence") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCobalt,
                        unfocusedBorderColor = TechBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Database Reset & Clear
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(TechCardDark)
                .border(1.dp, TechBorder, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "DATABASE & STORAGE MANAGEMENT",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color.White
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onResetDemoData,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reset Demo Data", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = onClearData,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralRed),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Clear All Leads", fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
