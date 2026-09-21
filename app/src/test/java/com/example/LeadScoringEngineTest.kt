package com.example

import com.example.service.LeadScoringEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LeadScoringEngineTest {

    @Test
    fun testLeadScoring_NoWebsite_HighPotential() {
        val result = LeadScoringEngine.calculateScore(
            website = null,
            websiteQuality = "NONE",
            isMobileFriendly = false,
            hasPhone = true,
            hasEmail = false,
            socialProfilesCount = 2,
            reviewCount = 150,
            category = "Restaurant & Dining"
        )

        // 30 (no web) + 10 (social) + 5 (phone) + 5 (total channels >=3) + 5 (reviews >=20) + 5 (local category) = 60
        assertTrue("Score should be >= 50", result.score >= 50)
        assertTrue("Reasons should highlight missing website", result.reasons.any { it.contains("NO website") })
    }

    @Test
    fun testLeadScoring_PoorWebsite_HighPriority() {
        val result = LeadScoringEngine.calculateScore(
            website = "http://old-plumber.com",
            websiteQuality = "POOR",
            isMobileFriendly = false,
            hasPhone = true,
            hasEmail = true,
            socialProfilesCount = 1,
            reviewCount = 80,
            category = "Plumber & Contractors"
        )

        // 25 (poor) + 15 (not mobile) + 10 (social) + 5 (phone) + 5 (email) + 5 (multiple channels) + 5 (reviews) + 5 (category) = 75
        assertEquals("MEDIUM", result.priority)
        assertTrue("Score should be >= 70", result.score >= 70)
    }

    @Test
    fun testScoreNeverExceeds100() {
        val result = LeadScoringEngine.calculateScore(
            website = null,
            websiteQuality = "NONE",
            isMobileFriendly = false,
            hasPhone = true,
            hasEmail = true,
            socialProfilesCount = 5,
            reviewCount = 500,
            category = "Dentist & Healthcare"
        )

        assertTrue(result.score <= 100)
    }
}
