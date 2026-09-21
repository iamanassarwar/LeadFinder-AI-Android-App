package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.LeadFinderDatabase
import com.example.data.model.ActivityLogEntity
import com.example.data.model.LeadEntity
import com.example.data.model.LeadNoteEntity
import com.example.data.model.LeadTagEntity
import com.example.data.model.OutreachDraftEntity
import com.example.data.model.SearchHistoryEntity
import com.example.data.repository.AppSettings
import com.example.data.repository.LeadRepository
import com.example.data.repository.SettingsManager
import com.example.service.ExportService
import com.example.service.ExtractionProgress
import com.example.service.GeminiService
import com.example.service.OutreachContent
import com.example.service.PlacesExtractionService
import com.example.service.WebsiteAnalyzerService
import com.example.service.WebsiteAuditResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen(val label: String) {
    DASHBOARD("Dashboard"),
    EXTRACT_LEADS("Extract Leads"),
    ALL_LEADS("All Leads"),
    HIGH_POTENTIAL("High Potential Leads"),
    AI_ASSISTANT("AI Assistant"),
    WEBSITE_ANALYZER("Website Analyzer"),
    EXPORTS("Exports"),
    SETTINGS("Settings")
}

data class AssistantMessage(
    val sender: String, // "USER" or "AI"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isError: Boolean = false
)

class LeadFinderViewModel(application: Application) : AndroidViewModel(application) {
    private val database = LeadFinderDatabase.getInstance(application)
    val repository = LeadRepository(database.leadDao())
    val settingsManager = SettingsManager(application)
    val websiteAnalyzer = WebsiteAnalyzerService()

    val geminiService = GeminiService(
        getApiKey = { settingsManager.getEffectiveGeminiKey() },
        getModel = { settingsManager.settings.value.geminiModel }
    )

    val extractionService = PlacesExtractionService(
        repository = repository,
        geminiService = geminiService,
        websiteAnalyzer = websiteAnalyzer
    )

    // --- State ---
    private val _currentScreen = MutableStateFlow(AppScreen.DASHBOARD)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    val appSettings: StateFlow<AppSettings> = settingsManager.settings

    val allLeads: StateFlow<List<LeadEntity>> = repository.allLeads
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val highPotentialLeads: StateFlow<List<LeadEntity>> = repository.highPotentialLeads
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentSearches: StateFlow<List<SearchHistoryEntity>> = repository.recentSearches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentLogs: StateFlow<List<ActivityLogEntity>> = repository.recentLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val extractionProgress: StateFlow<ExtractionProgress> = extractionService.progress

    private var extractionJob: Job? = null

    // Detail & Selection State
    private val _selectedLeadId = MutableStateFlow<Long?>(null)
    val selectedLeadId: StateFlow<Long?> = _selectedLeadId.asStateFlow()

    private val _selectedLead = MutableStateFlow<LeadEntity?>(null)
    val selectedLead: StateFlow<LeadEntity?> = _selectedLead.asStateFlow()

    private val _selectedLeadNotes = MutableStateFlow<List<LeadNoteEntity>>(emptyList())
    val selectedLeadNotes: StateFlow<List<LeadNoteEntity>> = _selectedLeadNotes.asStateFlow()

    private val _selectedLeadTags = MutableStateFlow<List<LeadTagEntity>>(emptyList())
    val selectedLeadTags: StateFlow<List<LeadTagEntity>> = _selectedLeadTags.asStateFlow()

    private val _selectedLeadDrafts = MutableStateFlow<List<OutreachDraftEntity>>(emptyList())
    val selectedLeadDrafts: StateFlow<List<OutreachDraftEntity>> = _selectedLeadDrafts.asStateFlow()

    // Bulk selection
    private val _selectedLeadIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedLeadIds: StateFlow<Set<Long>> = _selectedLeadIds.asStateFlow()

    // Global Search & Table Filters
    private val _searchFilterText = MutableStateFlow("")
    val searchFilterText: StateFlow<String> = _searchFilterText.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<String?>(null)
    val selectedCategoryFilter: StateFlow<String?> = _selectedCategoryFilter.asStateFlow()

