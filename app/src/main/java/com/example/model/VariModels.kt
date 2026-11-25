package com.example.model

enum class VariTab(val route: String) {
    WRITE("write"),
    MY_STYLE("my_style"),
    IDEAS("ideas"),
    MY_SONGS("my_songs")
}

object VariCatalog {
    const val OTHER_LANGUAGE = "Other"

    val presetLanguages: List<String> = listOf(
        "Tamil",
        "English",
        "Hindi",
        "Telugu",
        "Malayalam",
        "Kannada",
        OTHER_LANGUAGE
    )

    val moods: List<String> = listOf(
        "Happy",
        "Sad",
        "Romantic",
        "Energetic",
        "Motivational",
        "Peaceful"
    )

    val songTypes: List<String> = listOf(
        "Melody",
        "Folk",
        "Rap",
        "Devotional",
        "Party",
        "Lullaby"
    )
}

data class GeneratedLyrics(
    val title: String,
    val verses: List<String>,
    val chorus: String,
    val bridge: String,
    val language: String,
    val mood: String,
    val songType: String,
    val topic: String,
    val usedStyleTitles: List<String> = emptyList(),
    val isPlaceholder: Boolean = true
) {
    fun toFormattedText(): String = buildString {
        appendLine(title)
        appendLine()
        verses.forEachIndexed { index, verse ->
            appendLine("[Verse ${index + 1}]")
            appendLine(verse.trim())
            appendLine()
            if (index == 0 && chorus.isNotBlank()) {
                appendLine("[Chorus]")
                appendLine(chorus.trim())
                appendLine()
            }
        }
        if (verses.isEmpty() && chorus.isNotBlank()) {
            appendLine("[Chorus]")
            appendLine(chorus.trim())
            appendLine()
        }
        if (bridge.isNotBlank()) {
            appendLine("[Bridge]")
            appendLine(bridge.trim())
            appendLine()
        }
        if (chorus.isNotBlank() && verses.size > 1) {
            appendLine("[Chorus]")
            appendLine(chorus.trim())
        }
    }.trim()
}

data class ImprovementSuggestion(
    val category: String,
    val tip: String,
    val isPlaceholder: Boolean = true
)

data class SongIdea(
    val title: String,
    val oneLineStory: String,
    val suggestedMood: String,
    val suggestedSongType: String,
    val language: String,
    val isPlaceholder: Boolean = true
)

sealed interface VariUiState<out T> {
    data object Idle : VariUiState<Nothing>
    data object Loading : VariUiState<Nothing>
    data class Streaming(val text: String) : VariUiState<Nothing>
    data class Success<T>(val data: T) : VariUiState<T>
    data class Error(val message: String) : VariUiState<Nothing>
}

data class BackupSummary(
    val songCount: Int,
    val styleCount: Int
)

enum class VisionMode {
    NEW_FROM_PHOTO,
    CONTINUE_MY_LYRICS
}

data class ContinuedLyricsResult(
    val originalLyrics: String,
    val continuationText: String,
    val language: String,
    val mood: String,
    val songType: String
) {
    fun combinedFullLyrics(): String = buildString {
        appendLine(originalLyrics.trim())
        appendLine()
        append(continuationText.trim())
    }.trim()

    fun inferredTitle(): String {
        val firstLine = originalLyrics.trim()
            .lineSequence()
            .map { it.trim() }
            .firstOrNull { it.isNotEmpty() && !it.startsWith("[") }
            ?.take(48)
        return firstLine ?: "Continued $mood Song"
    }
}

