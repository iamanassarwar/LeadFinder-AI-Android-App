package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "outreach_drafts",
    foreignKeys = [
        ForeignKey(
            entity = LeadEntity::class,
            parentColumns = ["id"],
            childColumns = ["lead_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["lead_id"])]
)
data class OutreachDraftEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "lead_id")
    val leadId: Long,

    @ColumnInfo(name = "channel")
    val channel: String = "EMAIL", // EMAIL, WHATSAPP, DM, SMS

    @ColumnInfo(name = "tone")
    val tone: String = "PROFESSIONAL", // PROFESSIONAL, FRIENDLY, DIRECT

    @ColumnInfo(name = "subject")
    val subject: String = "",

    @ColumnInfo(name = "body")
    val body: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
