package com.geletinousamigo.media.jexoplayer

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.PlayerView

@Composable
fun JexoPlayerSurface(
    modifier: Modifier = Modifier,
    onPlayerViewAvailable: (PlayerView) -> Unit,
) {

    AndroidView(
        factory = { context ->
            PlayerView(context).apply {
                useController = false
                onPlayerViewAvailable(this)
            }
        },
        modifier = modifier,
    )

}