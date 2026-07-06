package com.kogen.giraffe.analizer.parsers

import android.content.Context
import com.kogen.giraffe.analizer.AnalysisResult
import com.kogen.giraffe.analizer.utils.MediaSignatures
import com.kogen.giraffe.analizer.utils.saveMediaToCache
import com.kogen.giraffe.ui.common.domain.models.GiraffeContentType

internal class GiraffeImageParser : ContentParser {
    override fun parse(message: Any, context: Context): AnalysisResult? {
        val str = message.toString()
        val bytes = MediaSignatures.tryDecodeBase64(str)
            ?: MediaSignatures.tryDecodeProtobufOctal(str)
            ?: str.toByteArray(Charsets.ISO_8859_1)

        if (MediaSignatures.isImage(bytes)) {
            val path = saveMediaToCache(context, bytes, "img", "png")
            if (path != null) {
                return AnalysisResult(
                    contentType = GiraffeContentType.Image,
                    textContent = str,
                    filePath = path,
                )
            }
        }
        return null
    }
}