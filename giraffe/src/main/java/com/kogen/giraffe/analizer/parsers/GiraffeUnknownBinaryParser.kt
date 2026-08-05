package com.kogen.giraffe.analizer.parsers

import android.content.Context
import com.kogen.giraffe.analizer.utils.ProtoWireScanner
import com.kogen.giraffe.analizer.utils.saveMediaToCache
import com.kogen.giraffe.ui.common.domain.models.GiraffeContentType

internal class GiraffeUnknownBinaryParser : ContentParser {
    override fun parse(originalBytes: ByteArray, context: Context): ParserResult? {
        val candidate = ProtoWireScanner().findBinaryLeaves(originalBytes).firstOrNull()
            ?: return null

        val path = saveMediaToCache(context, candidate, "unknown", "bin")
        return ParserResult(
            contentType = GiraffeContentType.Unknown,
            bytes = candidate,
            filePath = path,
        )
    }
}