package com.example.service

import com.example.data.model.LeadEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class GeminiAnalysisResult(
    val leadScore: Int,
    val priority: String,
    val businessSummary: String,
    val websiteOpportunity: String,
    val reasons: List<String>,
    val recommendedServices: List<String>,
    val outreachAngle: String
)

data class OutreachContent(
    val subject: String,
    val body: String,
    val tips: String = ""
)

data class ExtractedBusinessDto(
    val name: String,
    val category: String,
    val address: String?,
    val city: String?,
    val phone: String?,
    val website: String?,
    val rating: Double,
    val reviewCount: Int,
    val mapUrl: String?,
    val providerPlaceId: String?
)

class GeminiService(private val getApiKey: () -> String, private val getModel: () -> String) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun testConnection(customApiKey: String? = null, customModel: String? = null): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = customApiKey ?: getApiKey()
        val model = customModel ?: getModel()

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(Exception("Gemini API key is not configured. Please enter your API key in Settings."))
        }

        try {
            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
            val bodyJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", "Respond strictly with: 'LeadFinder AI Gemini Connection Successful!'")
                            })
                        })
                    })
                })
            }

            val request = Request.Builder()
                .url(endpoint)
                .post(bodyJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val raw = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Gemini API error (HTTP ${response.code}): $raw"))
            }

            val parsed = extractTextFromGeminiResponse(raw)
            Result.success(parsed)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun analyzeBusiness(lead: LeadEntity): Result<GeminiAnalysisResult> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        val model = getModel()

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(Exception("Gemini API key missing. Configure in Settings."))
        }

        val prompt = """
            You are an expert B2B business intelligence analyst.
            Analyze ONLY the supplied public business data:
            - Business Name: ${lead.businessName}
            - Category: ${lead.category}
            - Location: ${lead.address ?: "Unknown"}, City: ${lead.city ?: "Unknown"}
            - Phone: ${lead.phone ?: "None"}
            - Email: ${lead.email ?: "None"}
            - Website: ${lead.website ?: "None"}
            - Website Quality: ${lead.websiteQuality} (Status: ${lead.websiteStatus}, Mobile Friendly: ${lead.mobileFriendly}, HTTPS: ${lead.httpsEnabled})
            - Rating: ${lead.rating} (${lead.reviewCount} reviews)
            - Socials: FB: ${lead.facebookUrl ?: "None"}, IG: ${lead.instagramUrl ?: "None"}, LI: ${lead.linkedinUrl ?: "None"}

            Evaluate the opportunity for providing website design, local SEO, mobile optimization, or online presence development.
            Return STRICTLY a valid JSON object with these exact keys:
            {
              "lead_score": (integer 0 to 100),
              "priority": ("HIGH", "MEDIUM", or "LOW"),
              "business_summary": "Concise 2-sentence summary of the business operations and digital footprint.",
              "website_opportunity": "Specific commercial rationale for why they need a modern web presence or redesign.",
              "reasons": ["Specific factor 1", "Specific factor 2", "Specific factor 3"],
              "recommended_services": ["e.g. Modern Mobile-Friendly Web Design", "Local SEO & Google Maps Sync", "Online Appointment / Ordering System"],
              "outreach_angle": "The best psychological hook/opening angle for pitching to the business owner."
            }
            Do not invent imaginary information. Output ONLY the JSON block.
        """.trimIndent()

        try {
            val rawJson = callGeminiRaw(apiKey, model, prompt)
            val cleanJson = cleanJsonOutput(rawJson)
            val json = JSONObject(cleanJson)

            val score = json.optInt("lead_score", lead.leadScore)
            val priority = json.optString("priority", lead.leadPriority)
            val summary = json.optString("business_summary", "")
            val opp = json.optString("website_opportunity", "")
            val outreachAngle = json.optString("outreach_angle", "")

            val reasons = mutableListOf<String>()
            val reasonsArr = json.optJSONArray("reasons")
            if (reasonsArr != null) {
                for (i in 0 until reasonsArr.length()) {
                    reasons.add(reasonsArr.getString(i))
                }
            }

            val services = mutableListOf<String>()
            val servicesArr = json.optJSONArray("recommended_services")
            if (servicesArr != null) {
                for (i in 0 until servicesArr.length()) {
                    services.add(servicesArr.getString(i))
                }
            }

            Result.success(
                GeminiAnalysisResult(
                    leadScore = score,
                    priority = priority,
                    businessSummary = summary,
                    websiteOpportunity = opp,
                    reasons = reasons,
                    recommendedServices = services,
                    outreachAngle = outreachAngle
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generateOutreachDraft(
        lead: LeadEntity,
        channel: String, // EMAIL, WHATSAPP, DM, SMS
        tone: String,    // PROFESSIONAL, FRIENDLY, DIRECT
        customInstructions: String = ""
    ): Result<OutreachContent> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        val model = getModel()

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(Exception("Gemini API key missing. Configure in Settings."))
        }

        val channelGuideline = when (channel.uppercase()) {
            "EMAIL" -> "Create a high-converting Cold Email with a compelling, non-spammy Subject Line and a personalized 3-paragraph body with a clear soft Call To Action."
            "WHATSAPP" -> "Create a natural WhatsApp message (no formal email subject). Keep it brief, conversational, respectful of their time, and include emojis where appropriate."
            "DM" -> "Create an Instagram/Facebook Direct Message draft. Casual yet highly professional, referencing their local presence and offering a quick mockup."
            else -> "Create a short 2-3 sentence SMS/Text proposal."
        }

        val prompt = """
            You are a master B2B sales copywriter specializing in web design and digital agency outreach.
            Target Business:
            - Name: ${lead.businessName}
            - Category: ${lead.category}
            - Location: ${lead.city ?: "Local area"}
            - Current Website: ${lead.website ?: "None (no website)"}
            - Website Quality: ${lead.websiteQuality}
            - Rating: ${lead.rating} (${lead.reviewCount} reviews)
            - Channel: $channel ($channelGuideline)
            - Tone: $tone
            - Additional User Customization: ${customInstructions.ifBlank { "Focus on converting local mobile visitors into paying customers." }}

            Rules:
            1. Human-review proposal only. Never sound like a generic bot.
            2. Reference their actual business name and category.
            3. Point out why their current lack of a modern, mobile-ready website or poor site is losing them customers in ${lead.city ?: "their neighborhood"}.
            4. Keep the Call to Action frictionless (e.g. "Happy to share a free 2-minute video mockup").
            5. Return STRICTLY a JSON object with:
            {
              "subject": "Email subject line (empty string if WhatsApp/DM)",
              "body": "The full outreach message text formatted cleanly with line breaks",
              "tips": "One practical tip for sending this outreach"
            }
        """.trimIndent()

        try {
            val raw = callGeminiRaw(apiKey, model, prompt)
            val clean = cleanJsonOutput(raw)
            val obj = JSONObject(clean)
            Result.success(
                OutreachContent(
                    subject = obj.optString("subject", ""),
                    body = obj.optString("body", clean),
                    tips = obj.optString("tips", "Review and personalize before sending.")
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun refineOutreach(currentDraft: String, instruction: String): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        val model = getModel()

        val prompt = """
            Refine the following outreach message according to this instruction: "$instruction".
            Keep placeholders intact if any. Maintain a high conversion standard. Return ONLY the revised message text.

            Current Message:
            $currentDraft
        """.trimIndent()

        try {
            val raw = callGeminiRaw(apiKey, model, prompt)
            Result.success(raw.trim())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun answerLeadFinderQuestion(
        question: String,
        dbSummary: String,
        topLeadsContext: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        val model = getModel()

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(Exception("Gemini API key is required. Configure in Settings."))
        }

        val prompt = """
            You are the LeadFinder AI Lead Assistant. You help users understand, query, and act upon their local business leads stored in their local database.

            Current Database State:
            $dbSummary

            Representative Leads from Database:
            $topLeadsContext

            User Question: "$question"

            Instructions:
            - Answer accurately based ONLY on the data provided above.
            - If asked for specific categories (e.g., restaurants, dentists) or criteria (no website, score > 80), list the matching businesses clearly with their scores and recommendations.
            - Provide tactical sales advice on how to approach them.
            - Keep your response structured, concise, and actionable with bullet points.
        """.trimIndent()

        try {
            val res = callGeminiRaw(apiKey, model, prompt)
            Result.success(res.trim())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchBusinessesWithGemini(
        category: String,
        location: String,
        limit: Int
    ): Result<List<ExtractedBusinessDto>> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        val model = getModel()

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(Exception("Gemini API key missing. Configure in Settings."))
        }

        val prompt = """
            Discover $limit real, publicly operating local businesses in "$location" for the category "$category".
            Find real establishments (e.g. restaurants, clinics, repair shops, spas, contractors).
            For each business, identify real public details (name, address, city, phone if public, website if they have one or state null if they do NOT have one, estimated Google review count and rating).

            Return STRICTLY a valid JSON array of objects with these keys:
            [
              {
                "name": "Actual Business Name",
                "category": "$category",
                "address": "Street Address in $location",
                "city": "$location",
                "phone": "+1 ... or local format",
                "website": "https://... or null if no website",
                "rating": 4.5,
                "review_count": 85,
                "map_url": "https://maps.google.com/?q=...",
                "provider_place_id": "unique_place_identifier_or_hash"
              }
            ]
            Ensure at least 30-50% of the returned businesses have NO website or an outdated web presence, as this tool is used to prospect local web design clients.
            Output ONLY the JSON array.
        """.trimIndent()

        try {
            val raw = callGeminiRaw(apiKey, model, prompt)
            val clean = cleanJsonOutput(raw)
            val arr = JSONArray(clean)
            val list = mutableListOf<ExtractedBusinessDto>()

            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    ExtractedBusinessDto(
                        name = obj.getString("name"),
                        category = obj.optString("category", category),
                        address = obj.optString("address", null),
                        city = obj.optString("city", location),
                        phone = obj.optString("phone", null),
                        website = if (obj.isNull("website") || obj.optString("website") == "null") null else obj.optString("website"),
                        rating = obj.optDouble("rating", 4.0),
                        reviewCount = obj.optInt("review_count", 25),
                        mapUrl = obj.optString("map_url", null),
                        providerPlaceId = obj.optString("provider_place_id", "gen_${System.currentTimeMillis()}_$i")
                    )
                )
            }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun callGeminiRaw(apiKey: String, model: String, promptText: String): String {
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

        val bodyJson = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", promptText)
                        })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.4)
                put("topP", 0.95)
            })
        }

        val request = Request.Builder()
            .url(endpoint)
            .post(bodyJson.toString().toRequestBody(jsonMediaType))
            .build()

        val response = client.newCall(request).execute()
        val rawResponse = response.body?.string().orEmpty()

        if (!response.isSuccessful) {
            throw Exception("Gemini API call failed (HTTP ${response.code}): $rawResponse")
        }

        return extractTextFromGeminiResponse(rawResponse)
    }

    private fun extractTextFromGeminiResponse(jsonStr: String): String {
        val obj = JSONObject(jsonStr)
        val candidates = obj.optJSONArray("candidates")
        if (candidates != null && candidates.length() > 0) {
            val first = candidates.getJSONObject(0)
            val content = first.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            if (parts != null && parts.length() > 0) {
                return parts.getJSONObject(0).optString("text", "")
            }
        }
        return ""
    }

    private fun cleanJsonOutput(raw: String): String {
        var text = raw.trim()
        if (text.startsWith("```json")) {
            text = text.removePrefix("```json").trim()
        } else if (text.startsWith("```")) {
            text = text.removePrefix("```").trim()
        }
        if (text.endsWith("```")) {
            text = text.removeSuffix("```").trim()
        }
        return text.trim()
    }
}
