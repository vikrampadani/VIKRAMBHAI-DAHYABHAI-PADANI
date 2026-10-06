package com.example.data.repository

import com.example.data.local.dao.DailyStatsDao
import com.example.data.local.dao.JapaSessionDao
import com.example.data.local.entity.DailyStatsEntity
import com.example.data.local.entity.JapaSessionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class JapaRepository(
    private val sessionDao: JapaSessionDao,
    private val statsDao: DailyStatsDao
) {
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun getTodayDateString(): String = LocalDate.now().format(dateFormatter)

    val allSessions: Flow<List<JapaSessionEntity>> = sessionDao.getAllSessions()

    fun getSessionsForDateRange(startTime: Long, endTime: Long): Flow<List<JapaSessionEntity>> {
        return sessionDao.getSessionsByDateRange(startTime, endTime)
    }

    suspend fun recordSession(
        mantraId: Long,
        mantraName: String,
        count: Int,
        target: Int,
        durationMs: Long,
        startedAt: Long,
        completedAt: Long = System.currentTimeMillis(),
        isCompleted: Boolean = count >= target
    ): Long {
        if (count <= 0) return -1

        val session = JapaSessionEntity(
            mantraId = mantraId,
            mantraName = mantraName,
            count = count,
            target = target,
            durationMs = durationMs,
            startedAt = startedAt,
            completedAt = completedAt,
            isCompleted = isCompleted
        )

        val id = sessionDao.insertSession(session)

        // Update DailyStats
        val todayStr = getTodayDateString()
        val existingStats = statsDao.getDailyStatsDirect(todayStr)
        val newTotal = (existingStats?.totalCount ?: 0) + count
        val newRounds = (existingStats?.roundsCompleted ?: 0) + (count / 108)

        statsDao.insertOrUpdate(
            DailyStatsEntity(
                date = todayStr,
                totalCount = newTotal,
                roundsCompleted = newRounds,
                target = target
            )
        )

        return id
    }

    suspend fun deleteSession(id: Long) {
        sessionDao.deleteSessionById(id)
    }

    suspend fun clearAllHistory() {
        sessionDao.clearAllSessions()
        statsDao.clearAll()
    }

    fun getTodayStats(): Flow<DailyStatsEntity?> {
        return statsDao.getDailyStats(getTodayDateString())
    }

    val lifetimeTotalCount: Flow<Int> = sessionDao.getLifetimeTotalCount().map { it ?: 0 }
    val lifetimeSessionCount: Flow<Int> = sessionDao.getLifetimeSessionCount()
    val completed108RoundsCount: Flow<Int> = sessionDao.getCompleted108RoundsCount().map { it ?: 0 }
    val mostUsedMantra: Flow<String?> = sessionDao.getMostUsedMantraName()

    fun getRecent7DaysStats(): Flow<List<DailyStatsEntity>> = statsDao.getRecentDailyStats(7)
    fun getRecent30DaysStats(): Flow<List<DailyStatsEntity>> = statsDao.getRecentDailyStats(30)

    fun getTotalCountSince(sinceTimestamp: Long): Flow<Int> {
        return sessionDao.getTotalCountSince(sinceTimestamp).map { it ?: 0 }
    }

    suspend fun calculateCurrentStreak(): Int {
        val today = LocalDate.now()
        var streak = 0
        var checkDate = today

        // Check if user chanted today
        val todayStats = statsDao.getDailyStatsDirect(today.format(dateFormatter))
        if (todayStats != null && todayStats.totalCount > 0) {
            streak++
            checkDate = today.minusDays(1)
        } else {
            // If haven't chanted today yet, check yesterday to keep streak alive
            val yesterdayStats = statsDao.getDailyStatsDirect(today.minusDays(1).format(dateFormatter))
            if (yesterdayStats != null && yesterdayStats.totalCount > 0) {
                streak++
                checkDate = today.minusDays(2)
            } else {
                return 0
            }
        }

        // Count consecutive preceding days
        for (i in 0 until 365) {
            val stats = statsDao.getDailyStatsDirect(checkDate.format(dateFormatter))
            if (stats != null && stats.totalCount > 0) {
                streak++
                checkDate = checkDate.minusDays(1)
            } else {
                break
            }
        }

        return streak
    }

    suspend fun calculateLongestStreak(): Int {
        val allStats = statsDao.getRecentDailyStatsDirect(365)
        if (allStats.isEmpty()) return 0

        var maxStreak = 0
        var currentStreak = 0
        var previousDate: LocalDate? = null

        // allStats ordered by date DESC
        for (stat in allStats) {
            val statDate = runCatching { LocalDate.parse(stat.date, dateFormatter) }.getOrNull()
                ?: continue

            if (stat.totalCount > 0) {
                if (previousDate == null || previousDate.minusDays(1) == statDate) {
                    currentStreak++
                } else {
                    currentStreak = 1
                }
                if (currentStreak > maxStreak) {
                    maxStreak = currentStreak
                }
            } else {
                currentStreak = 0
            }
            previousDate = statDate
        }

        return maxStreak
    }
}
