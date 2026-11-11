package com.growthaxys.churchmanagements.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [SyncMetadataEntity::class, SyncOutboxEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun syncDao(): SyncDao
}
