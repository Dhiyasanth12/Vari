package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.local.SavedSongEntity
import com.example.model.VariCatalog
import com.example.ui.SongsTabState
import com.example.ui.components.EmptyStudioCard
import com.example.ui.components.MetadataPill
import com.example.ui.components.variTextFieldColors
import java.text.DateFormat
import java.util.Date

@Composable
fun MySongsScreen(
    state: SongsTabState,
    songs: List<SavedSongEntity>,
    onSearchQueryChanged: (String) -> Unit,
    onLanguageFilterSelected: (String) -> Unit,
    onCopySong: (SavedSongEntity) -> Unit,
    onShareSong: (SavedSongEntity) -> Unit,
    onUpdateSong: (SavedSongEntity) -> Unit,
    onDeleteSong: (SavedSongEntity) -> Unit,
    onExportAll: () -> Unit,
    onImportBackup: () -> Unit,
    modifier: Modifier = Modifier
) {
    var viewingSong by remember { mutableStateOf<SavedSongEntity?>(null) }
    var editingSong by remember { mutableStateOf<SavedSongEntity?>(null) }
    var deletingSong by remember { mutableStateOf<SavedSongEntity?>(null) }

    val availableLanguages = remember(songs) {
        val defaults = VariCatalog.presetLanguages.filter { it != VariCatalog.OTHER_LANGUAGE }
        val custom = songs.map { it.language.trim() }
            .filter { lang -> lang.isNotBlank() && defaults.none { it.equals(lang, ignoreCase = true) } }
            .distinct()
        listOf("All") + defaults + custom
    }

    val filteredSongs = remember(songs, state.searchQuery, state.languageFilter) {
        val q = state.searchQuery.trim().lowercase()
        songs.filter { song ->
            val matchesLang = state.languageFilter == "All" ||
                song.language.equals(state.languageFilter, ignoreCase = true)
            val matchesQuery = q.isEmpty() ||
                song.title.lowercase().contains(q) ||
                song.topic.lowercase().contains(q) ||
                song.lyrics.lowercase().contains(q)
            matchesLang && matchesQuery
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header + Backup Controls
            item(key = "songs_header") {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.songs_header_title),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.songs_header_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FilledTonalButton(
                                onClick = onExportAll,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("export_all_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Download,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(stringResource(R.string.btn_export_all))
                            }
                            OutlinedButton(
                                onClick = onImportBackup,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("import_backup_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Upload,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(stringResource(R.string.btn_import))
                            }
                        }
                    }
                }
            }

            // Search & Language Filter
            item(key = "songs_search_filter") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = onSearchQueryChanged,
                        singleLine = true,
                        placeholder = { Text(stringResource(R.string.hint_search_songs)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = {
                            if (state.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChanged("") }) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = stringResource(R.string.cd_clear_search)
                                    )
                                }
                            }
                        },
                        colors = variTextFieldColors(),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("songs_search_input")
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        availableLanguages.forEach { lang ->
                            val isSelected = lang == state.languageFilter
                            FilterChip(
                                selected = isSelected,
                                onClick = { onLanguageFilterSelected(lang) },
                                label = {
                                    Text(
                                        text = if (lang == "All") stringResource(R.string.filter_all) else lang,
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier
                                    .heightIn(min = 44.dp)
                                    .testTag("songs_filter_${lang.lowercase()}")
                            )
                        }
                    }
                }
            }

            // Song Cards or Empty States
            if (songs.isEmpty()) {
                item(key = "songs_empty") {
                    EmptyStudioCard(
                        icon = Icons.Filled.QueueMusic,
                        title = stringResource(R.string.songs_empty_title),
                        body = stringResource(R.string.songs_empty_body)
                    )
                }
            } else if (filteredSongs.isEmpty()) {
                item(key = "songs_no_matches") {
                    EmptyStudioCard(
                        icon = Icons.Filled.Search,
                        title = stringResource(R.string.songs_no_matches_title),
                        body = stringResource(R.string.songs_no_matches_body)
                    )
                }
            } else {
                items(items = filteredSongs, key = { it.id }) { song ->
                    SavedSongCard(
                        song = song,
                        onView = { viewingSong = song },
                        onCopy = { onCopySong(song) },
                        onEdit = { editingSong = song },
                        onShare = { onShareSong(song) },
                        onDelete = { deletingSong = song }
                    )
                }
            }

            item(key = "songs_bottom_spacer") {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // View Song Dialog
    viewingSong?.let { song ->
        Dialog(onDismissRequest = { viewingSong = null }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = song.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewingSong = null }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = stringResource(R.string.cd_close_dialog)
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MetadataPill(text = song.language, isAccent = true)
                        MetadataPill(text = song.mood)
                        MetadataPill(text = song.songType)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 380.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = song.lyrics,
                            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 28.sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(
                            onClick = { onCopySong(song) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.btn_copy))
                        }
                        Button(
                            onClick = { onShareSong(song) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.btn_share))
                        }
                    }
                }
            }
        }
    }

    // Edit Song Dialog
    editingSong?.let { song ->
        EditSavedSongDialog(
            song = song,
            onDismiss = { editingSong = null },
            onSave = { updated ->
                onUpdateSong(updated)
                editingSong = null
            }
        )
    }

    // Confirm Delete Dialog
    deletingSong?.let { song ->
        AlertDialog(
            onDismissRequest = { deletingSong = null },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            title = { Text(stringResource(R.string.dialog_delete_title)) },
            text = { Text(stringResource(R.string.dialog_delete_song_body, song.title)) },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteSong(song)
                        deletingSong = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(stringResource(R.string.btn_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingSong = null }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }
}

@Composable
private fun SavedSongCard(
    song: SavedSongEntity,
    onView: () -> Unit,
    onCopy: () -> Unit,
    onEdit: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    val dateText = remember(song.createdAt) {
        DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(song.createdAt))
    }

    Surface(
        onClick = onView,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("saved_song_card_${song.id}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MetadataPill(text = song.language, isAccent = true)
                MetadataPill(text = song.mood)
                MetadataPill(text = song.songType)
            }

            Text(
                text = song.lyrics,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(
                        onClick = onCopy,
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.btn_copy))
                    }
                    TextButton(
                        onClick = onEdit,
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.btn_edit))
                    }
                    TextButton(
                        onClick = onShare,
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.btn_share))
                    }
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.DeleteOutline,
                        contentDescription = stringResource(R.string.btn_delete),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun EditSavedSongDialog(
    song: SavedSongEntity,
    onDismiss: () -> Unit,
    onSave: (SavedSongEntity) -> Unit
) {
    var title by remember(song.id) { mutableStateOf(song.title) }
    var language by remember(song.id) { mutableStateOf(song.language) }
    var lyrics by remember(song.id) { mutableStateOf(song.lyrics) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.dialog_edit_song_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    singleLine = true,
                    label = { Text(stringResource(R.string.label_song_title)) },
                    colors = variTextFieldColors(),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = language,
                    onValueChange = { language = it },
                    singleLine = true,
                    label = { Text(stringResource(R.string.label_language)) },
                    colors = variTextFieldColors(),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = lyrics,
                    onValueChange = { lyrics = it },
                    label = { Text(stringResource(R.string.label_lyrics_text)) },
                    minLines = 7,
                    maxLines = 14,
                    colors = variTextFieldColors(),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text(stringResource(R.string.btn_cancel))
                    }
                    Button(
                        onClick = {
                            if (title.isNotBlank() && lyrics.isNotBlank()) {
                                onSave(
                                    song.copy(
                                        title = title.trim(),
                                        language = language.trim().ifBlank { song.language },
                                        lyrics = lyrics.trim()
                                    )
                                )
                            }
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .height(48.dp)
                    ) {
                        Text(stringResource(R.string.btn_save_changes))
                    }
                }
            }
        }
    }
}
