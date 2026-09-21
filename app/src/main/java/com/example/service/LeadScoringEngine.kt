package com.example.service

data class ScoreResult(
    val score: Int,
    val priority: String, // HIGH, MEDIUM, LOW
    val reasons: List<String>,
    val explanation: String
)

object LeadScoringEngine {
    private val localCustomerFacingKeywords = listOf(
        "restaurant", "cafe", "bakery", "dentist", "doctor", "clinic", "plumber",
        "electrician", "contractor", "salon", "spa", "gym", "fitness", "lawyer",
        "attorney", "auto", "mechanic", "roofing", "cleaning", "vet", "veterinarian",
        "boutique", "catering", "photography", "landscaping"
    )

    fun calculateScore(
        website: String?,
        websiteQuality: String,
        isMobileFriendly: Boolean,
        hasPhone: Boolean,
        hasEmail: Boolean,
        socialProfilesCount: Int,
        reviewCount: Int,
        category: String
    ): ScoreResult {
        var score = 0
        val reasons = mutableListOf<String>()

        val hasNoWebsite = website.isNullOrBlank() || website.equals("null", ignoreCase = true) || websiteQuality == "NONE"

        if (hasNoWebsite) {
            score += 30
            reasons.add("+30: Business currently has NO website (prime target for full site build)")
        } else {
            when (websiteQuality.uppercase()) {
                "POOR" -> {
                    score += 25
                    reasons.add("+25: Website quality is POOR (needs complete modern redesign)")
                }
                "AVERAGE" -> {
                    score += 10
                    reasons.add("+10: Website quality is AVERAGE (upgrade & SEO optimization opportunity)")
                }
                "UNKNOWN" -> {
                    score += 5
                    reasons.add("+5: Website status unknown or pending audit")
                }
            }

            if (!isMobileFriendly) {
                score += 15
                reasons.add("+15: Website is NOT mobile-friendly (critical conversion loss)")
            }
        }

        if (socialProfilesCount > 0) {
            score += 10
            reasons.add("+10: Active on social media ($socialProfilesCount detected, values digital presence)")
        }

        if (hasPhone) {
            score += 5
            reasons.add("+5: Direct phone contact number verified")
        }

        if (hasEmail) {
            score += 5
            reasons.add("+5: Direct email address available for cold outreach")
        }

        val totalChannels = (if (hasPhone) 1 else 0) + (if (hasEmail) 1 else 0) + socialProfilesCount
        if (totalChannels >= 3) {
            score += 5
            reasons.add("+5: Multiple active communication channels ($totalChannels detected)")
        }

        if (reviewCount >= 20) {
            score += 5
            reasons.add("+5: Strong customer volume ($reviewCount reviews, established business with budget)")
        }

        val isLocalFacing = localCustomerFacingKeywords.any {
            category.contains(it, ignoreCase = true)
        }
        if (isLocalFacing) {
            score += 5
            reasons.add("+5: High-margin local customer-facing niche ($category)")
        }

        val finalScore = score.coerceIn(0, 100)
        val priority = when {
            finalScore >= 80 -> "HIGH"
            finalScore >= 60 -> "MEDIUM"
            else -> "LOW"
        }

        val explanation = when (priority) {
            "HIGH" -> "Exceptional lead! High commercial intent with severe digital presence gaps (missing or outdated web presence combined with active business operation)."
            "MEDIUM" -> "Solid prospecting lead with clear upgrade opportunities and verified contact channels."
            else -> "Low urgency lead. The business may already have an active web presence or limited contact avenues."
        }

        return ScoreResult(
            score = finalScore,
            priority = priority,
            reasons = reasons,
            explanation = explanation
        )
    }
}
