package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.model.LeadEntity
import com.example.ui.components.AppBottomNavBar
import com.example.ui.components.AppNavRail
import com.example.ui.components.OutreachGeneratorDialog
import com.example.ui.components.TopHeader
import com.example.ui.screens.AiAssistantScreen
import com.example.ui.screens.AllLeadsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ExportsScreen
import com.example.ui.screens.ExtractLeadsScreen
import com.example.ui.screens.HighPotentialScreen
import com.example.ui.screens.LeadDetailModal
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.WebsiteAnalyzerScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TechNavyDark
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.LeadFinderViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: LeadFinderViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                LeadFinderApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun LeadFinderApp(viewModel: LeadFinderViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val settings by viewModel.appSettings.collectAsState()
    val allLeads by viewModel.allLeads.collectAsState()
    val highPotentialLeads by viewModel.highPotentialLeads.collectAsState()
    val filteredLeads by viewModel.filteredLeads.collectAsState()
    val recentSearches by viewModel.recentSearches.collectAsState()
    val recentLogs by viewModel.recentLogs.collectAsState()
    val extractionProgress by viewModel.extractionProgress.collectAsState()

    val selectedLead by viewModel.selectedLead.collectAsState()
    val selectedLeadNotes by viewModel.selectedLeadNotes.collectAsState()
    val selectedLeadTags by viewModel.selectedLeadTags.collectAsState()
    val selectedLeadDrafts by viewModel.selectedLeadDrafts.collectAsState()
    val selectedLeadIds by viewModel.selectedLeadIds.collectAsState()

    val searchFilterText by viewModel.searchFilterText.collectAsState()
    val categoryFilter by viewModel.selectedCategoryFilter.collectAsState()
    val statusFilter by viewModel.selectedStatusFilter.collectAsState()
    val websiteFilter by viewModel.selectedWebsiteFilter.collectAsState()

    val standaloneAuditResult by viewModel.standaloneAuditResult.collectAsState()
    val isAuditingUrl by viewModel.isAuditingUrl.collectAsState()

    val assistantMessages by viewModel.assistantMessages.collectAsState()
    val isAssistantThinking by viewModel.isAssistantThinking.collectAsState()

    val currentOutreachDraft by viewModel.currentOutreachDraft.collectAsState()
    val isGeneratingOutreach by viewModel.isGeneratingOutreach.collectAsState()

    val toastMessage by viewModel.toastMessage.collectAsState()

    // Outreach modal state
    var outreachDialogLead by remember { mutableStateOf<LeadEntity?>(null) }

    // Display toasts safely
    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(viewModel.getApplication(), it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(TechNavyDark)) {
        val isWideScreen = maxWidth >= 600.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = TechNavyDark,
            topBar = {
                TopHeader(
                    settings = settings,
                    searchQuery = searchFilterText,
                    onSearchChange = {
                        viewModel.setSearchFilter(it)
                        if (currentScreen != AppScreen.ALL_LEADS && it.isNotBlank()) {
                            viewModel.navigateTo(AppScreen.ALL_LEADS)
                        }
                    },
                    onSettingsClick = { viewModel.navigateTo(AppScreen.SETTINGS) }
                )
            },
            bottomBar = {
                if (!isWideScreen) {
                    AppBottomNavBar(
                        currentScreen = currentScreen,
                        onSelectScreen = { viewModel.navigateTo(it) }
                    )
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isWideScreen) {
                    AppNavRail(
                        currentScreen = currentScreen,
                        onSelectScreen = { viewModel.navigateTo(it) }
                    )
                }

                Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                    when (currentScreen) {
                        AppScreen.DASHBOARD -> {
                            DashboardScreen(
                                leads = allLeads,
                                highPotentialLeads = highPotentialLeads,
                                recentLogs = recentLogs,
                                onNavigate = { viewModel.navigateTo(it) },
                                onSelectLead = { viewModel.selectLead(it) },
                                onToggleFavorite = { viewModel.toggleLeadFavorite(it) },
                                onGenerateOutreach = { outreachDialogLead = it }
                            )
                        }

                        AppScreen.EXTRACT_LEADS -> {
                            ExtractLeadsScreen(
                                progress = extractionProgress,
                                recentSearches = recentSearches,
                                onStartExtraction = { cat, loc, rad, lim, noWeb, phone ->
                                    viewModel.startExtraction(cat, loc, rad, lim, noWeb, phone)
                                },
                                onStopExtraction = { viewModel.stopExtraction() }
                            )
                        }

                        AppScreen.ALL_LEADS -> {
                            AllLeadsScreen(
                                leads = filteredLeads,
                                selectedLeadIds = selectedLeadIds,
                                selectedCategoryFilter = categoryFilter,
                                selectedStatusFilter = statusFilter,
                                selectedWebsiteFilter = websiteFilter,
                                onSetCategoryFilter = { viewModel.setCategoryFilter(it) },
                                onSetStatusFilter = { viewModel.setStatusFilter(it) },
                                onSetWebsiteFilter = { viewModel.setWebsiteFilter(it) },
                                onToggleSelect = { viewModel.toggleLeadSelection(it) },
                                onSelectAll = { viewModel.selectAllFilteredLeads(it) },
                                onDeleteSelected = { viewModel.deleteSelectedLeads() },
                                onExportSelected = { viewModel.exportLeads("CSV", "SELECTED") },
                                onSelectLead = { viewModel.selectLead(it) },
                                onToggleFavorite = { viewModel.toggleLeadFavorite(it) },
                                onGenerateOutreach = { outreachDialogLead = it }
                            )
                        }

                        AppScreen.HIGH_POTENTIAL -> {
                            HighPotentialScreen(
                                highPotentialLeads = highPotentialLeads,
                                selectedLeadIds = selectedLeadIds,
                                onToggleSelect = { viewModel.toggleLeadSelection(it) },
                                onSelectLead = { viewModel.selectLead(it) },
                                onToggleFavorite = { viewModel.toggleLeadFavorite(it) },
                                onGenerateOutreach = { outreachDialogLead = it }
                            )
                        }

                        AppScreen.AI_ASSISTANT -> {
                            AiAssistantScreen(
                                messages = assistantMessages,
                                isThinking = isAssistantThinking,
                                onSendMessage = { viewModel.sendAssistantQuery(it) }
                            )
                        }

                        AppScreen.WEBSITE_ANALYZER -> {
                            WebsiteAnalyzerScreen(
                                auditResult = standaloneAuditResult,
                                isAuditing = isAuditingUrl,
                                onAuditWebsite = { viewModel.auditWebsite(it) }
                            )
                        }

                        AppScreen.EXPORTS -> {
                            ExportsScreen(
                                allLeads = allLeads,
                                selectedLeadIds = selectedLeadIds,
                                onExport = { fmt, scope -> viewModel.exportLeads(fmt, scope) }
                            )
                        }

                        AppScreen.SETTINGS -> {
                            SettingsScreen(
                                settings = settings,
                                geminiService = viewModel.geminiService,
                                onUpdateSettings = { geminiKey, model, mapsKey, radius, limit, threshold, demo ->
                                    viewModel.updateSettings(geminiKey, model, mapsKey, radius, limit, threshold, demo)
                                },
                                onClearData = { viewModel.clearAllData() },
                                onResetDemoData = { viewModel.resetDemoData() }
                            )
                        }
                    }
                }
            }
        }
    }

    // Lead Detail Modal
    if (selectedLead != null) {
        LeadDetailModal(
            lead = selectedLead!!,
            notes = selectedLeadNotes,
            tags = selectedLeadTags,
            drafts = selectedLeadDrafts,
            onClose = { viewModel.selectLead(null) },
            onAnalyzeAi = { viewModel.analyzeLeadWithAI(selectedLead!!) },
            onOpenOutreachDialog = { outreachDialogLead = selectedLead },
            onUpdateStatus = { viewModel.updateLeadStatus(selectedLead!!.id, it) },
            onAddNote = { viewModel.addNoteToSelectedLead(it) },
            onDeleteNote = { viewModel.deleteNote(it) },
            onAddTag = { viewModel.addTagToSelectedLead(it) },
            onRemoveTag = { viewModel.removeTagFromSelectedLead(it) },
            onDeleteLead = {
                viewModel.deleteLead(selectedLead!!.id)
                viewModel.selectLead(null)
            }
        )
    }

    // Outreach Generator Dialog
    if (outreachDialogLead != null) {
        OutreachGeneratorDialog(
            lead = outreachDialogLead!!,
            currentDraft = currentOutreachDraft,
            isGenerating = isGeneratingOutreach,
            onGenerate = { channel, tone, instructions ->
                viewModel.generateOutreach(outreachDialogLead!!, channel, tone, instructions)
            },
            onRefine = { instruction ->
                viewModel.refineOutreach(instruction)
            },
            onDismiss = { outreachDialogLead = null }
        )
    }
}
