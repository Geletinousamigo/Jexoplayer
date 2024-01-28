package com.geletinousamigo.media.jexoplayer

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.media3.common.C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.geletinousamigo.media.jexoplayer.model.VideoPlayerSource
import com.geletinousamigo.media.jexoplayer.ui.mobile.AudioTrackChangeButton
import com.geletinousamigo.media.jexoplayer.ui.mobile.AudioTrackSelectorDialog
import com.geletinousamigo.media.jexoplayer.ui.mobile.FullScreenButton
import com.geletinousamigo.media.jexoplayer.ui.mobile.JexoGesturesBox
import com.geletinousamigo.media.jexoplayer.ui.mobile.JexoProgressIndicator
import com.geletinousamigo.media.jexoplayer.ui.mobile.PlayPauseButton
import com.geletinousamigo.media.jexoplayer.ui.mobile.PositionAndDurationNumbers
import com.geletinousamigo.media.jexoplayer.ui.mobile.SubtitleButton
import com.geletinousamigo.media.jexoplayer.ui.theme.JexoplayerTheme
import com.geletinousamigo.media.jexoplayer.util.keepScreenOn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope

@OptIn(UnstableApi::class)
class MainActivity : ComponentActivity() {

    @Composable
    fun rememberExoPlayer(listener: Player.Listener? = null) = remember {
        ExoPlayer.Builder(applicationContext).apply {
            setMediaSourceFactory(
                ProgressiveMediaSource.Factory(DefaultDataSource.Factory(applicationContext))
            )
            setVideoScalingMode(VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
            setSeekForwardIncrementMs(10_000L)
            setSeekBackIncrementMs(10_000L)
        }.build().apply {
            playWhenReady = true
            repeatMode = Player.REPEAT_MODE_ONE
            listener?.let {
                addListener(listener)
            }
        }
    }

    override fun onCreate(savedInstancejexoPlayer: Bundle?) {
        super.onCreate(savedInstancejexoPlayer)
        val userAgent = Util.getUserAgent(this, "plaYtv")
        setContent {
            JexoplayerTheme {
                val source = VideoPlayerSource.Network.Hls(
                    url = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8",
                    userAgent = userAgent
                )

                val liveHlsSource = VideoPlayerSource.Network.Hls(
                    url = "https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8",
                    userAgent = userAgent
                )

                val playerListener: Player.Listener = object : Player.Listener {
                    override fun onEvents(player: Player, events: Player.Events) {
                        super.onEvents(player, events)
                        if (events.contains(Player.EVENT_IS_PLAYING_CHANGED)) {
                            sequenceOf(
                                Toast.makeText(
                                    applicationContext,
                                    "Player Event is here, don't worry",
                                    Toast.LENGTH_LONG
                                ).show()
                            )
                        }
                    }
                }


                val exoPlayer = rememberExoPlayer(/*playerListener*/)

                val jexoPlayer = rememberJexoPlayerController(liveHlsSource) {
                    exoPlayer
                }


                val lifecycleOwner = LocalLifecycleOwner.current

                LaunchedEffect(key1 = Unit, block = {
                    window.keepScreenOn()
                })


                DisposableEffect(lifecycleOwner) {
                    onDispose {
                        window.keepScreenOn(false)
                    }
                }

                Scaffold {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(it)
                            .systemBarsPadding()
                    ) {
//                        AnimatedVisibility(visible = videoSourceState.source != null) {
                        JexoPlayer(
                            controller = jexoPlayer,
                            controlsEnabled = true,
                            gesturesEnabled = true,
                            backgroundColor = Color.Black,
                            subtitle = {
                                Text(
                                    modifier = Modifier.padding(12.dp).align(Alignment.CenterHorizontally),
                                    text = "Subtitle will be drawn here ..."
                                )
                            }
                        ) {
                            val isAudioTrackDialogVisible = remember { mutableStateOf(false) }

                            JexoPlayerOverlay(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter),
                                topBar = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = "Awesome Video Title")
                                        Spacer(modifier = Modifier.weight(1f))
                                        SubtitleButton()
                                        AudioTrackChangeButton(
                                            isAudioTrackDialogVisible = isAudioTrackDialogVisible
                                        )
                                        FullScreenButton()
                                    }
                                },
                                centerButton = {
                                    PlayPauseButton()
                                },
                                progressIndicator = {
                                    CircularProgressIndicator()
                                },
                                subtitles = {  /*TODO Implement subtitles*/ },
                                controls = {
                                    VideoControls()
                                },
                                durationRow = {
                                    PositionAndDurationNumbers()
                                },
                                gestureBox = {
                                    JexoGesturesBox(modifier = Modifier.matchParentSize())
                                },
                                dialog = {
                                    AudioTrackSelectorDialog(
                                        isAudioTrackDialogVisible = isAudioTrackDialogVisible
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }


    @Composable
    fun VideoControls() {
        JexoPlayerMainFrame(
            seeker = {
                JexoProgressIndicator()
            }
        )
    }


}



