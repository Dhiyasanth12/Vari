package com.example.data.ai

import android.graphics.Bitmap
import android.util.Log
import com.example.data.local.StyleLyricEntity
import com.example.model.ContinuedLyricsResult
import com.example.model.GeneratedLyrics
import com.example.model.ImprovementSuggestion
import com.example.model.SongIdea
import com.example.model.VariCatalog
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "FirebaseVariAi"
private const val PRIMARY_MODEL = "gemini-3.8-flash"
private const val FAST_FALLBACK_MODEL = "gemini-3.5-flash-lite"
private const val PRIMARY_TIMEOUT_MS = 30_000L
private const val FALLBACK_TIMEOUT_MS = 30_000L

class FirebaseVariAiRepository : VariAiRepository {

    private val improvementsSchema = Schema.obj(
        properties = mapOf(
            "suggestions" to Schema.array(
                items = Schema.obj(
                    properties = mapOf(
                        "category" to Schema.string(description = "Focus area such as Rhyme, Emotion, Flow, or Imagery"),
                        "tip" to Schema.string(description = "Concrete, actionable lyric improvement suggestion")
                    )
                ),
                description = "3 to 5 clear improvement suggestions on rhyme, emotion, and flow"
            )
        )
    )

    private val ideasSchema = Schema.obj(
        properties = mapOf(
            "ideas" to Schema.array(
                items = Schema.obj(
                    properties = mapOf(
                        "title" to Schema.string(description = "Fresh song title in the requested language"),
                        "oneLineStory" to Schema.string(description = "One-line song story or concept in the requested language"),
                        "suggestedMood" to Schema.enumeration(
                            values = VariCatalog.moods,
                            description = "Best fitting mood from the allowed list"
                        ),
                        "suggestedSongType" to Schema.enumeration(
                            values = VariCatalog.songTypes,
                            description = "Best fitting song type from the allowed list"
                        )
                    )
                ),
                description = "Exactly 5 original song concepts"
            )
        )
    )

    // Streaming plain-text lyric models (also support multimodal image+text input)
    private val primaryLyricsModel by lazy { createLyricsModel(PRIMARY_MODEL) }
    private val fastLyricsModel by lazy { createLyricsModel(FAST_FALLBACK_MODEL) }

    // Dedicated continuation models so the base "always give a title/full song" rule doesn't override continuation rules
    private val primaryContinuationModel by lazy { createContinuationModel(PRIMARY_MODEL) }
    private val fastContinuationModel by lazy { createContinuationModel(FAST_FALLBACK_MODEL) }

    // Structured JSON models for improvements & fresh ideas
    private val primaryImprovementsModel by lazy { createImprovementsModel(PRIMARY_MODEL) }
    private val fastImprovementsModel by lazy { createImprovementsModel(FAST_FALLBACK_MODEL) }

    private val primaryIdeasModel by lazy { createIdeasModel(PRIMARY_MODEL) }
    private val fastIdeasModel by lazy { createIdeasModel(FAST_FALLBACK_MODEL) }

    private fun createLyricsModel(modelName: String): GenerativeModel =
        Firebase.ai.generativeModel(
            modelName = modelName,
            generationConfig = generationConfig {
                temperature = 0.85f
                maxOutputTokens = 1200
            },
            systemInstruction = content {
                text(VariAiRepository.SYSTEM_INSTRUCTION)
            }
        )

    private fun createContinuationModel(modelName: String): GenerativeModel =
        Firebase.ai.generativeModel(
            modelName = modelName,
            generationConfig = generationConfig {
                temperature = 0.8f
                maxOutputTokens = 900
            },
            systemInstruction = content {
                text(
                    "You are Vari, the personal songwriter of Dhiyasanth. Your job right now is to " +
                        "seamlessly CONTINUE his unfinished song lyrics using an inspiration photo. " +
                        "CRITICAL RULES: Keep his original lyrics untouched. Output ONLY the new " +
                        "continuation lines—NEVER repeat, rewrite, or include any of his existing lines, " +
                        "and NEVER generate a song title or restart the song. Match his exact mood, " +
                        "rhyme pattern, vocabulary, writing style, and emotional flow. For Tamil, use " +
                        "natural spoken-poetic Tamil in proper Tamil script. When observing the photo, " +
                        "never identify individuals and never assume the user's personal emotions—use " +
                        "only the visual scene and atmosphere as creative inspiration."
                )
            }
        )

