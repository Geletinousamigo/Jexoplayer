package com.geletinousamigo.media.jexoplayer.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.ExoPlayer
import com.geletinousamigo.media.jexoplayer.JexoPlayer
import com.geletinousamigo.media.jexoplayer.JexoPlayerMainFrame
import com.geletinousamigo.media.jexoplayer.JexoPlayerOverlay
import com.geletinousamigo.media.jexoplayer.model.VideoPlayerSource
import com.geletinousamigo.media.jexoplayer.rememberJexoPlayerController
import com.geletinousamigo.media.jexoplayer.tv.ui.theme.JexoplayerTheme
import com.geletinousamigo.media.jexoplayer.ui.tv.TvAudioTrackSelectorModal
import com.geletinousamigo.media.jexoplayer.ui.tv.VideoPlayerSeeker
import com.geletinousamigo.media.jexoplayer.util.keepScreenOn

@androidx.annotation.OptIn(UnstableApi::class)
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JexoplayerTheme {
                val source = VideoPlayerSource.Network.Hls(
                    url = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8",
                    userAgent = Util.getUserAgent(this, "plaYtv")
                )
//                val jexoPlayer = rememberCustomPlayer(source)
                val exoPlayer = remember {
                    ExoPlayer.Builder(applicationContext).build()
                }
                val jexoPlayerController = rememberJexoPlayerController(source){
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

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .systemBarsPadding()
                ) {
                    JexoPlayer(
                        controller = jexoPlayerController,
                        controlsEnabled = true,
                        gesturesEnabled = false,
                        backgroundColor = Color.Black
                    ) {

                        val isAudioTrackDialogVisible = remember { mutableStateOf(false) }


                        JexoPlayerOverlay(
                            modifier = Modifier
                                .align(Alignment.BottomCenter),
                            centerButton = {  },
                            subtitles = { /*TODO Implement subtitles*/ },
                            controls = {
                                VideoControls()
                            },
                            dialog = {
                                TvAudioTrackSelectorModal(
                                    isAudioTrackModalVisible = isAudioTrackDialogVisible
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun VideoControls() {
        JexoPlayerMainFrame(
            seeker = {
                VideoPlayerSeeker()
            }
        )
    }
}

