package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ActivityLogEntity
import com.example.data.model.LeadEntity
import com.example.data.model.LeadNoteEntity
import com.example.data.model.LeadTagEntity
import com.example.data.model.OutreachDraftEntity
import com.example.data.model.SearchHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LeadDao {
    @Query("SELECT * FROM businesses ORDER BY created_at DESC")
    fun getAllLeads(): Flow<List<LeadEntity>>

    @Query("SELECT * FROM businesses WHERE id = :id LIMIT 1")
    fun getLeadById(id: Long): Flow<LeadEntity?>

    @Query("SELECT * FROM businesses WHERE id = :id LIMIT 1")
    suspend fun getLeadByIdDirect(id: Long): LeadEntity?

    @Query("SELECT * FROM businesses WHERE lead_score >= :minScore ORDER BY lead_score DESC")
    fun getHighPotentialLeads(minScore: Int = 80): Flow<List<LeadEntity>>

    @Query("SELECT * FROM businesses WHERE website IS NULL OR website = '' OR website_status = 'NONE' ORDER BY lead_score DESC")
    fun getLeadsWithoutWebsite(): Flow<List<LeadEntity>>

    @Query("SELECT * FROM businesses WHERE status = :status ORDER BY created_at DESC")
    fun getLeadsByStatus(status: String): Flow<List<LeadEntity>>

    @Query("SELECT * FROM businesses WHERE category LIKE '%' || :category || '%' ORDER BY lead_score DESC")
    fun getLeadsByCategory(category: String): Flow<List<LeadEntity>>

    @Query("""
        SELECT * FROM businesses 
        WHERE (provider_place_id IS NOT NULL AND provider_place_id = :providerPlaceId)
           OR (LOWER(business_name) = LOWER(:businessName) AND (:phone IS NOT NULL AND phone = :phone))
           OR (LOWER(business_name) = LOWER(:businessName) AND (:address IS NOT NULL AND LOWER(address) = LOWER(:address)))
        LIMIT 1
    """)
    suspend fun findDuplicateLead(
        providerPlaceId: String?,
        businessName: String,
        phone: String?,
        address: String?
    ): LeadEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLead(lead: LeadEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeads(leads: List<LeadEntity>): List<Long>

    @Update
    suspend fun updateLead(lead: LeadEntity)

    @Delete
    suspend fun deleteLead(lead: LeadEntity)

    @Query("DELETE FROM businesses WHERE id = :id")
    suspend fun deleteLeadById(id: Long)

    @Query("DELETE FROM businesses")
    suspend fun clearAllLeads()

    // --- Notes ---
    @Query("SELECT * FROM lead_notes WHERE lead_id = :leadId ORDER BY created_at DESC")
    fun getNotesForLead(leadId: Long): Flow<List<LeadNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: LeadNoteEntity): Long

    @Query("DELETE FROM lead_notes WHERE id = :id")
    suspend fun deleteNote(id: Long)

    // --- Tags ---
    @Query("SELECT * FROM lead_tags WHERE lead_id = :leadId")
    fun getTagsForLead(leadId: Long): Flow<List<LeadTagEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: LeadTagEntity): Long

    @Query("DELETE FROM lead_tags WHERE lead_id = :leadId AND tag_name = :tagName")
    suspend fun deleteTag(leadId: Long, tagName: String)

    // --- Outreach Drafts ---
    @Query("SELECT * FROM outreach_drafts WHERE lead_id = :leadId ORDER BY created_at DESC")
    fun getDraftsForLead(leadId: Long): Flow<List<OutreachDraftEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDraft(draft: OutreachDraftEntity): Long

    @Query("DELETE FROM outreach_drafts WHERE id = :id")
    suspend fun deleteDraft(id: Long)

    // --- Search History ---
    @Query("SELECT * FROM searches ORDER BY created_at DESC LIMIT 20")
    fun getRecentSearches(): Flow<List<SearchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearch(search: SearchHistoryEntity): Long

    // --- Activity Logs ---
    @Query("SELECT * FROM activity_logs ORDER BY created_at DESC LIMIT :limit")
    fun getRecentLogs(limit: Int = 30): Flow<List<ActivityLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ActivityLogEntity): Long
}
