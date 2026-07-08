package com.kogen.giraffe.analizer.parsers

import android.content.Context
import com.kogen.giraffe.analizer.AnalysisResult
import com.kogen.giraffe.analizer.utils.MediaSignatures
import com.kogen.giraffe.analizer.utils.saveMediaToCache
import com.kogen.giraffe.ui.common.domain.models.GiraffeContentType

internal class GiraffeVideoParser : ContentParser {
    override fun parse(
        message: String,
        originalBytes: ByteArray,
        context: Context
    ): AnalysisResult? {
        val bytes = MediaSignatures.tryDecodeBase64(message)
            ?: MediaSignatures.tryDecodeProtobufOctal(message)
            ?: message.toByteArray(Charsets.ISO_8859_1)

        if (MediaSignatures.isVideo(bytes)) {
            val path = saveMediaToCache(context, bytes, "video", "mp4")
            if (path != null) {
                return AnalysisResult(
                    contentType = GiraffeContentType.Video,
                    textContent = message,
                    filePath = path
                )
            }
        }
        return null
    }
}