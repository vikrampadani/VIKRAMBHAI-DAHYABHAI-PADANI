package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.JapaSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JapaSessionDao {
    @Query("SELECT * FROM japa_sessions ORDER BY completedAt DESC")
    fun getAllSessions(): Flow<List<JapaSessionEntity>>

    @Query("SELECT * FROM japa_sessions WHERE completedAt BETWEEN :startTime AND :endTime ORDER BY completedAt DESC")
    fun getSessionsByDateRange(startTime: Long, endTime: Long): Flow<List<JapaSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: JapaSessionEntity): Long

    @Query("DELETE FROM japa_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: Long)

    @Query("DELETE FROM japa_sessions")
    suspend fun clearAllSessions()

    @Query("SELECT SUM(count) FROM japa_sessions")
    fun getLifetimeTotalCount(): Flow<Int?>

    @Query("SELECT COUNT(*) FROM japa_sessions")
    fun getLifetimeSessionCount(): Flow<Int>

    @Query("SELECT SUM(count / 108) FROM japa_sessions")
    fun getCompleted108RoundsCount(): Flow<Int?>

    @Query("SELECT mantraName FROM japa_sessions GROUP BY mantraName ORDER BY SUM(count) DESC LIMIT 1")
    fun getMostUsedMantraName(): Flow<String?>

    @Query("SELECT SUM(count) FROM japa_sessions WHERE completedAt >= :sinceTimestamp")
    fun getTotalCountSince(sinceTimestamp: Long): Flow<Int?>
}
