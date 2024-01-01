package com.geletinousamigo.media.jexoplayer.util

import androidx.media3.common.C.TRACK_TYPE_AUDIO
import androidx.media3.common.Tracks
import com.geletinousamigo.media.jexoplayer.model.Language
import java.util.Locale
import kotlin.time.Duration

fun Duration.formatTime(): String {
    return toComponents { h, m, s, _ ->
        if(h > 0) {
            "$h:${m.padStartWith0()}:${s.padStartWith0()}"
        } else {
            "${m.padStartWith0()}:${s.padStartWith0()}"
        }
    }
}

private fun Number.padStartWith0() = this.toString().padStart(2, '0')

object ExoUtils {
    fun getLanguagesFromTrackInfo(track: Tracks): List<Language> {
        return track.groups.asSequence().mapNotNull {
            if (it.type != TRACK_TYPE_AUDIO) {
                return@mapNotNull null
            }
            val group = it
            val formats = (0 until group.length).map { index -> group.getTrackFormat(index) }
            formats.mapNotNull { format ->
                format.language
            }
        }
            .flatten()
            .toSet()
            .map {
                val locale = Locale(it)
                val languageName = locale.getDisplayLanguage(locale)
                Language(code = it, name = languageName)
            }.toList()
    }


}






