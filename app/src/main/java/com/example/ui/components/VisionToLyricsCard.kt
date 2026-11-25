package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WarningAmber
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.ContinuedLyricsResult
import com.example.model.VariUiState
import com.example.model.VisionMode
import com.example.ui.VisionSectionState
import com.example.ui.theme.VisionDeepGlass
import com.example.ui.theme.VisionElectricBlue
import com.example.ui.theme.VisionGlowViolet
import com.example.ui.theme.VisionNeonBlue
import com.example.ui.theme.VisionNeonViolet
import com.example.ui.theme.VisionSoftLavender
import com.example.ui.theme.VisionSurfaceGlass
import kotlinx.coroutines.delay

@Composable
fun VisionToLyricsCard(
    visionState: VisionSectionState,
    selectedLanguageOption: String,
    customLanguage: String,
    selectedLanguage: String,
    selectedMood: String,
    selectedSongType: String,
    onModeSelected: (VisionMode) -> Unit,
    onLanguageSelected: (String) -> Unit,
    onCustomLanguageChanged: (String) -> Unit,
    onMoodSelected: (String) -> Unit,
    onCaptureClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onRemoveImage: () -> Unit,
    onOpenSettings: () -> Unit,
    onExistingLyricsChanged: (String) -> Unit,
    onGenerateFromPhoto: () -> Unit,
    onContinueMyLyrics: () -> Unit,
    onCopyContinuation: (String) -> Unit,
    onCopyFullSong: (String) -> Unit,
    onSaveContinuedSong: () -> Unit,
    onRegenerateContinuation: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteGlow = rememberInfiniteTransition(label = "vision_neon_glow")
    val glowAlpha by infiniteGlow.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.92f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    val neonBorderBrush = Brush.linearGradient(
        colors = listOf(
            VisionNeonViolet.copy(alpha = glowAlpha),
            VisionNeonBlue.copy(alpha = glowAlpha),
            VisionGlowViolet.copy(alpha = glowAlpha * 0.85f)
        )
    )

    val cardBgBrush = Brush.verticalGradient(
        colors = listOf(
            VisionDeepGlass,
            VisionSurfaceGlass.copy(alpha = 0.92f),
            MaterialTheme.colorScheme.surface
        )
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.Transparent,
            border = BorderStroke(1.25.dp, neonBorderBrush),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(cardBgBrush)
                .testTag("vision_to_lyrics_card")
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header with Glowing Neon Camera Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        VisionNeonViolet.copy(alpha = 0.42f * glowAlpha),
                                        VisionNeonBlue.copy(alpha = 0.14f),
                                        Color.Transparent
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(VisionGlowViolet, VisionElectricBlue)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CameraAlt,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.vision_feature_title),
                            style = MaterialTheme.typography.titleLarge,
                            color = VisionSoftLavender
                        )
                        Text(
                            text = stringResource(R.string.vision_feature_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Mode Switcher: New from Photo vs Continue My Lyrics
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.65f))
                        .border(1.dp, VisionNeonViolet.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    VisionModePill(
                        label = stringResource(R.string.vision_mode_new_song),
                        selected = visionState.mode == VisionMode.NEW_FROM_PHOTO,
                        onClick = { onModeSelected(VisionMode.NEW_FROM_PHOTO) },
                        tag = "vision_mode_new_song_tab",
                        modifier = Modifier.weight(1f)
                    )
                    VisionModePill(
                        label = stringResource(R.string.vision_mode_continue),
                        selected = visionState.mode == VisionMode.CONTINUE_MY_LYRICS,
                        onClick = { onModeSelected(VisionMode.CONTINUE_MY_LYRICS) },
                        tag = "vision_mode_continue_tab",
                        modifier = Modifier.weight(1f)
                    )
                }

                // Camera Capture & Gallery Upload Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onCaptureClick,
                        enabled = !visionState.isAnalyzingPhoto,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.White
                        ),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                        modifier = Modifier
                            .weight(1.2f)
                            .height(54.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(VisionGlowViolet, VisionElectricBlue)
                                )
                            )
                            .testTag("capture_inspiration_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(19.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.btn_capture_inspiration),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onGalleryClick,
                        enabled = !visionState.isAnalyzingPhoto,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, VisionNeonBlue.copy(alpha = 0.55f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = VisionNeonBlue
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .testTag("upload_gallery_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PhotoLibrary,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.btn_upload_gallery),
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1
                        )
                    }
                }

                // Friendly Camera Permission Denied Guidance
                AnimatedVisibility(visible = visionState.cameraPermissionDenied) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.75f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("camera_permission_denied_banner")
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.WarningAmber,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = stringResource(R.string.vision_camera_denied_title),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                            Text(
                                text = stringResource(R.string.vision_camera_denied_body),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilledTonalButton(
                                    onClick = onOpenSettings,
                                    modifier = Modifier
                                        .heightIn(min = 48.dp)
                                        .testTag("open_camera_settings_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Settings,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(stringResource(R.string.btn_open_settings))
                                }
                                OutlinedButton(
                                    onClick = onGalleryClick,
                                    modifier = Modifier.heightIn(min = 48.dp)
                                ) {
                                    Text(stringResource(R.string.btn_upload_gallery))
                                }
                            }
                        }
                    }
                }

                // Camera Unavailable / Load Error
                AnimatedVisibility(visible = visionState.cameraStatusError != null) {
                    Text(
                        text = visionState.cameraStatusError.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                // Glassmorphism Photo Preview + Animated Scanning Overlay
                AnimatedVisibility(visible = visionState.selectedBitmap != null) {
                    visionState.selectedBitmap?.let { bitmap ->
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            GlassPhotoPreview(
                                bitmap = bitmap,
                                isScanning = visionState.isAnalyzingPhoto
                            )

                            // Retake & Remove Image Controls
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = onCaptureClick,
                                    enabled = !visionState.isAnalyzingPhoto,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, VisionNeonViolet.copy(alpha = 0.55f)),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = VisionSoftLavender
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("vision_retake_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Refresh,
                                        contentDescription = null,
                                        modifier = Modifier.size(17.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(stringResource(R.string.btn_retake_photo))
                                }

                                OutlinedButton(
                                    onClick = onRemoveImage,
                                    enabled = !visionState.isAnalyzingPhoto,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("vision_remove_image_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.DeleteOutline,
                                        contentDescription = null,
                                        modifier = Modifier.size(17.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(stringResource(R.string.btn_remove_image))
                                }
                            }
                        }
                    }
                }

                // Continue My Lyrics Input Box + Direct Language & Mood Selectors
                AnimatedVisibility(visible = visionState.mode == VisionMode.CONTINUE_MY_LYRICS) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = visionState.existingLyrics,
                            onValueChange = onExistingLyricsChanged,
                            label = { Text(stringResource(R.string.label_existing_lyrics)) },
                            placeholder = { Text(stringResource(R.string.hint_existing_lyrics)) },
                            minLines = 4,
                            maxLines = 8,
                            colors = variTextFieldColors(),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("continue_existing_lyrics_input")
                        )

                        LanguageDropdownSelector(
                            selectedOption = selectedLanguageOption,
                            customLanguage = customLanguage,
                            onOptionSelected = onLanguageSelected,
                            onCustomLanguageChanged = onCustomLanguageChanged,
                            dropdownTag = "continue_language_dropdown",
                            customFieldTag = "continue_custom_language_input"
                        )

                        StudioChipGroup(
                            label = stringResource(R.string.label_mood),
                            options = com.example.model.VariCatalog.moods,
                            selectedOption = selectedMood,
                            onSelect = onMoodSelected,
                            tagPrefix = "continue_mood"
                        )

                        Text(
                            text = stringResource(R.string.vision_continue_rules_note),
                            style = MaterialTheme.typography.bodySmall,
                            color = VisionNeonBlue.copy(alpha = 0.9f)
                        )
                    }
                }

                // Active Context Pills (mirrors Write screen Language / Mood / Style)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MetadataPill(
                        text = selectedLanguage.ifBlank { "Language" },
                        isAccent = true
                    )
                    MetadataPill(text = selectedMood)
                    MetadataPill(text = selectedSongType)
                }

                // Validation Message
                AnimatedVisibility(visible = visionState.validationError != null) {
                    Text(
                        text = visionState.validationError.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.testTag("vision_validation_error")
                    )
                }

                // Glowing Primary Vision Action Button
                val isContinue = visionState.mode == VisionMode.CONTINUE_MY_LYRICS
                val actionEnabled = !visionState.isAnalyzingPhoto && visionState.selectedBitmap != null
                Button(
                    onClick = if (isContinue) onContinueMyLyrics else onGenerateFromPhoto,
                    enabled = actionEnabled,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.White,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .then(
                            if (actionEnabled) {
                                Modifier.background(
                                    Brush.horizontalGradient(
                                        listOf(VisionNeonViolet, VisionGlowViolet, VisionElectricBlue)
                                    )
                                )
                            } else {
                                Modifier
                            }
                        )
                        .testTag(
                            if (isContinue) "vision_continue_lyrics_button" else "vision_generate_lyrics_button"
                        )
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
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(
                                if (isContinue) R.string.btn_continue_lyrics else R.string.btn_generate_from_photo
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Animated Vision Loading Messages Card when analyzing
        AnimatedVisibility(visible = visionState.isAnalyzingPhoto) {
            VisionAnalyzingBanner()
        }

        // Continuation Streaming / Result / Error Output
        when (val contState = visionState.continuationState) {
            is VariUiState.Idle -> Unit
            is VariUiState.Loading -> Unit // Covered by VisionAnalyzingBanner + Scanning overlay
            is VariUiState.Streaming -> {
                StreamingContinuationCard(
                    originalLyrics = visionState.existingLyrics,
                    partialContinuation = contState.text
                )
            }
            is VariUiState.Error -> {
                StudioErrorCard(
                    message = contState.message,
                    onRetry = onContinueMyLyrics
                )
            }
            is VariUiState.Success -> {
                ContinuedLyricsResultCard(
                    result = contState.data,
                    isSaved = visionState.isContinuationSaved,
                    onCopyContinuation = { onCopyContinuation(contState.data.continuationText) },
                    onCopyFullSong = { onCopyFullSong(contState.data.combinedFullLyrics()) },
                    onSaveFullSong = onSaveContinuedSong,
                    onRegenerate = onRegenerateContinuation
                )
            }
        }
    }
}

