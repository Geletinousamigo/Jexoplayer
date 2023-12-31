package com.geletinousamigo.media.jexoplayer

import androidx.compose.runtime.Stable
import com.geletinousamigo.media.jexoplayer.model.JexoState
import com.geletinousamigo.media.jexoplayer.model.Language
import com.geletinousamigo.media.jexoplayer.model.VideoPlayerSource
import kotlinx.coroutines.flow.StateFlow

interface JexoPlayer {

    fun setSource(source: VideoPlayerSource)

    fun play()

    fun pause()

    fun playPauseToggle()

    fun seekForward()

    fun seekRewind()

    fun seek(position: Long)

    fun reset()

    fun toggleFullscreen()

    fun setPreferredAudioLanguage(lang: Language)

    val state: StateFlow<JexoState>
}