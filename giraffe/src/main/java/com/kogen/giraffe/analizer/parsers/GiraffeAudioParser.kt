package com.kogen.giraffe.analizer.parsers

import android.content.Context
import com.kogen.giraffe.analizer.AnalysisResult
import com.kogen.giraffe.analizer.utils.MediaSignatures
import com.kogen.giraffe.analizer.utils.saveMediaToCache
import com.kogen.giraffe.ui.common.domain.models.GiraffeContentType

internal class GiraffeAudioParser : ContentParser {
    override fun parse(
        message: String,
        originalBytes: ByteArray,
        context: Context
    ): AnalysisResult? {
        val bytes = MediaSignatures.tryDecodeBase64(message)
            ?: MediaSignatures.tryDecodeProtobufOctal(message)
            ?: message.toByteArray(Charsets.ISO_8859_1)

        if (MediaSignatures.isAudio(bytes)) {
            val path = saveMediaToCache(context, bytes, "audio", "mp3")
            if (path != null) {
                return AnalysisResult(
                    contentType = GiraffeContentType.Audio,
                    textContent = message,
                    filePath = path
                )
            }
        }
        return null
    }
}