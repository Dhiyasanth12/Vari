package com.example.ui

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.ai.VariAiRepository
import com.example.data.local.SavedSongEntity
import com.example.data.local.StyleLyricEntity
import com.example.data.local.VariLocalRepository
import com.example.model.BackupSummary
import com.example.model.ContinuedLyricsResult
import com.example.model.GeneratedLyrics
import com.example.model.ImprovementSuggestion
import com.example.model.SongIdea
import com.example.model.VariCatalog
import com.example.model.VariTab
import com.example.model.VariUiState
import com.example.model.VisionMode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WriteTabState(
    val languageOption: String = "Tamil",
    val customLanguage: String = "",
    val topic: String = "",
    val mood: String = "Romantic",
    val songType: String = "Melody",
    val extraInstructions: String = "",
    val validationError: String? = null,
    val generationState: VariUiState<GeneratedLyrics> = VariUiState.Idle,
    val improvementsState: VariUiState<List<ImprovementSuggestion>> = VariUiState.Idle,
    val isCurrentSaved: Boolean = false,
    val lastGenerationFromPhoto: Boolean = false
) {
    val resolvedLanguage: String
        get() = if (languageOption == VariCatalog.OTHER_LANGUAGE) {
            customLanguage.trim()
        } else {
            languageOption
        }
}

data class StyleTabState(
    val editingId: Long? = null,
    val title: String = "",
    val languageOption: String = "Tamil",
    val customLanguage: String = "",
    val mood: String = "Romantic",
    val lyricsText: String = "",
    val validationError: String? = null
) {
    val resolvedLanguage: String
        get() = if (languageOption == VariCatalog.OTHER_LANGUAGE) {
            customLanguage.trim()
        } else {
            languageOption
        }
}

data class IdeasTabState(
    val languageOption: String = "Tamil",
    val customLanguage: String = "",
    val validationError: String? = null,
    val ideasState: VariUiState<List<SongIdea>> = VariUiState.Idle
) {
    val resolvedLanguage: String
        get() = if (languageOption == VariCatalog.OTHER_LANGUAGE) {
            customLanguage.trim()
        } else {
            languageOption
        }
}

data class SongsTabState(
    val searchQuery: String = "",
    val languageFilter: String = "All"
)

data class VisionSectionState(
    val mode: VisionMode = VisionMode.NEW_FROM_PHOTO,
    val selectedBitmap: Bitmap? = null,
    val existingLyrics: String = "",
    val cameraPermissionDenied: Boolean = false,
    val cameraStatusError: String? = null,
    val validationError: String? = null,
    val isAnalyzingPhoto: Boolean = false,
    val continuationState: VariUiState<ContinuedLyricsResult> = VariUiState.Idle,
    val isContinuationSaved: Boolean = false
)

