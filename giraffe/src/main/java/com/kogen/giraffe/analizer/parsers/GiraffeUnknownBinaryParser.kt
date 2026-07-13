package com.kogen.giraffe.analizer.parsers

import android.content.Context
import com.kogen.giraffe.analizer.utils.MediaSignatures.isLikelyUtf8Text
import com.kogen.giraffe.analizer.utils.ProtoWireScanner
import com.kogen.giraffe.analizer.utils.saveMediaToCache
import com.kogen.giraffe.ui.common.domain.models.GiraffeContentType

internal class GiraffeUnknownBinaryParser : ContentParser {
    override fun parse(originalBytes: ByteArray, context: Context): ParserResult? {
        val fields = ProtoWireScanner().scan(originalBytes)

        val candidate = fields
            .filter { it.wireType == 2 && it.bytes != null }
            .filter { it.bytes!!.size > 16 }
            .firstOrNull { !isLikelyUtf8Text(it.bytes!!) }
            ?: return null

        return candidate.bytes?.let { bytes ->
            val path = saveMediaToCache(context, bytes, "unknown", "bin")
            ParserResult(
                contentType = GiraffeContentType.Unknown,
                bytes = bytes,
                filePath = path,
            )
        }
    }
}