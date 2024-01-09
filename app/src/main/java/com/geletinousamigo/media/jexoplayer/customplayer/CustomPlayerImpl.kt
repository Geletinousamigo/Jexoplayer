package com.geletinousamigo.media.jexoplayer.customplayer

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.RawResourceDataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.geletinousamigo.media.jexoplayer.JavaNetCookieJar
import com.geletinousamigo.media.jexoplayer.JexoPlayer
import com.geletinousamigo.media.jexoplayer.JexoPlayerImpl
import com.geletinousamigo.media.jexoplayer.model.JexoState
import com.geletinousamigo.media.jexoplayer.model.VideoPlayerSource
import okhttp3.OkHttpClient
import java.net.CookieManager
import java.net.CookiePolicy

@Composable
fun rememberCustomPlayer(
    source: VideoPlayerSource? = null
): JexoPlayer {
    val context = LocalContext.current

    return rememberSaveable(
        context,
        saver = CustomPlayerImpl.saver(context),
        init = {
            CustomPlayerImpl(
                context = context,
                initialState = JexoState()
            ).apply {
                source?.let { setSource(it) }
            }
        }
    )
}

class CustomPlayerImpl(
    context: Context,
    initialState: JexoState
) : JexoPlayerImpl(context, initialState) {

    @OptIn(UnstableApi::class)
    override fun provideMediaSource(): MediaSource {
        val mediaLiveConfiguration =
            MediaItem.LiveConfiguration.Builder().setMaxPlaybackSpeed(1.0f).build()

        val okHttpClient = OkHttpClient.Builder()
            .cookieJar(
                cookieJar = JavaNetCookieJar(
                    CookieManager().apply {
                        setCookiePolicy(CookiePolicy.ACCEPT_ALL)
                    }
                )
            ).build()
        return when (val source = videoPlayerSource) {
            is VideoPlayerSource.Network.Dash -> {
                val dataSourceFactory = OkHttpDataSource.Factory(okHttpClient)
                    .apply {
                        setUserAgent(source.userAgent)
                        setDefaultRequestProperties(source.headers)
                    }

                val dashDrmConfiguration = MediaItem.DrmConfiguration
                    .Builder(C.WIDEVINE_UUID)
                    .setMultiSession(true)
                    .setLicenseUri(source.licenseUrl)
                    .setLicenseRequestHeaders(source.headers)
                    .build()

                val dashMediaItem = MediaItem.Builder().apply {
                    setUri(Uri.parse(source.url))
                    setDrmConfiguration(dashDrmConfiguration)
                    setLiveConfiguration(mediaLiveConfiguration)
                    setMimeType(MimeTypes.APPLICATION_MPD)
                    setTag(null)
                }.build()

                DashMediaSource
                    .Factory(dataSourceFactory)
                    .createMediaSource(dashMediaItem)
            }

            is VideoPlayerSource.Network.Hls -> {
                val dataSourceFactory = OkHttpDataSource.Factory(okHttpClient)
                    .apply {
                        setUserAgent(source.userAgent)
                        setDefaultRequestProperties(source.headers)
                    }

                val hslMediaItem = MediaItem.Builder()
                    .setUri(Uri.parse(source.url))
                    .setDrmConfiguration(
                        MediaItem.DrmConfiguration.Builder(C.WIDEVINE_UUID)
                            .setMultiSession(true)
                            .build()
                    )
                    .setLiveConfiguration(mediaLiveConfiguration)
                    .setTag(null)
                    .build()

                HlsMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(hslMediaItem)
            }

            is VideoPlayerSource.Raw -> {
                val dataSourceFactory = DefaultHttpDataSource.Factory()
                    .apply {
                        setUserAgent(source.userAgent)
                    }
                ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(
                        MediaItem.fromUri(
                            RawResourceDataSource.buildRawResourceUri(source.resId)
                        )
                    )
            }

        }
    }

    companion object {
        fun saver(context: Context) = object : Saver<CustomPlayerImpl, JexoState> {
            override fun restore(value: JexoState): CustomPlayerImpl {
                return CustomPlayerImpl(
                    context = context,
                    initialState = value
                )
            }

            override fun SaverScope.save(value: CustomPlayerImpl): JexoState {
                return value.currentState { it }
            }
        }
    }
}

