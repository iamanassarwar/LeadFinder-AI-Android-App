package com.example.data.repository

import com.example.data.local.LeadDao
import com.example.data.model.ActivityLogEntity
import com.example.data.model.LeadEntity
import com.example.data.model.LeadNoteEntity
import com.example.data.model.LeadTagEntity
import com.example.data.model.OutreachDraftEntity
import com.example.data.model.SearchHistoryEntity
import kotlinx.coroutines.flow.Flow

class LeadRepository(private val leadDao: LeadDao) {
    val allLeads: Flow<List<LeadEntity>> = leadDao.getAllLeads()
    val highPotentialLeads: Flow<List<LeadEntity>> = leadDao.getHighPotentialLeads(80)
    val noWebsiteLeads: Flow<List<LeadEntity>> = leadDao.getLeadsWithoutWebsite()
    val recentSearches: Flow<List<SearchHistoryEntity>> = leadDao.getRecentSearches()
    val recentLogs: Flow<List<ActivityLogEntity>> = leadDao.getRecentLogs(30)

    fun getLeadById(id: Long): Flow<LeadEntity?> = leadDao.getLeadById(id)

    suspend fun getLeadByIdDirect(id: Long): LeadEntity? = leadDao.getLeadByIdDirect(id)

    fun getNotesForLead(leadId: Long): Flow<List<LeadNoteEntity>> = leadDao.getNotesForLead(leadId)

    fun getTagsForLead(leadId: Long): Flow<List<LeadTagEntity>> = leadDao.getTagsForLead(leadId)

    fun getDraftsForLead(leadId: Long): Flow<List<OutreachDraftEntity>> = leadDao.getDraftsForLead(leadId)

    suspend fun findDuplicate(
        providerPlaceId: String?,
        businessName: String,
        phone: String?,
        address: String?
    ): LeadEntity? {
        return leadDao.findDuplicateLead(providerPlaceId, businessName, phone, address)
    }

    suspend fun insertLead(lead: LeadEntity): Long {
        val id = leadDao.insertLead(lead)
        leadDao.insertLog(
            ActivityLogEntity(
                leadId = id,
                action = "LEAD_CREATED",
                details = "Lead '${lead.businessName}' added with score ${lead.leadScore} (${lead.leadPriority})"
            )
        )
        return id
    }

    suspend fun updateLead(lead: LeadEntity) {
        leadDao.updateLead(lead)
    }

    suspend fun updateLeadStatus(id: Long, status: String) {
        val lead = leadDao.getLeadByIdDirect(id) ?: return
        leadDao.updateLead(lead.copy(status = status, updatedAt = System.currentTimeMillis()))
        leadDao.insertLog(
            ActivityLogEntity(
                leadId = id,
                action = "STATUS_CHANGED",
                details = "Status changed to $status"
            )
        )
    }

    suspend fun updateLeadPriority(id: Long, priority: String) {
        val lead = leadDao.getLeadByIdDirect(id) ?: return
        leadDao.updateLead(lead.copy(leadPriority = priority, updatedAt = System.currentTimeMillis()))
    }

    suspend fun toggleFavorite(id: Long) {
        val lead = leadDao.getLeadByIdDirect(id) ?: return
        leadDao.updateLead(lead.copy(isFavorite = !lead.isFavorite, updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteLead(id: Long) {
        val lead = leadDao.getLeadByIdDirect(id)
        leadDao.deleteLeadById(id)
        if (lead != null) {
            leadDao.insertLog(
                ActivityLogEntity(
                    leadId = null,
                    action = "LEAD_DELETED",
                    details = "Deleted lead '${lead.businessName}'"
                )
            )
        }
    }

    suspend fun clearAllLeads() {
        leadDao.clearAllLeads()
        leadDao.insertLog(
            ActivityLogEntity(
                action = "DATABASE_CLEARED",
                details = "All leads cleared from database"
            )
        )
    }

    suspend fun addNote(leadId: Long, content: String): Long {
        val id = leadDao.insertNote(LeadNoteEntity(leadId = leadId, content = content))
        leadDao.insertLog(
            ActivityLogEntity(
                leadId = leadId,
                action = "NOTE_ADDED",
                details = "Note recorded: ${content.take(40)}..."
            )
        )
        return id
    }

    suspend fun deleteNote(id: Long) {
        leadDao.deleteNote(id)
    }

    suspend fun addTag(leadId: Long, tag: String): Long {
        return leadDao.insertTag(LeadTagEntity(leadId = leadId, tagName = tag.trim().lowercase()))
    }

    suspend fun removeTag(leadId: Long, tag: String) {
        leadDao.deleteTag(leadId, tag.trim().lowercase())
    }

    suspend fun saveDraft(draft: OutreachDraftEntity): Long {
        val id = leadDao.insertDraft(draft)
        leadDao.insertLog(
            ActivityLogEntity(
                leadId = draft.leadId,
                action = "DRAFT_SAVED",
                details = "Saved ${draft.channel} outreach draft (${draft.tone})"
            )
        )
        return id
    }

    suspend fun deleteDraft(id: Long) {
        leadDao.deleteDraft(id)
    }

    suspend fun recordSearch(keyword: String, location: String, radiusKm: Int, count: Int): Long {
        return leadDao.insertSearch(
            SearchHistoryEntity(
                keyword = keyword,
                location = location,
                radiusKm = radiusKm,
                resultCount = count
            )
        )
    }
}
