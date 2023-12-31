package com.geletinousamigo.media.jexoplayer

import android.app.Activity
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.geletinousamigo.media.jexoplayer.model.VideoPlayerSource
import com.geletinousamigo.media.jexoplayer.ui.theme.JexoplayerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstancejexoPlayer: Bundle?) {
        super.onCreate(savedInstancejexoPlayer)
        setContent {
            JexoplayerTheme {

                val context = LocalContext.current
                val activity = context as Activity
                val jexoPlayer = rememberJexoPlayer(
                    source = VideoPlayerSource.Network.Hls(
                        "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8"
                    )
                )


                val lifecycleOwner = LocalLifecycleOwner.current

                DisposableEffect(jexoPlayer, lifecycleOwner) {
                    val observer = object : DefaultLifecycleObserver {
                        override fun onStop(owner: LifecycleOwner) {
                            jexoPlayer.pause()
                            super.onStop(owner)
                        }

                        override fun onDestroy(owner: LifecycleOwner) {
                            jexoPlayer.reset()
                            super.onDestroy(owner)
                        }

                        override fun onResume(owner: LifecycleOwner) {
                            jexoPlayer.play()
                            super.onResume(owner)
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)

                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                        activity.window.clearFlags(
                            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        )
                    }
                }


                Scaffold {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(it)
                            .systemBarsPadding()
                    ) {
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
                                }
                            )
                            AudioTrackSelectorDialog(
                                isAudioTrackDialogVisible =isAudioTrackDialogVisible
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

