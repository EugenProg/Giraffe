package com.kogen.giraffe.analizer.parsers

import android.content.Context
import com.kogen.giraffe.analizer.AnalysisResult
import com.kogen.giraffe.analizer.utils.MediaSignatures
import com.kogen.giraffe.analizer.utils.saveMediaToCache
import com.kogen.giraffe.ui.common.domain.models.GiraffeContentType

internal class GiraffeUnknownBinaryParser : ContentParser {
    override fun parse(originalBytes: ByteArray, context: Context): ParserResult? {

//        val bytes = MediaSignatures.tryDecodeProtobufOctal(message) ?: return null
//
//        if (bytes.isNotEmpty()) {
//            val path = saveMediaToCache(context, bytes, "binary", "bin")
//            return path?.let {
//                ParserResult(
//                    contentType = GiraffeContentType.Unknown,
//                    filePath = it,
//                    startIndexOfContent = match.start,
//                    endIndexOfContent = endIndex,
//                )
//            }
//        }
        return null
    }
}