@Composable
private fun VisionModePill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    tag: String,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(11.dp),
        color = if (selected) VisionGlowViolet else Color.Transparent,
        modifier = modifier
            .heightIn(min = 44.dp)
            .testTag(tag)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun GlassPhotoPreview(
    bitmap: Bitmap,
    isScanning: Boolean
) {
    val imageBitmap = remember(bitmap) { bitmap.asImageBitmap() }
    val scanTransition = rememberInfiniteTransition(label = "photo_scan_laser")
    val scanProgress by scanTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1750, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scan_line_y"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(228.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(VisionDeepGlass)
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    listOf(
                        VisionNeonViolet.copy(alpha = if (isScanning) 0.95f else 0.55f),
                        VisionNeonBlue.copy(alpha = if (isScanning) 0.95f else 0.55f)
                    )
                ),
                shape = RoundedCornerShape(18.dp)
            )
            .testTag("vision_photo_preview")
    ) {
        Image(
            bitmap = imageBitmap,
            contentDescription = stringResource(R.string.cd_vision_preview),
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Subtle glass vignette at bottom for depth
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Transparent,
                            VisionDeepGlass.copy(alpha = 0.55f)
                        )
                    )
                )
        )

        // Animated Neon Scanning Beam while Gemini analyzes the photo
        if (isScanning) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val y = size.height * scanProgress
                // Dark translucent tint for high-contrast neon scan beam
                drawRect(color = VisionDeepGlass.copy(alpha = 0.34f))
                // Soft glowing aura band
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            VisionNeonViolet.copy(alpha = 0.28f),
                            VisionNeonBlue.copy(alpha = 0.42f),
                            VisionNeonViolet.copy(alpha = 0.28f),
                            Color.Transparent
                        ),
                        startY = (y - 54f).coerceAtLeast(0f),
                        endY = (y + 54f).coerceAtMost(size.height)
                    )
                )
                // Crisp glowing laser line
                drawLine(
                    brush = Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            VisionNeonViolet,
                            VisionNeonBlue,
                            VisionNeonViolet,
                            Color.Transparent
                        )
                    ),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 5f
                )
            }
        }
    }
}

