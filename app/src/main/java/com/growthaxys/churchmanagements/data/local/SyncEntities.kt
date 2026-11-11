package com.growthaxys.churchmanagements.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_metadata", primaryKeys = ["key", "accountScope"])
data class SyncMetadataEntity(
    val key: String,
    val accountScope: String,
    val lastSyncedAt: Long?
)

@Entity(tableName = "sync_outbox")
data class SyncOutboxEntity(
    @PrimaryKey val operationId: String,
    val accountScope: String,
    val entityType: String,
    val entityId: String?,
    val operationType: String,
    val payload: String,
    val createdAt: Long,
    val retryCount: Int = 0,
    val syncStatus: String = "PENDING",
    val lastError: String? = null
)
