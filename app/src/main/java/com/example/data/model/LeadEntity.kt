package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "businesses",
    indices = [
        Index(value = ["business_name"]),
        Index(value = ["phone"]),
        Index(value = ["website"]),
        Index(value = ["provider_place_id"], unique = false),
        Index(value = ["lead_score"]),
        Index(value = ["lead_priority"]),
        Index(value = ["status"]),
        Index(value = ["created_at"])
    ]
)
data class LeadEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "provider_place_id")
    val providerPlaceId: String? = null,

    @ColumnInfo(name = "business_name")
    val businessName: String,

    @ColumnInfo(name = "category")
    val category: String = "General",

    @ColumnInfo(name = "categories")
    val categories: String = "",

    @ColumnInfo(name = "phone")
    val phone: String? = null,

    @ColumnInfo(name = "international_phone")
    val internationalPhone: String? = null,

    @ColumnInfo(name = "email")
    val email: String? = null,

    @ColumnInfo(name = "website")
    val website: String? = null,

    @ColumnInfo(name = "address")
    val address: String? = null,

    @ColumnInfo(name = "city")
    val city: String? = null,

    @ColumnInfo(name = "country")
    val country: String? = null,

    @ColumnInfo(name = "latitude")
    val latitude: Double? = null,

    @ColumnInfo(name = "longitude")
    val longitude: Double? = null,

    @ColumnInfo(name = "map_url")
    val mapUrl: String? = null,

    @ColumnInfo(name = "rating")
    val rating: Double = 0.0,

    @ColumnInfo(name = "review_count")
    val reviewCount: Int = 0,

    @ColumnInfo(name = "opening_hours")
    val openingHours: String? = null,

    @ColumnInfo(name = "business_status")
    val businessStatus: String = "OPERATIONAL",

    @ColumnInfo(name = "price_level")
    val priceLevel: String? = null,

    @ColumnInfo(name = "facebook_url")
    val facebookUrl: String? = null,

    @ColumnInfo(name = "instagram_url")
    val instagramUrl: String? = null,

    @ColumnInfo(name = "linkedin_url")
    val linkedinUrl: String? = null,

    @ColumnInfo(name = "other_social_urls")
    val otherSocialUrls: String? = null,

    @ColumnInfo(name = "website_status")
    val websiteStatus: String = "NONE", // NONE, ACCESSIBLE, UNREACHABLE, SLOW, OLD_DESIGN

    @ColumnInfo(name = "website_quality")
    val websiteQuality: String = "UNKNOWN", // GOOD, AVERAGE, POOR, NONE, UNKNOWN

    @ColumnInfo(name = "mobile_friendly")
    val mobileFriendly: Boolean = false,

    @ColumnInfo(name = "https_enabled")
    val httpsEnabled: Boolean = false,

    @ColumnInfo(name = "contact_form_present")
    val contactFormPresent: Boolean = false,

    @ColumnInfo(name = "lead_score")
    val leadScore: Int = 0,

    @ColumnInfo(name = "lead_priority")
    val leadPriority: String = "LOW", // HIGH, MEDIUM, LOW

    @ColumnInfo(name = "lead_reason")
    val leadReason: String = "",

    @ColumnInfo(name = "ai_summary")
    val aiSummary: String? = null,

    @ColumnInfo(name = "ai_recommendations")
    val aiRecommendations: String? = null,

    @ColumnInfo(name = "status")
    val status: String = "NEW", // NEW, CONTACTED, INTERESTED, FOLLOW_UP, CONVERTED, NOT_INTERESTED, ARCHIVED

    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
