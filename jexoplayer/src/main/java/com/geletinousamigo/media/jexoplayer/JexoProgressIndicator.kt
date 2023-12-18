package com.geletinousamigo.media.jexoplayer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun JexoProgressIndicator(
    modifier: Modifier = Modifier
) {
    val controller = LocalJexoPlayer.current
    val videoPlayerUiState by controller.collect()

    with(videoPlayerUiState) {
        SeekBar(
            progress = currentPosition,
            max = duration,
            enabled = controlsVisible && controlsEnabled,
            onSeek = {
                controller.previewSeekTo(it)
            },
            onSeekStopped = {
                controller.seek(it)
            },
            secondaryProgress = secondaryProgress,
            seekerPopup = {
                JexoPlayerSurface(
                    modifier = Modifier
                        .height(48.dp)
                        .width(48.dp * videoSize.width / videoSize.height)
                        .background(Color.DarkGray)
                ) {
                    controller.previewPlayerViewAvailable(it)
                }
            },
            modifier = modifier
        )
    }
}