@file:Suppress("unused")

package com.geletinousamigo.media.jexoplayer.util

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.view.KeyEvent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.geletinousamigo.media.jexoplayer.LocalJexoPlayer

private val DPadEventsKeyCodes = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
    listOf(
        KeyEvent.KEYCODE_DPAD_LEFT,
        KeyEvent.KEYCODE_SYSTEM_NAVIGATION_LEFT,
        KeyEvent.KEYCODE_DPAD_RIGHT,
        KeyEvent.KEYCODE_SYSTEM_NAVIGATION_RIGHT,
        KeyEvent.KEYCODE_DPAD_UP,
        KeyEvent.KEYCODE_SYSTEM_NAVIGATION_UP,
        KeyEvent.KEYCODE_DPAD_DOWN,
        KeyEvent.KEYCODE_SYSTEM_NAVIGATION_DOWN,
        KeyEvent.KEYCODE_DPAD_CENTER,
        KeyEvent.KEYCODE_ENTER,
        KeyEvent.KEYCODE_NUMPAD_ENTER
    )
} else {
    listOf(
        KeyEvent.KEYCODE_DPAD_LEFT,
        KeyEvent.KEYCODE_DPAD_RIGHT,
        KeyEvent.KEYCODE_DPAD_UP,
        KeyEvent.KEYCODE_DPAD_DOWN,
        KeyEvent.KEYCODE_DPAD_CENTER,
        KeyEvent.KEYCODE_ENTER,
        KeyEvent.KEYCODE_NUMPAD_ENTER
    )
}

/**
 * Handles horizontal (Left & Right) D-Pad Keys and consumes the event(s) so that the focus doesn't
 * accidentally move to another element.
 * */
fun Modifier.handleDPadKeyEvents(
    onLeft: (() -> Unit)? = null,
    onRight: (() -> Unit)? = null,
    onEnter: (() -> Unit)? = null
) = onPreviewKeyEvent {
    fun onActionUp(block: () -> Unit) {
        if (it.nativeKeyEvent.action == KeyEvent.ACTION_UP) block()
    }

    if (DPadEventsKeyCodes.contains(it.nativeKeyEvent.keyCode)) {
        when (it.nativeKeyEvent.keyCode) {
            KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_SYSTEM_NAVIGATION_LEFT -> {
                onLeft?.apply {
                    onActionUp(::invoke)
                    return@onPreviewKeyEvent true
                }
            }
            KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_SYSTEM_NAVIGATION_RIGHT -> {
                onRight?.apply {
                    onActionUp(::invoke)
                    return@onPreviewKeyEvent true
                }
            }
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                onEnter?.apply {
                    onActionUp(::invoke)
                    return@onPreviewKeyEvent true
                }
            }
        }
    }
    false
}

/**
 * Handles all D-Pad Keys
 * */
fun Modifier.handleDPadKeyEvents(
    onLeft: (() -> Unit)? = null,
    onRight: (() -> Unit)? = null,
    onUp: (() -> Unit)? = null,
    onDown: (() -> Unit)? = null,
    onEnter: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null
) = onKeyEvent {

    if (DPadEventsKeyCodes.contains(it.nativeKeyEvent.keyCode) && it.nativeKeyEvent.action == KeyEvent.ACTION_UP) {
        when (it.nativeKeyEvent.keyCode) {
            KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_SYSTEM_NAVIGATION_LEFT -> {
                onLeft?.invoke().also { return@onKeyEvent true }
            }
            KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_SYSTEM_NAVIGATION_RIGHT -> {
                onRight?.invoke().also { return@onKeyEvent true }
            }
            KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_SYSTEM_NAVIGATION_UP -> {
                onUp?.invoke().also { return@onKeyEvent true }
            }
            KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_SYSTEM_NAVIGATION_DOWN -> {
                onDown?.invoke().also { return@onKeyEvent true }
            }
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                onEnter?.invoke().also { return@onKeyEvent true }
            }
            KeyEvent.KEYCODE_BACK -> {
                onBack?.invoke().also { return@onKeyEvent true }
            }
        }
    }
    false
}

@Composable
fun Modifier.dPadEvents(): Modifier {
    val controller = LocalJexoPlayer.current
    val isVisible by controller.collect{ controlsVisible }
    return this.handleDPadKeyEvents(
        onLeft = {
            if (!isVisible){
                controller.seekRewind()
            }
        },
        onRight = {
            if (!isVisible){
                controller.seekForward()
            }
        },
        onBack = { controller.clearHideSeconds() },
        onUp = { controller.addHideSeconds(3) },
        onDown = { controller.addHideSeconds(3) },
        onEnter = {
            controller.playPauseToggle()
            controller.addHideSeconds(3)
        }
    )
}

@Composable
fun isLandScape(): Boolean {
    val configuration = LocalConfiguration.current
    return configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
}
@Composable
@Stable
fun isTelevision(): Boolean {
    val context = LocalContext.current
    return context.isTelevision()
}

fun Context.isTelevision(): Boolean {
    return packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)
}

data class FocusRequesterModifiers(
    val parentModifier: Modifier,
    val childModifier: Modifier
)

/**
 * This modifier can be used to gain focus on a focusable component when it becomes visible
 * for the first time.
 * */
@Composable
fun Modifier.focusOnInitialVisibility(isVisible: MutableState<Boolean>): Modifier {
    val focusRequester = remember { FocusRequester() }

    return focusRequester(focusRequester)
        .onPlaced {
            if (!isVisible.value) {
                focusRequester.requestFocus()
                isVisible.value = true
            }
        }
}

/**
 * Returns a set of modifiers [FocusRequesterModifiers] which can be used for restoring focus and
 * specifying the initially focused item.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun createInitialFocusRestorerModifiers(): FocusRequesterModifiers {
    val focusRequester = remember { FocusRequester() }
    val childFocusRequester = remember { FocusRequester() }

    val parentModifier = Modifier
        .focusRequester(focusRequester)
        .focusProperties {
            exit = {
                focusRequester.saveFocusedChild()
                FocusRequester.Default
            }
            enter = {
                if (!focusRequester.restoreFocusedChild())
                    childFocusRequester
                else
                    FocusRequester.Cancel
            }
        }

    val childModifier = Modifier.focusRequester(childFocusRequester)

    return FocusRequesterModifiers(parentModifier, childModifier)
}


/**
 * Used to apply modifiers conditionally.
 */
fun Modifier.ifElse(
    condition: () -> Boolean,
    ifTrueModifier: Modifier,
    ifFalseModifier: Modifier = Modifier
): Modifier = then(if (condition()) ifTrueModifier else ifFalseModifier)


/**
 * Used to apply modifiers conditionally.
 */
fun Modifier.ifElse(
    condition: Boolean,
    ifTrueModifier: Modifier,
    ifFalseModifier: Modifier = Modifier
): Modifier = ifElse({ condition }, ifTrueModifier, ifFalseModifier)