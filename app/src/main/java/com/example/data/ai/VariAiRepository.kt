package com.example.data.ai

import android.graphics.Bitmap
import com.example.data.local.StyleLyricEntity
import com.example.model.ContinuedLyricsResult
import com.example.model.GeneratedLyrics
import com.example.model.ImprovementSuggestion
import com.example.model.SongIdea

/**
 * Single AI boundary for Vari (வரி), backed by Firebase AI Logic.
 */
interface VariAiRepository {

    suspend fun generateLyrics(
        language: String,
        topic: String,
        mood: String,
        songType: String,
        extraInstructions: String,
        styleReferences: List<StyleLyricEntity>,
        variationSeed: Int = 0,
        onPartialText: (String) -> Unit = {}
    ): GeneratedLyrics

    suspend fun generateLyricsFromImage(
        image: Bitmap,
        language: String,
        mood: String,
        songType: String,
        extraInstructions: String,
        styleReferences: List<StyleLyricEntity>,
        variationSeed: Int = 0,
        onPartialText: (String) -> Unit = {}
    ): GeneratedLyrics

    suspend fun continueLyricsFromImage(
        existingLyrics: String,
        image: Bitmap,
        language: String,
        mood: String,
        songType: String,
        extraInstructions: String,
        styleReferences: List<StyleLyricEntity>,
        variationSeed: Int = 0,
        onPartialText: (String) -> Unit = {}
    ): ContinuedLyricsResult

    suspend fun suggestImprovements(
        lyrics: GeneratedLyrics,
        styleReferences: List<StyleLyricEntity>
    ): List<ImprovementSuggestion>

    suspend fun generateFreshIdeas(
        language: String,
        styleReferences: List<StyleLyricEntity>,
        variationSeed: Int = 0
    ): List<SongIdea>

    companion object {
        const val SYSTEM_INSTRUCTION: String =
            "You are Vari, the personal songwriter of Dhiyasanth. Learn his writing style, " +
                "favorite words, imagery, and emotion from his saved lyrics. Write completely " +
                "new and original lyrics in the language he asks for. Never copy his old lines. " +
                "Write natural, singable lines with good rhyme and rhythm for that language. " +
                "For Tamil, use proper Tamil script and natural spoken-poetic Tamil. Always give: " +
                "a title, 2 or 3 verses, a catchy chorus, and a short bridge. If he asks for " +
                "suggestions, give improvement tips on rhyme, emotion, and flow."
    }
}
