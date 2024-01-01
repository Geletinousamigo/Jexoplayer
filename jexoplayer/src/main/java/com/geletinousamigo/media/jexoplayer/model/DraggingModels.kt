package com.geletinousamigo.media.jexoplayer.model

import android.os.Parcelable
import com.geletinousamigo.media.jexoplayer.util.formatTime
import kotlinx.parcelize.Parcelize
import kotlin.math.abs
import kotlin.time.Duration.Companion.milliseconds

@Parcelize
data class DraggingProgress(
    val finalTime: Float,
    val diffTime: Float
) : Parcelable {
    val progressText: String
        get() = "${finalTime.toLong().milliseconds.formatTime()} " +
                "[${if (diffTime < 0) "-" else "+"}${
                    abs(diffTime.toLong()).milliseconds.formatTime()
                }]"
}

enum class QuickSeekDirection {
    None,
    Rewind,
    Forward
}