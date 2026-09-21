package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CoralRed
import com.example.ui.theme.ElectricCobalt
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.VividAmber

@Composable
fun ScoreBadge(
    score: Int,
    priority: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, dotColor) = when {
        score >= 80 -> Triple(EmeraldGreen.copy(alpha = 0.18f), EmeraldGreen, EmeraldGreen)
        score >= 60 -> Triple(VividAmber.copy(alpha = 0.18f), VividAmber, VividAmber)
        else -> Triple(Color(0xFF64748B).copy(alpha = 0.2f), Color(0xFF94A3B8), Color(0xFF94A3B8))
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "$score/100",
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = priority,
            color = textColor.copy(alpha = 0.85f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun WebsiteQualityBadge(quality: String, modifier: Modifier = Modifier) {
    val (color, text) = when (quality.uppercase()) {
        "GOOD" -> Pair(EmeraldGreen, "Good Web")
        "AVERAGE" -> Pair(VividAmber, "Average Web")
        "POOR" -> Pair(CoralRed, "Poor Web")
        "NONE" -> Pair(CoralRed, "No Website")
        else -> Pair(Color(0xFF94A3B8), "Pending Audit")
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
