package com.geletinousamigo.media.jexoplayer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import com.geletinousamigo.media.jexoplayer.model.JexoState
import com.geletinousamigo.media.jexoplayer.model.VideoPlayerSource

val LocalJexoPlayer =
    compositionLocalOf<JexoPlayerImpl> { error("JexoPlayer is not initialized") }

@Composable
fun rememberJexoPlayer(
    source: VideoPlayerSource? = null
): JexoPlayer {
    val context = LocalContext.current

    return rememberSaveable(
        context,
        saver = JexoPlayerImpl.saver(context),
        init = {
            JexoPlayerImpl(
                context = context,
                initialState = JexoState()
            ).apply {
                source?.let { setSource(it) }
            }
        }
    )
}


