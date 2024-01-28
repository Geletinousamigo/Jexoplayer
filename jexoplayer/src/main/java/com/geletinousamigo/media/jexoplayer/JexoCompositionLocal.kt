package com.geletinousamigo.media.jexoplayer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.media3.exoplayer.ExoPlayer
import com.geletinousamigo.media.jexoplayer.model.JexoState
import com.geletinousamigo.media.jexoplayer.model.VideoPlayerSource

val LocalJexoPlayer =
    compositionLocalOf<JexoPlayerControllerImpl> { error("JexoPlayer is not initialized") }

@Composable
fun rememberJexoPlayerController(
    source: VideoPlayerSource? = null,
    exoPlayer:  () -> ExoPlayer,
): JexoPlayerController {
    val context = LocalContext.current

    return rememberSaveable(
        saver = JexoPlayerControllerImpl.saver(context, exoPlayer)
    ){
        JexoPlayerControllerImpl(
            context = context,
            initialState = JexoState(),
            initialExoPlayer = exoPlayer
        ).apply {
            source?.let { setSource(it) }
        }
    }
}


