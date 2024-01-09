package com.geletinousamigo.media.jexoplayer.ui.tv

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import androidx.tv.material3.MaterialTheme

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun VideoPlayerControllerText(
    text: String,
    color: Color = MaterialTheme.colorScheme.inverseOnSurface
) {
    Text(
        modifier = Modifier.padding(horizontal = 12.dp),
        text = text,
        color = color,
        fontWeight = FontWeight.SemiBold
    )
}