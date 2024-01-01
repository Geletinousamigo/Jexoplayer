package com.geletinousamigo.media.jexoplayer

import android.net.Uri
import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaItem.DrmConfiguration
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import okhttp3.OkHttpClient

private const val UserAgent = "JexoPlayer(0.0.1)"

data class VideoSourceState(
    val source: MediaSource? = null
)
@OptIn(UnstableApi::class)
class PlayerViewModel : ViewModel() {

    init {
        createMediaSource()
    }

    private val _videoSourceState = MutableStateFlow(VideoSourceState())
//    val videoSourceState = _videoSourceState.asStateFlow()


    /**r
     * Stream Url from Network or Raw Source
     * */
    private val url = "https://moctobpltc-i.akamaihd.net/hls/live/571329/eight/playlist.m3u8"

    /**
     * Headers from Persistance Storage
     * */
    private val headers = mapOf(Pair("",""))


    private fun createDataSourceFactory(): DataSource.Factory {
        return OkHttpDataSource.Factory(OkHttpClient()).apply {
            setUserAgent(UserAgent)
            setDefaultRequestProperties(headers)
        }
    }

    private fun createDrmConfiguration(): DrmConfiguration {
        return DrmConfiguration.Builder(C.WIDEVINE_UUID)
            .build()
    }
    private val liveConfig = MediaItem.LiveConfiguration.Builder().setMaxPlaybackSpeed(1.0f).build()

    private fun createMediaItem(): MediaItem {
        return MediaItem.Builder()
            .setUri(Uri.parse(url))
            .setDrmConfiguration(createDrmConfiguration())
            .setLiveConfiguration(liveConfig)
            .setTag(null)
            .build()
    }

    fun createMediaSource() {
        val source = HlsMediaSource.Factory(createDataSourceFactory())
            .createMediaSource(createMediaItem())
        _videoSourceState.update {
            VideoSourceState(source)
        }
    }



}