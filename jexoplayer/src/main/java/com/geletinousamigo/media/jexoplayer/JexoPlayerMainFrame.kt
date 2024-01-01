package com.geletinousamigo.media.jexoplayer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun JexoPlayerMainFrame(
    modifier: Modifier = Modifier,
    seeker: @Composable () -> Unit,
    more: (@Composable () -> Unit)? = null
) {
    Column(modifier.fillMaxWidth()) {
        seeker()
        if (more != null) {
            Spacer(Modifier.height(12.dp))
            Box(Modifier.align(Alignment.CenterHorizontally)) {
                more()
            }
        }
    }
}

@Composable
fun JexoPlayerTvMainFrame(
    modifier: Modifier = Modifier,
    mediaTitle: @Composable () -> Unit,
    seeker: @Composable () -> Unit,
    mediaActions: @Composable () -> Unit = {},
    more: (@Composable () -> Unit)? = null
) {
    Column(modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Box(Modifier.weight(1f)) { mediaTitle() }
            mediaActions()
        }
        Spacer(Modifier.height(16.dp))
        seeker()
        if (more != null) {
            Spacer(Modifier.height(12.dp))
            Box(Modifier.align(Alignment.CenterHorizontally)) {
                more()
            }
        }
    }
}


@Preview(device = "spec:width=411dp,height=891dp")
@Composable
private fun MediaPlayerMainFramePreviewLayout() {
    JexoPlayerTvMainFrame(
        mediaTitle = {
            Box(
                Modifier
                    .border(2.dp, Color.Red)
                    .background(Color.LightGray)
                    .fillMaxWidth()
                    .height(64.dp)
            )
        },
        mediaActions = {
            Box(
                Modifier
                    .border(2.dp, Color.Red)
                    .background(Color.LightGray)
                    .size(196.dp, 40.dp)
            )
        },
        seeker = {
            Box(
                Modifier
                    .border(2.dp, Color.Red)
                    .background(Color.LightGray)
                    .fillMaxWidth()
                    .height(16.dp)
            )
        },
        more = {
            Box(
                Modifier
                    .border(2.dp, Color.Red)
                    .background(Color.LightGray)
                    .size(145.dp, 16.dp)
            )
        },
    )
}


