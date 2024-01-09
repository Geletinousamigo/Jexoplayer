package com.geletinousamigo.media.jexoplayer

import androidx.media3.exoplayer.source.MediaSource
import com.geletinousamigo.media.jexoplayer.model.JexoState
import com.geletinousamigo.media.jexoplayer.model.Language
import com.geletinousamigo.media.jexoplayer.model.VideoPlayerSource
import kotlinx.coroutines.flow.StateFlow

interface JexoPlayer {

    fun provideMediaSource(): MediaSource
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