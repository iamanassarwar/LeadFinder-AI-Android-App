package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.ActivityLogEntity
import com.example.data.model.LeadEntity
import com.example.data.model.LeadNoteEntity
import com.example.data.model.LeadTagEntity
import com.example.data.model.OutreachDraftEntity
import com.example.data.model.SearchHistoryEntity

@Database(
    entities = [
        LeadEntity::class,
        LeadNoteEntity::class,
        LeadTagEntity::class,
        OutreachDraftEntity::class,
        SearchHistoryEntity::class,
        ActivityLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class LeadFinderDatabase : RoomDatabase() {
    abstract fun leadDao(): LeadDao

    companion object {
        @Volatile
        private var INSTANCE: LeadFinderDatabase? = null

        fun getInstance(context: Context): LeadFinderDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LeadFinderDatabase::class.java,
                    "leadfinder_database.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
