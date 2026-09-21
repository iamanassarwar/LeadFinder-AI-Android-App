package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.service.WebsiteAuditResult
import com.example.ui.components.WebsiteQualityBadge
import com.example.ui.theme.CoralRed
import com.example.ui.theme.ElectricCobalt
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.TechBorder
import com.example.ui.theme.TechCardDark
import com.example.ui.theme.VividAmber

@Composable
fun WebsiteAnalyzerScreen(
    auditResult: WebsiteAuditResult?,
    isAuditing: Boolean,
    onAuditWebsite: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var urlInput by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Title
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Language, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Website Presence Audit Tool",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        }
        Text(
            text = "Inspect any business website for mobile compatibility, SSL security, contact forms, and sales friction.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // URL Input Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(TechCardDark)
                .border(1.dp, TechBorder, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "TARGET WEBSITE URL",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    placeholder = { Text("e.g. business-website.com") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCobalt,
                        unfocusedBorderColor = TechBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("website_audit_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { onAuditWebsite(urlInput) },
                    enabled = urlInput.isNotBlank() && !isAuditing,
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCobalt),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("run_audit_button")
                ) {
                    if (isAuditing) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Inspecting Website...", color = Color.White)
                    } else {
                        Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Audit Website Now", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        // Audit Results Display
        if (auditResult != null) {
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(TechCardDark)
                    .border(1.dp, TechBorder, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = auditResult.url.ifBlank { "No Website Found" },
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            if (auditResult.pageTitle.isNotBlank()) {
                                Text(
                                    text = auditResult.pageTitle,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        WebsiteQualityBadge(quality = auditResult.quality)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = auditResult.summary,
                        fontSize = 12.sp,
                        color = Color.White,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Inspection Criteria Grid
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        AuditRow(label = "Website Accessible", passed = auditResult.isAccessible, detail = if (auditResult.isAccessible) "HTTP ${auditResult.httpStatusCode} (${auditResult.responseTimeMs}ms)" else "Unreachable")
                        AuditRow(label = "Mobile Viewport Configured", passed = auditResult.isMobileFriendly, detail = if (auditResult.isMobileFriendly) "Responsive Tag Present" else "Missing (Desktop Only)")
                        AuditRow(label = "HTTPS / SSL Secured", passed = auditResult.isHttps, detail = if (auditResult.isHttps) "Encrypted" else "Insecure (HTTP)")
                        AuditRow(label = "Lead Capture / Contact Form", passed = auditResult.hasContactForm, detail = if (auditResult.hasContactForm) "Found" else "No form detected")
                        AuditRow(label = "Click-to-Call Phone", passed = auditResult.hasPhoneVisible, detail = if (auditResult.hasPhoneVisible) "Visible" else "Missing direct link")
                        AuditRow(label = "Direct Email Link", passed = auditResult.hasEmailVisible, detail = if (auditResult.hasEmailVisible) "Visible" else "None")
                        AuditRow(label = "WhatsApp Quick Chat", passed = auditResult.hasWhatsApp, detail = if (auditResult.hasWhatsApp) "Available" else "None")
                    }

                    if (auditResult.detectedSocials.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Detected Social Links: ${auditResult.detectedSocials.joinToString(", ")}",
                            fontSize = 11.sp,
                            color = ElectricCyan,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (auditResult.recommendations.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "PROSPECT PITCH OPPORTUNITIES",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = VividAmber
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        auditResult.recommendations.forEach { rec ->
                            Row(
                                modifier = Modifier.padding(vertical = 2.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text("• ", color = VividAmber, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(text = rec, fontSize = 12.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun AuditRow(label: String, passed: Boolean, detail: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(TechBorder.copy(alpha = 0.25f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (passed) Icons.Default.CheckCircle else Icons.Default.Close,
                contentDescription = null,
                tint = if (passed) EmeraldGreen else CoralRed,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label, fontSize = 12.sp, color = Color.White)
        }
        Text(text = detail, fontSize = 11.sp, color = if (passed) EmeraldGreen else CoralRed, fontWeight = FontWeight.Medium)
    }
}
