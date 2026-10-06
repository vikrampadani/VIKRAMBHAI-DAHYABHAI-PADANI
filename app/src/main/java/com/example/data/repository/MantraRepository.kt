package com.example.data.repository

import com.example.data.local.DefaultMantras
import com.example.data.local.dao.MantraDao
import com.example.data.local.entity.MantraEntity
import kotlinx.coroutines.flow.Flow

class MantraRepository(private val mantraDao: MantraDao) {

    val allMantras: Flow<List<MantraEntity>> = mantraDao.getAllMantras()
    val favoriteMantras: Flow<List<MantraEntity>> = mantraDao.getFavoriteMantras()

    fun getMantraById(id: Long): Flow<MantraEntity?> = mantraDao.getMantraById(id)

    suspend fun getMantraByIdDirect(id: Long): MantraEntity? = mantraDao.getMantraByIdDirect(id)

    suspend fun ensureDefaultsLoaded() {
        if (mantraDao.getMantraCount() == 0) {
            mantraDao.insertAll(DefaultMantras.list)
        }
    }

    suspend fun insertMantra(
        name: String,
        transliteration: String = "",
        meaning: String = "",
        deity: String = ""
    ): Long {
        val entity = MantraEntity(
            name = name.trim(),
            transliteration = transliteration.trim(),
            meaning = meaning.trim(),
            deity = deity.trim(),
            isFavorite = false,
            isDefault = false,
            sortOrder = 999
        )
        return mantraDao.insertMantra(entity)
    }

    suspend fun updateMantra(mantra: MantraEntity) {
        mantraDao.updateMantra(mantra)
    }

    suspend fun toggleFavorite(id: Long, currentFavorite: Boolean) {
        mantraDao.updateFavorite(id, !currentFavorite)
    }

    suspend fun deleteCustomMantra(id: Long): Boolean {
        return mantraDao.deleteCustomMantraById(id) > 0
    }

    suspend fun resetDefaults() {
        mantraDao.insertAll(DefaultMantras.list)
    }
}
