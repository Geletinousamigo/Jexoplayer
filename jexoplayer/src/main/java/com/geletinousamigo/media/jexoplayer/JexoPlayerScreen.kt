package com.geletinousamigo.media.jexoplayer

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.geletinousamigo.media.jexoplayer.util.dPadEvents
import com.geletinousamigo.media.jexoplayer.util.ifElse
import com.geletinousamigo.media.jexoplayer.util.isLandScape
import com.geletinousamigo.media.jexoplayer.util.isTelevision

@Composable
fun JexoPlayer(
    controller: JexoPlayerController,
    modifier: Modifier = Modifier,
    controlsEnabled: Boolean = true,
    gesturesEnabled: Boolean = true,
    backgroundColor: Color = Color.Black,
    observer: DefaultLifecycleObserver = defaultLifeCycleObserver(controller),
    subtitle: @Composable () -> Unit = { },
    content: @Composable BoxScope.() -> Unit = { },
) {
    require(controller is JexoPlayerControllerImpl) {
        "Use [rememberJexoPlayer()] to create an instance of [JexoPlayer]"
    }

    SideEffect {
        controller.enableControls(controlsEnabled)
        controller.enableGestures(gesturesEnabled)
    }

    CompositionLocalProvider(
        LocalContentColor provides Color.White,
        LocalJexoPlayer provides controller
    ) {
        val aspectRatio by controller.collect {
            if (videoSize.width > 0 && videoSize.height > 0) {
                (videoSize.width / videoSize.height)
            } else {
                16 / 9f
            }
        }
        val useSubtitles by controller.collect { useSubTitles }
        val lifecycleOwner = LocalLifecycleOwner.current
        var heightOfSubtitle by remember {
            mutableIntStateOf(0)
        }


        DisposableEffect(controller, lifecycleOwner) {

            lifecycleOwner.lifecycle.addObserver(observer)

            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }

        Column(
            modifier = Modifier
                .background(backgroundColor)
                .wrapContentSize()
        ) {
            Box(
                modifier = Modifier
                    .background(color = backgroundColor)
                    .focusable()
                    .then(modifier)
                    .ifElse(
                        isLandScape(),
                        Modifier.weight(1f),
                        Modifier.aspectRatio(16 / 9f)
                    )
                    .ifElse(
                        isTelevision(),
                        Modifier
                            .fillMaxHeight()
                            .dPadEvents(),
                        Modifier/*.aspectRatio(aspectRatio)*/
                    )
            ) {
                JexoPlayerSurface(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .aspectRatio(aspectRatio)
//                    .ifElse(isLandScape(), Modifier.fillMaxHeight(), Modifier)
                ) {
                    controller.playerViewAvailable(it)
                }
                content()

            }
            if (useSubtitles) {
                subtitle()
            }
        }
    }
}

private fun defaultLifeCycleObserver(
    jexoPlayerController: JexoPlayerController
) = object : DefaultLifecycleObserver {
    override fun onStop(owner: LifecycleOwner) {
        jexoPlayerController.pause()
        super.onStop(owner)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        jexoPlayerController.reset()
        super.onDestroy(owner)
    }

    override fun onResume(owner: LifecycleOwner) {
        jexoPlayerController.play()
        super.onResume(owner)
    }
}