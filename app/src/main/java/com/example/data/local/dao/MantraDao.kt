package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.MantraEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MantraDao {
    @Query("SELECT * FROM mantras ORDER BY isFavorite DESC, sortOrder ASC, id ASC")
    fun getAllMantras(): Flow<List<MantraEntity>>

    @Query("SELECT * FROM mantras WHERE isFavorite = 1 ORDER BY sortOrder ASC, id ASC")
    fun getFavoriteMantras(): Flow<List<MantraEntity>>

    @Query("SELECT * FROM mantras WHERE id = :id LIMIT 1")
    fun getMantraById(id: Long): Flow<MantraEntity?>

    @Query("SELECT * FROM mantras WHERE id = :id LIMIT 1")
    suspend fun getMantraByIdDirect(id: Long): MantraEntity?

    @Query("SELECT COUNT(*) FROM mantras")
    suspend fun getMantraCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMantra(mantra: MantraEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(mantras: List<MantraEntity>)

    @Update
    suspend fun updateMantra(mantra: MantraEntity)

    @Query("UPDATE mantras SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean)

    @Delete
    suspend fun deleteMantra(mantra: MantraEntity)

    @Query("DELETE FROM mantras WHERE id = :id AND isDefault = 0")
    suspend fun deleteCustomMantraById(id: Long): Int
}
