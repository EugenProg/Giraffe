package com.kogen.giraffe.analizer.parsers

import android.content.Context
import com.kogen.giraffe.analizer.AnalysisResult
import com.kogen.giraffe.analizer.utils.MediaSignatures
import com.kogen.giraffe.analizer.utils.saveMediaToCache
import com.kogen.giraffe.ui.common.domain.models.GiraffeContentType

internal class GiraffeImageParser : ContentParser {
    override fun parse(message: String, originalBytes: ByteArray, context: Context): AnalysisResult? {
        val pngHeader = byteArrayOf(0x89.toByte(), 0x50.toByte(), 0x4E.toByte(), 0x47.toByte())

        var startIndex = -1
        for (i in 0 until originalBytes.size - pngHeader.size) {
            var match = true
            for (j in pngHeader.indices) {
                if (originalBytes[i + j] != pngHeader[j]) {
                    match = false
                    break
                }
            }
            if (match) {
                startIndex = i
                break
            }
        }

        if (startIndex == -1) return null

        val endIndex = MediaSignatures.findEndOfMedia(originalBytes, startIndex, MediaSignatures.PNG_END)

        if (endIndex == -1) return null

        val chunk = originalBytes.copyOfRange(startIndex, endIndex)
        val path = saveMediaToCache(context, chunk, "img", "png")

        return path?.let { AnalysisResult(GiraffeContentType.Image, message, it) }
    }
}