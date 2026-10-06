package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "japa_sessions")
data class JapaSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val mantraId: Long,
    val mantraName: String,
    val count: Int,
    val target: Int,
    val durationMs: Long,
    val startedAt: Long,
    val completedAt: Long,
    val isCompleted: Boolean = false
)
