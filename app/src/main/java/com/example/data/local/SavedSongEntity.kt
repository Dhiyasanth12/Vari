package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_songs")
data class SavedSongEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val language: String,
    val mood: String,
    val songType: String,
    val topic: String,
    val lyrics: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