class VariViewModel(
    private val localRepository: VariLocalRepository,
    private val aiRepository: VariAiRepository
) : ViewModel() {

    private val _currentTab = MutableStateFlow(VariTab.WRITE)
    val currentTab: StateFlow<VariTab> = _currentTab.asStateFlow()

    private val _writeState = MutableStateFlow(WriteTabState())
    val writeState: StateFlow<WriteTabState> = _writeState.asStateFlow()

    private val _visionState = MutableStateFlow(VisionSectionState())
    val visionState: StateFlow<VisionSectionState> = _visionState.asStateFlow()

    private val _styleState = MutableStateFlow(StyleTabState())
    val styleState: StateFlow<StyleTabState> = _styleState.asStateFlow()

    private val _ideasState = MutableStateFlow(IdeasTabState())
    val ideasState: StateFlow<IdeasTabState> = _ideasState.asStateFlow()

    private val _songsState = MutableStateFlow(SongsTabState())
    val songsState: StateFlow<SongsTabState> = _songsState.asStateFlow()

    val styleLyrics: StateFlow<List<StyleLyricEntity>> = localRepository.styleLyrics.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    val savedSongs: StateFlow<List<SavedSongEntity>> = localRepository.savedSongs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    private val _snackbarMessages = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val snackbarMessages: SharedFlow<String> = _snackbarMessages.asSharedFlow()

    private var writeVariationSeed = 0
    private var visionVariationSeed = 0
    private var continueVariationSeed = 0
    private var ideasVariationSeed = 0

    fun selectTab(tab: VariTab) {
        _currentTab.value = tab
    }

    fun postMessage(message: String) {
        _snackbarMessages.tryEmit(message)
    }

    // --- WRITE TAB ACTIONS ---

    fun onWriteLanguageSelected(option: String) {
        _writeState.update { it.copy(languageOption = option, validationError = null) }
    }

    fun onWriteCustomLanguageChanged(value: String) {
        _writeState.update { it.copy(customLanguage = value, validationError = null) }
    }

    fun onTopicChanged(value: String) {
        _writeState.update { it.copy(topic = value, validationError = null) }
    }

    fun onWriteMoodSelected(mood: String) {
        _writeState.update { it.copy(mood = mood) }
    }

    fun onWriteSongTypeSelected(songType: String) {
        _writeState.update { it.copy(songType = songType) }
    }

    fun onExtraInstructionsChanged(value: String) {
        _writeState.update { it.copy(extraInstructions = value) }
    }

    fun createLyrics(isRegenerate: Boolean = false) {
        val current = _writeState.value
        if (current.languageOption == VariCatalog.OTHER_LANGUAGE && current.customLanguage.isBlank()) {
            _writeState.update { it.copy(validationError = "Please type a language name.") }
            return
        }
        if (current.topic.isBlank()) {
            _writeState.update { it.copy(validationError = "Please enter a topic or story for your song.") }
            return
        }

        if (isRegenerate) {
            writeVariationSeed += 1
        }
        val seed = writeVariationSeed

        viewModelScope.launch {
            _writeState.update {
                it.copy(
                    validationError = null,
                    generationState = VariUiState.Loading,
                    improvementsState = VariUiState.Idle,
                    isCurrentSaved = false,
                    lastGenerationFromPhoto = false
                )
            }
            try {
                val targetLang = current.resolvedLanguage
                val styleRefs = localRepository.selectStyleReferences(targetLang, maxCount = 3)
                val result = aiRepository.generateLyrics(
                    language = targetLang,
                    topic = current.topic.trim(),
                    mood = current.mood,
                    songType = current.songType,
                    extraInstructions = current.extraInstructions.trim(),
                    styleReferences = styleRefs,
                    variationSeed = seed,
                    onPartialText = { partial ->
                        _writeState.update { state ->
                            state.copy(generationState = VariUiState.Streaming(partial))
                        }
                    }
                )
                _writeState.update {
                    it.copy(generationState = VariUiState.Success(result))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _writeState.update {
                    it.copy(
                        generationState = VariUiState.Error(
                            e.localizedMessage ?: "Something went wrong while writing lyrics. Please try again."
                        )
                    )
                }
            }
        }
    }

    fun suggestImprovements() {
        val successState = _writeState.value.generationState as? VariUiState.Success ?: return
        val currentLyrics = successState.data

        viewModelScope.launch {
            _writeState.update { it.copy(improvementsState = VariUiState.Loading) }
            try {
                val styleRefs = localRepository.selectStyleReferences(currentLyrics.language, maxCount = 3)
                val tips = aiRepository.suggestImprovements(currentLyrics, styleRefs)
                _writeState.update { it.copy(improvementsState = VariUiState.Success(tips)) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _writeState.update {
                    it.copy(
                        improvementsState = VariUiState.Error(
                            e.localizedMessage ?: "Could not fetch improvement suggestions. Please try again."
                        )
                    )
                }
            }
        }
    }

    fun saveGeneratedSong(savedConfirmation: String) {
        val successState = _writeState.value.generationState as? VariUiState.Success ?: return
        val lyrics = successState.data
        if (_writeState.value.isCurrentSaved) return

        viewModelScope.launch {
            localRepository.saveSong(
                title = lyrics.title,
                language = lyrics.language,
                mood = lyrics.mood,
                songType = lyrics.songType,
                topic = lyrics.topic,
                lyrics = lyrics.toFormattedText()
            )
            _writeState.update { it.copy(isCurrentSaved = true) }
            postMessage(savedConfirmation)
        }
    }

    fun regenerateCurrentWriteResult() {
        if (_writeState.value.lastGenerationFromPhoto && _visionState.value.selectedBitmap != null) {
            generateLyricsFromPhoto(isRegenerate = true)
        } else {
            createLyrics(isRegenerate = true)
        }
    }

    // --- VISION TO LYRICS & CONTINUE MY LYRICS ACTIONS ---

    fun onVisionModeSelected(mode: VisionMode) {
        _visionState.update { it.copy(mode = mode, validationError = null) }
    }

    fun onVisionPhotoCaptured(bitmap: Bitmap) {
        _visionState.update {
            it.copy(
                selectedBitmap = bitmap,
                cameraPermissionDenied = false,
                cameraStatusError = null,
                validationError = null
            )
        }
    }

    fun onVisionPhotoRemoved() {
        _visionState.update {
            it.copy(
                selectedBitmap = null,
                validationError = null,
                cameraStatusError = null
            )
        }
    }

    fun onCameraPermissionResult(granted: Boolean) {
        _visionState.update {
            it.copy(
                cameraPermissionDenied = !granted,
                cameraStatusError = null
            )
        }
    }

    fun onCameraStatusError(message: String) {
        _visionState.update { it.copy(cameraStatusError = message) }
    }

    fun onExistingLyricsChanged(text: String) {
        _visionState.update { it.copy(existingLyrics = text, validationError = null) }
    }

    fun generateLyricsFromPhoto(isRegenerate: Boolean = false) {
        val writeCurrent = _writeState.value
        val visionCurrent = _visionState.value
        val bitmap = visionCurrent.selectedBitmap
        if (bitmap == null) {
            _visionState.update { it.copy(validationError = "Capture or upload an inspiration photo first.") }
            return
        }
        if (writeCurrent.languageOption == VariCatalog.OTHER_LANGUAGE && writeCurrent.customLanguage.isBlank()) {
            _visionState.update { it.copy(validationError = "Please type a language name in the Language field below.") }
            return
        }

        if (isRegenerate) {
            visionVariationSeed += 1
        }
        val seed = visionVariationSeed

        viewModelScope.launch {
            _visionState.update {
                it.copy(
                    validationError = null,
                    cameraStatusError = null,
                    isAnalyzingPhoto = true
                )
            }
            _writeState.update {
                it.copy(
                    validationError = null,
                    generationState = VariUiState.Loading,
                    improvementsState = VariUiState.Idle,
                    isCurrentSaved = false,
                    lastGenerationFromPhoto = true
                )
            }
            try {
                val targetLang = writeCurrent.resolvedLanguage
                val styleRefs = localRepository.selectStyleReferences(targetLang, maxCount = 3)
                val result = aiRepository.generateLyricsFromImage(
                    image = bitmap,
                    language = targetLang,
                    mood = writeCurrent.mood,
                    songType = writeCurrent.songType,
                    extraInstructions = writeCurrent.extraInstructions.trim(),
                    styleReferences = styleRefs,
                    variationSeed = seed,
                    onPartialText = { partial ->
                        _writeState.update { state ->
                            state.copy(generationState = VariUiState.Streaming(partial))
                        }
                    }
                )
                _writeState.update { it.copy(generationState = VariUiState.Success(result)) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _writeState.update {
                    it.copy(
                        generationState = VariUiState.Error(
                            e.localizedMessage ?: "Could not generate lyrics from this image. Please try again."
                        )
                    )
                }
            } finally {
                _visionState.update { it.copy(isAnalyzingPhoto = false) }
            }
        }
    }

    fun continueLyricsFromPhoto(isRegenerate: Boolean = false) {
        val writeCurrent = _writeState.value
        val visionCurrent = _visionState.value
        val bitmap = visionCurrent.selectedBitmap
        if (bitmap == null) {
            _visionState.update { it.copy(validationError = "Capture or upload an inspiration photo first.") }
            return
        }
        if (visionCurrent.existingLyrics.isBlank()) {
            _visionState.update { it.copy(validationError = "Paste your existing lyrics so Vari knows where to continue.") }
            return
        }
        if (writeCurrent.languageOption == VariCatalog.OTHER_LANGUAGE && writeCurrent.customLanguage.isBlank()) {
            _visionState.update { it.copy(validationError = "Please type a language name in the Language field below.") }
            return
        }

        if (isRegenerate) {
            continueVariationSeed += 1
        }
        val seed = continueVariationSeed

        viewModelScope.launch {
            _visionState.update {
                it.copy(
                    validationError = null,
                    cameraStatusError = null,
                    isAnalyzingPhoto = true,
                    continuationState = VariUiState.Loading,
                    isContinuationSaved = false
                )
            }
            try {
                val targetLang = writeCurrent.resolvedLanguage
                val styleRefs = localRepository.selectStyleReferences(targetLang, maxCount = 3)
                val result = aiRepository.continueLyricsFromImage(
                    existingLyrics = visionCurrent.existingLyrics.trim(),
                    image = bitmap,
                    language = targetLang,
                    mood = writeCurrent.mood,
                    songType = writeCurrent.songType,
                    extraInstructions = writeCurrent.extraInstructions.trim(),
                    styleReferences = styleRefs,
                    variationSeed = seed,
                    onPartialText = { partial ->
                        _visionState.update { state ->
                            state.copy(continuationState = VariUiState.Streaming(partial))
                        }
                    }
                )
                _visionState.update { it.copy(continuationState = VariUiState.Success(result)) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _visionState.update {
                    it.copy(
                        continuationState = VariUiState.Error(
                            e.localizedMessage ?: "Could not continue your lyrics right now. Please try again."
                        )
                    )
                }
            } finally {
                _visionState.update { it.copy(isAnalyzingPhoto = false) }
            }
        }
    }

    fun saveContinuedSong(savedConfirmation: String) {
        val successState = _visionState.value.continuationState as? VariUiState.Success ?: return
        val continued = successState.data
        if (_visionState.value.isContinuationSaved) return

        viewModelScope.launch {
            localRepository.saveSong(
                title = continued.inferredTitle(),
                language = continued.language,
                mood = continued.mood,
                songType = continued.songType,
                topic = "Continued with Photo Inspiration",
                lyrics = continued.combinedFullLyrics()
            )
            _visionState.update { it.copy(isContinuationSaved = true) }
            postMessage(savedConfirmation)
        }
    }

    // --- MY STYLE TAB ACTIONS ---

    fun onStyleTitleChanged(value: String) {
        _styleState.update { it.copy(title = value, validationError = null) }
    }

    fun onStyleLanguageSelected(option: String) {
        _styleState.update { it.copy(languageOption = option, validationError = null) }
    }

    fun onStyleCustomLanguageChanged(value: String) {
        _styleState.update { it.copy(customLanguage = value, validationError = null) }
    }

    fun onStyleMoodSelected(mood: String) {
        _styleState.update { it.copy(mood = mood) }
    }

    fun onStyleLyricsTextChanged(value: String) {
        _styleState.update { it.copy(lyricsText = value, validationError = null) }
    }

    fun startEditingStyle(item: StyleLyricEntity) {
        val isPreset = VariCatalog.presetLanguages.any {
            it != VariCatalog.OTHER_LANGUAGE && it.equals(item.language, ignoreCase = true)
        }
        val matchedPreset = VariCatalog.presetLanguages.firstOrNull {
            it != VariCatalog.OTHER_LANGUAGE && it.equals(item.language, ignoreCase = true)
        }
        _styleState.update {
            it.copy(
                editingId = item.id,
                title = item.title,
                languageOption = if (isPreset && matchedPreset != null) matchedPreset else VariCatalog.OTHER_LANGUAGE,
                customLanguage = if (isPreset) "" else item.language,
                mood = item.mood.ifBlank { "Romantic" },
                lyricsText = item.lyrics,
                validationError = null
            )
        }
    }

    fun cancelEditingStyle() {
        _styleState.value = StyleTabState()
    }

    fun saveStyleLyric(savedMessage: String, updatedMessage: String) {
        val current = _styleState.value
        if (current.title.isBlank()) {
            _styleState.update { it.copy(validationError = "Please add a title for your lyrics.") }
            return
        }
        if (current.languageOption == VariCatalog.OTHER_LANGUAGE && current.customLanguage.isBlank()) {
            _styleState.update { it.copy(validationError = "Please enter the language name.") }
            return
        }
        if (current.lyricsText.isBlank()) {
            _styleState.update { it.copy(validationError = "Paste your lyrics or favorite lines before saving.") }
            return
        }

        val isUpdate = current.editingId != null
        viewModelScope.launch {
            localRepository.saveStyleLyric(
                id = current.editingId ?: 0L,
                title = current.title,
                language = current.resolvedLanguage,
                mood = current.mood,
                lyrics = current.lyricsText
            )
            _styleState.value = StyleTabState(
                languageOption = current.languageOption,
                customLanguage = current.customLanguage,
                mood = current.mood
            )
            postMessage(if (isUpdate) updatedMessage else savedMessage)
        }
    }

    fun deleteStyleLyric(id: Long, deletedMessage: String) {
        viewModelScope.launch {
            localRepository.deleteStyleLyric(id)
            if (_styleState.value.editingId == id) {
                _styleState.value = StyleTabState()
            }
            postMessage(deletedMessage)
        }
    }

    // --- IDEAS TAB ACTIONS ---

    fun onIdeasLanguageSelected(option: String) {
        _ideasState.update { it.copy(languageOption = option, validationError = null) }
    }

    fun onIdeasCustomLanguageChanged(value: String) {
        _ideasState.update { it.copy(customLanguage = value, validationError = null) }
    }

    fun generateFreshIdeas() {
        val current = _ideasState.value
        if (current.languageOption == VariCatalog.OTHER_LANGUAGE && current.customLanguage.isBlank()) {
            _ideasState.update { it.copy(validationError = "Please type a language name.") }
            return
        }

        ideasVariationSeed += 1
        val seed = ideasVariationSeed

        viewModelScope.launch {
            _ideasState.update { it.copy(validationError = null, ideasState = VariUiState.Loading) }
            try {
                val targetLang = current.resolvedLanguage
                val styleRefs = localRepository.selectStyleReferences(targetLang, maxCount = 3)
                val ideas = aiRepository.generateFreshIdeas(
                    language = targetLang,
                    styleReferences = styleRefs,
                    variationSeed = seed
                )
                _ideasState.update { it.copy(ideasState = VariUiState.Success(ideas)) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _ideasState.update {
                    it.copy(
                        ideasState = VariUiState.Error(
                            e.localizedMessage ?: "Could not generate song ideas right now. Please try again."
                        )
                    )
                }
            }
        }
    }

    fun applyIdeaToWriteTab(idea: SongIdea, loadedMessage: String) {
        val matchedPreset = VariCatalog.presetLanguages.firstOrNull {
            it != VariCatalog.OTHER_LANGUAGE && it.equals(idea.language, ignoreCase = true)
        }
        val matchedMood = VariCatalog.moods.firstOrNull {
            it.equals(idea.suggestedMood, ignoreCase = true)
        } ?: _writeState.value.mood
        val matchedType = VariCatalog.songTypes.firstOrNull {
            it.equals(idea.suggestedSongType, ignoreCase = true)
        } ?: _writeState.value.songType

        _writeState.update {
            it.copy(
                languageOption = matchedPreset ?: VariCatalog.OTHER_LANGUAGE,
                customLanguage = if (matchedPreset == null) idea.language else "",
                topic = "${idea.title} — ${idea.oneLineStory}",
                mood = matchedMood,
                songType = matchedType,
                validationError = null
            )
        }
        _currentTab.value = VariTab.WRITE
        postMessage(loadedMessage)
    }

    // --- MY SONGS TAB ACTIONS ---

    fun onSearchQueryChanged(query: String) {
        _songsState.update { it.copy(searchQuery = query) }
    }

    fun onLanguageFilterSelected(language: String) {
        _songsState.update { it.copy(languageFilter = language) }
    }

    fun updateSavedSong(song: SavedSongEntity, updatedMessage: String) {
        viewModelScope.launch {
            localRepository.updateSavedSong(song)
            postMessage(updatedMessage)
        }
    }

    fun deleteSavedSong(id: Long, deletedMessage: String) {
        viewModelScope.launch {
            localRepository.deleteSavedSong(id)
            postMessage(deletedMessage)
        }
    }

    suspend fun buildExportBackup(): Pair<String, BackupSummary> {
        return localRepository.exportBackupJson()
    }

    suspend fun importBackup(rawJson: String): Result<BackupSummary> {
        return try {
            Result.success(localRepository.importBackupJson(rawJson))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class VariViewModelFactory(
    private val localRepository: VariLocalRepository,
    private val aiRepository: VariAiRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VariViewModel::class.java)) {
            return VariViewModel(localRepository, aiRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
