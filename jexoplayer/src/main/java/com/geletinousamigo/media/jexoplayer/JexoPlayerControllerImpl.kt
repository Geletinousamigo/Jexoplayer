package com.geletinousamigo.media.jexoplayer

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.RawResourceDataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.MetadataRetriever
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.PlayerView
import com.geletinousamigo.media.jexoplayer.model.DraggingProgress
import com.geletinousamigo.media.jexoplayer.model.JexoState
import com.geletinousamigo.media.jexoplayer.model.Language
import com.geletinousamigo.media.jexoplayer.model.PlaybackState
import com.geletinousamigo.media.jexoplayer.model.VideoPlayerSource
import com.geletinousamigo.media.jexoplayer.util.ExoUtils
import com.geletinousamigo.media.jexoplayer.util.FlowDebouncer
import com.geletinousamigo.media.jexoplayer.util.setScreenOrientation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import java.net.CookieManager
import java.net.CookiePolicy
import java.util.concurrent.atomic.AtomicBoolean

private const val TAG = "JexoPlayerImpl"

@OptIn(UnstableApi::class)
open class JexoPlayerControllerImpl(
    private val context: Context,
    private val initialState: JexoState,
    private val initialExoPlayer: () -> ExoPlayer
) : JexoPlayerController {

    private var exoPlayerState = mutableStateOf(initialExoPlayer)
    final override val exoPlayer: ExoPlayer get() = exoPlayerState.value.invoke()

    private val okHttpClient = OkHttpClient.Builder()
        .cookieJar(
            cookieJar = JavaNetCookieJar(
                CookieManager().apply {
                    setCookiePolicy(CookiePolicy.ACCEPT_ALL)
                }
            )
        ).build()

    private val countDownTimer = MutableStateFlow(value = 3)

    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<JexoState>
        get() = _state.asStateFlow()

    /**
     * Some properties in initial state are not applicable until player is ready.
     * These are kept in this container. Once the player is ready for the first time,
     * they are applied and removed.
     */
    private var initialStateRunner: (() -> Unit)? = {
        exoPlayer.seekTo(initialState.currentPosition)
    }

    fun <T> currentState(filter: (JexoState) -> T): T {
        return filter(_state.value)
    }

    @Composable
    fun collect(): State<JexoState> {
        return _state.collectAsState()
    }

    @SuppressLint("StateFlowValueCalledInComposition")
    @Composable
    fun <T> collect(filter: JexoState.() -> T): State<T> {
        return remember(filter) {
            _state.map { it.filter() }
        }.collectAsState(
            initial = _state.value.filter()
        )
    }

    lateinit var videoPlayerSource: VideoPlayerSource
    private var playerView: PlayerView? = null

    private var updateDurationAndPositionJob: Job? = null
    private var anotherJob: Job? = null

    private val playerListener: Player.Listener = object : Player.Listener {

        override fun onEvents(player: Player, events: Player.Events) {
            super.onEvents(player, events)
            if (events.contains(Player.EVENT_IS_PLAYING_CHANGED)) {
                _state.update {
                    it.copy(
                        isPlaying = player.isPlaying,
                    )
                }
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (PlaybackState.of(playbackState) == PlaybackState.READY) {
                initialStateRunner = initialStateRunner?.let {
                    it.invoke()
                    null
                }

                updateDurationAndPositionJob?.cancel()
                updateDurationAndPositionJob = coroutineScope.launch {
                    while (isActive) {
                        updateDurationAndPosition()
                        delay(250)
                    }
                }
            }

            _state.update {
                it.copy(
//                    isPlaying = exoPlayer.isPlaying,
                    playbackState = PlaybackState.of(playbackState)
                )
            }
        }

        override fun onTracksChanged(tracks: Tracks) {
            super.onTracksChanged(tracks)
            val audioTracksList = ExoUtils.getLanguagesFromTrackInfo(
                exoPlayer.currentTracks
            )
            _state.update {
                it.copy(
                    audioTracks = audioTracksList,
                )
            }
        }

        override fun onVideoSizeChanged(videoSize: VideoSize) {
            super.onVideoSizeChanged(videoSize)

            videoSize.height.let {
                videoSize.width.let {
                    _state.update {
                        it.copy(
                            videoWidth = videoSize.width.toFloat(),
                            videoHeight = videoSize.height.toFloat()
                        )
                    }
                }
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            super.onPlayerError(error)
            when (error.errorCode) {
                PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW -> {
                    Toast.makeText(context, "Behind Live Window", Toast.LENGTH_LONG).show()
                    exoPlayer.apply {
                        seekToDefaultPosition()
                        seekTo(currentPosition)
                        prepare()
                        play()
                    }
                }

                PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED -> {
                    Toast.makeText(context, "Network Connection Failed", Toast.LENGTH_LONG).show()
                    exoPlayer.apply {
//                        seekToDefaultPosition()
                        seekTo(currentPosition)
                        prepare()
                        play()
                    }
                }

                else -> {
                    Toast.makeText(
                        context,
                        "PlayerError: ${error.errorCodeName}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }


    /**
     * Internal exoPlayer instance
     */
    /*private val exoPlayer = ExoPlayer.Builder(context)
//        .setVideoScalingMode(VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
        .setSeekForwardIncrementMs(10_000L)
        .setSeekBackIncrementMs(10_000L)
        .build()
        .apply {
            addListener(playerListener)

        }*/

    /**
     * Not so efficient way of showing preview in video slider.
     */
    private val previewExoPlayer = ExoPlayer.Builder(context)
        .build()
        .apply {
            playWhenReady = false
        }

    private val previewSeekDebouncer = FlowDebouncer<Long>(200L)

    init {
        exoPlayer.addListener(playerListener)
        exoPlayer.playWhenReady = initialState.isPlaying

        coroutineScope.launch {
            val countTimeJob = async {
                countDownTimer.collectLatest { time ->
                    Log.d(TAG, "currentlyTime is: $time")
                    if (time > 0) {
                        _state.update { it.copy(controlsVisible = true) }
                        delay(1000)
                        countDownTimer.emit(countDownTimer.value - 1)
                    } else {
                        _state.update { it.copy(controlsVisible = false) }
                    }
                }
            }
            val previewJob = async {
                previewSeekDebouncer.collect { position ->
                    previewExoPlayer.seekTo(position)
                }
            }
            countTimeJob.await()
            previewJob.await()
        }
    }

    /**
     * A flag to indicate whether source is already set and waiting for
     * playerView to become available.
     */
    private val waitPlayerViewToPrepare = AtomicBoolean(false)

    override fun toggleFullscreen() {
        if (!_state.value.isFullScreen) {
            context.setScreenOrientation(orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
            _state.update {
                it.copy(isFullScreen = true)
            }
        } else {
            context.setScreenOrientation(orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            _state.update {
                it.copy(isFullScreen = false)
            }
        }
    }

    override fun setPreferredAudioLanguage(lang: Language) {
        exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
            .buildUpon()
            .setPreferredAudioLanguage(lang.code)
            .build()

        _state.update {
            it.copy(
                selectedAudioTrack = lang
            )
        }
    }

    override fun toggleSubtitles() {
        _state.update {
            it.copy(
                useSubTitles = !_state.value.useSubTitles
            )
        }
    }

    override fun setSource(source: VideoPlayerSource) {
        this.videoPlayerSource = source
        if (playerView == null) {
            waitPlayerViewToPrepare.set(true)
        } else {
            prepare()
        }
    }

    override fun play() {
        if (exoPlayer.playbackState == Player.STATE_ENDED) {
            exoPlayer.seekTo(0)
        }
        exoPlayer.playWhenReady = true
    }

    override fun pause() {
        exoPlayer.playWhenReady = false
    }

    override fun playPauseToggle() {
        if (exoPlayer.isPlaying) pause()
        else play()
    }

    override fun seekForward() {
        exoPlayer.seekForward()
        updateDurationAndPosition()
    }

    override fun seekRewind() {
        exoPlayer.seekBack()
        updateDurationAndPosition()
    }

    override fun seek(position: Long) {
        exoPlayer.seekTo(position)
        updateDurationAndPosition()
    }

    override fun reset() {
        release()
    }

    private fun release() {
        exoPlayer.release()
        previewExoPlayer.release()
    }

    fun enableGestures(isEnabled: Boolean) {
        _state.update { it.copy(gesturesEnabled = isEnabled) }
    }

    fun enableControls(enabled: Boolean) {
        _state.update { it.copy(controlsEnabled = enabled) }
    }

    fun showControls() {
        _state.update { it.copy(controlsVisible = true) }
    }

    fun hideControls() {
        _state.update { it.copy(controlsVisible = false) }
    }

    fun setDraggingProgress(draggingProgress: DraggingProgress?) {
        _state.update { it.copy(draggingProgress = draggingProgress) }
    }

    fun addHideSeconds(hideSeconds: Int = 3) {
        anotherJob?.cancel()
        anotherJob = coroutineScope.launch {
            countDownTimer.emit(hideSeconds)
        }
    }

    fun clearHideSeconds() {
        anotherJob?.cancel()
        anotherJob = coroutineScope.launch {
            countDownTimer.emit(0)
        }
    }

    private fun updateDurationAndPosition() {
        _state.update {
            it.copy(
                duration = exoPlayer.duration.coerceAtLeast(0),
                currentPosition = exoPlayer.currentPosition.coerceAtLeast(0),
                secondaryProgress = exoPlayer.bufferedPosition.coerceAtLeast(0)
            )
        }
    }

    override fun provideMediaSource(): MediaSource {

        val mediaLiveConfiguration =
            MediaItem.LiveConfiguration.Builder().setMaxPlaybackSpeed(1.0f).build()

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
                    .apply {
                        if (source.licenseUrl.isNotEmpty()){
                            setLicenseUri(source.licenseUrl)
                        }
                    }
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

    @OptIn(UnstableApi::class)
    private fun prepare() {

        exoPlayer.setMediaSource(provideMediaSource())
        previewExoPlayer.setMediaSource(provideMediaSource())

        exoPlayer.prepare()
        previewExoPlayer.prepare()
    }

    fun playerViewAvailable(playerView: PlayerView) {
        this.playerView = playerView
        playerView.player = exoPlayer
        playerView.setBackgroundColor(Color.Black.toArgb())

        if (waitPlayerViewToPrepare.compareAndSet(true, false)) {
            prepare()
        }
    }

    fun previewPlayerViewAvailable(playerView: PlayerView) {
        playerView.player = previewExoPlayer
    }

    fun previewSeekTo(position: Long) {
        // position is very accurate. Thumbnail doesn't have to be.
        // Roll to the nearest "even" integer.
        val seconds = position.toInt() / 1000
        val nearestEven = (seconds - seconds.rem(2)).toLong()
        coroutineScope.launch {
            previewSeekDebouncer.put(nearestEven * 1000)
        }
    }

    companion object {
        fun saver(context: Context, exoPlayer: ()-> ExoPlayer) = object : Saver<JexoPlayerControllerImpl, JexoState> {
            override fun restore(value: JexoState): JexoPlayerControllerImpl {
                return JexoPlayerControllerImpl(
                    context = context,
                    initialState = value,
                    initialExoPlayer = exoPlayer
                )
            }

            override fun SaverScope.save(value: JexoPlayerControllerImpl): JexoState {
                return value.currentState { it }
            }
        }
    }
}

