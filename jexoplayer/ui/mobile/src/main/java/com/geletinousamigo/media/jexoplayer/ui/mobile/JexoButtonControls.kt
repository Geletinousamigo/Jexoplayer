package com.geletinousamigo.media.jexoplayer.ui.mobile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.SubtitlesOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.geletinousamigo.media.jexoplayer.LocalJexoPlayer
import com.geletinousamigo.media.jexoplayer.model.Language
import com.geletinousamigo.media.jexoplayer.model.PlaybackState.BUFFERING
import com.geletinousamigo.media.jexoplayer.model.PlaybackState.ENDED
import com.geletinousamigo.media.jexoplayer.util.formatTime
import kotlin.time.Duration.Companion.milliseconds


@Composable
fun FullScreenButton(
    modifier: Modifier = Modifier
) {
    val controller = LocalJexoPlayer.current
    val isFullscreen by controller.collect { isFullScreen }
    IconButton(
        onClick = { controller.toggleFullscreen() },
        modifier = modifier
    ) {
        if (isFullscreen) {
            ShadowedIcon(icon = Icons.Filled.FullscreenExit)
        } else {
            ShadowedIcon(icon = Icons.Filled.Fullscreen)
        }
    }
}

@Preview
@Composable
fun AudioTrackChangeButton(
    modifier: Modifier = Modifier,
    isAudioTrackDialogVisible: MutableState<Boolean> = mutableStateOf(false),
) {
    IconButton(
        onClick = {
            isAudioTrackDialogVisible.value = isAudioTrackDialogVisible.value.not()
        },
        modifier = modifier
    ) {
        ShadowedIcon(icon = Icons.Filled.Settings)
    }
}

@Preview
@Composable
fun SubtitleButton(
    modifier: Modifier = Modifier,
) {
    val controller = LocalJexoPlayer.current
    val useSubtitles by controller.collect { useSubTitles }
    IconButton(
        onClick = {
            controller.toggleSubtitles()
        },
        modifier = modifier
    ) {
        if (useSubtitles) {
            ShadowedIcon(icon = Icons.Filled.Subtitles)
        } else {
            ShadowedIcon(icon = Icons.Filled.SubtitlesOff)
        }

    }
}

@Composable
fun PositionAndDurationNumbers(
    modifier: Modifier = Modifier
) {
    val controller = LocalJexoPlayer.current
    val state by controller.collect()

    val positionText by controller.collect {
        currentPosition.milliseconds.formatTime()
    }
    val remainingDurationText by controller.collect {
        (duration - currentPosition).milliseconds.formatTime()
    }
    val appearAlpha = remember { Animatable(0f) }

    LaunchedEffect(state.controlsVisible) {
        appearAlpha.animateTo(
            targetValue = if (state.controlsVisible) 1f else 0f,
            animationSpec = tween(
                durationMillis = 250,
                easing = LinearEasing
            )
        )
    }

    Row(
        modifier = modifier
            .alpha(appearAlpha.value)
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        Text(
            positionText,
            style = TextStyle(
                shadow = Shadow(
                    blurRadius = 8f,
                    offset = Offset(2f, 2f)
                )
            )
        )
        Box(modifier = Modifier.weight(1f))
        Text(
            remainingDurationText,
            style = TextStyle(
                shadow = Shadow(
                    blurRadius = 8f,
                    offset = Offset(2f, 2f)
                )
            )
        )
    }
}

@Preview
@Composable
fun PlayPauseButton(
    modifier: Modifier = Modifier,
) {
    val controller = LocalJexoPlayer.current

    val isPlaying by controller.collect { isPlaying }
    val playbackState by controller.collect { playbackState }

    if (playbackState == BUFFERING) {
        /*CircularProgressIndicator(
            modifier = modifier
        )*/
    } else {
        IconButton(
            onClick = { controller.playPauseToggle() },
            modifier = modifier
        ) {
            if (isPlaying) {
                ShadowedIcon(icon = Icons.Filled.Pause)
            } else {
                when (playbackState) {
                    ENDED -> {
                        ShadowedIcon(icon = Icons.Filled.Restore)
                    }

                    else -> {
                        ShadowedIcon(icon = Icons.Filled.PlayArrow)
                    }
                }
            }
        }
    }


}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun AudioTrackSelectorDialog(
    modifier: Modifier = Modifier,
    isAudioTrackDialogVisible: MutableState<Boolean> = mutableStateOf(false)
) {

    val controller = LocalJexoPlayer.current
    val selectedAudioTrack by controller.collect { selectedAudioTrack }
    val audioTracks by controller.collect { audioTracks }

    AnimatedVisibility(visible = isAudioTrackDialogVisible.value) {

        var isExpanded by remember { mutableStateOf(false) }
        var tempSelectedLang by remember { mutableStateOf(selectedAudioTrack) }

        AlertDialog(
            onDismissRequest = {
                isAudioTrackDialogVisible.value = false
                controller.clearHideSeconds()
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (selectedAudioTrack != tempSelectedLang) {
                            controller.setPreferredAudioLanguage(tempSelectedLang!!)
                        }
                        isAudioTrackDialogVisible.value = false
                    }
                ) {
                    Text(text = "Apply")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        isAudioTrackDialogVisible.value = false
                    }
                ) {
                    Text(text = "Cancel")
                }
            },
            title = {
                Text(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    text = "Settings"
                )
            },
            text = {
                Row {
                    Text(text = "Audio: ")
                    ExposedDropdownMenuBox(
                        expanded = isExpanded,
                        onExpandedChange = { isExpanded = it }
                    ) {
                        TextField(
                            value = tempSelectedLang?.name ?: "None",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
                            },
                            shape = OutlinedTextFieldDefaults.shape,
                            colors = ExposedDropdownMenuDefaults.textFieldColors(),
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = isExpanded,
                            onDismissRequest = { isExpanded = false }
                        ) {
                            audioTracks.forEach {
                                DropdownMenuItem(
                                    text = { Text(text = it.name) },
                                    onClick = {
                                        tempSelectedLang = it
                                        isExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        )

        /*LazyColumn(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    text = "Select Preferred Language"
                )
            }

            items(audioTracks) { lang ->
                SelectableItem(
                    language = lang,
                    selectedAudioTrack = selectedAudioTrack ?: audioTracks.first(),
                    onClickListener = controller::setPreferredAudioLanguage
                )
            }
        }*/

    }
}

@Composable
fun SelectableItem(
    language: Language,
    selectedAudioTrack: Language,
    onClickListener: (Language) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = (language.code == selectedAudioTrack.code),
                onClick = {
                    onClickListener(language)
                }
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = (language.code == selectedAudioTrack.code),
            onClick = {
                onClickListener(language)
            }
        )
        Text(
            text = language.name,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}