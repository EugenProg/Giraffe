package com.kogen.giraffe.analizer.parsers

import android.content.Context
import com.kogen.giraffe.analizer.utils.MediaSignatures
import com.kogen.giraffe.analizer.utils.saveMediaToCache
import com.kogen.giraffe.ui.common.domain.models.GiraffeContentType

internal class GiraffeImageParser : ContentParser {
    private enum class Format(val extension: String) {
        PNG("png"), JPEG("jpg"), GIF("gif"), WEBP("webp")
    }

    private data class Match(val start: Int, val format: Format)

    override fun parse(originalBytes: ByteArray, context: Context): ParserResult? {
        val match = findEarliestMatch(originalBytes) ?: return null

        val endIndex = when (match.format) {
            Format.PNG -> MediaSignatures.findEndOfMedia(
                originalBytes,
                match.start,
                MediaSignatures.PNG_END
            )

            Format.JPEG -> MediaSignatures.findEndOfMedia(
                originalBytes,
                match.start,
                MediaSignatures.JPEG_END
            )

            Format.GIF -> MediaSignatures.findEndOfMedia(
                originalBytes,
                match.start,
                MediaSignatures.GIF_END
            )

            Format.WEBP -> MediaSignatures.findRiffEnd(originalBytes, match.start)
        }
        if (endIndex == -1) return null

        val chunk = originalBytes.copyOfRange(match.start, endIndex)
        val path = saveMediaToCache(context, chunk, "img", match.format.extension)

        return path?.let {
            ParserResult(
                contentType = GiraffeContentType.Image,
                filePath = it,
                bytes = chunk,
            )
        }
    }

    private fun findEarliestMatch(bytes: ByteArray): Match? {
        val candidates = listOfNotNull(
            MediaSignatures.indexOf(bytes, MediaSignatures.PNG).takeIf { it != -1 }
                ?.let { Match(it, Format.PNG) },
            MediaSignatures.indexOf(bytes, MediaSignatures.JPEG).takeIf { it != -1 }
                ?.let { Match(it, Format.JPEG) },
            MediaSignatures.indexOf(bytes, MediaSignatures.GIF).takeIf { it != -1 }
                ?.let { Match(it, Format.GIF) },
            findWebpStart(bytes)?.let { Match(it, Format.WEBP) },
        )
        return candidates.minByOrNull { it.start }
    }

    private fun findWebpStart(bytes: ByteArray): Int? {
        var from = 0
        while (true) {
            val riffIndex = MediaSignatures.indexOf(bytes, MediaSignatures.WEBP, from)
            if (riffIndex == -1) return null
            if (MediaSignatures.indexOf(
                    bytes,
                    MediaSignatures.WEBP_TAG,
                    riffIndex + 8
                ) == riffIndex + 8
            ) {
                return riffIndex
            }
            from = riffIndex + 1
        }
    }
}
