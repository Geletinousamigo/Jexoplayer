package com.geletinousamigo.media.jexoplayer.model

import androidx.annotation.RawRes

sealed class VideoPlayerSource {
    data class Raw(
        @RawRes val resId: Int
    ) : VideoPlayerSource()

    data object Network{
        data class Dash(
            val url: String, val headers: Map<String, String> = mapOf()
        ) : VideoPlayerSource()

        data class Hls(
            val url: String, val headers: Map<String, String> = mapOf()
        ) : VideoPlayerSource()
    }
}