package com.kamal.creditliability.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.kamal.creditliability.data.entity.EmiObligationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EmiObligationDao {
    @Query("SELECT * FROM emi_obligations ORDER BY active DESC, nextDueDate ASC")
    fun observeAll(): Flow<List<EmiObligationEntity>>

    @Query("SELECT * FROM emi_obligations WHERE cardId = :cardId ORDER BY nextDueDate ASC")
    fun observeByCard(cardId: Long): Flow<List<EmiObligationEntity>>

    @Insert
    suspend fun insert(value: EmiObligationEntity): Long

    @Update
    suspend fun update(value: EmiObligationEntity): Int

    @Delete
    suspend fun delete(value: EmiObligationEntity): Int

    @Query("DELETE FROM emi_obligations")
    suspend fun deleteAll()
}