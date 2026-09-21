package com.example.service

import android.content.Context
import android.content.Intent
import com.example.data.model.LeadEntity
import org.json.JSONArray
import org.json.JSONObject

object ExportService {
    fun generateCsv(leads: List<LeadEntity>): String {
        val builder = StringBuilder()
        // CSV Header
        builder.append("Business Name,Category,Phone,Email,Website,Facebook,Instagram,LinkedIn,Address,City,Rating,Reviews,Website Status,Website Quality,Lead Score,Priority,AI Summary,Recommended Services,Status\n")

        for (lead in leads) {
            builder.append(escapeCsv(lead.businessName)).append(",")
            builder.append(escapeCsv(lead.category)).append(",")
            builder.append(escapeCsv(lead.phone.orEmpty())).append(",")
            builder.append(escapeCsv(lead.email.orEmpty())).append(",")
            builder.append(escapeCsv(lead.website.orEmpty())).append(",")
            builder.append(escapeCsv(lead.facebookUrl.orEmpty())).append(",")
            builder.append(escapeCsv(lead.instagramUrl.orEmpty())).append(",")
            builder.append(escapeCsv(lead.linkedinUrl.orEmpty())).append(",")
            builder.append(escapeCsv(lead.address.orEmpty())).append(",")
            builder.append(escapeCsv(lead.city.orEmpty())).append(",")
            builder.append(lead.rating).append(",")
            builder.append(lead.reviewCount).append(",")
            builder.append(escapeCsv(lead.websiteStatus)).append(",")
            builder.append(escapeCsv(lead.websiteQuality)).append(",")
            builder.append(lead.leadScore).append(",")
            builder.append(escapeCsv(lead.leadPriority)).append(",")
            builder.append(escapeCsv(lead.aiSummary.orEmpty())).append(",")
            builder.append(escapeCsv(lead.aiRecommendations.orEmpty())).append(",")
            builder.append(escapeCsv(lead.status)).append("\n")
        }
        return builder.toString()
    }

    fun generateJson(leads: List<LeadEntity>): String {
        val array = JSONArray()
        for (lead in leads) {
            val obj = JSONObject().apply {
                put("id", lead.id)
                put("business_name", lead.businessName)
                put("category", lead.category)
                put("phone", lead.phone)
                put("email", lead.email)
                put("website", lead.website)
                put("address", lead.address)
                put("city", lead.city)
                put("rating", lead.rating)
                put("review_count", lead.reviewCount)
                put("website_status", lead.websiteStatus)
                put("website_quality", lead.websiteQuality)
                put("mobile_friendly", lead.mobileFriendly)
                put("https_enabled", lead.httpsEnabled)
                put("lead_score", lead.leadScore)
                put("lead_priority", lead.leadPriority)
                put("lead_reason", lead.leadReason)
                put("ai_summary", lead.aiSummary)
                put("ai_recommendations", lead.aiRecommendations)
                put("status", lead.status)
            }
            array.put(obj)
        }
        return array.toString(2)
    }

    fun shareExportData(context: Context, data: String, mimeType: String, title: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, data)
            putExtra(Intent.EXTRA_TITLE, title)
            type = mimeType
        }
        val shareIntent = Intent.createChooser(sendIntent, title)
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }

    private fun escapeCsv(value: String): String {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\""
        }
        return value
    }
}
