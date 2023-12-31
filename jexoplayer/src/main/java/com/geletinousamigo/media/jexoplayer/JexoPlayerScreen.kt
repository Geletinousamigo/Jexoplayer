package com.geletinousamigo.media.jexoplayer

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun JexoPlayerScreen(
    jexoPlayer: JexoPlayer,
    modifier: Modifier = Modifier,
    controlsEnabled: Boolean = true,
    gesturesEnabled: Boolean = true,
    backgroundColor: Color = Color.Black,
    content: @Composable BoxScope.() -> Unit = {  },
) {
    require(jexoPlayer is JexoPlayerImpl) {
        "Use [rememberJexoPlayer()] to create an instance of [JexoPlayer]"
    }

    SideEffect {
        jexoPlayer.enableControls(controlsEnabled)
        jexoPlayer.enableGestures(gesturesEnabled)
    }

    CompositionLocalProvider(
        LocalContentColor provides Color.White,
        LocalJexoPlayer provides jexoPlayer
    ) {
        val aspectRatio by jexoPlayer.collect { (videoSize.width / videoSize.height).coerceAtLeast(16/9f) }

        Box(
            modifier = Modifier
                .background(color = backgroundColor)
//                .defaultMinSize(minHeight = 250.dp)
//                .dPadEvents()
                .focusable()
                .aspectRatio(aspectRatio)
                .then(modifier)
        ) {
            JexoPlayerSurface (modifier = Modifier.align(Alignment.Center)){
                jexoPlayer.playerViewAvailable(it)
            }
            content()
            JexoGesturesBox(modifier = Modifier.matchParentSize())

            /*JexoButtons(modifier = Modifier.matchParentSize())
            JexoProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            )*/

        }
    }
}