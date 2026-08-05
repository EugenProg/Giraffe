package com.kogen.giraffe.analizer.parsers

import android.content.Context
import com.kogen.giraffe.analizer.utils.MediaSignatures
import com.kogen.giraffe.analizer.utils.Mp3FrameSync
import com.kogen.giraffe.analizer.utils.PcmAudioHeuristics
import com.kogen.giraffe.analizer.utils.ProtoWireScanner
import com.kogen.giraffe.analizer.utils.saveMediaToCache
import com.kogen.giraffe.ui.common.domain.models.GiraffeContentType

internal class GiraffeAudioParser : ContentParser {
    private enum class Format(val extension: String) {
        MP3("mp3"), WAV("wav")
    }

    private data class Match(val start: Int, val format: Format)

    override fun parse(originalBytes: ByteArray, context: Context): ParserResult? {
        val leaves = ProtoWireScanner().findBinaryLeaves(originalBytes)

        for (leaf in leaves) {
            val match = findEarliestMatch(leaf) ?: continue

            val endIndex = when (match.format) {
                Format.MP3 -> leaf.size
                Format.WAV -> MediaSignatures.findRiffEnd(leaf, match.start)
                    .let { if (it == -1) leaf.size else it }
            }

            val chunk = leaf.copyOfRange(match.start, endIndex)
            val path = saveMediaToCache(context, chunk, "audio", match.format.extension)

            path?.let {
                return ParserResult(
                    contentType = GiraffeContentType.Audio,
                    filePath = it,
                    bytes = chunk
                )
            }
        }

        val pcm = leaves.firstOrNull { PcmAudioHeuristics.looksLikePcm16(it) } ?: return null

        val wavBytes = PcmAudioHeuristics.wrapAsWav(pcm)
        val path = saveMediaToCache(context, wavBytes, "audio_pcm", "wav")

        return path?.let {
            ParserResult(contentType = GiraffeContentType.Audio, filePath = it, bytes = pcm)
        }
    }

    private fun findEarliestMatch(bytes: ByteArray): Match? {
        val candidates = listOfNotNull(
            Mp3FrameSync.findValidatedStart(bytes)?.let { Match(it, Format.MP3) },
            if (MediaSignatures.matchesAt(bytes, 0, MediaSignatures.WAV) &&
                MediaSignatures.matchesAt(bytes, 8, MediaSignatures.WAVE_TAG)
            ) {
                Match(0, Format.WAV)
            } else {
                null
            },
        )
        return candidates.minByOrNull { it.start }
    }
}
