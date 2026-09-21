package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "searches")
data class SearchHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "keyword")
    val keyword: String,

    @ColumnInfo(name = "location")
    val location: String,

    @ColumnInfo(name = "radius_km")
    val radiusKm: Int,

    @ColumnInfo(name = "result_count")
    val resultCount: Int,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
