package com.geletinousamigo.media.jexoplayer.ui.tv

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.ListItem
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.geletinousamigo.media.jexoplayer.LocalJexoPlayer


@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvAudioTrackSelectorModal(
    isAudioTrackModalVisible: MutableState<Boolean>,
) {
    val controller = LocalJexoPlayer.current
    val languages by controller.collect { audioTracks }
    val selectedLang by controller.collect { selectedAudioTrack }

    AnimatedVisibility(
        visible = isAudioTrackModalVisible.value,
        enter = slideInVertically { it },
        exit = slideOutVertically { it }
    ) {
        TvLazyColumn(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(20.dp)
            /*.border(
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.border),
                shape = RoundedCornerShape(20.dp)
            )*/
        ) {
            item {
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = "Audio Tracks",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(12.dp))

                HorizontalDivider()
            }

            languages.forEach { lang ->
                item {
                    ListItem(
                        selected = selectedLang == lang,
                        onClick = {
                            controller.setPreferredAudioLanguage(lang)
                            isAudioTrackModalVisible.value = false
                        },
                        trailingContent = {
                            if (selectedLang == lang) {
                                Icon(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .padding(8.dp),
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = "Checked",
                                    tint = LocalContentColor.current
                                )
                            }
                        }
                    ) {
                        Text(text = lang.name)
                    }
                }
            }
        }
    }
}