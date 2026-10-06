package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_stats")
data class DailyStatsEntity(
    @PrimaryKey
    val date: String, // Format: yyyy-MM-dd
    val totalCount: Int,
    val roundsCompleted: Int = 0,
    val target: Int = 108
)
