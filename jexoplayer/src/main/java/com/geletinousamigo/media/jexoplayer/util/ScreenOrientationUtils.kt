package com.geletinousamigo.media.jexoplayer.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

fun Context.setScreenOrientation(orientation: Int) {
    val activity = this.findActivity() ?: return

    activity.requestedOrientation = orientation
    if (orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
        hideSystemUi()
    } else {
        showSystemUi()
    }
}

internal fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

internal fun Context.hideSystemUi() {
    val activity = this.findActivity() ?: return
    val window = activity.window ?: return
    val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
    windowInsetsController.systemBarsBehavior =
        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    window.decorView.setOnApplyWindowInsetsListener { view, windowInsets ->
        if (windowInsets.isVisible(WindowInsetsCompat.Type.navigationBars())
            || windowInsets.isVisible(WindowInsetsCompat.Type.statusBars())
        ) {
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        }
        view.onApplyWindowInsets(windowInsets)
    }
}

internal fun Context.showSystemUi() {
    val activity = this.findActivity() ?: return
    val window = activity.window ?: return
    val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
    windowInsetsController.systemBarsBehavior =
        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    window.decorView.setOnApplyWindowInsetsListener { view, windowInsets ->
//        if (!(windowInsets.isVisible(WindowInsetsCompat.Type.navigationBars())
//            || windowInsets.isVisible(WindowInsetsCompat.Type.statusBars())
//        )) {
        windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
//        }
        view.onApplyWindowInsets(windowInsets)
    }
}