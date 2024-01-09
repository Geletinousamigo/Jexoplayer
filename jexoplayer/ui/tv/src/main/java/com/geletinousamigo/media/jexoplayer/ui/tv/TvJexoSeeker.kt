package com.geletinousamigo.media.jexoplayer.ui.tv

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import com.geletinousamigo.media.jexoplayer.LocalJexoPlayer
import com.geletinousamigo.media.jexoplayer.model.JexoState
import com.geletinousamigo.media.jexoplayer.util.formatMinSec
import com.geletinousamigo.media.jexoplayer.util.formatTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun VideoPlayerSeeker() {

    val controller = LocalJexoPlayer.current
    val state by controller.collect()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(state.controlsVisible) {
        focusRequester.requestFocus()
    }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        VideoPlayerControlsIcon(
            modifier = Modifier.focusRequester(focusRequester),
            icon = if (!state.isPlaying) Icons.Default.PlayArrow else Icons.Default.Pause,
            onClick = { controller.playPauseToggle() },
            isPlaying = state.isPlaying,
            contentDescription = null
        )
        VideoPlayerControllerText(text = state.duration.formatMinSec())
        TvJexoProgressIndicator(
            duration = state.duration.toFloat(),
            progress = { (state.currentPosition / state.duration).toFloat() },
            onSeek = { progress -> controller.seek(progress.toLong()) },
            bufferedPosition = state.secondaryProgress.toFloat(),
            addHideSeconds = controller::addHideSeconds,
            primaryProgressColor = MaterialTheme.colorScheme.primaryContainer,
            secondaryProgressColor = MaterialTheme.colorScheme.secondaryContainer
        )
        VideoPlayerControllerText(text = state.currentPosition.formatMinSec())
    }
}