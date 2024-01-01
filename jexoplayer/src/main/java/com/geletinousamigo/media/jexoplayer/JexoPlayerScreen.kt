package com.geletinousamigo.media.jexoplayer

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.geletinousamigo.media.jexoplayer.util.dPadEvents
import com.geletinousamigo.media.jexoplayer.util.ifElse
import com.geletinousamigo.media.jexoplayer.util.isTelevision

@Composable
fun JexoPlayerScreen(
    jexoPlayer: JexoPlayer,
    modifier: Modifier = Modifier,
    controlsEnabled: Boolean = true,
    gesturesEnabled: Boolean = true,
    backgroundColor: Color = Color.Black,
    observer: DefaultLifecycleObserver = defaultLifeCycleObserver(jexoPlayer),
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
        val aspectRatio by jexoPlayer.collect {
            (videoSize.width / videoSize.height).coerceAtLeast(
                16 / 9f
            )
        }
        val lifecycleOwner = LocalLifecycleOwner.current


        DisposableEffect(jexoPlayer, lifecycleOwner) {

            lifecycleOwner.lifecycle.addObserver(observer)

            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }

        Box(
            modifier = Modifier
                .background(color = backgroundColor)
                .focusable()
                .aspectRatio(aspectRatio)
                .then(modifier)
                .ifElse(isTelevision(), Modifier.dPadEvents(), Modifier)
        ) {
            JexoPlayerSurface(modifier = Modifier.align(Alignment.Center)) {
                jexoPlayer.playerViewAvailable(it)
            }
            content()


        }
    }
}

private fun defaultLifeCycleObserver(
    jexoPlayer: JexoPlayer
) = object : DefaultLifecycleObserver {
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