package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "lead_tags",
    foreignKeys = [
        ForeignKey(
            entity = LeadEntity::class,
            parentColumns = ["id"],
            childColumns = ["lead_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["lead_id"]), Index(value = ["tag_name"])]
)
data class LeadTagEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "lead_id")
    val leadId: Long,

    @ColumnInfo(name = "tag_name")
    val tagName: String
)
