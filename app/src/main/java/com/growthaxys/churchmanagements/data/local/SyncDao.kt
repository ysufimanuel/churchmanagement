package com.growthaxys.churchmanagements.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncDao {
    @Query("SELECT * FROM sync_metadata WHERE accountScope = :accountScope")
    fun observeMetadata(accountScope: String): Flow<List<SyncMetadataEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMetadata(item: SyncMetadataEntity)

    @Query("SELECT * FROM sync_outbox WHERE accountScope = :accountScope AND syncStatus = 'PENDING' ORDER BY createdAt")
    fun observePending(accountScope: String): Flow<List<SyncOutboxEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun enqueue(item: SyncOutboxEntity)

    @Query("UPDATE sync_outbox SET retryCount = retryCount + 1, lastError = :error WHERE operationId = :operationId AND accountScope = :accountScope")
    suspend fun recordFailure(operationId: String, accountScope: String, error: String)

    @Query("UPDATE sync_outbox SET syncStatus = 'SYNCED', lastError = NULL WHERE operationId = :operationId AND accountScope = :accountScope")
    suspend fun markSynced(operationId: String, accountScope: String)
}
