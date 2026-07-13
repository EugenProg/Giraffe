package com.kogen.giraffe.analizer.parsers

import android.content.Context
import com.kogen.giraffe.analizer.utils.MediaSignatures
import com.kogen.giraffe.analizer.utils.saveMediaToCache
import com.kogen.giraffe.ui.common.domain.models.GiraffeContentType

internal class GiraffeVideoParser : ContentParser {
    override fun parse(originalBytes: ByteArray, context: Context): ParserResult? {
        val ftypIndex = MediaSignatures.indexOf(originalBytes, MediaSignatures.MP4_FTYP)
        if (ftypIndex < 4) return null

        val endIndex = MediaSignatures.findMp4End(originalBytes, ftypIndex)
            .let { if (it == -1) originalBytes.size else it }

        val chunk = originalBytes.copyOfRange(ftypIndex - 4, endIndex)
        val path = saveMediaToCache(context, chunk, "video", "mp4")

        return path?.let {
            ParserResult(
                contentType = GiraffeContentType.Video,
                filePath = it,
                bytes = chunk,
            )
        }
    }
}
