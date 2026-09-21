package com.example.service

data class SocialProfiles(
    val facebook: String? = null,
    val instagram: String? = null,
    val linkedin: String? = null,
    val tiktok: String? = null,
    val youtube: String? = null
) {
    val totalCount: Int
        get() = listOfNotNull(facebook, instagram, linkedin, tiktok, youtube).size
}

object SocialDetectionService {
    fun extractSocialsFromText(text: String): SocialProfiles {
        var fb: String? = null
        var ig: String? = null
        var li: String? = null
        var tt: String? = null
        var yt: String? = null

        val lines = text.split(Regex("[\"\\s,;<>]+"))
        for (token in lines) {
            val lower = token.lowercase()
            if (lower.contains("facebook.com/") && !lower.contains("/sharer") && fb == null) {
                fb = cleanSocialUrl(token)
            } else if (lower.contains("instagram.com/") && ig == null) {
                ig = cleanSocialUrl(token)
            } else if (lower.contains("linkedin.com/") && li == null) {
                li = cleanSocialUrl(token)
            } else if (lower.contains("tiktok.com/") && tt == null) {
                tt = cleanSocialUrl(token)
            } else if (lower.contains("youtube.com/") && yt == null) {
                yt = cleanSocialUrl(token)
            }
        }

        return SocialProfiles(
            facebook = fb,
            instagram = ig,
            linkedin = li,
            tiktok = tt,
            youtube = yt
        )
    }

    private fun cleanSocialUrl(url: String): String {
        var res = url.trim()
        if (res.startsWith("href=", ignoreCase = true)) {
            res = res.substring(5).trim('\'', '"')
        }
        return res
    }
}
