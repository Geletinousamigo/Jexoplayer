package com.geletinousamigo.media.jexoplayer.model

import androidx.annotation.RawRes

sealed class VideoPlayerSource() {
    data class Raw(
        @RawRes val resId: Int,
        val userAgent: String
    ) : VideoPlayerSource()

    data object Network{
        data class Dash(
            val url: String,
            val headers: Map<String, String> = mapOf(),
            val licenseUrl: String = "",
            val userAgent: String
        ) : VideoPlayerSource()

        data class Hls(
            val url: String,
            val headers: Map<String, String> = mapOf(),
            val userAgent: String
        ) : VideoPlayerSource()
    }
}