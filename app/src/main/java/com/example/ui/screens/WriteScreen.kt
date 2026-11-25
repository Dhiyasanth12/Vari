package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.TempleHindu
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.GeneratedLyrics
import com.example.model.ImprovementSuggestion
import com.example.model.VariCatalog
import com.example.model.VariUiState
import com.example.model.VisionMode
import com.example.ui.VisionSectionState
import com.example.ui.WriteTabState
import com.example.ui.components.FirebaseAiNoticeBanner
import com.example.ui.components.LanguageDropdownSelector
import com.example.ui.components.MetadataPill
import com.example.ui.components.StudioErrorCard
import com.example.ui.components.StudioLoadingCard
import com.example.ui.components.VisionToLyricsCard
import com.example.ui.components.variTextFieldColors
import com.example.ui.theme.GenreDevotionalGlow
import com.example.ui.theme.GenreFolkGlow
import com.example.ui.theme.GenreLullabyGlow
import com.example.ui.theme.GenreMelodyGlow
import com.example.ui.theme.GenrePartyGlow
import com.example.ui.theme.GenreRapGlow
import com.example.ui.theme.VisionDeepGlass
import com.example.ui.theme.VisionElectricBlue
import com.example.ui.theme.VisionGlowViolet
import com.example.ui.theme.VisionNeonBlue
import com.example.ui.theme.VisionNeonViolet
import com.example.ui.theme.VisionSoftLavender
import com.example.ui.theme.VisionSurfaceGlass
import kotlin.math.sin

