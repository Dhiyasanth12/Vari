package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VariDao {
    @Query("SELECT * FROM style_lyrics ORDER BY updatedAt DESC, id DESC")
    fun observeStyleLyrics(): Flow<List<StyleLyricEntity>>

    @Query("SELECT * FROM style_lyrics ORDER BY updatedAt DESC, id DESC")
    suspend fun getAllStyleLyricsOnce(): List<StyleLyricEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStyleLyric(item: StyleLyricEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStyleLyrics(items: List<StyleLyricEntity>)

    @Update
    suspend fun updateStyleLyric(item: StyleLyricEntity)

    @Query("DELETE FROM style_lyrics WHERE id = :id")
    suspend fun deleteStyleLyricById(id: Long)

    @Query("SELECT * FROM saved_songs ORDER BY createdAt DESC, id DESC")
    fun observeSavedSongs(): Flow<List<SavedSongEntity>>

    @Query("SELECT * FROM saved_songs ORDER BY createdAt DESC, id DESC")
    suspend fun getAllSavedSongsOnce(): List<SavedSongEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedSong(song: SavedSongEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedSongs(songs: List<SavedSongEntity>)

    @Update
    suspend fun updateSavedSong(song: SavedSongEntity)

    @Query("DELETE FROM saved_songs WHERE id = :id")
    suspend fun deleteSavedSongById(id: Long)
}
