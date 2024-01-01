package com.geletinousamigo.media.jexoplayer

import android.os.Bundle
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import com.geletinousamigo.media.jexoplayer.model.VideoPlayerSource
import com.geletinousamigo.media.jexoplayer.ui.mobile.AudioTrackChangeButton
import com.geletinousamigo.media.jexoplayer.ui.mobile.AudioTrackSelectorDialog
import com.geletinousamigo.media.jexoplayer.ui.mobile.FullScreenButton
import com.geletinousamigo.media.jexoplayer.ui.mobile.JexoGesturesBox
import com.geletinousamigo.media.jexoplayer.ui.mobile.JexoProgressIndicator
import com.geletinousamigo.media.jexoplayer.ui.mobile.PlayPauseButton
import com.geletinousamigo.media.jexoplayer.ui.mobile.PositionAndDurationNumbers
import com.geletinousamigo.media.jexoplayer.ui.theme.JexoplayerTheme
import com.geletinousamigo.media.jexoplayer.util.keepScreenOn

@OptIn(UnstableApi::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstancejexoPlayer: Bundle?) {
        super.onCreate(savedInstancejexoPlayer)
        setContent {
            JexoplayerTheme {
                val source = VideoPlayerSource.Network.Hls(
                    url = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8",
                    userAgent = Util.getUserAgent(this, "plaYtv")
                )
                val jexoPlayer = rememberJexoPlayer(source)
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
                            JexoPlayerScreen(
                                jexoPlayer = jexoPlayer,
                                controlsEnabled = true,
                                gesturesEnabled = true,
                                backgroundColor = Color.Black
                            ) {
                                val localPlayer = LocalJexoPlayer.current
                                val state by localPlayer.state.collectAsState()
                                val isAudioTrackDialogVisible = remember { mutableStateOf(false) }


                                JexoPlayerOverlay(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter),
                                    state = state,
                                    hideControls = {
                                        localPlayer.clearHideSeconds()
                                        localPlayer.hideControls()
                                    },
                                    topBar = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = "Awesome Video Title")
                                            Spacer(modifier = Modifier.weight(1f))
                                            AudioTrackChangeButton(
                                                isAudioTrackDialogVisible = isAudioTrackDialogVisible
                                            )
                                            FullScreenButton()
                                        }
                                    },
                                    centerButton = { PlayPauseButton() },
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
                                            isAudioTrackDialogVisible =isAudioTrackDialogVisible
                                        )
                                    }
                                )

                            }
//                        }
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



