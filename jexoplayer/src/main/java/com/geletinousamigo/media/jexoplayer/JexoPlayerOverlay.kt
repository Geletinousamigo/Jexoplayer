package com.geletinousamigo.media.jexoplayer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.geletinousamigo.media.jexoplayer.model.JexoState

@Composable
fun JexoPlayerOverlay(
    modifier: Modifier = Modifier,
//    focusRequester: FocusRequester = remember { FocusRequester() },
    state: JexoState = JexoState(),
    addHideSeconds: (Int) -> Unit = {  },
    topBar: @Composable () -> Unit = {  },
    centerButton: @Composable () -> Unit = {  },
    subtitles: @Composable () -> Unit = {  },
    controls: @Composable () -> Unit = {  }
) {


    /*LaunchedEffect(state.controlsVisible) {
        if (state.controlsVisible) {
            focusRequester.requestFocus()
        }
    }*/

    LaunchedEffect(state.isPlaying) {
        if (state.isPlaying) {
            addHideSeconds(Int.MAX_VALUE)
        } else {
            addHideSeconds(3)
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(state.controlsVisible, Modifier, fadeIn(), fadeOut()) {
            CinematicBackground(Modifier.fillMaxSize())
        }

        Column {
            
            AnimatedVisibility(
                visible = state.controlsVisible,
                Modifier,
                slideInVertically { -it },
                slideOutVertically { -it },
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 12.dp, top = 8.dp)
                ) {
                    topBar()
                }

            }
            
            Box(
                Modifier.weight(1f),
                contentAlignment = Alignment.BottomCenter
            ) {
                subtitles()
            }

            AnimatedVisibility(
                state.controlsVisible,
                Modifier,
                slideInVertically { it },
                slideOutVertically { it }
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 12.dp, top = 8.dp)
                ) {
                    controls()
                }
            }
        }
        centerButton()
        
    }

}

@Composable
fun CinematicBackground(modifier: Modifier = Modifier) {
    Spacer(
        modifier.background(
            Brush.verticalGradient(
                listOf(
                    Color.Black.copy(alpha = 0.1f),
                    Color.Black.copy(alpha = 0.8f)
                )
            )
        )
    )
}

@Preview(device = "spec:width=411dp,height=891dp", showBackground = true)
@Composable
private fun VideoPlayerOverlayPreview() {
    Box(Modifier.fillMaxSize()) {
        JexoPlayerOverlay(
            modifier = Modifier.align(Alignment.BottomCenter),
            subtitles = {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(Color.Red)
                )
            },
            controls = {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(Color.Blue)
                )
            },
            topBar = {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(Color.Gray)
                )
            },
            centerButton = {
                Box(
                    Modifier
                        .size(88.dp)
                        .background(Color.Green)
                )
            }
        )
    }
}