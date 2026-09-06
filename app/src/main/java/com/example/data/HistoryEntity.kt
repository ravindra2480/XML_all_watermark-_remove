package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "xml_clean_history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val watermarkCount: Int,
    val originalSize: Int,
    val cleanedSize: Int,
    val cleanedXml: String,
    val timestamp: Long = System.currentTimeMillis()
)