    private fun createImprovementsModel(modelName: String): GenerativeModel =
        Firebase.ai.generativeModel(
            modelName = modelName,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = improvementsSchema
                temperature = 0.6f
                maxOutputTokens = 800
            },
            systemInstruction = content {
                text(VariAiRepository.SYSTEM_INSTRUCTION)
            }
        )

    private fun createIdeasModel(modelName: String): GenerativeModel =
        Firebase.ai.generativeModel(
            modelName = modelName,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = ideasSchema
                temperature = 0.9f
                maxOutputTokens = 900
            },
            systemInstruction = content {
                text(VariAiRepository.SYSTEM_INSTRUCTION)
            }
        )

    override suspend fun generateLyrics(
        language: String,
        topic: String,
        mood: String,
        songType: String,
        extraInstructions: String,
        styleReferences: List<StyleLyricEntity>,
        variationSeed: Int,
        onPartialText: (String) -> Unit
    ): GeneratedLyrics {
        val promptText = buildString {
            appendStyleContext(styleReferences)
            appendLine("Write a completely new, original song with these exact details:")
            appendLine("- Language: $language (write natively in proper script and natural poetic flow for $language)")
            appendLine("- Topic / Story: $topic")
            appendLine("- Mood: $mood")
            appendLine("- Song Type: $songType")
            if (extraInstructions.isNotBlank()) {
                appendLine("- Extra Instructions: $extraInstructions")
            }
            if (variationSeed > 0) {
                appendLine("- Creative Direction: Take a fresh poetic angle different from earlier drafts (take #$variationSeed).")
            }
            appendLine()
            appendSongFormatInstructions()
        }

        val input = content { text(promptText) }
        val rawText = streamContentWithFallback(
            primary = primaryLyricsModel,
            fallback = fastLyricsModel,
            input = input,
            taskName = "Lyric generation",
            onPartialText = onPartialText
        )
        return parseStreamedLyrics(
            rawText = rawText,
            language = language,
            mood = mood,
            songType = songType,
            topic = topic,
            usedStyleTitles = styleReferences.map { it.title }
        )
    }

    override suspend fun generateLyricsFromImage(
        image: Bitmap,
        language: String,
        mood: String,
        songType: String,
        extraInstructions: String,
        styleReferences: List<StyleLyricEntity>,
        variationSeed: Int,
        onPartialText: (String) -> Unit
    ): GeneratedLyrics {
        val promptText = buildString {
            appendStyleContext(styleReferences)
            appendLine("Carefully analyze the attached inspiration photo across these visual dimensions:")
            appendLine("- Objects, environment, weather, colors, and lighting")
            appendLine("- General visual context of people if present (NEVER identify or name individuals)")
            appendLine("- Atmosphere, emotions suggested by the scene, and a possible poetic story behind the moment")
            appendLine("IMPORTANT: Do NOT assume the user's personal or actual emotions from the picture. Use the visual atmosphere strictly as creative inspiration.")
            appendLine()
            appendLine("Now transform what you see into completely original, singable song lyrics connecting the image's visual elements with:")
            appendLine("- Language: $language")
            if (language.equals("Tamil", ignoreCase = true)) {
                appendLine("- Tamil Craft: Write natural, poetic, singable Tamil in proper Tamil script with vivid imagery and emotional depth.")
            } else {
                appendLine("- Craft: Write naturally, musically, and creatively in $language.")
            }
            appendLine("- Mood: $mood")
            appendLine("- Song Style: $songType")
            if (extraInstructions.isNotBlank()) {
                appendLine("- Extra Instructions: $extraInstructions")
            }
            if (variationSeed > 0) {
                appendLine("- Creative Direction: Explore a fresh poetic metaphor from this image (take #$variationSeed).")
            }
            appendLine()
            appendSongFormatInstructions()
        }

        val input = content {
            image(image)
            text(promptText)
        }

        val rawText = streamContentWithFallback(
            primary = primaryLyricsModel,
            fallback = fastLyricsModel,
            input = input,
            taskName = "Vision lyric generation",
            onPartialText = onPartialText
        )

        return parseStreamedLyrics(
            rawText = rawText,
            language = language,
            mood = mood,
            songType = songType,
            topic = "Photo Inspiration ($mood $songType)",
            usedStyleTitles = styleReferences.map { it.title }
        )
    }

    override suspend fun continueLyricsFromImage(
        existingLyrics: String,
        image: Bitmap,
        language: String,
        mood: String,
        songType: String,
        extraInstructions: String,
        styleReferences: List<StyleLyricEntity>,
        variationSeed: Int,
        onPartialText: (String) -> Unit
    ): ContinuedLyricsResult {
        val promptText = buildString {
            appendStyleContext(styleReferences)
            appendLine("Analyze the attached photo (objects, environment, weather, colors, lighting, visual atmosphere, and story; general visual context only—never identify individuals and never assume the user's actual emotions).")
            appendLine()
            appendLine("The songwriter has already written these existing lyrics:")
            appendLine("--- EXISTING LYRICS START ---")
            appendLine(existingLyrics.trim())
            appendLine("--- EXISTING LYRICS END ---")
            appendLine()
            appendLine("Continue this song directly from the last line using the photo as visual inspiration:")
            appendLine("- Language: $language")
            appendLine("- Mood: $mood")
            appendLine("- Song Style: $songType")
            if (extraInstructions.isNotBlank()) {
                appendLine("- Extra Instructions: $extraInstructions")
            }
            if (variationSeed > 0) {
                appendLine("- Take #$variationSeed: offer a fresh continuation direction while staying faithful to the song.")
            }
            appendLine()
            appendLine("STRICT CONTINUATION RULES:")
            appendLine("1. Output ONLY the newly written continuation (1 to 2 verses and/or a bridge). Do NOT repeat or include any existing lines.")
            appendLine("2. Do NOT restart the song and do NOT add a new song title.")
            appendLine("3. Continue naturally right from the last line, preserving the same rhyme scheme, rhythm, vocabulary, writing voice, and emotional flow.")
            if (language.equals("Tamil", ignoreCase = true)) {
                appendLine("4. Use proper Tamil script and natural spoken-poetic Tamil.")
            }
            appendLine("5. No preamble, no explanations, no markdown bolding—output only the singable continuation lyrics.")
        }

        val input = content {
            image(image)
            text(promptText)
        }

        val continuation = streamContentWithFallback(
            primary = primaryContinuationModel,
            fallback = fastContinuationModel,
            input = input,
            taskName = "Continue lyrics from image",
            onPartialText = onPartialText
        ).trim()

        return ContinuedLyricsResult(
            originalLyrics = existingLyrics.trim(),
            continuationText = continuation,
            language = language,
            mood = mood,
            songType = songType
        )
    }

    private suspend fun streamContentWithFallback(
        primary: GenerativeModel,
        fallback: GenerativeModel,
        input: Content,
        taskName: String,
        onPartialText: (String) -> Unit
    ): String {
        val primaryBuffer = StringBuilder()
        try {
            Log.d(TAG, "$taskName streaming with $PRIMARY_MODEL")
            withTimeout(PRIMARY_TIMEOUT_MS) {
                primary.generateContentStream(input).collect { chunk ->
                    val piece = chunk.text.orEmpty()
                    if (piece.isNotEmpty()) {
                        primaryBuffer.append(piece)
                        onPartialText(primaryBuffer.toString())
                    }
                }
            }
            if (primaryBuffer.isNotBlank()) {
                return primaryBuffer.toString()
            }
        } catch (e: TimeoutCancellationException) {
            if (primaryBuffer.length >= 120) {
                Log.w(TAG, "$taskName timed out after partial output on $PRIMARY_MODEL; using streamed output")
                return primaryBuffer.toString()
            }
            Log.w(TAG, "$taskName timed out on $PRIMARY_MODEL; falling back to $FAST_FALLBACK_MODEL", e)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "$taskName failed on $PRIMARY_MODEL (${e.javaClass.simpleName}); trying $FAST_FALLBACK_MODEL", e)
        }

        val fallbackBuffer = StringBuilder()
        try {
            Log.d(TAG, "$taskName streaming with fallback $FAST_FALLBACK_MODEL")
            withTimeout(FALLBACK_TIMEOUT_MS) {
                fallback.generateContentStream(input).collect { chunk ->
                    val piece = chunk.text.orEmpty()
                    if (piece.isNotEmpty()) {
                        fallbackBuffer.append(piece)
                        onPartialText(fallbackBuffer.toString())
                    }
                }
            }
            if (fallbackBuffer.isNotBlank()) {
                return fallbackBuffer.toString()
            }
            throw IllegalStateException("Vari returned an empty response. Please try again.")
        } catch (e: TimeoutCancellationException) {
            if (fallbackBuffer.length >= 100) {
                return fallbackBuffer.toString()
            }
            Log.e(TAG, "$taskName timed out on both models", e)
            throw IllegalStateException("$taskName timed out. Check your connection and try again.", e)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "$taskName failed", e)
            throw e
        }
    }

    override suspend fun suggestImprovements(
        lyrics: GeneratedLyrics,
        styleReferences: List<StyleLyricEntity>
    ): List<ImprovementSuggestion> {
        val prompt = buildString {
            appendStyleContext(styleReferences)
            appendLine("Review the following ${lyrics.language} ${lyrics.mood} ${lyrics.songType} song lyrics about \"${lyrics.topic}\".")
            appendLine("Give 3 to 5 clear, practical suggestions focusing on rhyme, emotion, imagery, and singable flow.")
            appendLine()
            appendLine(lyrics.toFormattedText())
        }

        val rawJson = generateJsonWithFallback(
            primary = primaryImprovementsModel,
            fallback = fastImprovementsModel,
            prompt = prompt,
            taskName = "Improvement suggestions"
        )

        val json = JSONObject(rawJson)
        val arr = json.optJSONArray("suggestions")
            ?: throw IllegalStateException("Unexpected suggestions format. Please try again.")

        val results = mutableListOf<ImprovementSuggestion>()
        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            val category = obj.optString("category").trim().ifBlank { "Suggestion ${i + 1}" }
            val tip = obj.optString("tip").trim()
            if (tip.isNotEmpty()) {
                results.add(
                    ImprovementSuggestion(
                        category = category,
                        tip = tip,
                        isPlaceholder = false
                    )
                )
            }
        }
        if (results.isEmpty()) {
            throw IllegalStateException("No suggestions found. Please try again.")
        }
        return results
    }

    override suspend fun generateFreshIdeas(
        language: String,
        styleReferences: List<StyleLyricEntity>,
        variationSeed: Int
    ): List<SongIdea> {
        val prompt = buildString {
            appendStyleContext(styleReferences)
            appendLine("Suggest exactly 5 fresh, original song concepts in $language.")
            appendLine("Each concept must include a compelling title (in $language), a vivid one-line story, a fitting mood, and a fitting song type.")
            if (variationSeed > 0) {
                appendLine("Make these 5 concepts distinct and inventive (batch #$variationSeed).")
            }
        }

        val rawJson = generateJsonWithFallback(
            primary = primaryIdeasModel,
            fallback = fastIdeasModel,
            prompt = prompt,
            taskName = "Fresh ideas"
        )

        val json = JSONObject(rawJson)
        val arr = json.optJSONArray("ideas")
            ?: throw IllegalStateException("Unexpected ideas format. Please try again.")

        val results = mutableListOf<SongIdea>()
        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            val title = obj.optString("title").trim()
            val story = obj.optString("oneLineStory").trim()
            val mood = obj.optString("suggestedMood").trim().ifBlank { "Romantic" }
            val songType = obj.optString("suggestedSongType").trim().ifBlank { "Melody" }
            if (title.isNotEmpty() && story.isNotEmpty()) {
                results.add(
                    SongIdea(
                        title = title,
                        oneLineStory = story,
                        suggestedMood = mood,
                        suggestedSongType = songType,
                        language = language,
                        isPlaceholder = false
                    )
                )
            }
        }
        if (results.isEmpty()) {
            throw IllegalStateException("Could not parse fresh ideas. Please try again.")
        }
        return results.take(5)
    }

    private suspend fun generateJsonWithFallback(
        primary: GenerativeModel,
        fallback: GenerativeModel,
        prompt: String,
        taskName: String
    ): String {
        try {
            val response = withTimeout(PRIMARY_TIMEOUT_MS) {
                primary.generateContent(prompt)
            }
            val text = response.text
            if (!text.isNullOrBlank()) return text
        } catch (e: TimeoutCancellationException) {
            Log.w(TAG, "$taskName timed out on $PRIMARY_MODEL; falling back to $FAST_FALLBACK_MODEL", e)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "$taskName failed on $PRIMARY_MODEL; trying $FAST_FALLBACK_MODEL", e)
        }

        try {
            val fallbackResponse = withTimeout(FALLBACK_TIMEOUT_MS) {
                fallback.generateContent(prompt)
            }
            return fallbackResponse.text
                ?: throw IllegalStateException("$taskName returned an empty response. Please try again.")
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "$taskName timed out on both models", e)
            throw IllegalStateException("$taskName timed out. Please check your connection and try again.", e)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "$taskName failed", e)
            throw e
        }
    }

    private fun StringBuilder.appendSongFormatInstructions() {
        appendLine("Format your response cleanly using ONLY these exact section markers (no markdown asterisks or intro text):")
        appendLine("TITLE: <creative song title>")
        appendLine("[Verse 1]")
        appendLine("<4 lines>")
        appendLine("[Chorus]")
        appendLine("<4 catchy singable lines>")
        appendLine("[Verse 2]")
        appendLine("<4 lines>")
        appendLine("[Bridge]")
        appendLine("<2 to 4 emotional lines>")
    }

    private fun parseStreamedLyrics(
        rawText: String,
        language: String,
        mood: String,
        songType: String,
        topic: String,
        usedStyleTitles: List<String>
    ): GeneratedLyrics {
        var title = ""
        val verses = mutableListOf<String>()
        var chorus = ""
        var bridge = ""

        var currentSection = ""
        val currentLines = mutableListOf<String>()

        fun flushSection() {
            val body = currentLines.joinToString("\n").trim()
            currentLines.clear()
            if (body.isEmpty()) return
            when {
                currentSection.startsWith("verse") -> verses.add(body)
                currentSection == "chorus" -> if (chorus.isBlank()) chorus = body
                currentSection == "bridge" -> if (bridge.isBlank()) bridge = body
                else -> verses.add(body)
            }
        }

        val lines = rawText.replace("\r\n", "\n").lines()
        for (rawLine in lines) {
            val cleaned = rawLine.trim().removePrefix("**").removeSuffix("**").trim()
            if (cleaned.isEmpty()) {
                if (currentLines.isNotEmpty() && currentLines.last().isNotEmpty()) {
                    currentLines.add("")
                }
                continue
            }

            val lower = cleaned.lowercase()
            if (title.isEmpty() && (lower.startsWith("title:") || lower.startsWith("song title:") || lower.startsWith("# "))) {
                title = cleaned
                    .substringAfter(":", cleaned.removePrefix("# "))
                    .trim()
                    .trim('"', '\'', '*')
                continue
            }

            val normalizedHeader = cleaned
                .trim('[', ']', '(', ')', '*', '#', ':', ' ')
                .lowercase()

            when {
                normalizedHeader.startsWith("verse") || normalizedHeader.startsWith("charanam") || normalizedHeader.startsWith("சரணம்") -> {
                    flushSection()
                    currentSection = "verse"
                }
                normalizedHeader.startsWith("chorus") || normalizedHeader.startsWith("hook") || normalizedHeader.startsWith("pallavi") || normalizedHeader.startsWith("பல்லவி") -> {
                    flushSection()
                    currentSection = "chorus"
                }
                normalizedHeader.startsWith("bridge") || normalizedHeader.startsWith("anupallavi") || normalizedHeader.startsWith("அனுபல்லவி") -> {
                    flushSection()
                    currentSection = "bridge"
                }
                normalizedHeader == "title" -> {
                    flushSection()
                    currentSection = "title"
                }
                currentSection == "title" && title.isEmpty() -> {
                    title = cleaned.trim('"', '\'', '*')
                    currentSection = ""
                }
                else -> {
                    currentLines.add(rawLine.trimEnd())
                }
            }
        }
        flushSection()

        if (verses.isEmpty() && chorus.isBlank() && bridge.isBlank()) {
            val paragraphs = rawText.trim().split(Regex("\n\\s*\n")).map { it.trim() }.filter { it.isNotEmpty() }
            if (paragraphs.size >= 3) {
                if (title.isEmpty() && paragraphs.first().lines().size == 1) {
                    title = paragraphs.first().trim('*', '#', '"')
                    verses.add(paragraphs[1])
                    chorus = paragraphs[2]
                    if (paragraphs.size > 3) verses.add(paragraphs[3])
                    if (paragraphs.size > 4) bridge = paragraphs[4]
                } else {
                    verses.add(paragraphs[0])
                    chorus = paragraphs[1]
                    bridge = paragraphs.drop(2).joinToString("\n\n")
                }
            } else {
                verses.add(rawText.trim())
            }
        }

        val finalTitle = title.ifBlank { topic.take(48) }
        return GeneratedLyrics(
            title = finalTitle,
            verses = verses,
            chorus = chorus,
            bridge = bridge,
            language = language,
            mood = mood,
            songType = songType,
            topic = topic,
            usedStyleTitles = usedStyleTitles,
            isPlaceholder = false
        )
    }

    private fun StringBuilder.appendStyleContext(styleReferences: List<StyleLyricEntity>) {
        if (styleReferences.isEmpty()) {
            appendLine("No personal style examples are saved yet. Write in a warm, natural, emotionally resonant poetic style.")
            appendLine()
            return
        }
        appendLine("Here are up to 3 of Dhiyasanth's saved lyrics showing his personal writing style, rhythm, imagery, and emotional voice. Learn from this style, but NEVER copy or repeat any old lines:")
        appendLine()
        styleReferences.forEachIndexed { index, ref ->
            val snippet = ref.lyrics.trim().take(700)
            appendLine("--- Style Example ${index + 1}: \"${ref.title}\" (${ref.language}, ${ref.mood}) ---")
            appendLine(snippet)
            appendLine()
        }
    }
}