@Composable
fun WriteScreen(
    state: WriteTabState,
    visionState: VisionSectionState,
    savedStyleCount: Int,
    onLanguageSelected: (String) -> Unit,
    onCustomLanguageChanged: (String) -> Unit,
    onTopicChanged: (String) -> Unit,
    onMoodSelected: (String) -> Unit,
    onSongTypeSelected: (String) -> Unit,
    onExtraInstructionsChanged: (String) -> Unit,
    onCreateLyrics: () -> Unit,
    onRegenerate: () -> Unit,
    onCopyLyrics: (String) -> Unit,
    onSaveSong: () -> Unit,
    onSuggestImprovements: () -> Unit,
    onOpenMyStyle: () -> Unit,
    onVisionModeSelected: (VisionMode) -> Unit,
    onCaptureInspiration: () -> Unit,
    onUploadGallery: () -> Unit,
    onRemoveVisionImage: () -> Unit,
    onOpenCameraSettings: () -> Unit,
    onExistingLyricsChanged: (String) -> Unit,
    onGenerateFromPhoto: () -> Unit,
    onContinueMyLyrics: () -> Unit,
    onCopyContinuation: (String) -> Unit,
    onCopyFullSong: (String) -> Unit,
    onSaveContinuedSong: () -> Unit,
    onRegenerateContinuation: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Cosmic Moonlit Neon Hero Banner
            item(key = "hero_banner") {
                WriteHeroBanner()
            }

            // Personal Style Hint or Active Voice Status
            item(key = "style_status_banner") {
                StyleVoiceStatusCard(
                    savedStyleCount = savedStyleCount,
                    onOpenMyStyle = onOpenMyStyle
                )
            }

            // Vision to Lyrics & Continue My Lyrics AI Camera Section
            item(key = "vision_to_lyrics_section") {
                VisionToLyricsCard(
                    visionState = visionState,
                    selectedLanguageOption = state.languageOption,
                    customLanguage = state.customLanguage,
                    selectedLanguage = state.resolvedLanguage,
                    selectedMood = state.mood,
                    selectedSongType = state.songType,
                    onModeSelected = onVisionModeSelected,
                    onLanguageSelected = onLanguageSelected,
                    onCustomLanguageChanged = onCustomLanguageChanged,
                    onMoodSelected = onMoodSelected,
                    onCaptureClick = onCaptureInspiration,
                    onGalleryClick = onUploadGallery,
                    onRemoveImage = onRemoveVisionImage,
                    onOpenSettings = onOpenCameraSettings,
                    onExistingLyricsChanged = onExistingLyricsChanged,
                    onGenerateFromPhoto = onGenerateFromPhoto,
                    onContinueMyLyrics = onContinueMyLyrics,
                    onCopyContinuation = onCopyContinuation,
                    onCopyFullSong = onCopyFullSong,
                    onSaveContinuedSong = onSaveContinuedSong,
                    onRegenerateContinuation = onRegenerateContinuation
                )
            }

            // Main Text Lyric Composer Card
            item(key = "composer_form") {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        1.dp,
                        Brush.linearGradient(
                            listOf(
                                VisionNeonViolet.copy(alpha = 0.45f),
                                VisionNeonBlue.copy(alpha = 0.45f)
                            )
                        )
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        LanguageDropdownSelector(
                            selectedOption = state.languageOption,
                            customLanguage = state.customLanguage,
                            onOptionSelected = onLanguageSelected,
                            onCustomLanguageChanged = onCustomLanguageChanged,
                            dropdownTag = "write_language_dropdown",
                            customFieldTag = "write_custom_language_input"
                        )

                        OutlinedTextField(
                            value = state.topic,
                            onValueChange = onTopicChanged,
                            label = { Text(stringResource(R.string.label_topic)) },
                            placeholder = { Text(stringResource(R.string.hint_topic)) },
                            minLines = 2,
                            maxLines = 4,
                            colors = variTextFieldColors(),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("write_topic_input")
                        )

                        NeonSongTypeGrid(
                            label = stringResource(R.string.label_song_type),
                            selectedSongType = state.songType,
                            onSelect = onSongTypeSelected
                        )

                        NeonMoodPillGroup(
                            label = stringResource(R.string.label_mood),
                            selectedMood = state.mood,
                            onSelect = onMoodSelected
                        )

                        OutlinedTextField(
                            value = state.extraInstructions,
                            onValueChange = onExtraInstructionsChanged,
                            label = { Text(stringResource(R.string.label_extra_instructions)) },
                            placeholder = { Text(stringResource(R.string.hint_extra_instructions)) },
                            minLines = 2,
                            maxLines = 3,
                            colors = variTextFieldColors(),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("write_extra_instructions_input")
                        )

                        AnimatedVisibility(visible = state.validationError != null) {
                            Text(
                                text = state.validationError.orEmpty(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.testTag("write_validation_error")
                            )
                        }

                        val createEnabled = state.generationState !is VariUiState.Loading &&
                            state.generationState !is VariUiState.Streaming
                        Button(
                            onClick = onCreateLyrics,
                            enabled = createEnabled,
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = Color.White,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(58.dp)
                                .clip(RoundedCornerShape(28.dp))
                                .then(
                                    if (createEnabled) {
                                        Modifier
                                            .background(
                                                Brush.horizontalGradient(
                                                    listOf(
                                                        VisionGlowViolet,
                                                        VisionNeonViolet,
                                                        VisionElectricBlue,
                                                        VisionNeonBlue
                                                    )
                                                )
                                            )
                                            .border(
                                                width = 1.2.dp,
                                                brush = Brush.horizontalGradient(
                                                    listOf(VisionSoftLavender, VisionNeonBlue)
                                                ),
                                                shape = RoundedCornerShape(28.dp)
                                            )
                                    } else {
                                        Modifier
                                    }
                                )
                                .testTag("create_lyrics_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = stringResource(R.string.btn_create_lyrics),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Generation Output Region
            item(key = "generation_output") {
                when (val genState = state.generationState) {
                    is VariUiState.Idle -> Unit
                    is VariUiState.Loading -> {
                        if (!visionState.isAnalyzingPhoto) {
                            StudioLoadingCard(message = stringResource(R.string.loading_writing_lyrics))
                        }
                    }
                    is VariUiState.Streaming -> {
                        StreamingLyricsCard(partialText = genState.text)
                    }
                    is VariUiState.Error -> {
                        StudioErrorCard(
                            message = genState.message,
                            onRetry = if (state.lastGenerationFromPhoto) onGenerateFromPhoto else onCreateLyrics
                        )
                    }
                    is VariUiState.Success -> {
                        GeneratedLyricsCard(
                            lyrics = genState.data,
                            isSaved = state.isCurrentSaved,
                            isSuggesting = state.improvementsState is VariUiState.Loading,
                            onCopy = { onCopyLyrics(genState.data.toFormattedText()) },
                            onSave = onSaveSong,
                            onRegenerate = onRegenerate,
                            onSuggestImprovements = onSuggestImprovements
                        )
                    }
                }
            }

            // Improvement Suggestions Region
            item(key = "improvements_output") {
                when (val impState = state.improvementsState) {
                    is VariUiState.Idle -> Unit
                    is VariUiState.Loading, is VariUiState.Streaming -> {
                        StudioLoadingCard(message = stringResource(R.string.loading_suggestions))
                    }
                    is VariUiState.Error -> {
                        StudioErrorCard(
                            message = impState.message,
                            onRetry = onSuggestImprovements
                        )
                    }
                    is VariUiState.Success -> {
                        ImprovementsCard(suggestions = impState.data)
                    }
                }
            }

            item(key = "write_bottom_spacer") {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun WriteHeroBanner() {
    val infinitePulse = rememberInfiniteTransition(label = "hero_cosmic_wave")
    val wavePhase by infinitePulse.animateFloat(
        initialValue = 0f,
        targetValue = (Math.PI * 2.0).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(144.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        VisionDeepGlass,
                        VisionSurfaceGlass,
                        Color(0xFF0A1633)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        VisionNeonViolet.copy(alpha = 0.65f),
                        VisionNeonBlue.copy(alpha = 0.65f)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Glowing lavender full-moon aura on the right
            val moonCenter = Offset(size.width * 0.82f, size.height * 0.42f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        VisionSoftLavender.copy(alpha = 0.42f),
                        VisionNeonViolet.copy(alpha = 0.24f),
                        VisionNeonBlue.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = moonCenter,
                    radius = size.height * 0.85f
                ),
                radius = size.height * 0.85f,
                center = moonCenter
            )
            drawCircle(
                color = VisionSoftLavender.copy(alpha = 0.82f),
                radius = size.height * 0.22f,
                center = moonCenter
            )

            // Subtle cosmic stars
            val stars = listOf(
                Offset(0.14f, 0.22f), Offset(0.32f, 0.16f), Offset(0.55f, 0.24f),
                Offset(0.68f, 0.18f), Offset(0.92f, 0.22f), Offset(0.62f, 0.72f)
            )
            stars.forEachIndexed { idx, pt ->
                drawCircle(
                    color = if (idx % 2 == 0) VisionSoftLavender.copy(alpha = 0.6f) else VisionNeonBlue.copy(alpha = 0.65f),
                    radius = if (idx % 3 == 0) 3.2f else 2f,
                    center = Offset(size.width * pt.x, size.height * pt.y)
                )
            }

            // Dual glowing neon soundwave ribbons
            val wave1 = Path()
            val wave2 = Path()
            val steps = 40
            for (i in 0..steps) {
                val frac = i / steps.toFloat()
                val x = size.width * frac
                val y1 = size.height * 0.74f + sin(frac * 6.5f + wavePhase) * 14f
                val y2 = size.height * 0.80f + sin(frac * 5.2f - wavePhase * 0.8f) * 11f
                if (i == 0) {
                    wave1.moveTo(x, y1)
                    wave2.moveTo(x, y2)
                } else {
                    wave1.lineTo(x, y1)
                    wave2.lineTo(x, y2)
                }
            }
            drawPath(
                path = wave1,
                brush = Brush.horizontalGradient(listOf(VisionNeonViolet, VisionNeonBlue)),
                style = Stroke(width = 3.5f, cap = StrokeCap.Round)
            )
            drawPath(
                path = wave2,
                brush = Brush.horizontalGradient(
                    listOf(
                        VisionNeonBlue.copy(alpha = 0.55f),
                        VisionNeonViolet.copy(alpha = 0.45f)
                    )
                ),
                style = Stroke(width = 2f, cap = StrokeCap.Round)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.write_hero_eyebrow),
                style = MaterialTheme.typography.labelSmall,
                color = VisionNeonBlue
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.write_hero_title),
                style = MaterialTheme.typography.headlineSmall,
                color = VisionSoftLavender,
                modifier = Modifier.fillMaxWidth(0.76f)
            )
        }
    }
}

@Composable
private fun NeonSongTypeGrid(
    label: String,
    selectedSongType: String,
    onSelect: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        val rows = VariCatalog.songTypes.chunked(3)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            rows.forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowItems.forEach { genre ->
                        val isSelected = genre == selectedSongType
                        val accent = genreAccentColor(genre)
                        val icon = genreIcon(genre)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(86.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isSelected) {
                                        Brush.verticalGradient(
                                            listOf(
                                                accent.copy(alpha = 0.38f),
                                                VisionSurfaceGlass
                                            )
                                        )
                                    } else {
                                        Brush.verticalGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.surfaceContainer,
                                                VisionDeepGlass
                                            )
                                        )
                                    }
                                )
                                .border(
                                    width = if (isSelected) 1.75.dp else 1.dp,
                                    color = if (isSelected) accent else accent.copy(alpha = 0.32f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable { onSelect(genre) }
                                .testTag("write_song_type_${genre.lowercase()}")
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = accent,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(6.dp)
                                        .size(16.dp)
                                )
                            }
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else accent,
                                    modifier = Modifier.size(26.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = genre,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NeonMoodPillGroup(
    label: String,
    selectedMood: String,
    onSelect: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            VariCatalog.moods.forEach { mood ->
                val isSelected = mood == selectedMood
                val icon = moodIcon(mood)
                Surface(
                    onClick = { onSelect(mood) },
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) VisionGlowViolet else MaterialTheme.colorScheme.surfaceContainer,
                    border = BorderStroke(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) VisionNeonBlue else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("write_mood_${mood.lowercase()}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSelected) Color.White else VisionNeonViolet,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = mood,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

private fun genreAccentColor(genre: String): Color = when (genre) {
    "Melody" -> GenreMelodyGlow
    "Folk" -> GenreFolkGlow
    "Rap" -> GenreRapGlow
    "Devotional" -> GenreDevotionalGlow
    "Party" -> GenrePartyGlow
    "Lullaby" -> GenreLullabyGlow
    else -> VisionNeonViolet
}

private fun genreIcon(genre: String): ImageVector = when (genre) {
    "Melody" -> Icons.Filled.MusicNote
    "Folk" -> Icons.Filled.GraphicEq
    "Rap" -> Icons.Filled.Mic
    "Devotional" -> Icons.Filled.TempleHindu
    "Party" -> Icons.Filled.Celebration
    "Lullaby" -> Icons.Filled.NightlightRound
    else -> Icons.Filled.MusicNote
}

private fun moodIcon(mood: String): ImageVector = when (mood) {
    "Happy" -> Icons.Filled.SentimentSatisfiedAlt
    "Sad" -> Icons.Filled.SentimentDissatisfied
    "Romantic" -> Icons.Filled.Favorite
    "Energetic" -> Icons.Filled.Bolt
    "Motivational" -> Icons.Filled.EmojiEvents
    "Peaceful" -> Icons.Filled.Spa
    else -> Icons.Filled.SelfImprovement
}

@Composable
private fun StyleVoiceStatusCard(
    savedStyleCount: Int,
    onOpenMyStyle: () -> Unit
) {
    if (savedStyleCount == 0) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
            border = BorderStroke(1.dp, VisionNeonViolet.copy(alpha = 0.45f)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("write_empty_style_hint")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.EditNote,
                    contentDescription = null,
                    tint = VisionNeonBlue,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = stringResource(R.string.write_no_style_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    onClick = onOpenMyStyle,
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("jump_to_my_style_button")
                ) {
                    Text(
                        text = stringResource(R.string.write_add_style_cta),
                        color = VisionNeonBlue,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = VisionNeonBlue,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    } else {
        val usedCount = savedStyleCount.coerceAtMost(3)
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
            border = BorderStroke(1.dp, VisionNeonBlue.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = VisionNeonBlue,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = stringResource(R.string.write_style_active_fmt, usedCount, savedStyleCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
private fun GeneratedLyricsCard(
    lyrics: GeneratedLyrics,
    isSaved: Boolean,
    isSuggesting: Boolean,
    onCopy: () -> Unit,
    onSave: () -> Unit,
    onRegenerate: () -> Unit,
    onSuggestImprovements: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.2.dp,
            Brush.linearGradient(listOf(VisionNeonViolet, VisionNeonBlue))
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("generated_lyrics_card")
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (lyrics.isPlaceholder) {
                FirebaseAiNoticeBanner()
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = lyrics.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = VisionSoftLavender
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MetadataPill(text = lyrics.language, isAccent = true)
                    MetadataPill(text = lyrics.mood)
                    MetadataPill(text = lyrics.songType)
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            lyrics.verses.forEachIndexed { index, verse ->
                LyricSectionBlock(
                    label = stringResource(R.string.section_verse_fmt, index + 1),
                    text = verse
                )
                if (index == 0 && lyrics.chorus.isNotBlank()) {
                    LyricSectionBlock(
                        label = stringResource(R.string.section_chorus),
                        text = lyrics.chorus,
                        highlighted = true
                    )
                }
            }

            if (lyrics.bridge.isNotBlank()) {
                LyricSectionBlock(
                    label = stringResource(R.string.section_bridge),
                    text = lyrics.bridge
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilledTonalButton(
                        onClick = onCopy,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("copy_lyrics_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.btn_copy))
                    }

                    Button(
                        onClick = onSave,
                        enabled = !isSaved,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VisionGlowViolet,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1.35f)
                            .height(50.dp)
                            .testTag("save_to_songs_button")
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Filled.BookmarkAdded else Icons.Filled.BookmarkBorder,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(
                                if (isSaved) R.string.btn_saved_to_songs else R.string.btn_save_to_songs
                            ),
                            maxLines = 1
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onRegenerate,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("regenerate_lyrics_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.btn_regenerate), maxLines = 1)
                    }

                    FilledTonalButton(
                        onClick = onSuggestImprovements,
                        enabled = !isSuggesting,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier
                            .weight(1.35f)
                            .height(50.dp)
                            .testTag("suggest_improvements_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.TipsAndUpdates,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.btn_suggest_improvements), maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
private fun LyricSectionBlock(
    label: String,
    text: String,
    highlighted: Boolean = false
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (highlighted) {
            VisionSurfaceGlass
        } else {
            MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.72f)
        },
        border = if (highlighted) {
            BorderStroke(1.dp, VisionNeonBlue.copy(alpha = 0.45f))
        } else {
            null
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = if (highlighted) VisionNeonBlue else VisionNeonViolet
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 28.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ImprovementsCard(
    suggestions: List<ImprovementSuggestion>
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, VisionNeonBlue.copy(alpha = 0.45f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("improvements_result_card")
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Lightbulb,
                    contentDescription = null,
                    tint = VisionNeonBlue
                )
                Text(
                    text = stringResource(R.string.improvements_header),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (suggestions.any { it.isPlaceholder }) {
                FirebaseAiNoticeBanner()
            }

            suggestions.forEachIndexed { index, suggestion ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "${index + 1}. ${suggestion.category}",
                            style = MaterialTheme.typography.labelLarge,
                            color = VisionNeonViolet
                        )
                        Text(
                            text = suggestion.tip,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StreamingLyricsCard(
    partialText: String
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            Brush.horizontalGradient(listOf(VisionNeonViolet, VisionNeonBlue))
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("streaming_lyrics_card")
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = VisionNeonBlue
                )
                Text(
                    text = stringResource(R.string.loading_writing_lyrics),
                    style = MaterialTheme.typography.titleSmall,
                    color = VisionNeonBlue
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Text(
                text = partialText,
                style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 28.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
