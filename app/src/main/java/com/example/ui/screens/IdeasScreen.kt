package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.SongIdea
import com.example.model.VariUiState
import com.example.ui.IdeasTabState
import com.example.ui.components.EmptyStudioCard
import com.example.ui.components.FirebaseAiNoticeBanner
import com.example.ui.components.LanguageDropdownSelector
import com.example.ui.components.MetadataPill
import com.example.ui.components.StudioErrorCard
import com.example.ui.components.StudioLoadingCard

@Composable
fun IdeasScreen(
    state: IdeasTabState,
    onLanguageSelected: (String) -> Unit,
    onCustomLanguageChanged: (String) -> Unit,
    onGenerateIdeas: () -> Unit,
    onIdeaClick: (SongIdea) -> Unit,
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
            item(key = "ideas_controls") {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.ideas_header_title),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.ideas_header_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        LanguageDropdownSelector(
                            selectedOption = state.languageOption,
                            customLanguage = state.customLanguage,
                            onOptionSelected = onLanguageSelected,
                            onCustomLanguageChanged = onCustomLanguageChanged,
                            dropdownTag = "ideas_language_dropdown",
                            customFieldTag = "ideas_custom_language_input"
                        )

                        AnimatedVisibility(visible = state.validationError != null) {
                            Text(
                                text = state.validationError.orEmpty(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        Button(
                            onClick = onGenerateIdeas,
                            enabled = state.ideasState !is VariUiState.Loading,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("fresh_ideas_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = stringResource(R.string.btn_fresh_ideas),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            when (val ideasUi = state.ideasState) {
                is VariUiState.Idle -> {
                    item(key = "ideas_idle_empty") {
                        EmptyStudioCard(
                            icon = Icons.Filled.Lightbulb,
                            title = stringResource(R.string.ideas_empty_title),
                            body = stringResource(R.string.ideas_empty_body)
                        )
                    }
                }
                is VariUiState.Loading, is VariUiState.Streaming -> {
                    item(key = "ideas_loading") {
                        StudioLoadingCard(message = stringResource(R.string.loading_ideas))
                    }
                }
                is VariUiState.Error -> {
                    item(key = "ideas_error") {
                        StudioErrorCard(
                            message = ideasUi.message,
                            onRetry = onGenerateIdeas
                        )
                    }
                }
                is VariUiState.Success -> {
                    if (ideasUi.data.any { it.isPlaceholder }) {
                        item(key = "ideas_ai_notice") {
                            FirebaseAiNoticeBanner()
                        }
                    }
                    itemsIndexed(
                        items = ideasUi.data,
                        key = { index, idea -> "${index}_${idea.title}" }
                    ) { index, idea ->
                        IdeaConceptCard(
                            number = index + 1,
                            idea = idea,
                            onClick = { onIdeaClick(idea) }
                        )
                    }
                }
            }

            item(key = "ideas_bottom_spacer") {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun IdeaConceptCard(
    number: Int,
    idea: SongIdea,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("idea_card_$number")
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = number.toString().padStart(2, '0'),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    MetadataPill(text = idea.language, isAccent = true)
                    MetadataPill(text = idea.suggestedMood)
                    MetadataPill(text = idea.suggestedSongType)
                }
            }

            Text(
                text = idea.title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = idea.oneLineStory,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = stringResource(R.string.ideas_tap_hint),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