    private val _selectedStatusFilter = MutableStateFlow<String?>(null)
    val selectedStatusFilter: StateFlow<String?> = _selectedStatusFilter.asStateFlow()

    private val _selectedWebsiteFilter = MutableStateFlow<String?>(null) // "NO_WEBSITE", "POOR", "ALL"
    val selectedWebsiteFilter: StateFlow<String?> = _selectedWebsiteFilter.asStateFlow()

    // Filtered Leads Flow
    val filteredLeads: StateFlow<List<LeadEntity>> = combine(
        allLeads,
        _searchFilterText,
        _selectedCategoryFilter,
        _selectedStatusFilter,
        _selectedWebsiteFilter
    ) { leads, search, cat, stat, web ->
        leads.filter { lead ->
            val matchSearch = search.isBlank() ||
                lead.businessName.contains(search, ignoreCase = true) ||
                lead.category.contains(search, ignoreCase = true) ||
                lead.city?.contains(search, ignoreCase = true) == true ||
                lead.phone?.contains(search) == true

            val matchCategory = cat == null || lead.category.equals(cat, ignoreCase = true)
            val matchStatus = stat == null || lead.status.equals(stat, ignoreCase = true)

            val matchWebsite = when (web) {
                "NO_WEBSITE" -> lead.website.isNullOrBlank() || lead.websiteStatus == "NONE"
                "POOR" -> lead.websiteQuality.equals("POOR", ignoreCase = true)
                "AVERAGE" -> lead.websiteQuality.equals("AVERAGE", ignoreCase = true)
                "GOOD" -> lead.websiteQuality.equals("GOOD", ignoreCase = true)
                else -> true
            }

            matchSearch && matchCategory && matchStatus && matchWebsite
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Standalone Website Audit State
    private val _standaloneAuditResult = MutableStateFlow<WebsiteAuditResult?>(null)
    val standaloneAuditResult: StateFlow<WebsiteAuditResult?> = _standaloneAuditResult.asStateFlow()

    private val _isAuditingUrl = MutableStateFlow(false)
    val isAuditingUrl: StateFlow<Boolean> = _isAuditingUrl.asStateFlow()

    // AI Assistant State
    private val _assistantMessages = MutableStateFlow<List<AssistantMessage>>(
        listOf(
            AssistantMessage(
                sender = "AI",
                text = "Welcome to LeadFinder AI Assistant! I can help you analyze your prospecting database, identify prime web design opportunities, recommend high-value leads, or explain lead scoring. Try asking:\n• 'Show me businesses without websites'\n• 'Which leads have high potential scores?'\n• 'Give me top restaurant opportunities'"
            )
        )
    )
    val assistantMessages: StateFlow<List<AssistantMessage>> = _assistantMessages.asStateFlow()

    private val _isAssistantThinking = MutableStateFlow(false)
    val isAssistantThinking: StateFlow<Boolean> = _isAssistantThinking.asStateFlow()

    // Outreach Generator State
    private val _currentOutreachDraft = MutableStateFlow<OutreachContent?>(null)
    val currentOutreachDraft: StateFlow<OutreachContent?> = _currentOutreachDraft.asStateFlow()

    private val _isGeneratingOutreach = MutableStateFlow(false)
    val isGeneratingOutreach: StateFlow<Boolean> = _isGeneratingOutreach.asStateFlow()

    // Toast message for UI
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    init {
        // Seed initial sample data if empty on first launch
        viewModelScope.launch {
            allLeads.collect { leads ->
                if (leads.isEmpty()) {
                    seedInitialLeads()
                }
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun setSearchFilter(text: String) {
        _searchFilterText.value = text
    }

    fun setCategoryFilter(category: String?) {
        _selectedCategoryFilter.value = category
    }

    fun setStatusFilter(status: String?) {
        _selectedStatusFilter.value = status
    }

    fun setWebsiteFilter(filter: String?) {
        _selectedWebsiteFilter.value = filter
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun selectLead(leadId: Long?) {
        _selectedLeadId.value = leadId
        if (leadId != null) {
            viewModelScope.launch {
                repository.getLeadById(leadId).collect { lead ->
                    _selectedLead.value = lead
                }
            }
            viewModelScope.launch {
                repository.getNotesForLead(leadId).collect { notes ->
                    _selectedLeadNotes.value = notes
                }
            }
            viewModelScope.launch {
                repository.getTagsForLead(leadId).collect { tags ->
                    _selectedLeadTags.value = tags
                }
            }
            viewModelScope.launch {
                repository.getDraftsForLead(leadId).collect { drafts ->
                    _selectedLeadDrafts.value = drafts
                }
            }
        } else {
            _selectedLead.value = null
            _selectedLeadNotes.value = emptyList()
            _selectedLeadTags.value = emptyList()
            _selectedLeadDrafts.value = emptyList()
            _currentOutreachDraft.value = null
        }
    }

    fun toggleLeadSelection(id: Long) {
        val current = _selectedLeadIds.value.toMutableSet()
        if (current.contains(id)) {
            current.remove(id)
        } else {
            current.add(id)
        }
        _selectedLeadIds.value = current
    }

    fun selectAllFilteredLeads(selectAll: Boolean) {
        if (selectAll) {
            _selectedLeadIds.value = filteredLeads.value.map { it.id }.toSet()
        } else {
            _selectedLeadIds.value = emptySet()
        }
    }

    fun deleteSelectedLeads() {
        val ids = _selectedLeadIds.value.toList()
        viewModelScope.launch {
            ids.forEach { repository.deleteLead(it) }
            _selectedLeadIds.value = emptySet()
            showToast("Deleted ${ids.size} leads")
        }
    }

    fun deleteLead(leadId: Long) {
        viewModelScope.launch {
            repository.deleteLead(leadId)
            showToast("Lead deleted")
        }
    }

    fun updateLeadStatus(id: Long, status: String) {
        viewModelScope.launch {
            repository.updateLeadStatus(id, status)
            showToast("Updated lead status to $status")
        }
    }

    fun updateLeadPriority(id: Long, priority: String) {
        viewModelScope.launch {
            repository.updateLeadPriority(id, priority)
            showToast("Updated lead priority to $priority")
        }
    }

    fun toggleLeadFavorite(id: Long) {
        viewModelScope.launch {
            repository.toggleFavorite(id)
        }
    }

    fun addNoteToSelectedLead(note: String) {
        val leadId = _selectedLeadId.value ?: return
        if (note.isBlank()) return
        viewModelScope.launch {
            repository.addNote(leadId, note)
            showToast("Note saved")
        }
    }

    fun deleteNote(noteId: Long) {
        viewModelScope.launch {
            repository.deleteNote(noteId)
        }
    }

    fun addTagToSelectedLead(tag: String) {
        val leadId = _selectedLeadId.value ?: return
        if (tag.isBlank()) return
        viewModelScope.launch {
            repository.addTag(leadId, tag)
        }
    }

    fun removeTagFromSelectedLead(tag: String) {
        val leadId = _selectedLeadId.value ?: return
        viewModelScope.launch {
            repository.removeTag(leadId, tag)
        }
    }

    // --- Lead Extraction ---
    fun startExtraction(
        category: String,
        location: String,
        radiusKm: Int,
        maxResults: Int,
        filterRequireNoWebsite: Boolean,
        filterRequirePhone: Boolean
    ) {
        extractionJob?.cancel()
        extractionJob = viewModelScope.launch {
            val isDemo = settingsManager.settings.value.isDemoMode
            val res = extractionService.startExtraction(
                category = category,
                location = location,
                radiusKm = radiusKm,
                maxResults = maxResults,
                filterRequireNoWebsite = filterRequireNoWebsite,
                filterRequirePhone = filterRequirePhone,
                isDemoMode = isDemo
            )
            if (res.isSuccess) {
                showToast("Successfully extracted ${res.getOrNull()} leads!")
            } else {
                showToast("Extraction finished or stopped: ${res.exceptionOrNull()?.localizedMessage}")
            }
        }
    }

    fun stopExtraction() {
        extractionJob?.cancel()
        extractionJob = null
        showToast("Extraction cancelled")
    }

    // --- Website Analyzer ---
    fun auditWebsite(url: String) {
        if (url.isBlank()) return
        viewModelScope.launch {
            _isAuditingUrl.value = true
            _standaloneAuditResult.value = null
            val result = websiteAnalyzer.auditWebsite(url)
            _standaloneAuditResult.value = result
            _isAuditingUrl.value = false
        }
    }

    // --- AI Gemini Business Analysis ---
    fun analyzeLeadWithAI(lead: LeadEntity) {
        viewModelScope.launch {
            showToast("Analyzing '${lead.businessName}' with Gemini...")
            val result = geminiService.analyzeBusiness(lead)
            if (result.isSuccess) {
                val data = result.getOrThrow()
                val updated = lead.copy(
                    leadScore = data.leadScore,
                    leadPriority = data.priority,
                    leadReason = data.reasons.joinToString(" • "),
                    aiSummary = data.businessSummary,
                    aiRecommendations = data.recommendedServices.joinToString(" | ") + " [Angle: " + data.outreachAngle + "]",
                    updatedAt = System.currentTimeMillis()
                )
                repository.updateLead(updated)
                _selectedLead.value = updated
                showToast("AI Analysis complete! Score updated to ${data.leadScore}")
            } else {
                showToast("AI Analysis failed: ${result.exceptionOrNull()?.localizedMessage}")
            }
        }
    }

    // --- Outreach Draft Generation ---
    fun generateOutreach(
        lead: LeadEntity,
        channel: String, // EMAIL, WHATSAPP, DM, SMS
        tone: String,
        customInstructions: String = ""
    ) {
        viewModelScope.launch {
            _isGeneratingOutreach.value = true
            val res = geminiService.generateOutreachDraft(lead, channel, tone, customInstructions)
            if (res.isSuccess) {
                val content = res.getOrThrow()
                _currentOutreachDraft.value = content

                // Save into lead's outreach drafts table
                repository.saveDraft(
                    OutreachDraftEntity(
                        leadId = lead.id,
                        channel = channel,
                        tone = tone,
                        subject = content.subject,
                        body = content.body
                    )
                )
                showToast("Generated $channel draft successfully!")
            } else {
                showToast("Outreach generation failed: ${res.exceptionOrNull()?.localizedMessage}")
            }
            _isGeneratingOutreach.value = false
        }
    }

    fun refineOutreach(instruction: String) {
        val current = _currentOutreachDraft.value ?: return
        viewModelScope.launch {
            _isGeneratingOutreach.value = true
            val res = geminiService.refineOutreach(current.body, instruction)
            if (res.isSuccess) {
                _currentOutreachDraft.value = current.copy(body = res.getOrThrow())
                showToast("Draft updated: $instruction")
            } else {
                showToast("Refinement failed: ${res.exceptionOrNull()?.localizedMessage}")
            }
            _isGeneratingOutreach.value = false
        }
    }

    // --- AI Lead Assistant ---
    fun sendAssistantQuery(question: String) {
        if (question.isBlank()) return
        val userMsg = AssistantMessage(sender = "USER", text = question)
        _assistantMessages.value = _assistantMessages.value + userMsg

        viewModelScope.launch {
            _isAssistantThinking.value = true

            // Build controlled database context safely without exposing SQL
            val leads = allLeads.value
            val total = leads.size
            val noWebCount = leads.count { it.website.isNullOrBlank() || it.websiteStatus == "NONE" }
            val highPotCount = leads.count { it.leadScore >= 80 }
            val avgScore = if (leads.isNotEmpty()) leads.map { it.leadScore }.average().toInt() else 0

            val summaryContext = """
                Total Businesses: $total
                Businesses Without Website: $noWebCount
                High-Potential Leads (Score >= 80): $highPotCount
                Average Lead Score: $avgScore
            """.trimIndent()

            val topLeadsSample = leads.take(15).joinToString("\n") {
                "- Name: ${it.businessName}, Category: ${it.category}, City: ${it.city}, Score: ${it.leadScore} (${it.leadPriority}), Web: ${it.website ?: "NONE"}, Phone: ${it.phone ?: "None"}, Quality: ${it.websiteQuality}"
            }

            val answerRes = geminiService.answerLeadFinderQuestion(question, summaryContext, topLeadsSample)
            val aiMsg = if (answerRes.isSuccess) {
                AssistantMessage(sender = "AI", text = answerRes.getOrThrow())
            } else {
                AssistantMessage(
                    sender = "AI",
                    text = "Could not query Gemini: ${answerRes.exceptionOrNull()?.localizedMessage}. Please ensure your API key is configured in Settings.",
                    isError = true
                )
            }

            _assistantMessages.value = _assistantMessages.value + aiMsg
            _isAssistantThinking.value = false
        }
    }

    // --- Export ---
    fun exportLeads(format: String, scope: String) {
        val leadsToExport = when (scope) {
            "SELECTED" -> allLeads.value.filter { _selectedLeadIds.value.contains(it.id) }
            "HIGH_POTENTIAL" -> allLeads.value.filter { it.leadScore >= 80 }
            "NO_WEBSITE" -> allLeads.value.filter { it.website.isNullOrBlank() || it.websiteStatus == "NONE" }
            "FILTERED" -> filteredLeads.value
            else -> allLeads.value
        }

        if (leadsToExport.isEmpty()) {
            showToast("No leads match the export criteria.")
            return
        }

        val content = if (format == "CSV") {
            ExportService.generateCsv(leadsToExport)
        } else {
            ExportService.generateJson(leadsToExport)
        }

        val mimeType = if (format == "CSV") "text/csv" else "application/json"
        val title = "LeadFinder_Export_${System.currentTimeMillis()}.${format.lowercase()}"

        ExportService.shareExportData(getApplication(), content, mimeType, title)
        showToast("Exported ${leadsToExport.size} leads via Share Sheet")
    }

    // --- Settings ---
    fun updateSettings(
        geminiApiKey: String? = null,
        geminiModel: String? = null,
        googleMapsApiKey: String? = null,
        defaultRadius: Int? = null,
        defaultLimit: Int? = null,
        highPotentialThreshold: Int? = null,
        isDemoMode: Boolean? = null
    ) {
        settingsManager.updateSettings(
            geminiApiKey = geminiApiKey,
            geminiModel = geminiModel,
            googleMapsApiKey = googleMapsApiKey,
            defaultRadius = defaultRadius,
            defaultLimit = defaultLimit,
            highPotentialThreshold = highPotentialThreshold,
            isDemoMode = isDemoMode
        )
        showToast("Settings updated successfully")
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllLeads()
            selectLead(null)
            showToast("All leads cleared")
        }
    }

    fun resetDemoData() {
        viewModelScope.launch {
            repository.clearAllLeads()
            seedInitialLeads()
            showToast("Reset demo data successfully")
        }
    }

    private suspend fun seedInitialLeads() {
        val initialSeeds = listOf(
            LeadEntity(
                providerPlaceId = "seed_1",
                businessName = "Grand Central Italian Trattoria",
                category = "Restaurant & Dining",
                phone = "+1 (555) 723-9912",
                website = null,
                address = "424 Lexington Ave",
                city = "New York",
                rating = 4.7,
                reviewCount = 285,
                websiteStatus = "NONE",
                websiteQuality = "NONE",
                mobileFriendly = false,
                httpsEnabled = false,
                contactFormPresent = false,
                leadScore = 90,
                leadPriority = "HIGH",
                leadReason = "+30: No website • +10: Active social • +5: Direct phone • +5: High volume (285 reviews) • +5: High-margin restaurant niche",
                aiSummary = "High-traffic Italian restaurant with 285 reviews but zero website. Relies completely on third-party aggregators.",
                aiRecommendations = "Build custom reservation website | Google Maps sync | Online digital menu"
            ),
            LeadEntity(
                providerPlaceId = "seed_2",
                businessName = "Horizon Precision Dental",
                category = "Dentist & Healthcare",
                phone = "+1 (555) 891-2234",
                website = "http://horizon-dental-old.org",
                address = "1200 Market Street #300",
                city = "San Francisco",
                rating = 4.8,
                reviewCount = 142,
                websiteStatus = "ACCESSIBLE",
                websiteQuality = "POOR",
                mobileFriendly = false,
                httpsEnabled = false,
                contactFormPresent = false,
                leadScore = 85,
                leadPriority = "HIGH",
                leadReason = "+25: Website is POOR • +15: Not mobile-friendly • +5: Verified phone • +5: 142 positive reviews • +5: Medical niche",
                aiSummary = "Outdated HTTP-only website built in 2012. Lacks mobile responsive layout and online appointment scheduling.",
                aiRecommendations = "Responsive HIPAA-compliant redesign | SSL certificate installation | Online patient booking calendar"
            ),
            LeadEntity(
                providerPlaceId = "seed_3",
                businessName = "Titan Heating & Emergency Plumbing",
                category = "Plumber & Contractors",
                phone = "+1 (555) 314-5588",
                website = null,
                address = "780 Industrial Pkwy",
                city = "Chicago",
                rating = 4.5,
                reviewCount = 89,
                websiteStatus = "NONE",
                websiteQuality = "NONE",
                mobileFriendly = false,
                httpsEnabled = false,
                contactFormPresent = false,
                leadScore = 80,
                leadPriority = "HIGH",
                leadReason = "+30: No website • +5: Phone available • +5: Multiple contact channels • +5: Active local service niche",
                aiSummary = "Emergency plumber with steady reviews but no web landing page for emergency dispatch.",
                aiRecommendations = "High-converting emergency call-to-action landing page | WhatsApp integration | Local search Ads setup"
            ),
            LeadEntity(
                providerPlaceId = "seed_4",
                businessName = "Luxe Lotus Day Spa & Wellness",
                category = "Salon & Spa",
                phone = "+1 (555) 442-1089",
                website = "https://luxelotusspa.sample.com",
                address = "55 Boutique Blvd",
                city = "Miami",
                rating = 4.3,
                reviewCount = 56,
                websiteStatus = "ACCESSIBLE",
                websiteQuality = "AVERAGE",
                mobileFriendly = true,
                httpsEnabled = true,
                contactFormPresent = false,
                leadScore = 65,
                leadPriority = "MEDIUM",
                leadReason = "+10: Website is AVERAGE • +10: Active Instagram profile • +5: Direct phone • +5: High-margin luxury niche",
                aiSummary = "Functional website exists with SSL, but has no online treatment booking or gift card purchase system.",
                aiRecommendations = "Integrated booking engine | Gift card e-commerce | Instagram booking link"
            ),
            LeadEntity(
                providerPlaceId = "seed_5",
                businessName = "Apex Auto Collision Center",
                category = "Auto Repair & Mechanics",
                phone = "+1 (555) 918-7744",
                website = null,
                address = "910 Motor City Ave",
                city = "Detroit",
                rating = 4.6,
                reviewCount = 112,
                websiteStatus = "NONE",
                websiteQuality = "NONE",
                mobileFriendly = false,
                httpsEnabled = false,
                contactFormPresent = false,
                leadScore = 85,
                leadPriority = "HIGH",
                leadReason = "+30: No website • +5: Verified phone • +5: 112 reviews • +5: High-ticket commercial auto repair",
                aiSummary = "Well-reviewed body shop with zero web presence. Losing insurance claim leads to competitors.",
                aiRecommendations = "Turnkey collision repair quote estimator | Photo upload tool | Google Business Profile optimization"
            )
        )

        for (lead in initialSeeds) {
            repository.insertLead(lead)
        }
    }
}
