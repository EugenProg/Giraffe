package com.kogen.giraffe.analizer.parsers

import android.content.Context
import com.kogen.giraffe.analizer.AnalysisResult
import com.kogen.giraffe.analizer.utils.MediaSignatures
import com.kogen.giraffe.analizer.utils.saveMediaToCache
import com.kogen.giraffe.ui.common.domain.models.GiraffeContentType

internal class GiraffeAudioParser : ContentParser {
    private enum class Format(val extension: String) {
        MP3("mp3"), WAV("wav")
    }

    private data class Match(val start: Int, val format: Format)

    override fun parse(originalBytes: ByteArray, context: Context): ParserResult? {
        val match = findEarliestMatch(originalBytes) ?: return null

        val endIndex = when (match.format) {
            Format.MP3 -> originalBytes.size
            Format.WAV -> MediaSignatures.findRiffEnd(originalBytes, match.start)
                .let { if (it == -1) originalBytes.size else it }
        }

        val chunk = originalBytes.copyOfRange(match.start, endIndex)
        val path = saveMediaToCache(context, chunk, "audio", match.format.extension)

        return path?.let {
            ParserResult(
                contentType = GiraffeContentType.Audio,
                filePath = it,
                bytes = chunk,
            )
        }
    }

    private fun findEarliestMatch(bytes: ByteArray): Match? {
        val candidates = listOfNotNull(
            MediaSignatures.indexOf(bytes, MediaSignatures.MP3).takeIf { it != -1 }
                ?.let { Match(it, Format.MP3) },
            findWavStart(bytes)?.let { Match(it, Format.WAV) },
        )
        return candidates.minByOrNull { it.start }
    }

    private fun findWavStart(bytes: ByteArray): Int? {
        var from = 0
        while (true) {
            val riffIndex = MediaSignatures.indexOf(bytes, MediaSignatures.WAV, from)
            if (riffIndex == -1) return null
            if (MediaSignatures.indexOf(bytes, MediaSignatures.WAVE_TAG, riffIndex + 8) == riffIndex + 8) {
                return riffIndex
            }
            from = riffIndex + 1
        }
    }
}
