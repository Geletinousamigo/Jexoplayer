package com.geletinousamigo.media.jexoplayer.ui.tv


import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.geletinousamigo.media.jexoplayer.util.handleDPadKeyEvents

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun RowScope.TvJexoProgressIndicator(
    duration: Float,
    progress: ()->Float,
    bufferedPosition: Float,
    onSeek: (seekProgress: Float) -> Unit,
    addHideSeconds: (Int) -> Unit = {},
    primaryProgressColor: Color,
    secondaryProgressColor: Color

) {
    val interactionSource = remember { MutableInteractionSource() }
    var isSelected by remember { mutableStateOf(false) }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val color by rememberUpdatedState(
        newValue = if (isSelected) primaryProgressColor
        else secondaryProgressColor
    )
    val animatedIndicatorHeight by animateDpAsState(
        targetValue = 4.dp.times((if (isFocused) 2.5f else 1f)), label = ""
    )
    var seekProgress by remember { mutableFloatStateOf(progress()) }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(isSelected) {
        if (isSelected) {
            addHideSeconds(Int.MAX_VALUE)
        } else {
            addHideSeconds(3)
        }
    }

    Canvas(
        modifier = Modifier
            .weight(1f)
            .height(animatedIndicatorHeight)
            .padding(horizontal = 10.dp)
            .handleDPadKeyEvents(
                onBack = {
                    if (isSelected) {
                        focusManager.moveFocus(FocusDirection.Exit)
                    }
                },
                onEnter = {
                    if (isSelected) {
                        onSeek(seekProgress)
                        focusManager.moveFocus(FocusDirection.Exit)
                    } else {
                        seekProgress = progress()
                    }
                    isSelected = !isSelected
                },
                onLeft = {
                    if (isSelected && (seekProgress > 0)) {
                        seekProgress -= 0.01f
                    } else {
                        focusManager.moveFocus(FocusDirection.Left)
                    }
                },
                onRight = {
                    if (isSelected && (seekProgress < duration)) {
                        seekProgress += 0.01f
                    } else {
                        focusManager.moveFocus(FocusDirection.Right)
                    }
                }
            )
            .focusable(interactionSource = interactionSource),
        onDraw = {
            val yOffset = size.height.div(2)
            drawLine(
                color = color.copy(alpha = 0.24f),
                start = Offset(x = 0f, y = yOffset),
                end = Offset(x = size.width, y = yOffset),
                strokeWidth = size.height,
                cap = StrokeCap.Round
            )
            drawLine(
                color = color.copy(alpha = 0.24f),
                start = Offset(x = 0f, y = yOffset),
                end = Offset(x = size.width
                    .times(
                        bufferedPosition/duration
                    ), y = yOffset),
                strokeWidth = size.height,
                cap = StrokeCap.Round
            )
            drawLine(
                color = color,
                start = Offset(x = 0f, y = yOffset),
                end = Offset(
                    x = size
                        .width
                        .times(
                            if (isSelected) seekProgress
                            else progress()
                        )
                    /*.coerceIn(0f, size.width)*/,
                    y = yOffset
                ),
                strokeWidth = size.height,
                cap = StrokeCap.Round
            )
        }
    )
}