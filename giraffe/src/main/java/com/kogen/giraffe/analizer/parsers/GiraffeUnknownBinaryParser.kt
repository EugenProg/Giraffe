package com.kogen.giraffe.analizer.parsers

import android.content.Context
import com.kogen.giraffe.analizer.AnalysisResult
import com.kogen.giraffe.analizer.utils.MediaSignatures
import com.kogen.giraffe.analizer.utils.saveMediaToCache
import com.kogen.giraffe.ui.common.domain.models.GiraffeContentType

internal class GiraffeUnknownBinaryParser : ContentParser {
    override fun parse(
        message: String,
        originalBytes: ByteArray,
        context: Context
    ): AnalysisResult? {

        val bytes = MediaSignatures.tryDecodeProtobufOctal(message) ?: return null

        if (bytes.isNotEmpty()) {
            val path = saveMediaToCache(context, bytes, "binary", "bin")
            if (path != null) {
                return AnalysisResult(
                    contentType = GiraffeContentType.Unknown,
                    textContent = message,
                    filePath = path
                )
            }
        }
        return null
    }
}