package com.example.data

import kotlinx.coroutines.flow.Flow

class HistoryRepository(private val historyDao: HistoryDao) {
    val allHistory: Flow<List<HistoryEntity>> = historyDao.getAllHistory()

    suspend fun saveCleanedPreset(
        title: String,
        watermarkCount: Int,
        originalSize: Int,
        cleanedSize: Int,
        cleanedXml: String
    ): Long {
        return historyDao.insertHistory(
            HistoryEntity(
                title = title,
                watermarkCount = watermarkCount,
                originalSize = originalSize,
                cleanedSize = cleanedSize,
                cleanedXml = cleanedXml
            )
        )
    }

    suspend fun deleteHistory(id: Long) = historyDao.deleteById(id)

    suspend fun clearAll() = historyDao.clearAll()
}
