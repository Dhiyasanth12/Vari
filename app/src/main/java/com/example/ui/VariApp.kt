package com.example.ui

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.model.VariTab
import com.example.ui.components.VariTopAppBar
import com.example.ui.screens.IdeasScreen
import com.example.ui.screens.MySongsScreen
import com.example.ui.screens.MyStyleScreen
import com.example.ui.screens.WriteScreen
import com.example.util.VisionImageHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class NavDestination(
    val tab: VariTab,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

private val VariDestinations = listOf(
    NavDestination(
        tab = VariTab.WRITE,
        labelRes = R.string.tab_write,
        selectedIcon = Icons.Filled.Edit,
        unselectedIcon = Icons.Outlined.Edit,
        testTag = "nav_tab_write"
    ),
    NavDestination(
        tab = VariTab.MY_STYLE,
        labelRes = R.string.tab_my_style,
        selectedIcon = Icons.Filled.AutoAwesome,
        unselectedIcon = Icons.Outlined.AutoAwesome,
        testTag = "nav_tab_my_style"
    ),
    NavDestination(
        tab = VariTab.IDEAS,
        labelRes = R.string.tab_ideas,
        selectedIcon = Icons.Filled.Lightbulb,
        unselectedIcon = Icons.Outlined.Lightbulb,
        testTag = "nav_tab_ideas"
    ),
    NavDestination(
        tab = VariTab.MY_SONGS,
        labelRes = R.string.tab_my_songs,
        selectedIcon = Icons.Filled.LibraryMusic,
        unselectedIcon = Icons.Outlined.LibraryMusic,
        testTag = "nav_tab_my_songs"
    )
)

@Composable
fun VariApp(
    viewModel: VariViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val writeState by viewModel.writeState.collectAsStateWithLifecycle()
    val visionState by viewModel.visionState.collectAsStateWithLifecycle()
    val styleState by viewModel.styleState.collectAsStateWithLifecycle()
    val ideasState by viewModel.ideasState.collectAsStateWithLifecycle()
    val songsState by viewModel.songsState.collectAsStateWithLifecycle()
    val styleLyrics by viewModel.styleLyrics.collectAsStateWithLifecycle()
    val savedSongs by viewModel.savedSongs.collectAsStateWithLifecycle()

    // Localized messages for callbacks
    val msgCopied = stringResource(R.string.msg_copied)
    val msgContinuationCopied = stringResource(R.string.msg_continuation_copied)
    val msgFullSongCopied = stringResource(R.string.msg_full_song_copied)
    val msgSongSaved = stringResource(R.string.msg_song_saved)
    val msgSongUpdated = stringResource(R.string.msg_song_updated)
    val msgSongDeleted = stringResource(R.string.msg_song_deleted)
    val msgStyleSaved = stringResource(R.string.msg_style_saved)
    val msgStyleUpdated = stringResource(R.string.msg_style_updated)
    val msgStyleDeleted = stringResource(R.string.msg_style_deleted)
    val msgIdeaLoaded = stringResource(R.string.msg_idea_loaded)
    val msgExportFailed = stringResource(R.string.msg_export_failed)
    val msgImportFailed = stringResource(R.string.msg_import_failed)
    val msgCameraUnavailable = stringResource(R.string.vision_camera_unavailable)
    val msgImageLoadError = stringResource(R.string.vision_image_load_error)
    val shareChooserTitle = stringResource(R.string.share_chooser_title)

    LaunchedEffect(Unit) {
        viewModel.snackbarMessages.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // Back press on non-Write tabs cleanly returns to the Write tab
    BackHandler(enabled = currentTab != VariTab.WRITE) {
        viewModel.selectTab(VariTab.WRITE)
    }

    // Zero-permission SAF Backup Export launcher
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val (json, summary) = viewModel.buildExportBackup()
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(uri)?.use { stream ->
                            stream.write(json.toByteArray(Charsets.UTF_8))
                        } ?: error("Output stream unavailable")
                    }
                    viewModel.postMessage(
                        context.getString(R.string.msg_export_success_fmt, summary.songCount, summary.styleCount)
                    )
                } catch (_: Exception) {
                    viewModel.postMessage(msgExportFailed)
                }
            }
        }
    }

    // Zero-permission SAF Backup Import launcher
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val rawJson = try {
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use {
                            it.readText()
                        }
                    }
                } catch (_: Exception) {
                    null
                }
                if (rawJson.isNullOrBlank()) {
                    viewModel.postMessage(msgImportFailed)
                    return@launch
                }
                viewModel.importBackup(rawJson)
                    .onSuccess { summary ->
                        viewModel.postMessage(
                            context.getString(R.string.msg_import_success_fmt, summary.songCount, summary.styleCount)
                        )
                    }
                    .onFailure {
                        viewModel.postMessage(msgImportFailed)
                    }
            }
        }
    }

    // Camera capture & Zero-permission Android Photo Picker launchers
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    fun loadSelectedImageUri(uri: Uri) {
        scope.launch {
            try {
                val compressed = VisionImageHelper.loadCompressedBitmap(context, uri)
                viewModel.onVisionPhotoCaptured(compressed)
            } catch (_: Exception) {
                viewModel.onCameraStatusError(msgImageLoadError)
            }
        }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { captured ->
        val uri = pendingCameraUri
        pendingCameraUri = null
        if (captured && uri != null) {
            loadSelectedImageUri(uri)
        }
    }

    fun launchCameraCapture() {
        try {
            val captureUri = VisionImageHelper.createTempCameraUri(context)
            pendingCameraUri = captureUri
            takePictureLauncher.launch(captureUri)
        } catch (_: ActivityNotFoundException) {
            pendingCameraUri = null
            viewModel.onCameraStatusError(msgCameraUnavailable)
        } catch (_: Exception) {
            pendingCameraUri = null
            viewModel.onCameraStatusError(msgCameraUnavailable)
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.onCameraPermissionResult(granted)
        if (granted) {
            launchCameraCapture()
        }
    }

    fun requestCameraAndCapture() {
        val hasCamera = context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
        if (!hasCamera) {
            viewModel.onCameraStatusError(msgCameraUnavailable)
            return
        }
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            viewModel.onCameraPermissionResult(true)
            launchCameraCapture()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            loadSelectedImageUri(uri)
        }
    }

    fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
        context.startActivity(intent)
    }

    fun copyToClipboard(text: String, confirmation: String = msgCopied) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Vari Lyrics", text))
        viewModel.postMessage(confirmation)
    }

    fun shareLyricsText(title: String, lyrics: String) {
        val shareBody = "$title\n\n$lyrics"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, shareBody)
        }
        context.startActivity(Intent.createChooser(intent, shareChooserTitle))
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val useNavRail = maxWidth >= 600.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                VariTopAppBar(
                    styleCount = styleLyrics.size,
                    onStyleBadgeClick = { viewModel.selectTab(VariTab.MY_STYLE) }
                )
            },
            bottomBar = {
                if (!useNavRail) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp
                    ) {
                        VariDestinations.forEach { dest ->
                            val selected = currentTab == dest.tab
                            NavigationBarItem(
                                selected = selected,
                                onClick = { viewModel.selectTab(dest.tab) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = stringResource(dest.labelRes)
                                    )
                                },
                                label = {
                                    Text(
                                        text = stringResource(dest.labelRes),
                                        style = MaterialTheme.typography.labelMedium,
                                        maxLines = 1
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primary,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.testTag(dest.testTag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useNavRail) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        VariDestinations.forEach { dest ->
                            val selected = currentTab == dest.tab
                            NavigationRailItem(
                                selected = selected,
                                onClick = { viewModel.selectTab(dest.tab) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = stringResource(dest.labelRes)
                                    )
                                },
                                label = {
                                    Text(
                                        text = stringResource(dest.labelRes),
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primary,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.testTag(dest.testTag)
                            )
                        }
                    }
                }

                Crossfade(
                    targetState = currentTab,
                    label = "vari_tab_transition",
                    modifier = Modifier.weight(1f)
                ) { tab ->
                    when (tab) {
                        VariTab.WRITE -> WriteScreen(
                            state = writeState,
                            visionState = visionState,
                            savedStyleCount = styleLyrics.size,
                            onLanguageSelected = viewModel::onWriteLanguageSelected,
                            onCustomLanguageChanged = viewModel::onWriteCustomLanguageChanged,
                            onTopicChanged = viewModel::onTopicChanged,
                            onMoodSelected = viewModel::onWriteMoodSelected,
                            onSongTypeSelected = viewModel::onWriteSongTypeSelected,
                            onExtraInstructionsChanged = viewModel::onExtraInstructionsChanged,
                            onCreateLyrics = { viewModel.createLyrics(isRegenerate = false) },
                            onRegenerate = { viewModel.regenerateCurrentWriteResult() },
                            onCopyLyrics = { text -> copyToClipboard(text, msgCopied) },
                            onSaveSong = { viewModel.saveGeneratedSong(msgSongSaved) },
                            onSuggestImprovements = viewModel::suggestImprovements,
                            onOpenMyStyle = { viewModel.selectTab(VariTab.MY_STYLE) },
                            onVisionModeSelected = viewModel::onVisionModeSelected,
                            onCaptureInspiration = { requestCameraAndCapture() },
                            onUploadGallery = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            onRemoveVisionImage = {
                                VisionImageHelper.clearTempCaptures(context)
                                viewModel.onVisionPhotoRemoved()
                            },
                            onOpenCameraSettings = { openAppSettings() },
                            onExistingLyricsChanged = viewModel::onExistingLyricsChanged,
                            onGenerateFromPhoto = { viewModel.generateLyricsFromPhoto(isRegenerate = false) },
                            onContinueMyLyrics = { viewModel.continueLyricsFromPhoto(isRegenerate = false) },
                            onCopyContinuation = { text -> copyToClipboard(text, msgContinuationCopied) },
                            onCopyFullSong = { text -> copyToClipboard(text, msgFullSongCopied) },
                            onSaveContinuedSong = { viewModel.saveContinuedSong(msgSongSaved) },
                            onRegenerateContinuation = { viewModel.continueLyricsFromPhoto(isRegenerate = true) }
                        )

                        VariTab.MY_STYLE -> MyStyleScreen(
                            state = styleState,
                            styleLyrics = styleLyrics,
                            onTitleChanged = viewModel::onStyleTitleChanged,
                            onLanguageSelected = viewModel::onStyleLanguageSelected,
                            onCustomLanguageChanged = viewModel::onStyleCustomLanguageChanged,
                            onMoodSelected = viewModel::onStyleMoodSelected,
                            onLyricsTextChanged = viewModel::onStyleLyricsTextChanged,
                            onSaveStyle = { viewModel.saveStyleLyric(msgStyleSaved, msgStyleUpdated) },
                            onCancelEdit = viewModel::cancelEditingStyle,
                            onEditItem = viewModel::startEditingStyle,
                            onDeleteItem = { viewModel.deleteStyleLyric(it.id, msgStyleDeleted) }
                        )

                        VariTab.IDEAS -> IdeasScreen(
                            state = ideasState,
                            onLanguageSelected = viewModel::onIdeasLanguageSelected,
                            onCustomLanguageChanged = viewModel::onIdeasCustomLanguageChanged,
                            onGenerateIdeas = viewModel::generateFreshIdeas,
                            onIdeaClick = { idea -> viewModel.applyIdeaToWriteTab(idea, msgIdeaLoaded) }
                        )

                        VariTab.MY_SONGS -> MySongsScreen(
                            state = songsState,
                            songs = savedSongs,
                            onSearchQueryChanged = viewModel::onSearchQueryChanged,
                            onLanguageFilterSelected = viewModel::onLanguageFilterSelected,
                            onCopySong = { song -> copyToClipboard("${song.title}\n\n${song.lyrics}") },
                            onShareSong = { song -> shareLyricsText(song.title, song.lyrics) },
                            onUpdateSong = { updated -> viewModel.updateSavedSong(updated, msgSongUpdated) },
                            onDeleteSong = { song -> viewModel.deleteSavedSong(song.id, msgSongDeleted) },
                            onExportAll = {
                                val stamp = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
                                exportLauncher.launch("vari_backup_$stamp.json")
                            },
                            onImportBackup = {
                                importLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                            }
                        )
                    }
                }
            }
        }
    }
}
