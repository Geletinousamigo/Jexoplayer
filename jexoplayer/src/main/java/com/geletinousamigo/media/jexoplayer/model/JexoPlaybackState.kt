package com.geletinousamigo.media.jexoplayer.model

import androidx.media3.common.Player.*


enum class PlaybackState(val value: Int) {

    IDLE(STATE_IDLE),
    BUFFERING(STATE_BUFFERING),
    READY(STATE_READY),
    ENDED(STATE_ENDED);

    companion object {
        fun of(value: Int): PlaybackState {
            return entries.first { it.value == value }
        }
    }
}