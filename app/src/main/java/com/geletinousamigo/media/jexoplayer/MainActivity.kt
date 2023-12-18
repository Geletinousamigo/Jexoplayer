package com.geletinousamigo.media.jexoplayer

import android.app.Activity
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.geletinousamigo.media.jexoplayer.model.VideoPlayerSource
import com.geletinousamigo.media.jexoplayer.ui.JexoPlayerScreen
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
                        }

                        override fun onDestroy(owner: LifecycleOwner) {
                            jexoPlayer.reset()
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


                Column(modifier = Modifier.fillMaxSize()) {
                    JexoPlayerScreen(
                        jexoPlayer = jexoPlayer,
                        modifier = Modifier.fillMaxWidth(),
                        controlsEnabled = true,
                        gesturesEnabled = true,
                        backgroundColor = Color.Black
                    ) {
                        val localPlayer = LocalJexoPlayer.current
                        val state by localPlayer.state.collectAsState()

                        JexoPlayerOverlay(
                            modifier = Modifier.align(Alignment.BottomCenter),
                            state = state,
                            centerButton = { /*VideoPlayerPulse(pulsejexoPlayer)*/ },
                            subtitles = {  /*TODO Implement subtitles*/ },
                            controls = {
                                VideoControls()
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
            mediaTitle = {
                Text(text = "Video Name")
            },
            mediaActions = {

            },
            seeker = {
                JexoProgressIndicator()
            },
            more = {
                Button(onClick = {  }) {
                    Text(text = "More...")
                }
            }
        )
    }


}

