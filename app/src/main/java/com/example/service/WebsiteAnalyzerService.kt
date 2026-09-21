package com.example.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

data class WebsiteAuditResult(
    val url: String,
    val isAccessible: Boolean,
    val httpStatusCode: Int = 0,
    val isHttps: Boolean = false,
    val isMobileFriendly: Boolean = false,
    val hasTitle: Boolean = false,
    val pageTitle: String = "",
    val hasMetaDescription: Boolean = false,
    val hasContactForm: Boolean = false,
    val hasPhoneVisible: Boolean = false,
    val hasEmailVisible: Boolean = false,
    val hasWhatsApp: Boolean = false,
    val detectedSocials: List<String> = emptyList(),
    val responseTimeMs: Long = 0,
    val quality: String = "UNKNOWN", // GOOD, AVERAGE, POOR, NONE, UNKNOWN
    val summary: String = "",
    val recommendations: List<String> = emptyList()
)

class WebsiteAnalyzerService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    suspend fun auditWebsite(rawUrl: String?): WebsiteAuditResult = withContext(Dispatchers.IO) {
        if (rawUrl.isNullOrBlank()) {
            return@withContext WebsiteAuditResult(
                url = "",
                isAccessible = false,
                quality = "NONE",
                summary = "No website found for this business. Total absence of online web real-estate.",
                recommendations = listOf(
                    "Pitch turnkey custom website build",
                    "Offer domain registration & Google Business Profile linking",
                    "Highlight revenue lost to local competitors with modern websites"
                )
            )
        }

        var normalizedUrl = rawUrl.trim()
        if (!normalizedUrl.startsWith("http://") && !normalizedUrl.startsWith("https://")) {
            normalizedUrl = "https://$normalizedUrl"
        }

        val startTime = System.currentTimeMillis()
        try {
            val request = Request.Builder()
                .url(normalizedUrl)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) LeadFinder-Audit/1.0")
                .build()

            val response = client.newCall(request).execute()
            val durationMs = System.currentTimeMillis() - startTime
            val statusCode = response.code
            val isHttps = response.request.url.isHttps
            val bodyText = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext WebsiteAuditResult(
                    url = normalizedUrl,
                    isAccessible = false,
                    httpStatusCode = statusCode,
                    isHttps = isHttps,
                    responseTimeMs = durationMs,
                    quality = "POOR",
                    summary = "Website returned HTTP $statusCode error. The site is currently broken or unreachable for customers.",
                    recommendations = listOf(
                        "Point out server/hosting downtime to business owner",
                        "Offer website recovery or modern cloud hosting migration"
                    )
                )
            }

            val lowerBody = bodyText.lowercase()

            // 1. Mobile friendly check (viewport meta tag)
            val isMobileFriendly = lowerBody.contains("name=\"viewport\"") || lowerBody.contains("name='viewport'")

            // 2. Title extraction
            val titleMatcher = Pattern.compile("<title>(.*?)</title>", Pattern.CASE_INSENSITIVE or Pattern.DOTALL).matcher(bodyText)
            val pageTitle = if (titleMatcher.find()) titleMatcher.group(1)?.trim().orEmpty() else ""
            val hasTitle = pageTitle.isNotEmpty()

            // 3. Meta description
            val hasMetaDesc = lowerBody.contains("name=\"description\"") || lowerBody.contains("name='description'")

            // 4. Contact form
            val hasContactForm = lowerBody.contains("<form") && (
                lowerBody.contains("contact") || lowerBody.contains("message") ||
                lowerBody.contains("submit") || lowerBody.contains("inquiry") || lowerBody.contains("email")
            )

            // 5. Phone visible
            val hasPhone = lowerBody.contains("tel:") || Pattern.compile("\\+?[0-9]{1,4}?[-.\\s]?[0-9]{3,4}[-.\\s]?[0-9]{3,4}").matcher(bodyText).find()

            // 6. Email visible
            val hasEmail = lowerBody.contains("mailto:") || Pattern.compile("[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+").matcher(bodyText).find()

            // 7. WhatsApp link
            val hasWhatsApp = lowerBody.contains("wa.me") || lowerBody.contains("api.whatsapp.com")

            // 8. Social links
            val detectedSocials = mutableListOf<String>()
            if (lowerBody.contains("facebook.com")) detectedSocials.add("Facebook")
            if (lowerBody.contains("instagram.com")) detectedSocials.add("Instagram")
            if (lowerBody.contains("linkedin.com")) detectedSocials.add("LinkedIn")
            if (lowerBody.contains("tiktok.com")) detectedSocials.add("TikTok")
            if (lowerBody.contains("youtube.com")) detectedSocials.add("YouTube")

            // Scoring quality
            var qualityScore = 0
            if (isHttps) qualityScore += 20
            if (isMobileFriendly) qualityScore += 25
            if (hasTitle && pageTitle.length > 5) qualityScore += 15
            if (hasMetaDesc) qualityScore += 10
            if (hasContactForm) qualityScore += 15
            if (hasPhone || hasEmail || hasWhatsApp) qualityScore += 15
            if (durationMs < 2500) qualityScore += 10 // Fast load

            val quality = when {
                qualityScore >= 75 -> "GOOD"
                qualityScore >= 45 -> "AVERAGE"
                else -> "POOR"
            }

            val recommendations = mutableListOf<String>()
            if (!isHttps) recommendations.add("Install SSL certificate (enable HTTPS for security & Google ranking)")
            if (!isMobileFriendly) recommendations.add("Implement responsive mobile layout (over 65% of local searches are on mobile)")
            if (!hasContactForm) recommendations.add("Add automated lead capture & contact forms to convert visitors into buyers")
            if (!hasWhatsApp) recommendations.add("Add 1-click WhatsApp chat button for instant client inquiries")
            if (!hasMetaDesc) recommendations.add("Optimize local SEO meta tags to increase organic Google Maps ranking")
            if (durationMs > 2500) recommendations.add("Speed optimization: current response time is ${durationMs}ms (slow)")

            val summary = when (quality) {
                "GOOD" -> "Modern website with active security and responsive layout. Good baseline, can suggest performance or booking add-ons."
                "AVERAGE" -> "Functional site but exhibits notable gaps (missing meta SEO, limited lead capture, or average mobile responsiveness)."
                else -> "Outdated or poorly optimized web presence with critical missing features. Excellent prospect for a redesign pitch."
            }

            WebsiteAuditResult(
                url = normalizedUrl,
                isAccessible = true,
                httpStatusCode = statusCode,
                isHttps = isHttps,
                isMobileFriendly = isMobileFriendly,
                hasTitle = hasTitle,
                pageTitle = pageTitle,
                hasMetaDescription = hasMetaDesc,
                hasContactForm = hasContactForm,
                hasPhoneVisible = hasPhone,
                hasEmailVisible = hasEmail,
                hasWhatsApp = hasWhatsApp,
                detectedSocials = detectedSocials,
                responseTimeMs = durationMs,
                quality = quality,
                summary = summary,
                recommendations = recommendations
            )
        } catch (e: Exception) {
            val isHttpsAttempt = normalizedUrl.startsWith("https://")
            WebsiteAuditResult(
                url = normalizedUrl,
                isAccessible = false,
                isHttps = isHttpsAttempt,
                quality = "POOR",
                summary = "Connection failed (${e.localizedMessage ?: "timeout/unreachable"}). Site appears offline or improperly configured.",
                recommendations = listOf(
                    "Notify client that their website cannot be loaded by prospective customers",
                    "Offer modern reliable cloud hosting & redesign solution"
                )
            )
        }
    }
}
