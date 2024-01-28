package com.geletinousamigo.media.jexoplayer

import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.geletinousamigo.media.jexoplayer.model.PlaybackState

@Composable
fun JexoPlayerOverlay(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {  },
    centerButton: @Composable () -> Unit = {  },
    progressIndicator: @Composable () -> Unit = {  },
    subtitles: @Composable () -> Unit = {  },
    controls: @Composable () -> Unit = {  },
    durationRow: @Composable () -> Unit = {  },
    gestureBox: @Composable () -> Unit = {  },
    dialog: @Composable () -> Unit = {  }
) {

    val player = LocalJexoPlayer.current
    val state by player.state.collectAsState()

    val appearAlpha = remember { Animatable(0f) }
    val configuration = LocalConfiguration.current
    val isPortraitMode = configuration.orientation == ORIENTATION_PORTRAIT
    val paddingModifier = when (configuration.orientation) {
        ORIENTATION_LANDSCAPE -> {
            Modifier
                .padding(horizontal = 32.dp)
                .padding(bottom = 32.dp)
        }

        else -> {
            Modifier
        }
    }

    LaunchedEffect(state.controlsVisible) {
        appearAlpha.animateTo(
            targetValue = if (state.controlsVisible) 1f else 0f,
            animationSpec = tween(
                durationMillis = 250,
                easing = LinearEasing
            )
        )
    }
    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier
            .fillMaxSize()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                player.clearHideSeconds()
                player.hideControls()
            }
        )

        AnimatedVisibility(state.controlsVisible, Modifier, fadeIn(), fadeOut()) {
            CinematicBackground(Modifier.fillMaxSize())
        }

        Column(
            modifier = Modifier
                .alpha(appearAlpha.value)
        ) {

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
                paddingModifier,
                slideInVertically { it },
                slideOutVertically { it }
            ) {
                durationRow()
                /*PositionAndDurationNumbers(
                    modifier = paddingModifier
                )*/
            }
        }


        AnimatedVisibility(
            modifier = Modifier.align(Alignment.BottomCenter),
            visible = (state.controlsVisible || isPortraitMode)
        ) {
            Box(
                modifier = paddingModifier
            ) {
                controls()
            }
        }
        if (state.playbackState == PlaybackState.BUFFERING) {
            progressIndicator()
        } else {
            Box(modifier = Modifier.alpha(appearAlpha.value)) { centerButton() }
        }

        gestureBox()

        dialog()


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

@Preview(device = "spec:width=411dp,height=891dp")
@Composable
private fun VideoPlayerOverlayPreview() {
    Box(Modifier.fillMaxSize()) {
        JexoPlayerOverlay(
            modifier = Modifier.align(Alignment.BottomCenter),
            subtitles = {
                Box(
                    Modifier
                        .background(Color.Red)
                        .fillMaxWidth()
                        .height(100.dp)
                )
            },
            controls = {
                Box(
                    Modifier
                        .background(Color.Blue)
                        .fillMaxWidth()
                        .height(100.dp)
                )
            },
            topBar = {
                Box(
                    Modifier
                        .background(Color.Gray)
                        .fillMaxWidth()
                        .height(100.dp)
                )
            },
            centerButton = {
                Box(
                    Modifier
                        .background(Color.Green)
                        .size(88.dp)
                )
            }
        )
    }
}