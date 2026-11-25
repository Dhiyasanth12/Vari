package com.example.data.local

import com.example.model.BackupSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class VariLocalRepository(private val dao: VariDao) {

    val styleLyrics: Flow<List<StyleLyricEntity>> = dao.observeStyleLyrics()
    val savedSongs: Flow<List<SavedSongEntity>> = dao.observeSavedSongs()

    suspend fun saveStyleLyric(
        id: Long = 0L,
        title: String,
        language: String,
        mood: String,
        lyrics: String
    ) {
        val now = System.currentTimeMillis()
        if (id == 0L) {
            dao.insertStyleLyric(
                StyleLyricEntity(
                    title = title.trim(),
                    language = language.trim(),
                    mood = mood.trim(),
                    lyrics = lyrics.trim(),
                    createdAt = now,
                    updatedAt = now
                )
            )
        } else {
            val existing = dao.getAllStyleLyricsOnce().firstOrNull { it.id == id }
            dao.updateStyleLyric(
                StyleLyricEntity(
                    id = id,
                    title = title.trim(),
                    language = language.trim(),
                    mood = mood.trim(),
                    lyrics = lyrics.trim(),
                    createdAt = existing?.createdAt ?: now,
                    updatedAt = now
                )
            )
        }
    }

    suspend fun deleteStyleLyric(id: Long) {
        dao.deleteStyleLyricById(id)
    }

    suspend fun saveSong(
        title: String,
        language: String,
        mood: String,
        songType: String,
        topic: String,
        lyrics: String
    ): Long {
        val now = System.currentTimeMillis()
        return dao.insertSavedSong(
            SavedSongEntity(
                title = title.trim(),
                language = language.trim(),
                mood = mood.trim(),
                songType = songType.trim(),
                topic = topic.trim(),
                lyrics = lyrics.trim(),
                createdAt = now,
                updatedAt = now
            )
        )
    }

    suspend fun updateSavedSong(song: SavedSongEntity) {
        dao.updateSavedSong(song.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteSavedSong(id: Long) {
        dao.deleteSavedSongById(id)
    }

    /**
     * Picks up to 3 saved style lyrics for Vari's writing context:
     * prefers matching language first, then fills remaining slots randomly.
     */
    suspend fun selectStyleReferences(targetLanguage: String, maxCount: Int = 3): List<StyleLyricEntity> {
        val all = dao.getAllStyleLyricsOnce()
        if (all.isEmpty()) return emptyList()

        val normalizedTarget = targetLanguage.trim().lowercase()
        val sameLanguage = all.filter { it.language.trim().lowercase() == normalizedTarget }.shuffled()
        val otherLanguages = all.filter { it.language.trim().lowercase() != normalizedTarget }.shuffled()

        return (sameLanguage + otherLanguages).take(maxCount)
    }

    suspend fun exportBackupJson(): Pair<String, BackupSummary> = withContext(Dispatchers.IO) {
        val songs = dao.getAllSavedSongsOnce()
        val styles = dao.getAllStyleLyricsOnce()

        val root = JSONObject().apply {
            put("app", "Vari (வரி)")
            put("version", 1)
            put("exportedAt", System.currentTimeMillis())

            val songsArray = JSONArray()
            songs.forEach { song ->
                songsArray.put(
                    JSONObject().apply {
                        put("title", song.title)
                        put("language", song.language)
                        put("mood", song.mood)
                        put("songType", song.songType)
                        put("topic", song.topic)
                        put("lyrics", song.lyrics)
                        put("createdAt", song.createdAt)
                        put("updatedAt", song.updatedAt)
                    }
                )
            }
            put("mySongs", songsArray)

            val stylesArray = JSONArray()
            styles.forEach { style ->
                stylesArray.put(
                    JSONObject().apply {
                        put("title", style.title)
                        put("language", style.language)
                        put("mood", style.mood)
                        put("lyrics", style.lyrics)
                        put("createdAt", style.createdAt)
                        put("updatedAt", style.updatedAt)
                    }
                )
            }
            put("myStyle", stylesArray)
        }

        root.toString(2) to BackupSummary(songCount = songs.size, styleCount = styles.size)
    }

    suspend fun importBackupJson(rawJson: String): BackupSummary = withContext(Dispatchers.IO) {
        val root = JSONObject(rawJson)
        val now = System.currentTimeMillis()

        val existingSongs = dao.getAllSavedSongsOnce()
            .map { "${it.title.trim().lowercase()}|${it.lyrics.trim()}" }
            .toMutableSet()
        val existingStyles = dao.getAllStyleLyricsOnce()
            .map { "${it.title.trim().lowercase()}|${it.lyrics.trim()}" }
            .toMutableSet()

        val importedSongs = mutableListOf<SavedSongEntity>()
        val songsArray = root.optJSONArray("mySongs") ?: JSONArray()
        for (i in 0 until songsArray.length()) {
            val obj = songsArray.optJSONObject(i) ?: continue
            val title = obj.optString("title").trim()
            val lyrics = obj.optString("lyrics").trim()
            if (title.isEmpty() || lyrics.isEmpty()) continue
            val key = "${title.lowercase()}|$lyrics"
            if (existingSongs.add(key)) {
                importedSongs.add(
                    SavedSongEntity(
                        title = title,
                        language = obj.optString("language", "Tamil").ifBlank { "Tamil" },
                        mood = obj.optString("mood", "Romantic").ifBlank { "Romantic" },
                        songType = obj.optString("songType", "Melody").ifBlank { "Melody" },
                        topic = obj.optString("topic", ""),
                        lyrics = lyrics,
                        createdAt = obj.optLong("createdAt", now),
                        updatedAt = obj.optLong("updatedAt", now)
                    )
                )
            }
        }

        val importedStyles = mutableListOf<StyleLyricEntity>()
        val stylesArray = root.optJSONArray("myStyle") ?: JSONArray()
        for (i in 0 until stylesArray.length()) {
            val obj = stylesArray.optJSONObject(i) ?: continue
            val title = obj.optString("title").trim()
            val lyrics = obj.optString("lyrics").trim()
            if (title.isEmpty() || lyrics.isEmpty()) continue
            val key = "${title.lowercase()}|$lyrics"
            if (existingStyles.add(key)) {
                importedStyles.add(
                    StyleLyricEntity(
                        title = title,
                        language = obj.optString("language", "Tamil").ifBlank { "Tamil" },
                        mood = obj.optString("mood", "Romantic").ifBlank { "Romantic" },
                        lyrics = lyrics,
                        createdAt = obj.optLong("createdAt", now),
                        updatedAt = obj.optLong("updatedAt", now)
                    )
                )
            }
        }

        if (importedSongs.isNotEmpty()) {
            dao.insertSavedSongs(importedSongs)
        }
        if (importedStyles.isNotEmpty()) {
            dao.insertStyleLyrics(importedStyles)
        }

        BackupSummary(songCount = importedSongs.size, styleCount = importedStyles.size)
    }
}
