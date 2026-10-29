package com.kamal.creditliability.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.kamal.creditliability.data.entity.SmsMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SmsMessageDao {
    @Query("SELECT * FROM sms_messages ORDER BY receivedAt DESC, id DESC")
    fun observeAll(): Flow<List<SmsMessageEntity>>

    @Query("SELECT * FROM sms_messages WHERE processed = 0 ORDER BY receivedAt ASC")
    fun observeUnprocessed(): Flow<List<SmsMessageEntity>>

    @Query("SELECT * FROM sms_messages WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): SmsMessageEntity?

    @Query("""
        SELECT id FROM sms_messages
        WHERE sender = :sender AND body = :body
        AND receivedAt BETWEEN :receivedAt - 5000 AND :receivedAt + 5000
        LIMIT 1
    """)
    suspend fun findDuplicateId(sender: String, body: String, receivedAt: Long): Long?

    @Insert
    suspend fun insert(message: SmsMessageEntity): Long

    @Update
    suspend fun update(message: SmsMessageEntity): Int

    @Delete
    suspend fun delete(message: SmsMessageEntity): Int

    @Query("DELETE FROM sms_messages")
    suspend fun deleteAll()
}