@Composable
private fun VisionAnalyzingBanner() {
    val messages = listOf(
        stringResource(R.string.vision_loading_step_1),
        stringResource(R.string.vision_loading_step_2),
        stringResource(R.string.vision_loading_step_3)
    )
    var index by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1800L)
            index = (index + 1) % messages.size
        }
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = VisionSurfaceGlass,
        border = BorderStroke(
            1.dp,
            Brush.horizontalGradient(listOf(VisionNeonViolet, VisionNeonBlue))
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("vision_analyzing_banner")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.5.dp,
                color = VisionNeonBlue,
                trackColor = VisionNeonViolet.copy(alpha = 0.25f)
            )
            AnimatedContent(
                targetState = messages[index],
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(220)) },
                label = "vision_loading_text"
            ) { msg ->
                Text(
                    text = msg,
                    style = MaterialTheme.typography.titleSmall,
                    color = VisionSoftLavender
                )
            }
        }
    }
}

@Composable
private fun StreamingContinuationCard(
    originalLyrics: String,
    partialContinuation: String
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, VisionNeonBlue.copy(alpha = 0.55f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("streaming_continuation_card")
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.vision_original_section).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = originalLyrics.trim(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            HorizontalDivider(color = VisionNeonViolet.copy(alpha = 0.45f))
            Text(
                text = stringResource(R.string.vision_added_section).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = VisionNeonBlue
            )
            Text(
                text = partialContinuation,
                style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 28.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ContinuedLyricsResultCard(
    result: ContinuedLyricsResult,
    isSaved: Boolean,
    onCopyContinuation: () -> Unit,
    onCopyFullSong: () -> Unit,
    onSaveFullSong: () -> Unit,
    onRegenerate: () -> Unit
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
            .testTag("continued_lyrics_result_card")
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = stringResource(R.string.vision_continuation_header),
                style = MaterialTheme.typography.headlineSmall,
                color = VisionSoftLavender
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetadataPill(text = result.language, isAccent = true)
                MetadataPill(text = result.mood)
                MetadataPill(text = result.songType)
            }

            // Original Lyrics (Untouched)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.65f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = stringResource(R.string.vision_original_section).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = result.originalLyrics,
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 24.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Newly Generated Continuation
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = VisionDeepGlass,
                border = BorderStroke(1.dp, VisionNeonBlue.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = stringResource(R.string.vision_added_section).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = VisionNeonBlue
                    )
                    Text(
                        text = result.continuationText,
                        style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 28.sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Action Buttons (2x2 clean grid)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilledTonalButton(
                        onClick = onCopyContinuation,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("copy_continuation_button")
                    ) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(17.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.btn_copy_continuation), maxLines = 1)
                    }

                    FilledTonalButton(
                        onClick = onCopyFullSong,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("copy_full_continued_button")
                    ) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(17.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.btn_copy_full_song), maxLines = 1)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onRegenerate,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("regenerate_continuation_button")
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(17.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.btn_regenerate), maxLines = 1)
                    }

                    Button(
                        onClick = onSaveFullSong,
                        enabled = !isSaved,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .weight(1.25f)
                            .height(50.dp)
                            .testTag("save_continued_song_button")
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Filled.BookmarkAdded else Icons.Filled.BookmarkBorder,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(
                                if (isSaved) R.string.btn_saved_to_songs else R.string.btn_save_full_song
                            ),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
